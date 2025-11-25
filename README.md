# MedHead Emergency Allocation – Backend

Service Spring Boot (Java 17) pour la preuve de concept du système d’allocation d’hôpitaux/lits d’urgence.

## Description du projet
Ce service expose des API REST pour:
- Consulter la disponibilité des lits par hôpital
- Chercher le meilleur hôpital selon des critères métier
- Gérer la persistance via PostgreSQL

Le projet inclut OpenAPI/Swagger pour la documentation, Spring Security pour la sécurité, Actuator pour l’observabilité, et une configuration prête pour Docker/Docker Compose pour un déploiement rapide.

## Architecture
- Hexagone simplifié: contrôleurs REST, services, mappers (MapStruct), référentiels JPA.
- Persistence: Spring Data JPA + PostgreSQL.
- Sécurité: Spring Security + JWT (JJWT 0.12.x).
- Observabilité: Spring Boot Actuator.
- Documentation: Springdoc OpenAPI UI.

## Technologies utilisées
- Java 17, Spring Boot 3.2
- Spring Web, Spring Data JPA, Spring Security
- PostgreSQL, HikariCP
- Lombok, MapStruct
- JJWT (JSON Web Tokens)
- springdoc-openapi
- Actuator
- Maven
- Docker, Docker Compose

## Prérequis
- Java 17+
- Maven 3.9+
- Docker 24+ et Docker Compose

## Installation et démarrage

### Option A – Démarrage local (sans Docker)
1. Démarrer une instance PostgreSQL locale (port 5432) et créer la base `medhead_db` avec un utilisateur `medhead_user`/`medhead_password` ou ajuster `src/main/resources/application.properties`.
2. Exporter la variable d’environnement JWT:
   - macOS/Linux: `export JWT_SECRET="votre_cle_ultra_secrete"`
   - Windows (PowerShell): `$Env:JWT_SECRET = "votre_cle_ultra_secrete"`
3. Lancer l’application:
   - `./mvnw spring-boot:run`
   - Swagger UI: http://localhost:8080/api/swagger-ui/index.html
   - Health: http://localhost:8080/api/actuator/health

### Option B – Docker Compose (recommandé)
1. Créer un fichier `.env` (facultatif) pour surcharger les valeurs par défaut:
   ```
   POSTGRES_DB=medhead_db
   POSTGRES_USER=medhead_user
   POSTGRES_PASSWORD=medhead_password
   SPRING_PROFILES_ACTIVE=docker
   JWT_SECRET=change-me-in-prod
   ```
2. Construire et démarrer:
   - `docker compose up -d --build`
3. Accéder aux endpoints:
   - Swagger UI: http://localhost:8080/api/swagger-ui/index.html
   - Health: http://localhost:8080/api/actuator/health

## Configuration
- Profil par défaut: `docker` dans l’image; peut être surchargé via `SPRING_PROFILES_ACTIVE` (`prod` pour la production).
- Variables clés:
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `JWT_SECRET` (obligatoire en production)
  - `JAVA_OPTS` (tuning JVM, ex: `-Xms512m -Xmx1024m`)

Les fichiers de configuration pertinents:
- `src/main/resources/application.properties` (défaut/dev)
- `src/main/resources/application-docker.properties` (profil `docker`)
- `src/main/resources/application-prod.properties` (profil `prod`)

## Documentation API
- OpenAPI JSON: `/api/v3/api-docs`
- Swagger UI: `/api/swagger-ui/index.html`

## Tests
- Tests unitaires/intégration via Maven Surefire:
  - `./mvnw test`
- Technologies: JUnit, Spring Boot Test, Spring Security Test, H2, REST Assured.

## Structure du projet
```
src/
  main/
    java/com/medhead/bedallocation/
      controller/    # REST controllers
      service/       # logique métier
      repository/    # Spring Data JPA
      mapper/        # MapStruct DTO <-> Entities
      model/         # Entités & enums
      security/      # JWT & Security config
    resources/
      application.properties
      application-docker.properties
      application-prod.properties
```

## Principes architecturaux respectés
- Séparation des responsabilités (contrôleur/service/repository/mapper)
- DTO/Mapping explicite via MapStruct
- Configuration externalisée par variables d’environnement
- Immutabilité relative et réduction du couplage avec Lombok/MapStruct
- Observabilité via Actuator
- CI/CD Ready: Dockerfile multi-stage, docker-compose, propriétés de prod

## Déploiement (CI/CD – principes B3)
- Build reproductible: Docker multi-stage (Maven puis runtime OpenJDK 17 slim)
- Démarrage automatisé: `docker compose up -d --build`
- Healthcheck applicatif: `/api/actuator/health` pour readiness/liveness
- Configuration 12‑factor: variables d’environnement
- Images minimales et immuables, logs en stdout/stderr

## Auteur et licence
- Auteur: MedHead Team / Contributeurs
- Licence: MIT
