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
 * Реализует {@link AntStrategy} для юнитов-разведчиков.
 * Эта стратегия является stateful и полностью автономной.
 * <p>
 * <b>Архитектурная концепция:</b>
 * Стратегия реализует интеллектуальное, адаптивное позиционирование для создания
 * эффективного и равномерного разведывательного периметра. Она учитывает ландшафт,
 * динамические препятствия и предотвращает внутренние конфликты между юнитами.
 * <p>
 * <b>Последовательность принятия решений для каждого юнита:</b>
 * <ol>
 *     <li><b>Очистка состояния:</b> В начале хода удаляются данные о погибших или застрявших юнитах.</li>
 *     <li><b>Экстренный выход из улья:</b> Если юнит находится в зоне улья, ему отдается
 *     наивысший приоритет на выход. Эта команда игнорирует других дружественных юнитов как препятствия,
 *     чтобы гарантированно освободить спавн-поинт.</li>
 *     <li><b>Назначение цели (Двухэтапный процесс):</b>
 *         <ol>
 *             <li>Вычисляется <i>идеальная</i>, математически равноудаленная точка на патрульном кольце.</li>
 *             <li>С помощью поиска в ширину (BFS) находится <i>реальная</i>, ближайшая к идеальной,
 *             физически доступная точка на карте.</li>
 *         </ol>
 *     </li>
 *     <li><b>Движение к цели:</b> Строится путь к назначенной реальной цели с учетом всех штатных препятствий.</li>
 * </ol>
 */
public class ScoutStrategy implements AntStrategy {

    private static final int PATROL_RADIUS = 70;
    private static final int CLEARANCE_RADIUS = 2;
    private static final int MAX_SEARCH_DEPTH_FOR_AVAILABLE_HEX = 5;

    private final Pathfinder pathfinder;
    private final Map<String, Hex> scoutPatrolAssignments = new HashMap<>();
    private final Map<String, Hex> previousScoutPositions = new HashMap<>();


    public ScoutStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> allScouts, ArenaStateDto state) {
        if (allScouts.isEmpty() || state.spot() == null) {
            return Collections.emptyList();
        }

        cleanUpState(allScouts);

        List<ArenaStateDto.AntDto> sortedScouts = allScouts.stream()
                .sorted(Comparator.comparing(ArenaStateDto.AntDto::id))
                .toList();

        Set<Hex> clearanceZone = getClearanceZone(state.home());
        List<Hex> idealPerimeter = generateRing(state.spot(), PATROL_RADIUS);

        List<MoveCommandDto> commands = new ArrayList<>();
        Set<Hex> assignedTargetsThisTurn = new HashSet<>();

        for (int i = 0; i < sortedScouts.size(); i++) {
            ArenaStateDto.AntDto scout = sortedScouts.get(i);
            Hex currentPos = new Hex(scout.q(), scout.r());

            if (isStuck(scout, currentPos)) {
                scoutPatrolAssignments.remove(scout.id());
            }

            if (clearanceZone.contains(currentPos)) {
                createMoveAsideCommand(scout, state, assignedTargetsThisTurn).ifPresent(command -> {
                    commands.add(command);
                    assignedTargetsThisTurn.add(command.path().get(command.path().size() - 1));
                });
                continue;
            }

            if (!scoutPatrolAssignments.containsKey(scout.id())) {
                Set<Hex> allCurrentObstacles = buildComprehensiveObstacleSet(state, assignedTargetsThisTurn);
                int targetIndex = (i * idealPerimeter.size()) / sortedScouts.size();
                Hex idealTarget = idealPerimeter.get(targetIndex);

                findClosestAvailableHex(idealTarget, allCurrentObstacles)
                        .ifPresent(realTarget -> scoutPatrolAssignments.put(scout.id(), realTarget));
            }

            Hex target = scoutPatrolAssignments.get(scout.id());
            if (target != null && !target.equals(currentPos)) {
                Set<Hex> pathfindingObstacles = buildComprehensiveObstacleSet(state, assignedTargetsThisTurn);
                pathfindingObstacles.remove(target);

                StrategyHelper.createPathCommand(scout, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state))
                        .ifPresent(command -> {
                            commands.add(command);
                            assignedTargetsThisTurn.add(command.path().get(command.path().size() - 1));
                        });
            }
        }

        updatePreviousPositions(allScouts);
        return commands;
    }

    private Optional<Hex> findClosestAvailableHex(Hex idealTarget, Set<Hex> obstacles) {
        if (!obstacles.contains(idealTarget)) {
            return Optional.of(idealTarget);
        }

        Queue<Hex> queue = new LinkedList<>();
        queue.add(idealTarget);
        Set<Hex> visited = new HashSet<>();
        visited.add(idealTarget);
        int depth = 0;

        while (!queue.isEmpty() && depth < MAX_SEARCH_DEPTH_FOR_AVAILABLE_HEX) {
            int levelSize = queue.size();
            for (int i = 0; i < levelSize; i++) {
                Hex current = queue.poll();
                for (Hex neighbor : current.getNeighbors()) {
                    if (!visited.contains(neighbor)) {
                        if (!obstacles.contains(neighbor)) {
                            return Optional.of(neighbor);
                        }
                        visited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
            depth++;
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

    /**
     * Собирает специальный, "облегченный" набор препятствий для экстренного выхода из улья.
     * Этот набор намеренно ИГНОРИРУЕТ других дружественных юнитов, чтобы гарантировать
     * возможность хода, даже если все соседи заняты.
     */
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
        // Используем специальный набор препятствий, который игнорирует других муравьев.
        Set<Hex> obstacles = buildEscapeObstacleSet(state);
        // Также нужно избегать клеток, куда уже направлены другие разведчики в этом ходу.
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
    }

    private void updatePreviousPositions(List<ArenaStateDto.AntDto> allScouts) {
        previousScoutPositions.clear();
        allScouts.forEach(scout -> previousScoutPositions.put(scout.id(), new Hex(scout.q(), scout.r())));
    }
}
