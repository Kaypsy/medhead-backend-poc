package com.medhead.bedallocation.service.exception;

/**
 * Exception métier lancée lorsqu'une ressource demandée n'est pas trouvée.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
