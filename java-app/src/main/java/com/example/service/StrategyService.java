package com.example.service;

import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.strategy.AntStrategy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Сервис-диспетчер, который оркеструет применение различных стратегий поведения
 * для юнитов.
 * <p>
 * Его единственная задача — сгруппировать юнитов по типу и делегировать
 * принятие решений соответствующим, полностью автономным классам-стратегиям.
 * Он не содержит никакой игровой логики.
 */
public class StrategyService {

    private final StrategyProvider strategyProvider;

    public StrategyService(StrategyProvider strategyProvider, Pathfinder pathfinder) {
        this.strategyProvider = strategyProvider;
    }

    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        if (state.ants() == null || state.ants().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Группируем всех муравьев по их типу.
        Map<UnitType, List<ArenaStateDto.AntDto>> antsByType = state.ants().stream()
                .collect(Collectors.groupingBy(ant -> UnitType.fromApiId(ant.type())));

        // 2. Для каждой группы юнитов вызываем соответствующую стратегию и собираем команды.
        return antsByType.entrySet().stream()
                .flatMap(entry -> {
                    AntStrategy strategy = strategyProvider.getStrategy(entry.getKey());
                    return strategy.decideMoves(entry.getValue(), state).stream();
                })
                .collect(Collectors.toList());
    }
}
