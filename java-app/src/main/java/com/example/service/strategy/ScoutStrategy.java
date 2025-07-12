package com.example.service.strategy;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Реализация {@link AntStrategy} для юнитов-разведчиков.
 * <p>
 * Логика основана на строгой иерархии приоритетов:
 * 1. Выживание: При обнаружении угрозы разведчик отступает.
 * 2. Удержание позиции: Если текущая позиция оптимальна, разведчик остается на месте.
 * 3. Исследование: Поиск новой точки для обзора, максимизируя покрытие и минимизируя пересечение с другими разведчиками.
 */
public class ScoutStrategy implements AntStrategy {

    private static final int THREAT_RADIUS_BUFFER = 2;
    private static final int MIN_SCOUT_SEPARATION = 5;
    private static final int MIN_EXPLORE_DISTANCE_FROM_HOME = 8;
    private static final int TARGET_RING_RADIUS = 15;

    private final Pathfinder pathfinder;

    public ScoutStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> scouts, ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        if (scouts.isEmpty()) {
            return commands;
        }

        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);
        List<ArenaStateDto.AntDto> allScouts = state.ants().stream()
                .filter(a -> a.type() == UnitType.SCOUT.getApiId())
                .toList();

        for (ArenaStateDto.AntDto scout : scouts) {
            Hex currentHex = new Hex(scout.q(), scout.r());

            // Приоритет 1: Выживание
            Optional<MoveCommandDto> fleeCommand = createFleeCommand(scout, state, hexCosts, hexTypes);
            if (fleeCommand.isPresent()) {
                commands.add(fleeCommand.get());
                continue;
            }

            // Приоритет 2: Удержание позиции
            int distanceToHome = currentHex.distanceTo(state.spot());
            int distanceToNearestScout = getDistanceToNearestScout(currentHex, scout.id(), allScouts);

            if (distanceToHome > MIN_EXPLORE_DISTANCE_FROM_HOME && distanceToNearestScout > MIN_SCOUT_SEPARATION) {
                continue;
            }

            // Приоритет 3: Поиск новой точки обзора
            Optional<MoveCommandDto> exploreCommand = createExploreCommand(scout, allScouts, state, hexCosts, hexTypes);
            exploreCommand.ifPresent(commands::add);
        }

        return commands;
    }

    private Optional<MoveCommandDto> createFleeCommand(ArenaStateDto.AntDto scout, ArenaStateDto state, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Hex currentHex = new Hex(scout.q(), scout.r());
        return findClosestEnemy(currentHex, state.enemies())
                .flatMap(closestEnemy -> {
                    Hex enemyHex = new Hex(closestEnemy.q(), closestEnemy.r());
                    UnitType enemyType = UnitType.fromApiId(closestEnemy.type());
                    int threatDistance = enemyType.getSpeed() + THREAT_RADIUS_BUFFER;

                    if (currentHex.distanceTo(enemyHex) > threatDistance) {
                        return Optional.empty();
                    }

                    Hex fleeVector = new Hex(currentHex.q() - enemyHex.q(), currentHex.r() - enemyHex.r());
                    Hex target = currentHex.add(fleeVector);

                    return StrategyHelper.createPathCommand(scout, target, state, pathfinder, hexCosts, hexTypes);
                });
    }

    private Optional<MoveCommandDto> createExploreCommand(ArenaStateDto.AntDto scout, List<ArenaStateDto.AntDto> allScouts, ArenaStateDto state, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Hex homeSpot = state.spot();
        List<Hex> potentialTargets = generateRing(homeSpot, TARGET_RING_RADIUS);

        return potentialTargets.stream()
                .map(target -> new ScoredTarget(target, calculateScore(target, homeSpot, scout.id(), allScouts)))
                .max(Comparator.comparingDouble(ScoredTarget::score))
                .flatMap(bestTarget -> StrategyHelper.createPathCommand(scout, bestTarget.hex(), state, pathfinder, hexCosts, hexTypes));
    }

    private double calculateScore(Hex target, Hex homeSpot, String currentScoutId, List<ArenaStateDto.AntDto> allScouts) {
        int distanceToHome = target.distanceTo(homeSpot);
        int distanceToNearestScout = getDistanceToNearestScout(target, currentScoutId, allScouts);
        return distanceToHome + 2.0 * distanceToNearestScout;
    }

    private int getDistanceToNearestScout(Hex from, String selfId, List<ArenaStateDto.AntDto> allScouts) {
        return allScouts.stream()
                .filter(scout -> !scout.id().equals(selfId))
                .mapToInt(scout -> from.distanceTo(new Hex(scout.q(), scout.r())))
                .min()
                .orElse(Integer.MAX_VALUE);
    }

    private Optional<ArenaStateDto.EnemyDto> findClosestEnemy(Hex from, List<ArenaStateDto.EnemyDto> enemies) {
        return enemies.stream()
                .min(Comparator.comparingInt(enemy -> from.distanceTo(new Hex(enemy.q(), enemy.r()))));
    }

    private List<Hex> generateRing(Hex center, int radius) {
        if (radius <= 0) {
            return List.of(center);
        }
        Hex startHex = center.add(new Hex(radius, -radius));
        return Stream.iterate(0, i -> i < 6, i -> i + 1)
                .flatMap(i -> {
                    Hex direction = new Hex(0, 0).getNeighbors().get(i);
                    return Stream.iterate(startHex, h -> h.add(direction)).limit(radius);
                })
                .toList();
    }

    private record ScoredTarget(Hex hex, double score) {
    }
}
