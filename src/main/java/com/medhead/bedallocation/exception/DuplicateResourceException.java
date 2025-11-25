package com.medhead.bedallocation.exception;

/**
 * Exception indiquant qu'une ressource existe déjà (conflit).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException() {
        super();
    }

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
