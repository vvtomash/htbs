package org.acme.fintech.exception;

public class ClientConflictException extends RuntimeException {
    public ClientConflictException() {
        super();
    }

    public ClientConflictException(String message) {
        super(message);
    }

    public ClientConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
