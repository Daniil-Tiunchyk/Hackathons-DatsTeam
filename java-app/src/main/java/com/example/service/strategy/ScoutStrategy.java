package com.example.service.strategy;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализует {@link AntStrategy} для юнитов-разведчиков.
 * Эта стратегия является stateful и полностью автономной.
 * <p>
 * <b>Архитектурная концепция:</b>
 * Стратегия разделяет разведчиков на две роли:
 * <ol>
 *     <li><b>Страж Базы (1 юнит):</b> Ближайший к базе разведчик назначается на постоянное
 *     патрулирование малого радиуса вокруг улья для обеспечения раннего предупреждения.</li>
 *     <li><b>Исследователи Фронтира (остальные):</b> Динамически распределяются по границам
 *     известной карты для ее расширения, используя "черные списки" для избегания тупиков.</li>
 * </ol>
 */
public class ScoutStrategy implements AntStrategy {

    private static final int HOME_PATROL_RADIUS = 5;      // Для стража
    private static final int CLEARANCE_RADIUS = 2;
    private static final int MAX_SEARCH_DEPTH_FOR_AVAILABLE_HEX = 5;

    private final Pathfinder pathfinder;
    private final Map<String, Hex> scoutPatrolAssignments = new HashMap<>();
    private final Map<String, Hex> previousScoutPositions = new HashMap<>();
    private final Map<String, Set<Hex>> scoutInvalidTargets = new HashMap<>();
    private String homeGuardScoutId = null;

