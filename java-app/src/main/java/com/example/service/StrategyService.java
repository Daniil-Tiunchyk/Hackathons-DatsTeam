package com.example.service;

import com.example.domain.Hex;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.strategy.AntStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис-диспетчер, который оркеструет применение различных стратегий и
 * применяет высокоприоритетные глобальные правила.
 * <p>
 * <b>Порядок Действий:</b>
 * <ol>
 *     <li>Для всех юнитов создается "базовый план" с помощью их ролевых стратегий.</li>
 *     <li>Затем этот план корректируется: применяется глобальное правило по освобождению
 *     гексов улья, которое может переопределить команды из базового плана.</li>
 * </ol>
 */
public class StrategyService {

    private final StrategyProvider strategyProvider;

    public StrategyService(StrategyProvider strategyProvider) {
        this.strategyProvider = strategyProvider;
    }

    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        if (state.ants() == null || state.ants().isEmpty()) {
            return Collections.emptyList();
        }

        Map<UnitType, List<ArenaStateDto.AntDto>> antsByType = state.ants().stream()
                .collect(Collectors.groupingBy(ant -> UnitType.fromApiId(ant.type())));

        // 1. Создаем базовый план от ролевых стратегий
        Map<String, MoveCommandDto> basePlan = antsByType.entrySet().stream()
                .flatMap(entry -> {
                    AntStrategy strategy = strategyProvider.getStrategy(entry.getKey());
                    return strategy.decideMoves(entry.getValue(), state).stream();
                })
                .collect(Collectors.toMap(MoveCommandDto::ant, Function.identity(), (a, b) -> a));

        // 2. Применяем глобальные правила, которые могут переопределить базовый план
        return applyHighPriorityOverrides(state, basePlan);
    }

    /**
     * Применяет глобальные, высокоприоритетные правила, которые могут переопределить
     * любые решения, принятые ролевыми стратегиями.
     *
     * @param state    Текущее состояние мира.
     * @param basePlan Карта команд, сгенерированных ролевыми стратегиями.
     * @return Финальный список команд после применения глобальных правил.
     */
    private List<MoveCommandDto> applyHighPriorityOverrides(ArenaStateDto state, Map<String, MoveCommandDto> basePlan) {
        final Map<String, MoveCommandDto> finalPlan = new java.util.HashMap<>(basePlan);
        final Set<Hex> homeHexes = Set.copyOf(state.home());

        for (ArenaStateDto.AntDto ant : state.ants()) {
            // Глобальное правило №1: Освобождение улья.
            // Если юнит стоит на гексе улья И при этом НЕ несет еду (т.е. ему не нужно разгружаться),
            // он должен принудительно отойти в сторону.
            if (homeHexes.contains(new Hex(ant.q(), ant.r())) && !StrategyHelper.isCarryingFood(ant)) {
                StrategyHelper.createMoveAsideCommand(ant, state, homeHexes)
                        .ifPresent(cmd -> finalPlan.put(ant.id(), cmd));
            }
        }
        return new ArrayList<>(finalPlan.values());
    }
}
