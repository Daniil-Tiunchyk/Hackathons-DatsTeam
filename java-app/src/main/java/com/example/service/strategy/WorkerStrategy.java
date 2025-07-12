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
 * Этот класс полностью инкапсулирует всю логику поведения рабочих. Решения принимаются
 * для каждого юнита индивидуально на основе строгой иерархии приоритетов.
 * <ol>
 *     <li><b>Управление грузом:</b> Если рабочий несет >70% от своей грузоподъемности, он возвращается
 *     на базу. Если <70%, он ищет ближайший ресурс ТОГО ЖЕ ТИПА, чтобы дозагрузиться.</li>
 *     <li><b>Освобождение Улья:</b> Рабочий без еды на гексе улья немедленно отходит в сторону.</li>
 *     <li><b>Сбор Ресурсов:</b> Свободный рабочий ищет и направляется к ближайшей видимой еде.</li>
 *     <li><b>Активное Исследование:</b> Если задач выше нет, рабочий отправляется в "сумеречную зону"
 *     для поиска новых ресурсов.</li>
 * </ol>
 */
public class WorkerStrategy implements AntStrategy {

    private static final double RETURN_HOME_CAPACITY_THRESHOLD = 0.7;
    private final Pathfinder pathfinder;

    public WorkerStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> workers, ArenaStateDto state) {
        if (workers.isEmpty()) {
            return Collections.emptyList();
        }

        List<MoveCommandDto> commands = new ArrayList<>();
        Set<Hex> claimedFoodHexes = new HashSet<>();

        for (ArenaStateDto.AntDto worker : workers) {
            decideActionFor(worker, state, claimedFoodHexes).ifPresent(command -> {
                commands.add(command);
                // Если команда ведет к еде, помечаем эту еду как "занятую" для других
                if (command.path() != null && !command.path().isEmpty()) {
                    Hex targetHex = command.path().getLast();
                    if (state.food().stream().anyMatch(f -> new Hex(f.q(), f.r()).equals(targetHex))) {
                        claimedFoodHexes.add(targetHex);
                    }
                }
            });
        }
        return commands;
    }

    /**
     * Центральный метод принятия решений для одного рабочего, реализующий иерархию приоритетов.
     */
    private Optional<MoveCommandDto> decideActionFor(ArenaStateDto.AntDto worker, ArenaStateDto state, Set<Hex> claimedFood) {
        // Приоритет 1: Управление грузом
        if (isCarryingFood(worker)) {
            UnitType type = UnitType.fromApiId(worker.type());
            double capacity = type.getCapacity();
            double currentLoad = worker.food().amount();

            if (currentLoad / capacity >= RETURN_HOME_CAPACITY_THRESHOLD) {
                return createReturnHomeCommand(worker, state); // Еды достаточно, идем домой
            } else {
                // Еды мало, ищем еще, но только того же типа
                return findMoreFoodOfType(worker, state, claimedFood);
            }
        }

        Set<Hex> homeHexes = Set.copyOf(state.home());
        // Приоритет 2: Освобождение улья
        if (homeHexes.contains(new Hex(worker.q(), worker.r()))) {
            return createMoveAsideCommand(worker, state, homeHexes);
        }

        // Приоритет 3: Сбор ближайшей доступной еды
        Optional<MoveCommandDto> collectFoodCommand = findAndGoToClosestFood(worker, state, claimedFood);
        if (collectFoodCommand.isPresent()) {
            return collectFoodCommand;
        }

        // Приоритет 4: Исследование
        return createExploreCommand(worker, state);
    }

    private Optional<MoveCommandDto> createReturnHomeCommand(ArenaStateDto.AntDto worker, ArenaStateDto state) {
        return findClosestHomeHex(new Hex(worker.q(), worker.r()), state.home())
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
    }

    private Optional<MoveCommandDto> findMoreFoodOfType(ArenaStateDto.AntDto worker, ArenaStateDto state, Set<Hex> claimedFood) {
        int currentFoodType = worker.food().type();
        Hex currentPos = new Hex(worker.q(), worker.r());

        return state.food().stream()
                .filter(food -> food.type() == currentFoodType)
                .map(food -> new Hex(food.q(), food.r()))
                .filter(hex -> !claimedFood.contains(hex))
                .min(Comparator.comparingInt(currentPos::distanceTo))
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
    }

    private Optional<MoveCommandDto> findAndGoToClosestFood(ArenaStateDto.AntDto worker, ArenaStateDto state, Set<Hex> claimedFood) {
        Hex currentPos = new Hex(worker.q(), worker.r());
        Set<Hex> homeHexes = Set.copyOf(state.home());

        return state.food().stream()
                .filter(food -> !homeHexes.contains(new Hex(food.q(), food.r())))
                .map(food -> new Hex(food.q(), food.r()))
                .filter(hex -> !claimedFood.contains(hex))
                .min(Comparator.comparingInt(currentPos::distanceTo))
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
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

    private boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    private Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream().min(Comparator.comparingInt(from::distanceTo));
    }
}
