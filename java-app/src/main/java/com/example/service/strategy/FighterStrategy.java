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
 */
public class FighterStrategy implements AntStrategy {

    private final Pathfinder pathfinder;

    // --- Константы для настройки стратегии ---
    private static final int MIN_RAID_GROUP_SIZE = 4;
    private static final int PATROL_GROUP_SIZE = 2;
    private static final int PATROL_ZONE_RADIUS = 25; // На каком расстоянии от базы находятся зоны патрулирования
    private static final int PATROL_REACHED_DISTANCE = 3; // Расстояние, на котором цель считается достигнутой

    /**
     * Хранит назначения патрульных зон для каждого бойца, чтобы они не меняли цель каждый ход.
     * Ключ - ID муравья, Значение - Целевой гекс для патрулирования.
     */
    private final Map<String, Hex> patrolAssignments = new HashMap<>();

    public FighterStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state) {
        if (fighters.isEmpty() || state.home().isEmpty()) {
            return Collections.emptyList();
        }

        // Очищаем назначения для мертвых муравьев
        cleanUpDeadAntAssignments(state);

        List<MoveCommandDto> commands = new ArrayList<>();
        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);

        // --- Этап 1: Определение ролей (Оборона vs Гибкие задачи) ---
        List<Hex> defensivePerimeter = calculateDefensivePerimeter(state);
        Set<Hex> occupiedPerimeterHexes = state.ants().stream()
                .filter(a -> a.type() == UnitType.FIGHTER.getApiId() && defensivePerimeter.contains(new Hex(a.q(), a.r())))
                .map(a -> new Hex(a.q(), a.r()))
                .collect(Collectors.toSet());

        List<Hex> vacantPerimeterHexes = defensivePerimeter.stream()
                .filter(hex -> !occupiedPerimeterHexes.contains(hex))
                .collect(Collectors.toList());

        List<ArenaStateDto.AntDto> flexibleFighters = new ArrayList<>();

        for (ArenaStateDto.AntDto fighter : fighters) {
            Hex fighterHex = new Hex(fighter.q(), fighter.r());

            if (fighterHex.equals(state.spot())) { // Боец на точке спавна
                if (!vacantPerimeterHexes.isEmpty()) { // Есть место в обороне
                    findAndAssignBestVacantSpot(fighter, vacantPerimeterHexes, state, pathfinder, hexCosts, hexTypes)
                            .ifPresent(command -> {
                                commands.add(command);
                                vacantPerimeterHexes.remove(command.path().getLast());
                            });
                } else { // Оборона заполнена
                    flexibleFighters.add(fighter);
                }
            } else if (!defensivePerimeter.contains(fighterHex)) { // Боец вне обороны
                flexibleFighters.add(fighter);
            }
        }

        // --- Этап 2: Раздача задач "Гибким" бойцам ---
        if (!flexibleFighters.isEmpty()) {
            commands.addAll(assignFlexibleTasks(flexibleFighters, state, pathfinder, hexCosts, hexTypes));
        }

        return commands;
    }

    private List<MoveCommandDto> assignFlexibleTasks(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state, Pathfinder pathfinder, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        List<MoveCommandDto> commands = new ArrayList<>();
        List<ArenaStateDto.AntDto> availableFighters = new ArrayList<>(fighters);

        // Приоритет 1: Рейд на вражеский нектар
        if (availableFighters.size() >= MIN_RAID_GROUP_SIZE) {
            findBestRaidTarget(state).ifPresent(raidTarget -> {
                List<ArenaStateDto.AntDto> raidGroup = availableFighters.subList(0, MIN_RAID_GROUP_SIZE);
                for (ArenaStateDto.AntDto raider : raidGroup) {
                    // Если боец был в патруле, его цель сбрасывается для рейда
                    patrolAssignments.remove(raider.id());
                    StrategyHelper.createPathCommand(raider, raidTarget, state, pathfinder, hexCosts, hexTypes)
                            .ifPresent(commands::add);
                }
                availableFighters.removeAll(raidGroup);
            });
        }

        // Приоритет 2: Скоординированное патрулирование с постоянными целями
        assignAndExecutePatrolTasks(availableFighters, state, pathfinder, hexCosts, hexTypes, commands);

        return commands;
    }

    private void assignAndExecutePatrolTasks(List<ArenaStateDto.AntDto> fighters, ArenaStateDto state, Pathfinder pathfinder, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes, List<MoveCommandDto> commands) {
        List<Hex> patrolZones = generatePatrolZones(state.spot());
        int zoneIndex = 0;

        for (ArenaStateDto.AntDto fighter : fighters) {
            Hex currentPos = new Hex(fighter.q(), fighter.r());

            // Если у бойца уже есть цель, проверяем, не достиг ли он ее
            if (patrolAssignments.containsKey(fighter.id())) {
                Hex target = patrolAssignments.get(fighter.id());
                if (currentPos.distanceTo(target) <= PATROL_REACHED_DISTANCE) {
                    // Достиг, сбрасываем цель, чтобы на следующем шаге получить новую
                    patrolAssignments.remove(fighter.id());
                }
            }

            // Если у бойца нет цели, назначаем ему новую из списка зон
            if (!patrolAssignments.containsKey(fighter.id())) {
                Hex newTarget = findUnassignedPatrolZone(patrolZones);
                patrolAssignments.put(fighter.id(), newTarget);
            }

            // Отправляем бойца к его назначенной цели
            Hex assignedTarget = patrolAssignments.get(fighter.id());
            StrategyHelper.createPathCommand(fighter, assignedTarget, state, pathfinder, hexCosts, hexTypes)
                    .ifPresent(commands::add);
        }
    }

    /**
     * Находит патрульную зону, на которую еще не назначено много бойцов.
     */
    private Hex findUnassignedPatrolZone(List<Hex> patrolZones) {
        Map<Hex, Long> assignmentsCount = patrolAssignments.values().stream()
                .collect(Collectors.groupingBy(h -> h, Collectors.counting()));

        return patrolZones.stream()
                .min(Comparator.comparingLong(zone -> assignmentsCount.getOrDefault(zone, 0L)))
                .orElse(patrolZones.get(0)); // Возвращаем первую, если что-то пошло не так
    }

    /**
     * Генерирует 6 статичных зон для патрулирования в разных направлениях от базы.
     */
    private List<Hex> generatePatrolZones(Hex center) {
        // Используем 6 стандартных направлений для гексагональной сетки
        return Hex.DIRECTIONS.stream()
                .map(direction -> center.add(direction.multiply(PATROL_ZONE_RADIUS)))
                .collect(Collectors.toList());
    }

    private void cleanUpDeadAntAssignments(ArenaStateDto state) {
        Set<String> aliveAntIds = state.ants().stream().map(ArenaStateDto.AntDto::id).collect(Collectors.toSet());
        patrolAssignments.keySet().removeIf(antId -> !aliveAntIds.contains(antId));
    }

    // --- Остальные методы без изменений ---

    private Optional<Hex> findBestRaidTarget(ArenaStateDto state) {
        if (state.enemies().isEmpty()) return Optional.empty();
        return state.enemies().stream()
                .min(Comparator.comparingInt(e -> new Hex(e.q(), e.r()).distanceTo(state.spot())))
                .flatMap(enemy -> findWalkableNeighbor(new Hex(enemy.q(), enemy.r()), state));
    }

    private Optional<Hex> findWalkableNeighbor(Hex origin, ArenaStateDto state) {
        Set<Hex> obstacles = state.ants().stream().map(a -> new Hex(a.q(), a.r())).collect(Collectors.toSet());
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));
        state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .forEach(cell -> obstacles.add(new Hex(cell.q(), cell.r())));

        return origin.getNeighbors().stream()
                .filter(neighbor -> !obstacles.contains(neighbor))
                .findFirst();
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
                .sorted(Comparator.comparingInt((Hex h) -> h.distanceTo(state.spot()))
                        .thenComparing(Hex::q).thenComparing(Hex::r))
                .collect(Collectors.toList());
    }

    private Optional<MoveCommandDto> findAndAssignBestVacantSpot(ArenaStateDto.AntDto fighter, List<Hex> vacantSpots, ArenaStateDto state, Pathfinder pathfinder, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Hex fighterHex = new Hex(fighter.q(), fighter.r());
        return vacantSpots.stream()
                .min(Comparator.comparingInt(spot -> fighterHex.distanceTo(spot)))
                .flatMap(target -> StrategyHelper.createPathCommand(fighter, target, state, pathfinder, hexCosts, hexTypes));
    }
}