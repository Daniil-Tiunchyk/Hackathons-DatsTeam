package com.example.service;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Отвечает за принятие стратегических решений на основе цепочки приоритетных задач.
 * Этот подход обеспечивает соблюдение правил, отказоустойчивость и расширяемость.
 */
public class StrategyService {

    private final Pathfinder pathfinder;

    public StrategyService(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        Set<String> assignedAnts = new HashSet<>();
        Set<Hex> assignedFoodTargets = new HashSet<>();

        Map<Hex, Integer> hexCosts = state.map().stream()
                .collect(Collectors.toMap(cell -> new Hex(cell.q(), cell.r()), ArenaStateDto.MapCellDto::cost));

        // 1. Приоритет: муравьи с едой должны вернуться домой
        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (isCarryingFood(ant)) {
                tryToReturnHome(ant, state, hexCosts).ifPresent(command -> {
                    commands.add(command);
                    assignedAnts.add(ant.id());
                });
            }
        }

        // 2. Свободные муравьи идут за ближайшей свободной едой
        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (assignedAnts.contains(ant.id())) continue;

            tryToCollectFood(ant, state, hexCosts, assignedFoodTargets).ifPresent(command -> {
                commands.add(command);
                assignedAnts.add(ant.id());
                // Резервируем цель, чтобы другие муравьи за ней не пошли
                command.path().stream().reduce((first, second) -> second).ifPresent(assignedFoodTargets::add);
            });
        }

        // 3. Все остальные, кто стоит на основном гексе муравейника, отходят в сторону
        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (assignedAnts.contains(ant.id())) continue;

            Hex antHex = new Hex(ant.q(), ant.r());
            if (antHex.equals(state.spot())) {
                tryToMoveAside(ant, state, hexCosts).ifPresent(command -> {
                    commands.add(command);
                    assignedAnts.add(ant.id());
                });
            }
        }

        return commands;
    }

    private Optional<MoveCommandDto> tryToReturnHome(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts) {
        Hex antHex = new Hex(ant.q(), ant.r());
        return findClosestHomeHex(antHex, state.home())
                .flatMap(target -> createPathCommand(ant, target, state, hexCosts));
    }

    private Optional<MoveCommandDto> tryToCollectFood(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts, Set<Hex> assignedTargets) {
        Hex antHex = new Hex(ant.q(), ant.r());
        return findClosestAvailableFood(antHex, state.food(), assignedTargets)
                .flatMap(target -> createPathCommand(ant, target, state, hexCosts));
    }

    private Optional<MoveCommandDto> tryToMoveAside(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts) {
        Hex antHex = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> neighbors = new ArrayList<>(antHex.getNeighbors());
        Collections.shuffle(neighbors);

        return neighbors.stream()
                .filter(neighbor -> !obstacles.contains(neighbor))
                .findFirst()
                .flatMap(target -> {
                    UnitType unitType = UnitType.fromApiId(ant.type());
                    int costToMove = hexCosts.getOrDefault(target, 1);
                    if (unitType.getSpeed() >= costToMove) {
                        return Optional.of(new MoveCommandDto(ant.id(), List.of(target)));
                    }
                    return Optional.empty();
                });
    }

    private Optional<MoveCommandDto> createPathCommand(ArenaStateDto.AntDto ant, Hex target, ArenaStateDto state, Map<Hex, Integer> hexCosts) {
        Hex start = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> path = pathfinder.findPath(start, target, hexCosts, obstacles);

        if (!path.isEmpty()) {
            UnitType unitType = UnitType.fromApiId(ant.type());
            List<Hex> truncatedPath = truncatePathByMovementPoints(path, unitType.getSpeed(), hexCosts);
            if (!truncatedPath.isEmpty()) {
                return Optional.of(new MoveCommandDto(ant.id(), truncatedPath));
            }
        }
        return Optional.empty();
    }

    private Set<Hex> getObstaclesFor(ArenaStateDto.AntDto ant, ArenaStateDto state) {
        Set<Hex> obstacles = new HashSet<>();
        // Враги
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));
        // Дружественные юниты того же типа
        state.ants().stream()
                .filter(other -> other.type() == ant.type() && !other.id().equals(ant.id()))
                .forEach(other -> obstacles.add(new Hex(other.q(), other.r())));

        // Непроходимые и опасные гексы
        for (ArenaStateDto.MapCellDto cell : state.map()) {
            try {
                HexType hexType = HexType.fromApiId(cell.type());
                Hex cellHex = new Hex(cell.q(), cell.r());

                // Правило 1: Камни - непроходимы для всех
                if (hexType.isImpassable()) {
                    obstacles.add(cellHex);
                }

                // Правило 2: Кислота - смертельна для раненых муравьев
                // Считаем кислоту препятствием, если она убьет юнита
                if (hexType == HexType.ACID && ant.health() <= hexType.getDamage()) {
                    obstacles.add(cellHex);
                }
            } catch (IllegalArgumentException ignored) {
            }
        }
        return obstacles;
    }

    private List<Hex> truncatePathByMovementPoints(List<Hex> path, int maxPoints, Map<Hex, Integer> hexCosts) {
        List<Hex> resultPath = new ArrayList<>();
        int pointsSpent = 0;
        for (Hex step : path) {
            int cost = hexCosts.getOrDefault(step, 1);
            if (pointsSpent + cost <= maxPoints) {
                pointsSpent += cost;
                resultPath.add(step);
            } else {
                break;
            }
        }
        return resultPath;
    }

    private boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    private Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream().min(Comparator.comparingInt(from::distanceTo));
    }

    private Optional<Hex> findClosestAvailableFood(Hex from, List<ArenaStateDto.FoodDto> foods, Set<Hex> assignedTargets) {
        return foods.stream()
                .map(food -> new Hex(food.q(), food.r()))
                .filter(foodHex -> !assignedTargets.contains(foodHex))
                .min(Comparator.comparingInt(from::distanceTo));
    }
}
