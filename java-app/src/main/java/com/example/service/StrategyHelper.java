package com.example.service;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилитный класс, содержащий общие статические методы, используемые
 * различными стратегиями и сервисами.
 */
public final class StrategyHelper {

    private StrategyHelper() {
    }

    public static Optional<MoveCommandDto> createPathCommand(ArenaStateDto.AntDto ant, Hex target, ArenaStateDto state, Pathfinder pathfinder, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Hex start = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> path = pathfinder.findPath(ant, start, target, hexCosts, hexTypes, obstacles);
        if (path.isEmpty()) return Optional.empty();

        UnitType unitType = UnitType.fromApiId(ant.type());

        int movementLimit = (unitType == UnitType.SCOUT)
                ? unitType.getVision()
                : unitType.getSpeed();

        List<Hex> truncatedPath = truncatePathByMovementPoints(path, movementLimit, hexCosts);
        if (truncatedPath.isEmpty()) return Optional.empty();

        Hex finalDestination = truncatedPath.getLast();
        if (isUnsafeFinalDestination(finalDestination, ant, hexTypes)) {
            return Optional.empty();
        }

        return Optional.of(new MoveCommandDto(ant.id(), truncatedPath));
    }

    /**
     * Собирает полный набор препятствий для заданного юнита.
     * Препятствием считается любой другой дружественный юнит, враг или непроходимый гекс.
     *
     * @param ant   Юнит, для которого определяются препятствия.
     * @param state Текущее состояние арены.
     * @return Множество всех непроходимых гексов.
     */
    public static Set<Hex> getObstaclesFor(ArenaStateDto.AntDto ant, ArenaStateDto state) {
        Set<Hex> obstacles = new HashSet<>();
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));

        state.ants().stream()
                .filter(other -> other.type() == ant.type() && !other.id().equals(ant.id()))
                .forEach(other -> obstacles.add(new Hex(other.q(), other.r())));

        state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .forEach(cell -> obstacles.add(new Hex(cell.q(), cell.r())));
        return obstacles;
    }

    public static boolean isUnsafeFinalDestination(Hex destination, ArenaStateDto.AntDto ant, Map<Hex, HexType> hexTypes) {
        HexType destinationType = hexTypes.get(destination);
        if (destinationType == HexType.ACID) {
            if (UnitType.fromApiId(ant.type()) == UnitType.SCOUT) {
                return true;
            }
            return ant.health() <= destinationType.getDamage();
        }
        return false;
    }

    public static List<Hex> truncatePathByMovementPoints(List<Hex> path, int maxPoints, Map<Hex, Integer> hexCosts) {
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

    public static boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    public static Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream().min(Comparator.comparingInt(from::distanceTo));
    }

    public static Map<Hex, Integer> getHexCosts(ArenaStateDto state) {
        return state.map().stream().collect(Collectors.toMap(cell -> new Hex(cell.q(), cell.r()), ArenaStateDto.MapCellDto::cost, (a, b) -> a));
    }

    public static Map<Hex, HexType> getHexTypes(ArenaStateDto state) {
        return state.map().stream().collect(Collectors.toMap(
                cell -> new Hex(cell.q(), cell.r()),
                cell -> HexType.fromApiId(cell.type()),
                (a, b) -> a
        ));
    }
}
