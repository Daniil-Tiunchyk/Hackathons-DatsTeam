package com.example.dto;

/**
 * Объект Передачи Данных (DTO) для ответа от эндпоинта /api/register.
 * Содержит код ответа от сервера и сопроводительное сообщение.
 *
 * @param code    Код ответа (например, 2 для "слишком поздно").
 * @param message Описание результата операции.
 */
public record RegistrationResponseDto(int code, String message) {
}
