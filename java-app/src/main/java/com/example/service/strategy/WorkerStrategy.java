package com.example.service.strategy;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.ResourceType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyHelper;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализует {@link AntStrategy} для юнитов-рабочих.
 * <p>
 * Применяет интеллектуальную, иерархическую логику для каждого рабочего.
 * Глобальные правила (как освобождение улья) применяются в StrategyService.
 * <ol>
 *     <li><b>Управление Грузом:</b> Если несет >70% груза, возвращается. Если <70%, ищет еще ресурсы ТОГО ЖЕ ТИПА.</li>
 *     <li><b>Приоритетный Сбор:</b> Ищет наиболее "ценный" ресурс вне опасных зон и направляется к нему.</li>
 *     <li><b>Исследование:</b> Если задач нет, отправляется в "сумеречную зону" для поиска новых ресурсов.</li>
 * </ol>
 */
public class WorkerStrategy implements AntStrategy {

    private static final double RETURN_HOME_CAPACITY_THRESHOLD = 0.7;
    private static final int ENEMY_ANTHILL_DANGER_RADIUS = 4;
    private final Pathfinder pathfinder;

    public WorkerStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> workers, ArenaStateDto state) {
        if (workers.isEmpty()) {
            return Collections.emptyList();
        }

        List<MoveCommandDto> commands = new ArrayList<>();
        Set<Hex> claimedFoodHexes = new HashSet<>();
        Set<Hex> dangerZones = getEnemyAnthillDangerZones(state);

        for (ArenaStateDto.AntDto worker : workers) {
            // Рабочий, стоящий на базе или несущий еду, управляется глобальными правилами в StrategyService
            // или своей логикой возврата, поэтому здесь мы его можем пропустить, если он подпадает под эти условия.
            // Однако, чтобы не усложнять, оставляем полную логику, т.к. StrategyService ее переопределит.
            decideActionFor(worker, state, claimedFoodHexes, dangerZones).ifPresent(command -> {
                commands.add(command);
                if (command.path() != null && !command.path().isEmpty()) {
                    Hex targetHex = command.path().get(command.path().size() - 1);
                    if (state.food().stream().anyMatch(f -> new Hex(f.q(), f.r()).equals(targetHex))) {
                        claimedFoodHexes.add(targetHex);
                    }
                }
            });
        }
        return commands;
    }

    private Optional<MoveCommandDto> decideActionFor(ArenaStateDto.AntDto worker, ArenaStateDto state, Set<Hex> claimedFood, Set<Hex> dangerZones) {
        if (StrategyHelper.isCarryingFood(worker)) {
            UnitType type = UnitType.fromApiId(worker.type());
            if ((double) worker.food().amount() / type.getCapacity() >= RETURN_HOME_CAPACITY_THRESHOLD) {
                return createReturnHomeCommand(worker, state);
            } else {
                return findMoreFoodOfType(worker, state, claimedFood, dangerZones);
            }
        }

        // Логика освобождения улья теперь глобальная, но поиск еды и исследование остаются
        return findAndGoToBestFood(worker, state, claimedFood, dangerZones)
                .or(() -> createExploreCommand(worker, state));
    }

    private Optional<MoveCommandDto> findAndGoToBestFood(ArenaStateDto.AntDto worker, ArenaStateDto state, Set<Hex> claimedFood, Set<Hex> dangerZones) {
        Hex currentPos = new Hex(worker.q(), worker.r());

        return state.food().stream()
                .filter(food -> ResourceType.fromApiId(food.type()).isCollectible())
                .filter(food -> !dangerZones.contains(new Hex(food.q(), food.r())))
                .filter(food -> !claimedFood.contains(new Hex(food.q(), food.r())))
                .max(Comparator.comparingDouble(food -> calculateFoodValue(food, currentPos)))
                .map(bestFood -> new Hex(bestFood.q(), bestFood.r()))
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
    }

    private double calculateFoodValue(ArenaStateDto.FoodDto food, Hex from) {
        int distance = from.distanceTo(new Hex(food.q(), food.r()));
        if (distance == 0) return Double.MAX_VALUE;
        return (double) ResourceType.fromApiId(food.type()).getCalories() / distance;
    }

    private Optional<MoveCommandDto> findMoreFoodOfType(ArenaStateDto.AntDto worker, ArenaStateDto state, Set<Hex> claimedFood, Set<Hex> dangerZones) {
        ResourceType currentFoodType = ResourceType.fromApiId(worker.food().type());
        Hex currentPos = new Hex(worker.q(), worker.r());

        return state.food().stream()
                .filter(food -> ResourceType.fromApiId(food.type()) == currentFoodType)
                .filter(food -> !dangerZones.contains(new Hex(food.q(), food.r())))
                .map(food -> new Hex(food.q(), food.r()))
                .filter(hex -> !claimedFood.contains(hex))
                .min(Comparator.comparingInt(currentPos::distanceTo))
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
    }

    // --- Вспомогательные и существующие методы без изменений ---
    private Set<Hex> getEnemyAnthillDangerZones(ArenaStateDto state) {
        Set<Hex> dangerZones = new HashSet<>();
        if (state.enemies() == null) return dangerZones;

        Map<Hex, HexType> mapTypes = state.map().stream()
                .collect(Collectors.toMap(c -> new Hex(c.q(), c.r()), c -> HexType.fromApiId(c.type()), (a, b) -> a));

        for (ArenaStateDto.MapCellDto cell : state.map()) {
            if (HexType.fromApiId(cell.type()) == HexType.ANTHILL) {
                if (state.enemies().stream().anyMatch(e -> e.q() == cell.q() && e.r() == cell.r())) {
                    dangerZones.addAll(getHexesInRange(new Hex(cell.q(), cell.r()), ENEMY_ANTHILL_DANGER_RADIUS));
                }
            }
        }
        return dangerZones;
    }

    private Optional<MoveCommandDto> createReturnHomeCommand(ArenaStateDto.AntDto worker, ArenaStateDto state) {
        return findClosestHomeHex(new Hex(worker.q(), worker.r()), state.home())
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), StrategyHelper.getHexTypes(state)));
    }

    private Optional<MoveCommandDto> createExploreCommand(ArenaStateDto.AntDto worker, ArenaStateDto state) {
        Set<Hex> visibleHexes = calculateCurrentlyVisibleHexes(state);
        Set<Hex> knownHexes = state.map().stream()
                .map(cell -> new Hex(cell.q(), cell.r()))
                .collect(Collectors.toSet());

        Set<Hex> potentialTargets = new HashSet<>(knownHexes);
        potentialTargets.removeAll(visibleHexes);

        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);
        potentialTargets.removeIf(hex -> Optional.ofNullable(hexTypes.get(hex)).map(HexType::isImpassable).orElse(false));

        if (potentialTargets.isEmpty()) return Optional.empty();

        Hex currentHex = new Hex(worker.q(), worker.r());

        return potentialTargets.stream()
                .min(Comparator.comparingInt(currentHex::distanceTo))
                .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, StrategyHelper.getHexCosts(state), hexTypes));
    }

    private Set<Hex> calculateCurrentlyVisibleHexes(ArenaStateDto state) {
        Set<Hex> visibleHexes = new HashSet<>();
        for (ArenaStateDto.AntDto ant : state.ants()) {
            UnitType type = UnitType.fromApiId(ant.type());
            visibleHexes.addAll(getHexesInRange(new Hex(ant.q(), ant.r()), type.getVision()));
        }
        if (state.spot() != null) {
            visibleHexes.addAll(getHexesInRange(state.spot(), 2));
        }
        return visibleHexes;
    }

    private Set<Hex> getHexesInRange(Hex center, int radius) {
        Set<Hex> results = new HashSet<>();
        for (int q = -radius; q <= radius; q++) {
            for (int r = Math.max(-radius, -q - radius); r <= Math.min(radius, -q + radius); r++) {
                results.add(center.add(new Hex(q, r)));
            }
        }
        return results;
    }

    private Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream().min(Comparator.comparingInt(from::distanceTo));
    }
}
