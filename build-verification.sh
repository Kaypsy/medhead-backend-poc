#!/usr/bin/env bash

# Build verification script for a Spring Boot (Maven) project
# - Runs: clean, compile, test-compile, test, package -DskipTests
# - Checks: JAR presence, MapStruct generated sources, warnings, DB migrations, app startup, Swagger endpoint
#
# Usage:
#   chmod +x ./build-verification.sh
#   ./build-verification.sh

set -u

# -----------------------------
# Config
# -----------------------------
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
LOG_DIR="$PROJECT_ROOT/target/build-verification"
REPORT_FILE="$LOG_DIR/report.txt"
COMBINED_LOG="$LOG_DIR/maven-combined.log"
APP_LOG="$LOG_DIR/app-run.log"
SWAGGER_URL="http://localhost:8080/api/swagger-ui/index.html"
HEALTH_URL="http://localhost:8080/actuator/health"
STARTUP_TIMEOUT_SEC=90

mkdir -p "$LOG_DIR"
echo "" > "$REPORT_FILE"
echo "" > "$COMBINED_LOG"

# Pick Maven wrapper if available
if [ -x "$PROJECT_ROOT/mvnw" ]; then
  MVN="$PROJECT_ROOT/mvnw"
else
  MVN="mvn"
fi

pass_count=0
fail_count=0
warn_count=0

green()  { printf "\033[32m%s\033[0m" "$1"; }
red()    { printf "\033[31m%s\033[0m" "$1"; }
yellow() { printf "\033[33m%s\033[0m" "$1"; }
bold()   { printf "\033[1m%s\033[0m" "$1"; }

section() {
  echo "" | tee -a "$REPORT_FILE"
  echo "==================================================================" | tee -a "$REPORT_FILE"
  echo "$1" | tee -a "$REPORT_FILE"
  echo "==================================================================" | tee -a "$REPORT_FILE"
}

record_result() {
  local name="$1"; shift
  local status="$1"; shift
  if [ "$status" = "PASS" ]; then
    pass_count=$((pass_count+1))
    echo "[PASS] $name" | tee -a "$REPORT_FILE"
  elif [ "$status" = "WARN" ]; then
    warn_count=$((warn_count+1))
    echo "[WARN] $name" | tee -a "$REPORT_FILE"
  else
    fail_count=$((fail_count+1))
    echo "[FAIL] $name" | tee -a "$REPORT_FILE"
  fi
}

run_maven() {
  local name="$1"; shift
  local logfile="$LOG_DIR/${name// /_}.log"
  echo "--> Running: $name" | tee -a "$REPORT_FILE"
  echo "Command: $MVN $*" >> "$logfile"
  # Do not set -e here; we want to capture failures and continue
  set +e
  $MVN -B -DskipITs "$@" | tee "$logfile"
  local exit_code=${PIPESTATUS[0]}
  set -e
  cat "$logfile" >> "$COMBINED_LOG"
  if [ $exit_code -eq 0 ]; then
    record_result "$name" "PASS"
  else
    record_result "$name" "FAIL"
  fi
  return $exit_code
}

# -----------------------------
# 1) Maven lifecycle checks
# -----------------------------
section "MAVEN PHASES"
run_maven "mvn clean" clean
run_maven "mvn compile" compile
run_maven "mvn test-compile" test-compile
run_maven "mvn test" test
run_maven "mvn package -DskipTests" package -DskipTests

# -----------------------------
# 2) Artifacts & generated sources
# -----------------------------
section "ARTIFACTS & GENERATED SOURCES"

