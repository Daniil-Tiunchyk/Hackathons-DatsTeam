package com.example.dto;

import com.example.domain.Hex;

import java.util.List;

/**
 * Объект Передачи Данных (DTO), представляющий JSON-структуру ответа от эндпоинта /api/arena.
 * Использование Java records обеспечивает неизменяемость и лаконичность.
 */
public record ArenaStateDto(
        List<AntDto> ants,
        List<EnemyDto> enemies,
        List<FoodDto> food,
        List<Hex> home,
        List<MapCellDto> map,
        double nextTurnIn,
        int score,
        Hex spot,
        int turnNo
) {

    public record AntDto(String id, int type, int q, int r, int health, FoodData food) {
    }

    public record EnemyDto(int type, int q, int r, int health, FoodData food) {
    }

    public record FoodDto(int q, int r, int type, int amount) {
    }

    public record MapCellDto(int q, int r, int type, int cost) {
    }

    public record FoodData(int type, int amount) {
    }
}
