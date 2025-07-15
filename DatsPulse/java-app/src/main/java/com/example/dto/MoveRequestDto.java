package com.example.dto;

import java.util.List;

/**
 * Объект Передачи Данных (DTO), представляющий JSON-структуру тела запроса /api/move.
 */
public record MoveRequestDto(List<MoveCommandDto> moves) {
}