    public ScoutStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> allScouts, ArenaStateDto state) {
        if (allScouts.isEmpty() || state.spot() == null) {
            return Collections.emptyList();
        }

        cleanUpState(allScouts);
        assignHomeGuard(allScouts, state);

        List<MoveCommandDto> commands = new ArrayList<>();
        Set<Hex> assignedTargetsThisTurn = new HashSet<>();

        // Разделяем на стража и исследователей
        Optional<ArenaStateDto.AntDto> guardOpt = allScouts.stream().filter(s -> s.id().equals(homeGuardScoutId)).findFirst();
        List<ArenaStateDto.AntDto> explorers = allScouts.stream().filter(s -> !s.id().equals(homeGuardScoutId)).toList();

        // 1. Логика для Стража
        guardOpt.flatMap(guard -> handleScoutLogic(guard, state, assignedTargetsThisTurn, true)).ifPresent(commands::add);

        // 2. Логика для Исследователей
        for (ArenaStateDto.AntDto explorer : explorers) {
            handleScoutLogic(explorer, state, assignedTargetsThisTurn, false).ifPresent(commands::add);
        }

        updatePreviousPositions(allScouts);
        return commands;
    }

    /**
     * Центральный обработчик логики для одного разведчика.
     *
     * @param scout       Разведчик для обработки.
     * @param isHomeGuard true, если это страж базы, false - если исследователь.
     * @return Optional с командой на движение.
     */
    private Optional<MoveCommandDto> handleScoutLogic(ArenaStateDto.AntDto scout, ArenaStateDto state, Set<Hex> assignedTargets, boolean isHomeGuard) {
        Hex currentPos = new Hex(scout.q(), scout.r());

        if (isStuck(scout, currentPos)) {
            Hex failedTarget = scoutPatrolAssignments.get(scout.id());
            if (failedTarget != null) {
                scoutInvalidTargets.computeIfAbsent(scout.id(), k -> new HashSet<>()).add(failedTarget);
            }
            scoutPatrolAssignments.remove(scout.id());
        }

        if (getClearanceZone(state.home()).contains(currentPos)) {
            return createMoveAsideCommand(scout, state, assignedTargets).map(cmd -> {
                assignedTargets.add(cmd.path().getLast());
                return cmd;
            });
        }

        if (!scoutPatrolAssignments.containsKey(scout.id())) {
            findNewTarget(scout, state, assignedTargets, isHomeGuard).ifPresent(target -> scoutPatrolAssignments.put(scout.id(), target));
        }

        Hex target = scoutPatrolAssignments.get(scout.id());
        if (target != null && !target.equals(currentPos)) {
            Set<Hex> obstacles = buildComprehensiveObstacleSet(state, assignedTargets);
            obstacles.remove(target);
            return StrategyHelper.createPathCommand(scout, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state))
                    .map(cmd -> {
                        assignedTargets.add(cmd.path().getLast());
                        return cmd;
                    });
        }
        return Optional.empty();
    }

    private Optional<Hex> findNewTarget(ArenaStateDto.AntDto scout, ArenaStateDto state, Set<Hex> assignedTargets, boolean isHomeGuard) {
        if (isHomeGuard) {
            List<Hex> patrolRing = generateRing(state.spot(), HOME_PATROL_RADIUS);
            return patrolRing.stream()
                    .filter(h -> !assignedTargets.contains(h))
                    .min(Comparator.comparingInt(h -> new Hex(scout.q(), scout.r()).distanceTo(h)));
        } else {
            return findNewExplorationTarget(scout, state, assignedTargets);
        }
    }

    private void assignHomeGuard(List<ArenaStateDto.AntDto> allScouts, ArenaStateDto state) {
        boolean guardIsAlive = homeGuardScoutId != null && allScouts.stream().anyMatch(s -> s.id().equals(homeGuardScoutId));
        if (guardIsAlive) {
            return;
        }

        allScouts.stream()
                .min(Comparator.comparingInt(s -> new Hex(s.q(), s.r()).distanceTo(state.spot())))
                .ifPresent(closestScout -> homeGuardScoutId = closestScout.id());
    }

    // --- Методы ниже были рефакторены или остались без изменений ---

    private Optional<Hex> findNewExplorationTarget(ArenaStateDto.AntDto scout, ArenaStateDto state, Set<Hex> assignedTargets) {
        Set<Hex> personalInvalidTargets = scoutInvalidTargets.getOrDefault(scout.id(), Collections.emptySet());

        List<Hex> frontier = state.knownBoundaries().stream()
                .filter(hex -> !personalInvalidTargets.contains(hex) && !scoutPatrolAssignments.containsValue(hex))
                .sorted(Comparator.comparingInt(h -> h.distanceTo(state.spot())))
                .toList();

        if (frontier.isEmpty()) return Optional.empty();

        return frontier.stream()
                .max(Comparator.comparingInt(h -> findMinDistanceToTargets(h, assignedTargets)))
                .flatMap(idealTarget -> findClosestAvailableHex(idealTarget, buildComprehensiveObstacleSet(state, assignedTargets)));
    }

    private int findMinDistanceToTargets(Hex hex, Set<Hex> targets) {
        if (targets.isEmpty()) return Integer.MAX_VALUE;
        return targets.stream().mapToInt(hex::distanceTo).min().orElse(Integer.MAX_VALUE);
    }

    private Optional<Hex> findClosestAvailableHex(Hex idealTarget, Set<Hex> obstacles) {
        if (!obstacles.contains(idealTarget)) return Optional.of(idealTarget);
        Queue<Hex> queue = new LinkedList<>(List.of(idealTarget));
        Set<Hex> visited = new HashSet<>(List.of(idealTarget));
        for (int depth = 0; depth < MAX_SEARCH_DEPTH_FOR_AVAILABLE_HEX && !queue.isEmpty(); depth++) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                for (Hex neighbor : Objects.requireNonNull(queue.poll()).getNeighbors()) {
                    if (visited.add(neighbor)) {
                        if (!obstacles.contains(neighbor)) return Optional.of(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }
        return Optional.empty();
    }

    private Set<Hex> buildComprehensiveObstacleSet(ArenaStateDto state, Set<Hex> dynamicTargets) {
        Set<Hex> obstacles = new HashSet<>();
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));
        state.ants().forEach(a -> obstacles.add(new Hex(a.q(), a.r())));
        state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .forEach(cell -> obstacles.add(new Hex(cell.q(), cell.r())));
        obstacles.addAll(dynamicTargets);
        obstacles.addAll(state.home());
        return obstacles;
    }

    private Set<Hex> buildEscapeObstacleSet(ArenaStateDto state) {
        Set<Hex> obstacles = new HashSet<>();
        state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .forEach(cell -> obstacles.add(new Hex(cell.q(), cell.r())));
        obstacles.addAll(state.home());
        return obstacles;
    }

    private Optional<MoveCommandDto> createMoveAsideCommand(ArenaStateDto.AntDto scout, ArenaStateDto state, Set<Hex> assignedTargets) {
        Hex startPos = new Hex(scout.q(), scout.r());
        Set<Hex> obstacles = buildEscapeObstacleSet(state);
        obstacles.addAll(assignedTargets);

        return startPos.getNeighbors().stream()
                .filter(n -> !obstacles.contains(n))
                .min(Comparator.comparingInt(n -> n.distanceTo(startPos)))
                .map(exitHex -> new MoveCommandDto(scout.id(), List.of(exitHex)));
    }

    private boolean isStuck(ArenaStateDto.AntDto scout, Hex currentPos) {
        return currentPos.equals(previousScoutPositions.get(scout.id()));
    }

    private List<Hex> generateRing(Hex center, int radius) {
        List<Hex> results = new ArrayList<>();
        if (radius <= 0) {
            if (center != null) results.add(center);
            return results;
        }
        Hex hex = center.add(Hex.DIRECTIONS.get(4).multiply(radius));
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < radius; j++) {
                results.add(hex);
                hex = hex.getNeighbors().get(i);
            }
        }
        return results;
    }

    private Set<Hex> getClearanceZone(List<Hex> home) {
        Set<Hex> zone = new HashSet<>(home);
        home.forEach(h -> zone.addAll(generateRing(h, CLEARANCE_RADIUS)));
        return zone;
    }

    private void cleanUpState(List<ArenaStateDto.AntDto> aliveScouts) {
        Set<String> aliveScoutIds = aliveScouts.stream()
                .map(ArenaStateDto.AntDto::id)
                .collect(Collectors.toSet());
        scoutPatrolAssignments.keySet().removeIf(id -> !aliveScoutIds.contains(id));
        previousScoutPositions.keySet().removeIf(id -> !aliveScoutIds.contains(id));
        scoutInvalidTargets.keySet().removeIf(id -> !aliveScoutIds.contains(id));
        if (homeGuardScoutId != null && !aliveScoutIds.contains(homeGuardScoutId)) {
            homeGuardScoutId = null;
        }
    }

    private void updatePreviousPositions(List<ArenaStateDto.AntDto> allScouts) {
        previousScoutPositions.clear();
        allScouts.forEach(scout -> previousScoutPositions.put(scout.id(), new Hex(scout.q(), scout.r())));
    }
}
