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
 * Управляет обороной, рейдами и патрулированием, избегая назначения одной цели нескольким юнитам за ход.
 */
public class FighterStrategy implements AntStrategy {

    private final Pathfinder pathfinder;

    // --- Константы для настройки стратегии ---
    private static final int MIN_RAID_GROUP_SIZE = 4;
    private static final int PATROL_GROUP_SIZE = 2;
    private static final int PATROL_ZONE_RADIUS = 25;
    private static final int PATROL_REACHED_DISTANCE = 3;

    private final Map<String, Hex> patrolAssignments = new HashMap<>();

    public FighterStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state) {
        if (fighters.isEmpty() || state.home().isEmpty()) {
            return Collections.emptyList();
        }

        // --- Инициализация для текущего хода ---
        cleanUpDeadAntAssignments(state);
        List<MoveCommandDto> commands = new ArrayList<>();
        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);

        // **КЛЮЧЕВОЕ ИЗМЕНЕНИЕ**: Множество для отслеживания целей, занятых на ЭТОМ ходу.
        Set<Hex> claimedHexesThisTurn = new HashSet<>();

        // --- Этап 1: Определение ролей (Оборона vs Гибкие задачи) ---
        List<ArenaStateDto.AntDto> flexibleFighters = assignDefenders(fighters, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);

        // --- Этап 2: Раздача задач "Гибким" бойцам ---
        if (!flexibleFighters.isEmpty()) {
            assignFlexibleTasks(flexibleFighters, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
        }

        return commands;
    }

    private List<ArenaStateDto.AntDto> assignDefenders(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        List<ArenaStateDto.AntDto> flexibleFighters = new ArrayList<>();
        List<Hex> defensivePerimeter = calculateDefensivePerimeter(state);

        // Заполняем "занятые" клетки на периметре, чтобы новые защитники туда не шли
        state.ants().stream()
                .filter(a -> defensivePerimeter.contains(new Hex(a.q(), a.r())))
                .forEach(a -> claimedHexesThisTurn.add(new Hex(a.q(), a.r())));

        List<Hex> vacantPerimeterHexes = defensivePerimeter.stream()
                .filter(hex -> !claimedHexesThisTurn.contains(hex))
                .collect(Collectors.toList());

        for (ArenaStateDto.AntDto fighter : fighters) {
            Hex fighterHex = new Hex(fighter.q(), fighter.r());

            if (defensivePerimeter.contains(fighterHex)) {
                // Уже в обороне, ничего не делаем
                continue;
            }

            if (fighterHex.equals(state.spot())) { // Боец на точке спавна
                if (!vacantPerimeterHexes.isEmpty()) {
                    Hex target = vacantPerimeterHexes.remove(0); // Берем и удаляем первую свободную
                    StrategyHelper.createPathCommand(fighter, target, state, pathfinder, hexCosts, hexTypes)
                            .ifPresent(cmd -> {
                                commands.add(cmd);
                                claimedHexesThisTurn.add(cmd.path().getLast());
                            });
                } else {
                    flexibleFighters.add(fighter);
                }
            } else {
                flexibleFighters.add(fighter);
            }
        }
        return flexibleFighters;
    }

    private void assignFlexibleTasks(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        List<ArenaStateDto.AntDto> availableFighters = new ArrayList<>(fighters);

        // Приоритет 1: Рейд на вражеский нектар
        if (availableFighters.size() >= MIN_RAID_GROUP_SIZE) {
            findBestRaidTarget(state).ifPresent(raidTarget -> {
                List<ArenaStateDto.AntDto> raidGroup = availableFighters.subList(0, MIN_RAID_GROUP_SIZE);
                assignGroupTasks(raidGroup, raidTarget, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
                availableFighters.removeAll(raidGroup);
            });
        }

        // Приоритет 2: Скоординированное патрулирование парами
        while (availableFighters.size() >= PATROL_GROUP_SIZE) {
            List<ArenaStateDto.AntDto> patrolGroup = availableFighters.subList(0, PATROL_GROUP_SIZE);
            Hex patrolTarget = findNewPatrolTargetForGroup(patrolGroup);
            assignGroupTasks(patrolGroup, patrolTarget, state, commands, claimedHexesThisTurn, hexCosts, hexTypes);
            availableFighters.removeAll(patrolGroup);
        }
    }

    /**
     * Назначает задачи для группы, распределяя их по соседним клеткам вокруг основной цели.
     */
    private void assignGroupTasks(List<ArenaStateDto.AntDto> group, Hex primaryTarget, ArenaStateDto state, List<MoveCommandDto> commands, Set<Hex> claimedHexesThisTurn, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        List<Hex> targetArea = new ArrayList<>();
        if (!claimedHexesThisTurn.contains(primaryTarget)) {
            targetArea.add(primaryTarget);
        }
        // Добавляем соседние свободные клетки
        findWalkableNeighbors(primaryTarget, state, claimedHexesThisTurn).forEach(targetArea::add);

        for (ArenaStateDto.AntDto member : group) {
            if (targetArea.isEmpty()) break; // Если цели закончились
            Hex target = targetArea.remove(0);

            patrolAssignments.remove(member.id()); // Сбрасываем старую цель патруля
            StrategyHelper.createPathCommand(member, target, state, pathfinder, hexCosts, hexTypes)
                    .ifPresent(cmd -> {
                        commands.add(cmd);
                        claimedHexesThisTurn.add(cmd.path().getLast());
                    });
        }
    }

    private Hex findNewPatrolTargetForGroup(List<ArenaStateDto.AntDto> group) {
        for (ArenaStateDto.AntDto member : group) {
            // Если у кого-то из группы уже была цель, используем ее
            if (patrolAssignments.containsKey(member.id())) {
                Hex oldTarget = patrolAssignments.get(member.id());
                // Проверяем, не достигнута ли она
                if (new Hex(member.q(), member.r()).distanceTo(oldTarget) > PATROL_REACHED_DISTANCE) {
                    return oldTarget;
                }
            }
        }
        // Если старых целей нет или они достигнуты, генерируем новую
        List<Hex> patrolZones = generatePatrolZones(new Hex(0, 0)); // Центр относительно
        Hex newTarget = findUnassignedPatrolZone(patrolZones);
        group.forEach(m -> patrolAssignments.put(m.id(), newTarget));
        return newTarget;
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
                .sorted(Comparator.comparingInt((Hex h) -> h.distanceTo(state.spot())).thenComparing(Hex::q).thenComparing(Hex::r))
                .collect(Collectors.toList());
    }
}