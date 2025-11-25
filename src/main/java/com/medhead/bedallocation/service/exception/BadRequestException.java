package com.medhead.bedallocation.service.exception;

/**
 * Exception métier pour signaler une requête invalide (données manquantes, contraintes violées, etc.).
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
