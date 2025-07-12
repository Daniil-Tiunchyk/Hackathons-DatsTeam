package com.example.service;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.strategy.AntStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис-диспетчер, который оркеструет применение различных стратегий поведения
 * для юнитов в зависимости от их роли и текущей игровой ситуации.
 * <p>
 * Этот класс является "Контекстом" в терминах паттерна "Стратегия". Он не реализует
 * саму логику поведения, а делегирует ее соответствующим объектам-стратегиям,
 * которые получает от {@link StrategyProvider}.
 */
public class StrategyService {

    private final StrategyProvider strategyProvider;
    private final Pathfinder pathfinder;

    public StrategyService(StrategyProvider strategyProvider, Pathfinder pathfinder) {
        this.strategyProvider = strategyProvider;
        this.pathfinder = pathfinder;
    }

    /**
     * Главный метод, оркестрирующий принятие решений для всех юнитов в текущем ходе.
     * <p>
     * Логика построена на принципе последовательных приоритетных задач:
     * <ol>
     *     <li>Сначала обрабатываются универсальные, высокоприоритетные задачи (возврат с ресурсами, освобождение точки спавна).</li>
     *     <li>Затем оставшиеся свободные юниты группируются по типам.</li>
     *     <li>Каждая группа передается в соответствующую ей стратегию для принятия ролевых решений.</li>
     * </ol>
     *
     * @param state Актуальное состояние арены, полученное от сервера.
     * @return Агрегированный список {@link MoveCommandDto} от всех обработчиков, готовый к отправке.
     */
    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        Set<String> assignedAnts = state.ants().stream()
                .filter(ant -> handleUniversalTasks(ant, state, commands))
                .map(ArenaStateDto.AntDto::id)
                .collect(Collectors.toSet());

        Map<UnitType, List<ArenaStateDto.AntDto>> freeAntsByType = state.ants().stream()
                .filter(ant -> !assignedAnts.contains(ant.id()))
                .collect(Collectors.groupingBy(ant -> UnitType.fromApiId(ant.type())));

        freeAntsByType.forEach((type, ants) -> {
            AntStrategy strategy = strategyProvider.getStrategy(type);
            commands.addAll(strategy.decideMoves(ants, state));
        });

        return commands;
    }

    /**
     * Обрабатывает универсальные задачи для одного юнита и, в случае успеха, добавляет команду в список.
     *
     * @param ant      Юнит для проверки.
     * @param state    Текущее состояние игры.
     * @param commands Список, в который будет добавлена команда.
     * @return {@code true}, если для юнита была создана команда, иначе {@code false}.
     */
    private boolean handleUniversalTasks(ArenaStateDto.AntDto ant, ArenaStateDto state, List<MoveCommandDto> commands) {

        if (ant.type() == UnitType.FIGHTER.getApiId()) {
            return false; // Немедленно выходим, если это боец
        }

        if (StrategyHelper.isCarryingFood(ant)) {
            Optional<MoveCommandDto> command = createReturnHomeCommand(ant, state);
            command.ifPresent(commands::add);
            return command.isPresent();
        }

        if (new Hex(ant.q(), ant.r()).equals(state.spot())) {
            Optional<MoveCommandDto> command = createMoveAsideCommand(ant, state);
            command.ifPresent(commands::add);
            return command.isPresent();
        }

        return false;
    }

    /**
     * Создает команду на возвращение домой для юнита, несущего ресурсы.
     *
     * @param ant   Юнит, для которого формируется команда.
     * @param state Текущее состояние арены.
     * @return {@link Optional} с командой на передвижение, если путь найден и безопасен.
     */
    private Optional<MoveCommandDto> createReturnHomeCommand(ArenaStateDto.AntDto ant, ArenaStateDto state) {
        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);
        Hex antHex = new Hex(ant.q(), ant.r());

        return StrategyHelper.findClosestHomeHex(antHex, state.home())
                .flatMap(target -> StrategyHelper.createPathCommand(ant, target, state, pathfinder, hexCosts, hexTypes));
    }

    /**
     * Создает команду для юнита, стоящего на точке спавна, чтобы он отошел в сторону.
     *
     * @param ant   Юнит, блокирующий точку появления.
     * @param state Текущее состояние арены.
     * @return {@link Optional} с командой на один шаг в сторону, если это возможно.
     */
    private Optional<MoveCommandDto> createMoveAsideCommand(ArenaStateDto.AntDto ant, ArenaStateDto state) {
        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Set<Hex> obstacles = StrategyHelper.getObstaclesFor(ant, state);

        List<Hex> neighbors = new ArrayList<>(new Hex(ant.q(), ant.r()).getNeighbors());
        Collections.shuffle(neighbors); // Добавляем случайность, чтобы юниты не толпились в одном углу

        return neighbors.stream()
                .filter(neighbor -> !obstacles.contains(neighbor))
                .findFirst()
                .flatMap(target -> {
                    UnitType unitType = UnitType.fromApiId(ant.type());
                    int costToMove = hexCosts.getOrDefault(target, 1);
                    if (unitType.getSpeed() >= costToMove) {
                        return Optional.of(new MoveCommandDto(ant.id(), List.of(target)));
                    }
                    return Optional.empty();
                });
    }
}
