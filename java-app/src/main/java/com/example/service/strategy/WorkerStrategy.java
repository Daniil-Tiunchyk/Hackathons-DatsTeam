package com.example.service.strategy;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализует {@link AntStrategy} для юнитов-рабочих.
 * <p>
 * Применяет интеллектуальную, двухуровневую логику:
 * <ol>
 *     <li><b>Сбор ресурсов:</b> Назначает свободных рабочих на сбор ближайшей видимой еды.</li>
 *     <li><b>Активное исследование:</b> Рабочие, не занятые сбором, отправляются в
 *     "сумеречную зону" - известные, но невидимые в данный момент участки карты.
 *     Это позволяет целенаправленно расширять обзор для поиска новых ресурсов.</li>
 * </ol>
 */
public class WorkerStrategy implements AntStrategy {

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

        Set<Hex> homeHexes = Set.copyOf(state.home());
        List<ArenaStateDto.FoodDto> collectibleFood = state.food().stream()
                .filter(food -> !homeHexes.contains(new Hex(food.q(), food.r())))
                .toList();

        List<FoodAssignment> assignments = createOptimalAssignments(workers, collectibleFood);
        Set<String> assignedWorkerIds = assignments.stream()
                .map(assignment -> assignment.worker().id())
                .collect(Collectors.toSet());

        for (FoodAssignment assignment : assignments) {
            Hex target = new Hex(assignment.food().q(), assignment.food().r());
            StrategyHelper.createPathCommand(assignment.worker(), target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state))
                    .ifPresent(commands::add);
        }

        workers.stream()
                .filter(worker -> !assignedWorkerIds.contains(worker.id()))
                .forEach(idleWorker -> createExploreCommand(idleWorker, state).ifPresent(commands::add));

        return commands;
    }

    /**
     * Создает команду для бездействующего рабочего на исследование "сумеречной зоны".
     * Целью выбирается ближайший к рабочему безопасный гекс, который известен, но не виден в данный момент.
     *
     * @param worker Бездействующий рабочий.
     * @param state  Полное состояние мира.
     * @return {@link Optional} с командой на исследование или пустой, если исследовать нечего.
     */
    private Optional<MoveCommandDto> createExploreCommand(ArenaStateDto.AntDto worker, ArenaStateDto state) {
        Set<Hex> visibleHexes = calculateCurrentlyVisibleHexes(state);
        Set<Hex> knownHexes = state.map().stream()
                .map(cell -> new Hex(cell.q(), cell.r()))
                .collect(Collectors.toSet());

        Set<Hex> potentialTargets = new HashSet<>(knownHexes);
        potentialTargets.removeAll(visibleHexes);

        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);
        potentialTargets.removeIf(hex -> {
            HexType type = hexTypes.get(hex);
            return type != null && type.isImpassable();
        });

        if (potentialTargets.isEmpty()) {
            return Optional.empty();
        }

        Hex currentHex = new Hex(worker.q(), worker.r());

        return potentialTargets.stream()
                .min(Comparator.comparingInt(currentHex::distanceTo))
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), hexTypes));
    }

    /**
     * Рассчитывает полное множество всех гексов, видимых нашими юнитами в данный момент.
     */
    private Set<Hex> calculateCurrentlyVisibleHexes(ArenaStateDto state) {
        Set<Hex> visibleHexes = new HashSet<>();
        for (ArenaStateDto.AntDto ant : state.ants()) {
            UnitType type = UnitType.fromApiId(ant.type());
            visibleHexes.addAll(getHexesInRange(new Hex(ant.q(), ant.r()), type.getVision()));
        }
        if (state.spot() != null) {
            visibleHexes.addAll(getHexesInRange(state.spot(), 2)); // Обзор основного гекса улья
        }
        return visibleHexes;
    }

    /**
     * Возвращает все гексы в заданном радиусе от центра, включая сам центр.
     */
    private Set<Hex> getHexesInRange(Hex center, int radius) {
        Set<Hex> results = new HashSet<>();
        for (int q = -radius; q <= radius; q++) {
            for (int r = Math.max(-radius, -q - radius); r <= Math.min(radius, -q + radius); r++) {
                results.add(center.add(new Hex(q, r)));
            }
        }
        return results;
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
}
