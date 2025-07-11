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
        Set<Hex> assignedFoodTargets = new HashSet<>();
        Map<Hex, Integer> hexCosts = state.map().stream()
                .collect(Collectors.toMap(cell -> new Hex(cell.q(), cell.r()), ArenaStateDto.MapCellDto::cost));

        for (ArenaStateDto.AntDto ant : state.ants()) {

            Optional<MoveCommandDto> command = tryToReturnHome(ant, state, hexCosts);

            if (command.isEmpty()) {
                Optional<MoveCommandDto> collectFoodCommand = tryToCollectFood(ant, state, hexCosts, assignedFoodTargets);
                if (collectFoodCommand.isPresent()) {
                    command = collectFoodCommand;
                    List<Hex> path = collectFoodCommand.get().path();
                    if (!path.isEmpty()) {
                        assignedFoodTargets.add(path.get(path.size() - 1));
                    }
                }
            }

            if (command.isEmpty()) {
                command = tryToMoveAside(ant, state, hexCosts);
            }

            command.ifPresent(commands::add);
        }
        return commands;
    }

    private Optional<MoveCommandDto> tryToReturnHome(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts) {
        if (!isCarryingFood(ant)) {
            return Optional.empty();
        }

        Hex antHex = new Hex(ant.q(), ant.r());
        return findClosestHomeHex(antHex, state.home())
                .flatMap(target -> createPathCommand(ant, target, state, hexCosts));
    }

    private Optional<MoveCommandDto> tryToCollectFood(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts, Set<Hex> assignedTargets) {
        if (isCarryingFood(ant)) {
            return Optional.empty();
        }

        Hex antHex = new Hex(ant.q(), ant.r());
        return findClosestAvailableFood(antHex, state.food(), assignedTargets)
                .flatMap(target -> createPathCommand(ant, target, state, hexCosts));
    }

    private Optional<MoveCommandDto> tryToMoveAside(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts) {
        Hex antHex = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> neighbors = new ArrayList<>(getNeighbors(antHex));
        Collections.shuffle(neighbors);

        return neighbors.stream()
                .filter(neighbor -> !obstacles.contains(neighbor))
                .findFirst()
                .map(target -> {
                    UnitType unitType = UnitType.fromApiId(ant.type());
                    int costToMove = hexCosts.getOrDefault(target, 1);
                    if (unitType.getSpeed() >= costToMove) {
                        return new MoveCommandDto(ant.id(), List.of(target));
                    }
                    return null;
                });
    }

    private Optional<MoveCommandDto> createPathCommand(ArenaStateDto.AntDto ant, Hex target, ArenaStateDto state, Map<Hex, Integer> hexCosts) {
        Hex start = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> path = pathfinder.findPath(start, target, hexCosts, obstacles);

        if (path.size() > 1) {
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
        // Добавляем врагов
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));
        // Добавляем союзников того же типа
        state.ants().stream()
                .filter(other -> other.type() == ant.type() && !other.id().equals(ant.id()))
                .forEach(other -> obstacles.add(new Hex(other.q(), other.r())));

        // Добавляем препятствия, основанные на типе гекса
        for (ArenaStateDto.MapCellDto cell : state.map()) {
            try {
                HexType hexType = HexType.fromApiId(cell.type());
                Hex cellHex = new Hex(cell.q(), cell.r());

                // Правило 1: Камни - непроходимы для всех
                if (hexType.isImpassable()) {
                    obstacles.add(cellHex);
                }

                // Правило 2: Кислота - смертельна для раненых муравьев
                if (hexType == HexType.ACID && ant.health() <= hexType.getDamage()) {
                    obstacles.add(cellHex);
                }

            } catch (IllegalArgumentException e) {
                // Игнорируем неизвестные типы гексов, чтобы не прерывать работу
            }
        }
        return obstacles;
    }

    private List<Hex> truncatePathByMovementPoints(List<Hex> path, int maxPoints, Map<Hex, Integer> hexCosts) {
        List<Hex> resultPath = new ArrayList<>();
        int pointsSpent = 0;
        for (int i = 1; i < path.size(); i++) {
            Hex step = path.get(i);
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

    private List<Hex> getNeighbors(Hex hex) {
        return List.of(
                new Hex(hex.q() + 1, hex.r()), new Hex(hex.q() - 1, hex.r()),
                new Hex(hex.q(), hex.r() + 1), new Hex(hex.q(), hex.r() - 1),
                new Hex(hex.q() + 1, hex.r() - 1), new Hex(hex.q() - 1, hex.r() + 1)
        );
    }
}
