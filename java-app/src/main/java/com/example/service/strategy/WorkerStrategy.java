package com.example.service.strategy;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Реализация {@link AntStrategy} для юнитов-рабочих.
 * <p>
 * Инкапсулирует как саму стратегию (сбор ресурсов), так и логику
 * оптимального назначения задач, чтобы избежать избыточных вычислений.
 */
public class WorkerStrategy implements AntStrategy {

    private final Pathfinder pathfinder;

    public WorkerStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    /**
     * Внутренний record для представления созданного задания "юнит -> цель".
     */
    private record FoodAssignment(ArenaStateDto.AntDto worker, Hex target) {
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> workers, ArenaStateDto state) {
        if (workers.isEmpty() || state.food().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Получаем оптимальный план назначений
        List<FoodAssignment> assignments = createOptimalAssignments(workers, state.food());
        if (assignments.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);

        // 2. Преобразуем план в конкретные команды
        return assignments.stream()
                .flatMap(assignment ->
                        StrategyHelper.createPathCommand(
                                assignment.worker(),
                                assignment.target(),
                                state,
                                pathfinder,
                                hexCosts,
                                hexTypes
                        ).stream()
                )
                .toList();
    }

    /**
     * Создает оптимальный список назначений "рабочий -> еда" с помощью жадного алгоритма.
     * <p>
     *
     * @param availableWorkers Список свободных рабочих.
     * @param availableFood    Список доступной еды.
     * @return Список оптимально распределенных заданий.
     */
    private List<FoodAssignment> createOptimalAssignments(List<ArenaStateDto.AntDto> availableWorkers, List<ArenaStateDto.FoodDto> availableFood) {
        List<FoodAssignment> assignments = new ArrayList<>();
        List<ArenaStateDto.AntDto> workersPool = new ArrayList<>(availableWorkers);

        for (ArenaStateDto.FoodDto food : availableFood) {
            if (workersPool.isEmpty()) break;

            Hex foodHex = new Hex(food.q(), food.r());

            Optional<ArenaStateDto.AntDto> bestWorker = workersPool.stream()
                    .min(Comparator.comparingInt(worker -> new Hex(worker.q(), worker.r()).distanceTo(foodHex)));

            bestWorker.ifPresent(worker -> {
                assignments.add(new FoodAssignment(worker, foodHex));
                workersPool.remove(worker);
            });
        }
        return assignments;
    }
}
