package com.medhead.bedallocation.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global des exceptions pour exposer des erreurs normalisées.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 404 - Ressource non trouvée (nouveau package centralisé)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex,
                                                                HttpServletRequest request) {
        log.warn("[404] {} - path={} ", ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(ErrorResponse.of(status, safeMessage(ex, status), request.getRequestURI()), status);
    }

    // 409 - Conflit / duplicat
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(DuplicateResourceException ex,
                                                                 HttpServletRequest request) {
        log.warn("[409] {} - path={}", ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.CONFLICT;
        return new ResponseEntity<>(ErrorResponse.of(status, safeMessage(ex, status), request.getRequestURI()), status);
    }

    // 400 - Opération invalide
    @ExceptionHandler(InvalidOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOperation(InvalidOperationException ex,
                                                                HttpServletRequest request) {
        log.warn("[400] {} - path={}", ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(ErrorResponse.of(status, safeMessage(ex, status), request.getRequestURI()), status);
    }

    // 401 - Authentification
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex,
                                                              HttpServletRequest request) {
        log.warn("[401] {} - path={}", ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return new ResponseEntity<>(ErrorResponse.of(status, safeMessage(ex, status), request.getRequestURI()), status);
    }

    // 403 - Autorisation
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                            HttpServletRequest request) {
        log.warn("[403] {} - path={}", ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.FORBIDDEN;
        return new ResponseEntity<>(ErrorResponse.of(status, "Accès refusé", request.getRequestURI()), status);
    }

    // 400 - Erreurs de validation @Valid sur les DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, fe -> fe.getDefaultMessage() == null ? "Invalid" : fe.getDefaultMessage(),
                        (a, b) -> a, HashMap::new));

        String message = fieldErrors.entrySet().stream()
                .map(e -> e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining(", "));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        log.debug("[400] Validation error(s): {} - path={}", message, request.getRequestURI());
        return new ResponseEntity<>(ErrorResponse.of(status, message, request.getRequestURI()), status);
    }

    // Compatibilité avec les exceptions déjà présentes dans service.exception
    @ExceptionHandler(com.medhead.bedallocation.service.exception.ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLegacyResourceNotFound(
            com.medhead.bedallocation.service.exception.ResourceNotFoundException ex,
            HttpServletRequest request) {
        log.warn("[404] {} - path={} (legacy)", ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(ErrorResponse.of(status, safeMessage(ex, status), request.getRequestURI()), status);
    }

    @ExceptionHandler(com.medhead.bedallocation.service.exception.BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleLegacyBadRequest(
            com.medhead.bedallocation.service.exception.BadRequestException ex,
            HttpServletRequest request) {
        log.warn("[400] {} - path={} (legacy)", ex.getMessage(), request.getRequestURI());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(ErrorResponse.of(status, safeMessage(ex, status), request.getRequestURI()), status);
    }

    // 500 - Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        // Log en erreur avec stacktrace côté serveur, mais ne pas l'exposer au client
        log.error("[500] Unexpected error at path={}", request.getRequestURI(), ex);
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(ErrorResponse.of(status, "Une erreur interne est survenue", request.getRequestURI()), status);
    }

    private String safeMessage(Throwable ex, HttpStatus status) {
        // En production, on peut limiter/adapter les messages. Ici, on garde des messages clairs mais sans détails techniques.
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) {
            return status.getReasonPhrase();
        }
        return msg;
    }
}
