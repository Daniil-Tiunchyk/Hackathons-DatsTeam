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
 * Реализация {@link AntStrategy} для юнитов-бойцов.
 * Управляет обороной, рейдами и патрулированием с учетом приоритетных угроз.
 */
public class FighterStrategy implements AntStrategy {

    private final Pathfinder pathfinder;

    // --- Константы для настройки стратегии ---
    private static final int MIN_RAID_GROUP_SIZE = 4;
    private static final int THREAT_DETECTION_RADIUS = 3; // Радиус обнаружения угрозы у базы
    private static final int LOCAL_PURSUIT_ZONE_RADIUS = 10; // Радиус зоны контроля вокруг базы
    private static final int LOCAL_PURSUIT_ENGAGE_RADIUS = 7; // Радиус атаки для "охотников"
    private static final int PATROL_ZONE_RADIUS = 25;
    private static final int PATROL_REACHED_DISTANCE = 3;
    private static final int PATROL_GROUP_SIZE = 1;

    private final Map<String, Hex> patrolAssignments = new HashMap<>();

    public FighterStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state) {
        if (fighters.isEmpty() || state.home().isEmpty()) {
            return Collections.emptyList();
        }

        cleanUpDeadAntAssignments(state);
        List<MoveCommandDto> commands = new ArrayList<>();
        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);
        Set<Hex> claimedHexesThisTurn = new HashSet<>();

        // --- Этап 1: Управление обороной ---
        List<ArenaStateDto.AntDto> flexibleFighters = manageDefensivePerimeter(fighters, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);

        // --- Этап 2: Раздача задач "Гибким" бойцам ---
        if (!flexibleFighters.isEmpty()) {
            assignFlexibleTasks(flexibleFighters, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
        }

        return commands;
    }

    /**
     * Управляет оборонительным периметром: заделывает дыры, эвакуирует с базы и реагирует на угрозы.
     */
    private List<ArenaStateDto.AntDto> manageDefensivePerimeter(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        List<ArenaStateDto.AntDto> nonDefenders = new ArrayList<>();
        List<Hex> defensivePerimeter = calculateDefensivePerimeter(state);

        List<ArenaStateDto.AntDto> currentDefenders = fighters.stream()
                .filter(f -> defensivePerimeter.contains(new Hex(f.q(), f.r())))
                .collect(Collectors.toList());

        fighters.stream()
                .filter(f -> !currentDefenders.contains(f))
                .forEach(nonDefenders::add);

        claimedHexesThisTurn.addAll(currentDefenders.stream().map(d -> new Hex(d.q(), d.r())).collect(Collectors.toSet()));

        // Приоритет 1: Реагировать на угрозы у базы
        reinforcePerimeterUnderThreat(currentDefenders, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);

        // Приоритет 2: Эвакуировать бойцов с гексов базы и занять пустые места
        List<Hex> vacantPerimeterHexes = defensivePerimeter.stream()
                .filter(hex -> !claimedHexesThisTurn.contains(hex))
                .sorted(Comparator.comparingInt(h -> h.distanceTo(state.spot())))
                .collect(Collectors.toList());

        Iterator<ArenaStateDto.AntDto> iterator = nonDefenders.iterator();
        while(iterator.hasNext()){
            ArenaStateDto.AntDto fighter = iterator.next();
            if(state.home().contains(new Hex(fighter.q(), fighter.r()))) {
                if(!vacantPerimeterHexes.isEmpty()){
                    Hex target = vacantPerimeterHexes.remove(0);
                    createAndClaimMove(fighter, target, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
                    iterator.remove(); // Этот боец получил задачу
                }
            }
        }

        return nonDefenders;
    }

    /**
     * Если у базы есть угроза, перебрасывает самого дальнего защитника на заделку дыры.
     */
    private void reinforcePerimeterUnderThreat(List<ArenaStateDto.AntDto> defenders, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Optional<Hex> threatLocation = findClosestThreatToHome(state);
        if (threatLocation.isEmpty()) {
            return; // Угроз нет
        }
        Hex threatHex = threatLocation.get();

        // Ищем ближайшую к угрозе дыру в обороне
        findClosestVacantPerimeterHex(threatHex, state, claimedHexesThisTurn).ifPresent(holeToFill -> {
            // Ищем самого дальнего от угрозы защитника для переброски
            defenders.stream()
                    .max(Comparator.comparingInt(d -> new Hex(d.q(), d.r()).distanceTo(threatHex)))
                    .ifPresent(defenderToMove -> {
                        createAndClaimMove(defenderToMove, holeToFill, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
                        // Важно: этот защитник теперь занят и не может выполнять другие задачи.
                        // Поскольку мы не меняем список defenders, он просто получит команду и все.
                    });
        });
    }

    /**
     * Основной метод распределения задач для свободных бойцов.
     */
    private void assignFlexibleTasks(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        List<ArenaStateDto.AntDto> availableFighters = new ArrayList<>(fighters);

        // Приоритет 1: Локальная погоня для бойцов в зоне контроля
        List<ArenaStateDto.AntDto> hunters = availableFighters.stream()
                .filter(f -> new Hex(f.q(), f.r()).distanceTo(state.spot()) <= LOCAL_PURSUIT_ZONE_RADIUS)
                .collect(Collectors.toList());
        availableFighters.removeAll(hunters);
        handleLocalPursuit(hunters, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);

        // Приоритет 2: Дальний рейд на вражеский нектар
        if (availableFighters.size() >= MIN_RAID_GROUP_SIZE) {
            findBestRaidTarget(state).ifPresent(raidTarget -> {
                List<ArenaStateDto.AntDto> raidGroup = availableFighters.subList(0, MIN_RAID_GROUP_SIZE);
                assignGroupTasks(raidGroup, raidTarget, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
                raidGroup.clear();
            });
        }

        // Приоритет 3: Дальнее патрулирование
        while (availableFighters.size() >= PATROL_GROUP_SIZE) {
            List<ArenaStateDto.AntDto> patrolGroup = availableFighters.subList(0, PATROL_GROUP_SIZE);
            Hex patrolTarget = findNewPatrolTargetForGroup(patrolGroup, state);
            assignGroupTasks(patrolGroup, patrolTarget, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
            patrolGroup.clear();
        }
    }

    /**
     * Направляет "охотников" на ближайших врагов.
     */
    private void handleLocalPursuit(List<ArenaStateDto.AntDto> hunters, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Set<ArenaStateDto.EnemyDto> availableEnemies = new HashSet<>(state.enemies());
        for (ArenaStateDto.AntDto hunter : hunters) {
            Hex hunterPos = new Hex(hunter.q(), hunter.r());
            availableEnemies.stream()
                    .filter(e -> hunterPos.distanceTo(new Hex(e.q(), e.r())) <= LOCAL_PURSUIT_ENGAGE_RADIUS)
                    .min(Comparator.comparingInt(e -> hunterPos.distanceTo(new Hex(e.q(), e.r()))))
                    .ifPresent(targetEnemy -> {
                        createAndClaimMove(hunter, new Hex(targetEnemy.q(), targetEnemy.r()), state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
                        availableEnemies.remove(targetEnemy); // Этот враг уже атакован
                    });
        }
    }

    // --- Вспомогательные и общие методы ---

    private void createAndClaimMove(ArenaStateDto.AntDto ant, Hex target, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexes, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        StrategyHelper.createPathCommand(ant, target, state, pathfinder, hexCosts, hexTypes)
                .ifPresent(cmd -> {
                    if (!claimedHexes.contains(cmd.path().getLast())) {
                        commands.add(cmd);
                        claimedHexes.add(cmd.path().getLast());
                        patrolAssignments.remove(ant.id()); // Сбрасываем патруль, если дали другую команду
                    }
                });
    }

    private Optional<Hex> findClosestThreatToHome(ArenaStateDto state) {
        return state.enemies().stream()
                .filter(e -> state.home().stream().anyMatch(h -> h.distanceTo(new Hex(e.q(), e.r())) <= THREAT_DETECTION_RADIUS))
                .map(e -> new Hex(e.q(), e.r()))
                .min(Comparator.comparingInt(enemyHex ->
                        state.home().stream().mapToInt(homeHex -> homeHex.distanceTo(enemyHex)).min().orElse(Integer.MAX_VALUE)
                ));
    }

    private Optional<Hex> findClosestVacantPerimeterHex(Hex location, ArenaStateDto state, Set<Hex> claimedHexes) {
        return calculateDefensivePerimeter(state).stream()
                .filter(hex -> !claimedHexes.contains(hex))
                .min(Comparator.comparingInt(hex -> hex.distanceTo(location)));
    }

    // ... [Остальные методы, такие как assignGroupTasks, findNewPatrolTargetForGroup, calculateDefensivePerimeter и т.д., остаются без изменений из предыдущей версии] ...

    private void assignGroupTasks(List<ArenaStateDto.AntDto> group, Hex primaryTarget, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        List<Hex> targetArea = new ArrayList<>();
        if (!claimedHexesThisTurn.contains(primaryTarget)) {
            targetArea.add(primaryTarget);
        }
        targetArea.addAll(findWalkableNeighbors(primaryTarget, state, claimedHexesThisTurn));

        for (ArenaStateDto.AntDto member : group) {
            if (targetArea.isEmpty()) break;
            Hex target = targetArea.remove(0);
            createAndClaimMove(member, target, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
        }
    }

    private Hex findNewPatrolTargetForGroup(List<ArenaStateDto.AntDto> group, ArenaStateDto state) {
        for (ArenaStateDto.AntDto member : group) {
            if (patrolAssignments.containsKey(member.id())) {
                Hex oldTarget = patrolAssignments.get(member.id());
                if (new Hex(member.q(), member.r()).distanceTo(oldTarget) > PATROL_REACHED_DISTANCE) {
                    return oldTarget;
                }
            }
        }
        List<Hex> patrolZones = generatePatrolZones(state.spot());
        Hex newTarget = findUnassignedPatrolZone(patrolZones);
        group.forEach(m -> patrolAssignments.put(m.id(), newTarget));
        return newTarget;
    }

    private List<Hex> calculateDefensivePerimeter(ArenaStateDto state) {
        Set<Hex> perimeterCandidates = new HashSet<>();
        for (Hex homeHex : state.home()) {
            perimeterCandidates.addAll(homeHex.getNeighbors());
        }
        perimeterCandidates.removeAll(state.home());
        Set<Hex> impassableHexes = state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .map(cell -> new Hex(cell.q(), cell.r()))
                .collect(Collectors.toSet());
        return perimeterCandidates.stream()
                .filter(hex -> !impassableHexes.contains(hex))
                .collect(Collectors.toList());
    }

    private Hex findUnassignedPatrolZone(List<Hex> patrolZones) {
        Map<Hex, Long> assignmentsCount = patrolAssignments.values().stream().collect(Collectors.groupingBy(h -> h, Collectors.counting()));
        return patrolZones.stream()
                .min(Comparator.comparingLong(zone -> assignmentsCount.getOrDefault(zone, 0L)))
                .orElse(patrolZones.get(0));
    }

    private List<Hex> generatePatrolZones(Hex center) {
        return Hex.DIRECTIONS.stream()
                .map(direction -> center.add(direction.multiply(PATROL_ZONE_RADIUS)))
                .collect(Collectors.toList());
    }

    private void cleanUpDeadAntAssignments(ArenaStateDto state) {
        Set<String> aliveAntIds = state.ants().stream().map(ArenaStateDto.AntDto::id).collect(Collectors.toSet());
        patrolAssignments.keySet().removeIf(antId -> !aliveAntIds.contains(antId));
    }

    private Optional<Hex> findBestRaidTarget(ArenaStateDto state) {
        if (state.enemies().isEmpty()) return Optional.empty();
        return state.enemies().stream()
                .min(Comparator.comparingInt(e -> new Hex(e.q(), e.r()).distanceTo(state.spot())))
                .flatMap(enemy -> findWalkableNeighbor(new Hex(enemy.q(), enemy.r()), state, Collections.emptySet()));
    }

    private Optional<Hex> findWalkableNeighbor(Hex origin, ArenaStateDto state, Set<Hex> claimedHexes) {
        return findWalkableNeighbors(origin, state, claimedHexes).stream().findFirst();
    }

    private List<Hex> findWalkableNeighbors(Hex origin, ArenaStateDto state, Set<Hex> claimedHexes) {
        Set<Hex> obstacles = state.ants().stream().map(a -> new Hex(a.q(), a.r())).collect(Collectors.toSet());
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));
        state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .forEach(cell -> obstacles.add(new Hex(cell.q(), cell.r())));

        return origin.getNeighbors().stream()
                .filter(neighbor -> !obstacles.contains(neighbor) && !claimedHexes.contains(neighbor))
                .collect(Collectors.toList());
    }
}