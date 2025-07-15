package com.example.dto;

import com.example.domain.Hex;

import java.util.List;

/**
 * Объект Передачи Данных (DTO), представляющий одну команду на передвижение для муравья.
 */
public record MoveCommandDto(String ant, List<Hex> path) {
}
