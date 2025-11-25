package com.medhead.bedallocation.exception;

/**
 * Exception indiquant qu'une opération demandée est invalide dans le contexte courant.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException() {
        super();
    }

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
