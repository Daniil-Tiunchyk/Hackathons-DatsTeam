package com.example.service;

import com.example.domain.Hex;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Отвечает за принятие стратегических решений.
 * Анализирует состояние арены и генерирует список команд для юнитов.
 */
public class StrategyService {

    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        List<Hex> assignedFoodTargets = new ArrayList<>();

        for (ArenaStateDto.AntDto ant : state.ants()) {
            Hex antHex = new Hex(ant.q(), ant.r());

            Optional<Hex> targetHex;
            if (isCarryingFood(ant)) {
                targetHex = findClosestHomeHex(antHex, state.home());
            } else {
                targetHex = findClosestAvailableFood(antHex, state.food(), assignedFoodTargets);
            }

            targetHex.ifPresent(target -> {
                UnitType unitType = UnitType.fromApiId(ant.type());
                int speed = unitType.getSpeed();

                List<Hex> path = antHex.lineTo(target);
                if (path.size() > 1) { // Путь должен содержать больше, чем просто стартовый гекс
                    // Обрезаем путь до максимальной скорости
                    List<Hex>
                            movePath = path.subList(1, Math.min(path.size(), speed + 1));
                    commands.add(new MoveCommandDto(ant.id(), movePath));

                    // Если целью была еда, помечаем ее как занятую для этого хода
                    if (!isCarryingFood(ant)) {
                        assignedFoodTargets.add(target);
                    }
                }
            });
        }
        return commands;
    }

    private boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    private Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream()
                .min(Comparator.comparingInt(from::distanceTo));
    }

    private Optional<Hex> findClosestAvailableFood(Hex from, List<ArenaStateDto.FoodDto> foods, List<Hex> assignedTargets) {
        return foods.stream()
                .map(food -> new Hex(food.q(), food.r()))
                .filter(foodHex -> !assignedTargets.contains(foodHex)) // Исключаем уже занятые цели
                .min(Comparator.comparingInt(from::distanceTo));
    }
}
