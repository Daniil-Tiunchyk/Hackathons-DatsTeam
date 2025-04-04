package dev.datscity.client;

/**
 * Пользовательское исключение для ошибок API.
 */
public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
        System.out.println("[ApiException] " + message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
        System.out.println("[ApiException] " + message + " Причина: " + cause);
    }
}
