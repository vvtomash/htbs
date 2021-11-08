package org.acme.fintech.exception;

public class ClientValidationException extends RuntimeException {
    public ClientValidationException() {
        super();
    }

    public ClientValidationException(String message) {
        super(message);
    }

    public ClientValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
