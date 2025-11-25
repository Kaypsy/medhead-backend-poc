package com.medhead.bedallocation.exception;

/**
 * Exception fonctionnelle indiquant qu'une ressource n'a pas été trouvée.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException() {
        super();
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
