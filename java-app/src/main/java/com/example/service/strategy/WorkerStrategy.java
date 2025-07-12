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
 * Этот класс полностью инкапсулирует всю логику поведения рабочих,
 * следуя строгой иерархии приоритетов.
 * <ol>
 *     <li><b>Возврат Ресурсов:</b> Рабочий с едой всегда движется к ближайшему гексу улья.</li>
 *     <li><b>Освобождение Улья:</b> Рабочий без еды на гексе улья немедленно отходит в сторону.</li>
 *     <li><b>Сбор Ресурсов:</b> Свободный рабочий ищет и направляется к ближайшей видимой еде.</li>
 *     <li><b>Активное Исследование:</b> Если задач выше нет, рабочий отправляется в "сумеречную зону"
 *     (известные, но невидимые участки карты) для поиска новых ресурсов.</li>
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
        if (workers.isEmpty()) {
            return Collections.emptyList();
        }

        List<MoveCommandDto> commands = new ArrayList<>();
        Set<String> assignedWorkerIds = new HashSet<>();

        // Приоритет 1 и 2: Возврат с едой и освобождение улья.
        for (ArenaStateDto.AntDto worker : workers) {
            handleHighPriorityTasks(worker, state).ifPresent(command -> {
                commands.add(command);
                assignedWorkerIds.add(worker.id());
            });
        }

        List<ArenaStateDto.AntDto> availableWorkers = workers.stream()
                .filter(w -> !assignedWorkerIds.contains(w.id()))
                .collect(Collectors.toList());

        // Приоритет 3: Сбор видимой еды.
        Set<Hex> homeHexes = Set.copyOf(state.home());
        List<ArenaStateDto.FoodDto> collectibleFood = state.food().stream()
                .filter(food -> !homeHexes.contains(new Hex(food.q(), food.r())))
                .toList();

        List<FoodAssignment> foodAssignments = createOptimalAssignments(availableWorkers, collectibleFood);
        for (FoodAssignment assignment : foodAssignments) {
            Hex target = new Hex(assignment.food().q(), assignment.food().r());
            StrategyHelper.createPathCommand(assignment.worker(), target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state))
                    .ifPresent(commands::add);
            assignedWorkerIds.add(assignment.worker().id());
        }

        // Приоритет 4: Исследование.
        workers.stream()
                .filter(worker -> !assignedWorkerIds.contains(worker.id()))
                .forEach(idleWorker -> createExploreCommand(idleWorker, state).ifPresent(commands::add));

        return commands;
    }

    private Optional<MoveCommandDto> handleHighPriorityTasks(ArenaStateDto.AntDto worker, ArenaStateDto state) {
        Set<Hex> homeHexes = Set.copyOf(state.home());

        if (isCarryingFood(worker)) {
            return findClosestHomeHex(new Hex(worker.q(), worker.r()), new ArrayList<>(homeHexes))
                    .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
        }

        if (homeHexes.contains(new Hex(worker.q(), worker.r()))) {
            return createMoveAsideCommand(worker, state, homeHexes);
        }

        return Optional.empty();
    }

    private Optional<MoveCommandDto> createMoveAsideCommand(ArenaStateDto.AntDto worker, ArenaStateDto state, Set<Hex> homeHexes) {
        Set<Hex> obstacles = StrategyHelper.getObstaclesFor(worker, state);
        return new Hex(worker.q(), worker.r()).getNeighbors().stream()
                .filter(neighbor -> !obstacles.contains(neighbor) && !homeHexes.contains(neighbor))
                .findAny()
                .map(target -> new MoveCommandDto(worker.id(), List.of(target)));
    }

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

    private Set<Hex> calculateCurrentlyVisibleHexes(ArenaStateDto state) {
        Set<Hex> visibleHexes = new HashSet<>();
        for (ArenaStateDto.AntDto ant : state.ants()) {
            UnitType type = UnitType.fromApiId(ant.type());
            visibleHexes.addAll(getHexesInRange(new Hex(ant.q(), ant.r()), type.getVision()));
        }
        if (state.spot() != null) {
            visibleHexes.addAll(getHexesInRange(state.spot(), 2));
        }
        return visibleHexes;
    }

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

    private boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    private Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream().min(Comparator.comparingInt(from::distanceTo));
    }
}
