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

/**
 * Реализация {@link AntStrategy} для юнитов-разведчиков.
 * <p>
 * Стратегия детерминированно распределяет всех разведчиков по равномерному
 * гексагональному кольцу вокруг центра базы. Это обеспечивает стабильный
 * и максимально эффективный периметр обзора.
 * <p>
 * Логика основана на строгой иерархии приоритетов:
 * 1. Выживание: При угрозе разведчик отступает.
 * 2. Движение к посту и удержание: Каждый разведчик движется к своей, персонально
 * назначенной точке на периметре и удерживает ее.
 */
public class ScoutStrategy implements AntStrategy {

    private static final int THREAT_RADIUS_BUFFER = 2;
    private static final int TARGET_RADIUS = 5;
    private static final int HOLD_POSITION_THRESHOLD = 2;

    private final Pathfinder pathfinder;

    public ScoutStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> allScouts, ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        if (allScouts.isEmpty()) {
            return commands;
        }

        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);

        List<ArenaStateDto.AntDto> sortedScouts = allScouts.stream()
                .sorted(Comparator.comparing(ArenaStateDto.AntDto::id))
                .toList();

        Hex baseCenter = calculateCenterOfHome(state.home());
        List<Hex> perimeterPoints = generateRing(baseCenter, TARGET_RADIUS);
        if (perimeterPoints.isEmpty()) {
            return commands;
        }

        for (ArenaStateDto.AntDto scout : sortedScouts) {
            Hex currentHex = new Hex(scout.q(), scout.r());

            Optional<MoveCommandDto> fleeCommand = createFleeCommand(scout, state, hexCosts, hexTypes);
            if (fleeCommand.isPresent()) {
                commands.add(fleeCommand.get());
                continue;
            }

            int scoutIndex = findScoutIndex(scout, sortedScouts);
            if (scoutIndex == -1) continue;

            int targetPointIndex = (scoutIndex * perimeterPoints.size()) / sortedScouts.size();
            Hex idealTarget = perimeterPoints.get(targetPointIndex);

            if (currentHex.distanceTo(idealTarget) <= HOLD_POSITION_THRESHOLD) {
                continue;
            }

            StrategyHelper.createPathCommand(scout, idealTarget, state, pathfinder, hexCosts, hexTypes)
                    .ifPresent(commands::add);
        }
        return commands;
    }

    private int findScoutIndex(ArenaStateDto.AntDto scout, List<ArenaStateDto.AntDto> sortedScouts) {
        for (int i = 0; i < sortedScouts.size(); i++) {
            if (sortedScouts.get(i).id().equals(scout.id())) {
                return i;
            }
        }
        return -1;
    }

    private Hex calculateCenterOfHome(List<Hex> homeHexes) {
        if (homeHexes == null || homeHexes.isEmpty()) {
            return new Hex(0, 0);
        }
        double qSum = homeHexes.stream().mapToDouble(Hex::q).sum();
        double rSum = homeHexes.stream().mapToDouble(Hex::r).sum();
        return roundToHex(qSum / homeHexes.size(), rSum / homeHexes.size());
    }

    private Hex roundToHex(double q, double r) {
        double s = -q - r;
        long roundQ = Math.round(q);
        long roundR = Math.round(r);
        long roundS = Math.round(s);

        double qDiff = Math.abs(roundQ - q);
        double rDiff = Math.abs(roundR - r);
        double sDiff = Math.abs(roundS - s);

        if (qDiff > rDiff && qDiff > sDiff) {
            roundQ = -roundR - roundS;
        } else if (rDiff > sDiff) {
            roundR = -roundQ - roundS;
        }
        return new Hex((int) roundQ, (int) roundR);
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
                    Hex fleeTarget = currentHex.add(new Hex(fleeVector.q() * threatDistance, fleeVector.r() * threatDistance));

                    return StrategyHelper.createPathCommand(scout, fleeTarget, state, pathfinder, hexCosts, hexTypes);
                });
    }

    private Optional<ArenaStateDto.EnemyDto> findClosestEnemy(Hex from, List<ArenaStateDto.EnemyDto> enemies) {
        return enemies.stream()
                .min(Comparator.comparingInt(enemy -> from.distanceTo(new Hex(enemy.q(), enemy.r()))));
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
