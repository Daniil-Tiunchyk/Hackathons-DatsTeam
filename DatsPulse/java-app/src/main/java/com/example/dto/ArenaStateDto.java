package com.example.dto;

import com.example.domain.Hex;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Объект Передачи Данных (DTO), представляющий полное состояние арены.
 * Включает как динамические данные с последнего ответа API, так и
 * накопленную за раунд статическую информацию о карте.
 */
public record ArenaStateDto(
        List<AntDto> ants,
        List<EnemyDto> enemies,
        List<FoodDto> food,
        List<Hex> home,
        List<MapCellDto> map,
        Set<Hex> knownBoundaries,
        Set<Hex> currentlyVisibleHexes,
        double nextTurnIn,
        int score,
        Hex spot,
        int turnNo
) {
    public ArenaStateDto {
        ants = Optional.ofNullable(ants).orElse(Collections.emptyList());
        enemies = Optional.ofNullable(enemies).orElse(Collections.emptyList());
        food = Optional.ofNullable(food).orElse(Collections.emptyList());
        home = Optional.ofNullable(home).orElse(Collections.emptyList());
        map = Optional.ofNullable(map).orElse(Collections.emptyList());
        knownBoundaries = Optional.ofNullable(knownBoundaries).orElse(Collections.emptySet());
        currentlyVisibleHexes = Optional.ofNullable(currentlyVisibleHexes).orElse(Collections.emptySet());
    }

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
