package com.example.service.strategy;

import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;

import java.util.Collections;
import java.util.List;

/**
 * Реализация {@link AntStrategy} для юнитов-разведчиков.
 * <p>
 * ЗАГЛУШКА: В будущем здесь будет реализована разведывательная логика.
 */
public class ScoutStrategy implements AntStrategy {

    private final Pathfinder pathfinder;

    public ScoutStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> scouts, ArenaStateDto state) {
        // ЗАГЛУШКА: Здесь будет реализована логика для разведчиков.
        // Например:
        // - Движение в "туман войны" для исследования карты.
        // - Поиск вражеских муравейников и скоплений ресурсов.
        // - Избегание прямого столкновения с боевыми юнитами противника.
        return Collections.emptyList();
    }
}