# JAR presence
jar_count=$(ls -1 "$PROJECT_ROOT/target"/*.jar 2>/dev/null | wc -l | tr -d ' ')
if [ "$jar_count" -gt 0 ]; then
  echo "JAR(s) found:" | tee -a "$REPORT_FILE"
  ls -1 "$PROJECT_ROOT/target"/*.jar | tee -a "$REPORT_FILE"
  record_result "JAR generated in target/" "PASS"
else
  echo "No JAR found in target/" | tee -a "$REPORT_FILE"
  record_result "JAR generated in target/" "FAIL"
fi

# MapStruct generated sources
GEN_DIR="$PROJECT_ROOT/target/generated-sources/annotations"
if [ -d "$GEN_DIR" ] && find "$GEN_DIR" -type f -name "*.java" | grep -q "."; then
  echo "MapStruct generated sources present in $GEN_DIR" | tee -a "$REPORT_FILE"
  record_result "MapStruct classes generated" "PASS"
else
  echo "No MapStruct generated sources found in $GEN_DIR" | tee -a "$REPORT_FILE"
  record_result "MapStruct classes generated" "WARN"
fi

# Maven warnings count
total_warnings=$(grep -c "\[WARNING\]" "$COMBINED_LOG" || true)
echo "Maven warnings found: $total_warnings (see $COMBINED_LOG)" | tee -a "$REPORT_FILE"
if [ "$total_warnings" -gt 0 ]; then
  record_result "No critical Maven warnings" "WARN"
else
  record_result "No critical Maven warnings" "PASS"
fi

# -----------------------------
# 3) DB migrations coherence (best-effort)
# -----------------------------
section "DB MIGRATIONS"
if grep -q "flyway-core" "$PROJECT_ROOT/pom.xml"; then
  MIG_DIR="$PROJECT_ROOT/src/main/resources/db/migration"
  if [ -d "$MIG_DIR" ] && ls -1 "$MIG_DIR"/*.sql >/dev/null 2>&1; then
    echo "Flyway detected and migration scripts present in $MIG_DIR" | tee -a "$REPORT_FILE"
    record_result "DB migrations present (Flyway)" "PASS"
  else
    echo "Flyway dependency detected but no migration scripts in $MIG_DIR" | tee -a "$REPORT_FILE"
    record_result "DB migrations present (Flyway)" "WARN"
  fi
elif grep -q "liquibase-core" "$PROJECT_ROOT/pom.xml"; then
  CHANGELOG_HINT=$(grep -Eo "changelog.*\.yaml|changelog.*\.xml" "$PROJECT_ROOT/pom.xml" | head -n1 || true)
  echo "Liquibase detected. Changelog: ${CHANGELOG_HINT:-not found in pom.xml}" | tee -a "$REPORT_FILE"
  record_result "DB migrations present (Liquibase)" "PASS"
else
  echo "No DB migration tool dependency detected (Flyway/Liquibase)." | tee -a "$REPORT_FILE"
  record_result "DB migrations configured" "WARN"
fi

# -----------------------------
# 4) Start app and probe Swagger
# -----------------------------
section "APPLICATION STARTUP & SWAGGER CHECK"

APP_PID=""

start_app() {
  echo "Starting application with: mvn spring-boot:run -Dspring-boot.run.profiles=test" | tee -a "$REPORT_FILE"
  set +e
  # Start in background; redirect stdout+stderr to log
  ( "$MVN" -Dspring-boot.run.profiles=test spring-boot:run ) > "$APP_LOG" 2>&1 &
  APP_PID=$!
  set -e
  echo "App PID: $APP_PID" | tee -a "$REPORT_FILE"
}

stop_app() {
  if [ -n "$APP_PID" ] && ps -p "$APP_PID" >/dev/null 2>&1; then
    echo "Stopping application (PID $APP_PID)" | tee -a "$REPORT_FILE"
    kill "$APP_PID" >/dev/null 2>&1 || true
    sleep 2
    if ps -p "$APP_PID" >/dev/null 2>&1; then
      echo "Force killing application (PID $APP_PID)" | tee -a "$REPORT_FILE"
      kill -9 "$APP_PID" >/dev/null 2>&1 || true
    fi
  fi
}

probe_url() {
  local url="$1"
  curl -s -o /dev/null -w "%{http_code}" "$url"
}

wait_for_startup() {
  local waited=0
  while [ $waited -lt $STARTUP_TIMEOUT_SEC ]; do
    code=$(probe_url "$HEALTH_URL")
    if [ "$code" = "200" ] || grep -q "Started .* in .* seconds" "$APP_LOG" 2>/dev/null; then
      return 0
    fi
    sleep 3
    waited=$((waited+3))
  done
  return 1
}

start_app
if wait_for_startup; then
  record_result "Application started (spring-boot:run)" "PASS"
  code=$(probe_url "$SWAGGER_URL")
  if [ "$code" = "200" ] || [ "$code" = "302" ]; then
    echo "Swagger endpoint reachable ($SWAGGER_URL) -> HTTP $code" | tee -a "$REPORT_FILE"
    record_result "Swagger UI accessible" "PASS"
  else
    echo "Swagger endpoint NOT reachable ($SWAGGER_URL) -> HTTP $code. See $APP_LOG" | tee -a "$REPORT_FILE"
    record_result "Swagger UI accessible" "FAIL"
  fi
else
  echo "Application did not start within ${STARTUP_TIMEOUT_SEC}s. See $APP_LOG" | tee -a "$REPORT_FILE"
  record_result "Application started (spring-boot:run)" "FAIL"
  record_result "Swagger UI accessible" "FAIL"
fi
stop_app

# -----------------------------
# 5) Summary
# -----------------------------
section "SUMMARY"
echo "Passed: $pass_count | Warnings: $warn_count | Failed: $fail_count" | tee -a "$REPORT_FILE"
echo "Logs:" | tee -a "$REPORT_FILE"
echo " - Combined Maven log: $COMBINED_LOG" | tee -a "$REPORT_FILE"
echo " - App run log:        $APP_LOG" | tee -a "$REPORT_FILE"

if [ $fail_count -gt 0 ]; then
  echo
  red "Some checks failed. Review the logs above.\n"
  exit 1
else
  echo
  green "All mandatory checks passed (with possible warnings).\n"
  exit 0
fi
