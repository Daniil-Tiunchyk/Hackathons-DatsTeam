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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис-диспетчер, который оркеструет применение различных стратегий поведения
 * для юнитов.
 * <p>
 * Логика построена по принципу "Базовый План с Приоритетными Отменами":
 * 1. Для всех юнитов создается "идеальный" план действий с помощью их ролевых стратегий.
 * 2. Затем этот план корректируется: высокоприоритетные задачи (возврат с едой,
 * освобождение улья) отменяют и заменяют "идеальные" команды.
 * Разведчики из этого шага исключены и действуют полностью автономно.
 */
public class StrategyService {

    private final StrategyProvider strategyProvider;
    private final Pathfinder pathfinder;

    public StrategyService(StrategyProvider strategyProvider, Pathfinder pathfinder) {
        this.strategyProvider = strategyProvider;
        this.pathfinder = pathfinder;
    }

    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        if (state.ants() == null || state.ants().isEmpty()) {
            return Collections.emptyList();
        }

        Map<UnitType, List<ArenaStateDto.AntDto>> antsByType = state.ants().stream()
                .collect(Collectors.groupingBy(ant -> UnitType.fromApiId(ant.type())));

        Map<String, MoveCommandDto> basePlan = createBasePlan(antsByType, state);

        return applyHighPriorityOverrides(state, basePlan);
    }

    private Map<String, MoveCommandDto> createBasePlan(Map<UnitType, List<ArenaStateDto.AntDto>> antsByType, ArenaStateDto state) {
        return antsByType.entrySet().stream()
                .flatMap(entry -> {
                    AntStrategy strategy = strategyProvider.getStrategy(entry.getKey());
                    return strategy.decideMoves(entry.getValue(), state).stream();
                })
                .collect(Collectors.toMap(MoveCommandDto::ant, Function.identity(), (a, b) -> a));
    }

    private List<MoveCommandDto> applyHighPriorityOverrides(ArenaStateDto state, Map<String, MoveCommandDto> basePlan) {
        final Map<String, MoveCommandDto> finalPlan = new java.util.HashMap<>(basePlan);
        final Set<Hex> homeHexes = Set.copyOf(state.home());

        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (UnitType.fromApiId(ant.type()) == UnitType.SCOUT) {
                continue;
            }

            if (UnitType.fromApiId(ant.type()) == UnitType.FIGHTER) {
                continue;
            }

            if (StrategyHelper.isCarryingFood(ant)) {
                createReturnHomeCommand(ant, state, homeHexes).ifPresent(cmd -> finalPlan.put(ant.id(), cmd));
                continue;
            }

            if (homeHexes.contains(new Hex(ant.q(), ant.r()))) {
                createMoveAsideCommand(ant, state, homeHexes).ifPresent(cmd -> finalPlan.put(ant.id(), cmd));
            }
        }
        return new ArrayList<>(finalPlan.values());
    }

    private Optional<MoveCommandDto> createReturnHomeCommand(ArenaStateDto.AntDto ant, ArenaStateDto state, Set<Hex> homeHexes) {
        return StrategyHelper.findClosestHomeHex(new Hex(ant.q(), ant.r()), new ArrayList<>(homeHexes))
                .flatMap(target -> StrategyHelper.createPathCommand(ant, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
    }

    private Optional<MoveCommandDto> createMoveAsideCommand(ArenaStateDto.AntDto ant, ArenaStateDto state, Set<Hex> homeHexes) {
        Set<Hex> obstacles = StrategyHelper.getObstaclesFor(ant, state);
        return new Hex(ant.q(), ant.r()).getNeighbors().stream()
                .filter(neighbor -> !obstacles.contains(neighbor) && !homeHexes.contains(neighbor))
                .findAny()
                .map(target -> new MoveCommandDto(ant.id(), List.of(target)));
    }
}
