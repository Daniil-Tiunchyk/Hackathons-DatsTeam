package com.example.service.strategy;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация {@link AntStrategy} для юнитов-рабочих.
 * <p>
 * Применяет двухуровневую логику:
 * 1. Назначает рабочих на сбор ближайших ресурсов, ИСКЛЮЧАЯ уже сданные в улей.
 * 2. Оставшимся без дела рабочим дает команду отойти от базы на заданное
 * расстояние для перепозиционирования и предотвращения "пробок".
 */
public class WorkerStrategy implements AntStrategy {

    private static final int REPOSITION_DISTANCE = 6;
    private final Pathfinder pathfinder;

    public WorkerStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    private record FoodAssignment(ArenaStateDto.AntDto worker, ArenaStateDto.FoodDto food) {
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> workers, ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        if (workers.isEmpty()) {
            return commands;
        }

        // ФИКС: Исключаем еду, находящуюся на гексах нашего улья.
        Set<Hex> homeHexes = Set.copyOf(state.home());
        List<ArenaStateDto.FoodDto> collectibleFood = state.food().stream()
                .filter(food -> !homeHexes.contains(new Hex(food.q(), food.r())))
                .toList();

        // 1. Назначаем рабочих на сбор еды
        List<FoodAssignment> assignments = createOptimalAssignments(workers, collectibleFood);
        Set<String> assignedWorkerIds = assignments.stream()
                .map(assignment -> assignment.worker().id())
                .collect(Collectors.toSet());

        for (FoodAssignment assignment : assignments) {
            Hex target = new Hex(assignment.food().q(), assignment.food().r());
            StrategyHelper.createPathCommand(assignment.worker(), target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state))
                    .ifPresent(commands::add);
        }

        // 2. Оставшимся без дела даем команду на перепозиционирование
        workers.stream()
                .filter(worker -> !assignedWorkerIds.contains(worker.id()))
                .forEach(idleWorker -> createRepositionCommand(idleWorker, state).ifPresent(commands::add));

        return commands;
    }

    private Optional<MoveCommandDto> createRepositionCommand(ArenaStateDto.AntDto worker, ArenaStateDto state) {
        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);
        Hex currentHex = new Hex(worker.q(), worker.r());

        // Находим ближайшую к рабочему точку на кольце перепозиционирования
        return generateRing(state.spot(), REPOSITION_DISTANCE).stream()
                .min(Comparator.comparingInt(currentHex::distanceTo))
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, hexCosts, hexTypes));
    }

    private List<FoodAssignment> createOptimalAssignments(List<ArenaStateDto.AntDto> availableWorkers, List<ArenaStateDto.FoodDto> availableFood) {
        List<FoodAssignment> assignments = new ArrayList<>();
        List<ArenaStateDto.AntDto> workersPool = new ArrayList<>(availableWorkers);

        if (availableFood == null || availableFood.isEmpty()) {
            return assignments;
        }

        for (ArenaStateDto.FoodDto food : availableFood) {
            if (workersPool.isEmpty()) break;

            Hex foodHex = new Hex(food.q(), food.r());
            workersPool.stream()
                    .min(Comparator.comparingInt(worker -> new Hex(worker.q(), worker.r()).distanceTo(foodHex)))
                    .ifPresent(bestWorker -> {
                        assignments.add(new FoodAssignment(bestWorker, food));
                        workersPool.remove(bestWorker);
                    });
        }
        return assignments;
    }

    private List<Hex> generateRing(Hex center, int radius) {
        if (radius <= 0) {
            return List.of(center);
        }
        List<Hex> results = new ArrayList<>();
        Hex current = center.add(new Hex(0, -radius));

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < radius; j++) {
                results.add(current);
                current = current.getNeighbors().get((i + 2) % 6);
            }
        }
        return results;
    }
}
