package com.example.service.strategy;

import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;

import java.util.Collections;
import java.util.List;

/**
 * Реализация {@link AntStrategy} для юнитов-бойцов.
 * <p>
 * ЗАГЛУШКА: В будущем здесь будет реализована боевая логика.
 */
public class FighterStrategy implements AntStrategy {

    private final Pathfinder pathfinder;

    public FighterStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state) {
        // ЗАГЛУШКА: Здесь будет реализована логика для бойцов.
        // Например:
        // - Поиск и атака ближайших врагов.
        // - Защита ключевых точек (муравейник, скопления ресурсов).
        // - Формирование боевых групп для скоординированной атаки.
        return Collections.emptyList();
    }
}
