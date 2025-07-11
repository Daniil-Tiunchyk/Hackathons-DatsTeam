package com.example.service;

import com.example.domain.Hex;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class PoorStrategyService {

    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        // Список занятых целей с едой, чтобы несколько рабочих не шли за одним и тем же ресурсом
        List<Hex> assignedFoodTargets = new ArrayList<>();

        for (ArenaStateDto.AntDto ant : state.ants()) {
            Hex antHex = new Hex(ant.q(), ant.r());
            UnitType unitType = UnitType.fromApiId(ant.type()); // Получаем тип юнита
            int speed = unitType.getSpeed();

            Optional<Hex> targetHex = Optional.empty();
            List<Hex> path = new ArrayList<>();

            // Используем новую логику для принятия решения о возврате еды домой
            if (shouldReturnFood(ant, unitType)) {
                // Приоритет 1: Если муравей несет еду и должен ее сдать, возвращаемся домой
                targetHex = findClosestHomeHex(antHex, state.home());
                path = antHex.lineTo(targetHex.orElse(null));
            } else { // Если не возвращаем еду домой, решаем другие действия
                // Приоритет 2: Для Бойцов - ищем и атакуем ближайшего врага
                if (unitType == UnitType.FIGHTER) {
                    Optional<ArenaStateDto.EnemyDto> closestEnemy = findClosestEnemy(antHex, state.enemies());
                    if (closestEnemy.isPresent()) {
                        Hex enemyHex = new Hex(closestEnemy.get().q(), closestEnemy.get().r());
                        // Двигаемся в радиус атаки (смежный с врагом)
                        targetHex = findAdjacentSafeHex(antHex, enemyHex, state.map(), state.ants(), state.enemies(), state.home());
                        path = targetHex.map(antHex::lineTo).orElse(new ArrayList<>());
                    }
                }

                // Приоритет 3: Для Рабочих - ищем еду, если нет других немедленных задач И если не несем еду
                if (!targetHex.isPresent() && unitType == UnitType.WORKER && !isCarryingAnyFood(ant)) {
                    targetHex = findClosestAvailableFood(antHex, state.food(), assignedFoodTargets);
                    path = antHex.lineTo(targetHex.orElse(null));
                }

                // Приоритет 4: Для Разведчиков - исследуем или просто двигаемся безопасно
                if (!targetHex.isPresent() && unitType == UnitType.SCOUT) {
                    // Простая логика исследования: двигаемся к безопасной смежной клетке
                    // (для полноценного исследования требуется отслеживание "карты знаний")
                    targetHex = findUnexploredSafeAdjacentHex(antHex, state.map(), state.ants(), state.enemies(), state.home());
                    path = targetHex.map(antHex::lineTo).orElse(new ArrayList<>());
                }

                // Резервный вариант: Если конкретной цели нет, просто двигаемся на безопасную смежную клетку
                if (!targetHex.isPresent()) {
                    targetHex = findSafeAdjacentHex(antHex, state.map(), state.ants(), state.enemies(), state.home());
                    path = targetHex.map(antHex::lineTo).orElse(new ArrayList<>());
                }
            }


            if (targetHex.isPresent() && path.size() > 1) { // Путь должен содержать больше, чем просто стартовый гекс
                List<Hex> movePath = new ArrayList<>();
                int currentSpeedPoints = speed;
                for (int i = 1; i < path.size(); i++) {
                    Hex nextHex = path.get(i);
                    // Находим стоимость следующего гекса из данных карты
                    Optional<ArenaStateDto.MapCellDto> tileInfo = state.map().stream()
                            .filter(t -> t.q() == nextHex.q() && t.r() == nextHex.r())
                            .findFirst();

                    int tileCost = tileInfo.map(ArenaStateDto.MapCellDto::cost).orElse(1); // Стоимость по умолчанию, если не найдена
                    if (tileCost == 0) tileCost = 1; // Предотвращаем деление на ноль или бесконечный цикл, если стоимость 0
                    if (tileCost == Integer.MAX_VALUE || tileCost == 9999) tileCost = speed + 1; // Считаем фактически непроходимым

                    if (currentSpeedPoints >= tileCost) {
                        movePath.add(nextHex);
                        currentSpeedPoints -= tileCost;
                    } else {
                        break; // Не хватает очков передвижения для следующего шага
                    }
                }

                if (!movePath.isEmpty()) {
                    commands.add(new MoveCommandDto(ant.id(), movePath));

                    // Если целью была еда и муравей НЕ НЕС ЕДУ до этого момента, помечаем ее как занятую для этого хода
                    if (!isCarryingAnyFood(ant) && unitType == UnitType.WORKER && targetHex.equals(findClosestAvailableFood(antHex, state.food(), assignedFoodTargets))) {
                        assignedFoodTargets.add(targetHex.get());
                    }
                }
            }
        }
        return commands;
    }

    /**
     * Определяет, должен ли муравей возвращать еду домой, основываясь на его типе
     * и степени заполненности инвентаря.
     *
     * @param ant      Муравей.
     * @param unitType Тип муравья.
     * @return true, если муравей должен возвращать еду домой.
     */
    private boolean shouldReturnFood(ArenaStateDto.AntDto ant, UnitType unitType) {
        if (ant.food() == null || ant.food().amount() == 0) {
            return false; // Не несет еду
        }

        int currentFoodAmount = ant.food().amount();
        int cargoCapacity = unitType.getCapacity();

        if (cargoCapacity <= 0) { // Избегаем деления на ноль, если грузоподъемность равна 0
            return true; // Если по какой-то причине нет вместимости, всегда возвращаем, если что-то несем
        }

        if (unitType == UnitType.WORKER) {
            // Для Рабочего, возвращаем, если заполнен на 80% или более
            return (double) currentFoodAmount / cargoCapacity >= 0.8;
        } else if (unitType == UnitType.SCOUT || unitType == UnitType.FIGHTER) {
            // Для Разведчика или Бойца, возвращаем, только если заполнен на 100%
            return currentFoodAmount >= cargoCapacity;
        }
        return false; // По умолчанию для неизвестных типов или если условия не выполнены
    }

    /**
     * Проверяет, несет ли муравей какое-либо количество еды.
     *
     * @param ant Муравей.
     * @return true, если муравей несет еду.
     */
    private boolean isCarryingAnyFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    private Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream()
                .min(Comparator.comparingInt(from::distanceTo));
    }

    private Optional<ArenaStateDto.EnemyDto> findClosestEnemy(Hex from, List<ArenaStateDto.EnemyDto> enemies) {
        return enemies.stream()
                .min(Comparator.comparingInt(enemy -> from.distanceTo(new Hex(enemy.q(), enemy.r()))));
    }

    private Optional<Hex> findClosestAvailableFood(Hex from, List<ArenaStateDto.FoodDto> foods, List<Hex> assignedTargets) {
        // Приоритизируем еду с более высокой калорийностью
        return foods.stream()
                .filter(food -> !assignedTargets.contains(new Hex(food.q(), food.r())))
                .sorted(Comparator.comparingInt(ArenaStateDto.FoodDto::type).reversed()) // Предполагаем, что больший type = большая калорийность
                .map(food -> new Hex(food.q(), food.r())) // Преобразуем FoodDto в Hex здесь
                .min(Comparator.comparingInt(from::distanceTo));
    }

    /**
     * Находит безопасный смежный гекс для общего передвижения.
     * Исключает непроходимые, занятые или опасные (кислота) гексы.
     */
    private Optional<Hex> findSafeAdjacentHex(Hex currentHex, List<ArenaStateDto.MapCellDto> mapTiles,
                                              List<ArenaStateDto.AntDto> friendlyAnts,
                                              List<ArenaStateDto.EnemyDto> enemies,
                                              List<Hex> homeHexes) {
        List<Hex> neighbors = currentHex.getNeighbors();

        return neighbors.stream()
                .filter(neighbor -> {
                    Optional<ArenaStateDto.MapCellDto> tileInfo = mapTiles.stream()
                            .filter(t -> t.q() == neighbor.q() && t.r() == neighbor.r())
                            .findFirst();

                    // Неизвестный гекс или непроходимый
                    if (tileInfo.isEmpty()) return false;
                    int tileType = tileInfo.get().type();
                    int tileCost = tileInfo.get().cost();

                    // Избегаем камней (тип 5) и очень дорогих/непроходимых гексов
                    if (tileType == 5 || tileCost == 0 || tileCost == Integer.MAX_VALUE || tileCost == 9999) return false;

                    // Избегаем кислоты (тип 4)
                    if (tileType == 4) return false;

                    // Избегаем занятых гексов
                    boolean occupiedByFriendly = friendlyAnts.stream()
                            .anyMatch(ant -> ant.q() == neighbor.q() && ant.r() == neighbor.r());
                    if (occupiedByFriendly) return false;

                    boolean occupiedByEnemy = enemies.stream()
                            .anyMatch(enemy -> enemy.q() == neighbor.q() && enemy.r() == neighbor.r());
                    if (occupiedByEnemy) return false;

                    return true;
                })
                // ИСПРАВЛЕНО: Выбираем гекс, который находится дальше от своей базы.
                // Для каждого соседа находим расстояние до ближайшего гекса своей базы,
                // и затем выбираем соседа, для которого это расстояние максимально.
                .max(Comparator.comparingInt(neighbor ->
                        findClosestHomeHex(neighbor, homeHexes) // Находим ближайший гекс базы к текущему соседу
                                .map(homeHex -> neighbor.distanceTo(homeHex)) // Вычисляем расстояние от этого соседа до найденного гекса базы
                                .orElse(0) // Если по какой-то причине домашний гекс не найден, считаем расстояние 0
                ));
    }

    /**
     * Находит безопасный смежный гекс для атаки (смежный с целью).
     */
    private Optional<Hex> findAdjacentSafeHex(Hex antHex, Hex targetHex, List<ArenaStateDto.MapCellDto> mapTiles,
                                              List<ArenaStateDto.AntDto> friendlyAnts,
                                              List<ArenaStateDto.EnemyDto> enemies, List<Hex> homeHexes) {
        List<Hex> neighborsOfTarget = targetHex.getNeighbors(); // Соседи врага

        return neighborsOfTarget.stream()
                .filter(neighbor -> {
                    Optional<ArenaStateDto.MapCellDto> tileInfo = mapTiles.stream()
                            .filter(t -> t.q() == neighbor.q() && t.r() == neighbor.r())
                            .findFirst();

                    if (tileInfo.isEmpty()) return false;
                    int tileType = tileInfo.get().type();
                    int tileCost = tileInfo.get().cost();

                    // Избегаем камней и очень дорогих/непроходимых гексов
                    if (tileType == 5 || tileCost == 0 || tileCost == Integer.MAX_VALUE || tileCost == 9999) return false;

                    // В случае атаки, возможно, допустимо заходить на кислоту, но пока избегаем
                    if (tileType == 4) return false;

                    // Избегаем занятых гексов дружественными юнитами того же типа или любыми вражескими
                    boolean occupiedByFriendlySameType = friendlyAnts.stream()
                            .anyMatch(ant -> ant.q() == neighbor.q() && ant.r() == neighbor.r() && ant.type() == UnitType.fromApiId(ant.type()).getApiId());
                    if (occupiedByFriendlySameType) return false;

                    boolean occupiedByEnemy = enemies.stream()
                            .anyMatch(enemy -> enemy.q() == neighbor.q() && enemy.r() == neighbor.r());
                    if (occupiedByEnemy) return false;

                    return true;
                })
                .min(Comparator.comparingInt(antHex::distanceTo)); // Выбираем ближайший безопасный гекс к муравью
    }

    /**
     * Очень базовая логика для поиска цели исследования: просто найти безопасный смежный гекс.
     * Для полноценного исследования требуется отслеживание ранее посещенных и неисследованных областей.
     */
    private Optional<Hex> findUnexploredSafeAdjacentHex(Hex currentHex, List<ArenaStateDto.MapCellDto> mapTiles,
                                                        List<ArenaStateDto.AntDto> friendlyAnts,
                                                        List<ArenaStateDto.EnemyDto> enemies,
                                                        List<Hex> homeHexes) {
        // Сейчас просто используем логику поиска безопасного смежного гекса.
        // В реальной игре здесь нужна логика отслеживания видимости карты.
        return findSafeAdjacentHex(currentHex, mapTiles, friendlyAnts, enemies, homeHexes);
    }

}
