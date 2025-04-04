package org.example.dev.datscity.client;

/**
 * Собственное исключение для ошибок при обращении к API.
 */
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
