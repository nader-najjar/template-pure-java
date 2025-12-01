package io.template.shared.exceptions;

/**
 * Thrown when a required environment variable is missing or invalid.
 */
public final class MissingEnvironmentVariableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public MissingEnvironmentVariableException(String message) {
        super(message);
    }

    public MissingEnvironmentVariableException(String message, Throwable cause) {
        super(message, cause);
    }
}
