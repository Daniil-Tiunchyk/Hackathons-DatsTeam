package com.example.service;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Интеллектуальное ядро клиента, отвечающее за принятие стратегических решений.
 * <p>
 * Сервис функционирует как иерархическая машина состояний, где решения принимаются
 * на основе строгого набора приоритетов.
 */
public class StrategyService {

    private final Pathfinder pathfinder;

    public StrategyService(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    /**
     * Главный метод, оркестрирующий принятие решений для всех юнитов в текущем ходе.
     * <p>
     * Логика построена на принципе последовательных приоритетных задач:
     * <ol>
     *     <li>Юниты, несущие ресурсы, получают приказ вернуться на базу.</li>
     *     <li>Свободные юниты отправляются за ближайшими доступными ресурсами.</li>
     *     <li>Юниты, блокирующие точку появления, отходят в сторону.</li>
     * </ol>
     * Этот механизм гарантирует, что наиболее важные действия выполняются в первую очередь.
     *
     * @param state Актуальное состояние арены, полученное от сервера.
     * @return Список {@link MoveCommandDto}, готовых к отправке на сервер.
     */
    public List<MoveCommandDto> createMoveCommands(ArenaStateDto state) {
        List<MoveCommandDto> commands = new ArrayList<>();
        Set<String> assignedAnts = new HashSet<>();
        Set<Hex> assignedFoodTargets = new HashSet<>();

        Map<Hex, Integer> hexCosts = state.map().stream()
                .collect(Collectors.toMap(cell -> new Hex(cell.q(), cell.r()), ArenaStateDto.MapCellDto::cost));

        Map<Hex, HexType> hexTypes = state.map().stream()
                .collect(Collectors.toMap(
                        cell -> new Hex(cell.q(), cell.r()),
                        cell -> HexType.fromApiId(cell.type()),
                        (existing, replacement) -> existing // В случае дубликатов
                ));

        // 1. Приоритет: муравьи с едой должны вернуться домой
        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (isCarryingFood(ant)) {
                tryToReturnHome(ant, state, hexCosts, hexTypes).ifPresent(command -> {
                    commands.add(command);
                    assignedAnts.add(ant.id());
                });
            }
        }

        // 2. Свободные муравьи идут за ближайшей свободной едой
        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (assignedAnts.contains(ant.id())) continue;

            tryToCollectFood(ant, state, hexCosts, hexTypes, assignedFoodTargets).ifPresent(command -> {
                commands.add(command);
                assignedAnts.add(ant.id());
                // Резервируем цель, чтобы другие муравьи за ней не пошли
                command.path().stream().reduce((first, second) -> second).ifPresent(assignedFoodTargets::add);
            });
        }

        // 3. Все остальные, кто стоит на основном гексе муравейника, отходят в сторону
        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (assignedAnts.contains(ant.id())) continue;

            Hex antHex = new Hex(ant.q(), ant.r());
            if (antHex.equals(state.spot())) {
                tryToMoveAside(ant, state, hexCosts).ifPresent(command -> {
                    commands.add(command);
                    assignedAnts.add(ant.id());
                });
            }
        }

        return commands;
    }

    /**
     * Формирует команду для юнита, несущего ресурсы, чтобы он вернулся на ближайший гекс муравейника.
     *
     * @param ant      Юнит, для которого формируется команда.
     * @param state    Текущее состояние арены.
     * @param hexCosts Карта стоимостей передвижения по гексам.
     * @param hexTypes Карта типов гексов.
     * @return {@link Optional} с командой на передвижение, если путь найден и безопасен, иначе пустой.
     */
    private Optional<MoveCommandDto> tryToReturnHome(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Hex antHex = new Hex(ant.q(), ant.r());
        return findClosestHomeHex(antHex, state.home())
                .flatMap(target -> createPathCommand(ant, target, state, hexCosts, hexTypes));
    }

    /**
     * Формирует команду для свободного юнита, чтобы он отправился за ближайшим доступным ресурсом.
     *
     * @param ant             Юнит, для которого формируется команда.
     * @param state           Текущее состояние арены.
     * @param hexCosts        Карта стоимостей передвижения по гексам.
     * @param hexTypes        Карта типов гексов.
     * @param assignedTargets Набор ресурсов, к которым уже направляются другие юниты.
     * @return {@link Optional} с командой на передвижение, если цель найдена и путь к ней безопасен, иначе пустой.
     */
    private Optional<MoveCommandDto> tryToCollectFood(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes, Set<Hex> assignedTargets) {
        Hex antHex = new Hex(ant.q(), ant.r());
        return findClosestAvailableFood(antHex, state.food(), assignedTargets)
                .flatMap(target -> createPathCommand(ant, target, state, hexCosts, hexTypes));
    }

    /**
     * Формирует команду для юнита, стоящего на основном гексе муравейника, чтобы он отошел в сторону.
     * Это необходимо для освобождения точки появления новых юнитов.
     *
     * @param ant      Юнит, блокирующий точку появления.
     * @param state    Текущее состояние арены.
     * @param hexCosts Карта стоимостей передвижения.
     * @return {@link Optional} с командой на один шаг в сторону, если это возможно.
     */
    private Optional<MoveCommandDto> tryToMoveAside(ArenaStateDto.AntDto ant, ArenaStateDto state, Map<Hex, Integer> hexCosts) {
        Hex antHex = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> neighbors = new ArrayList<>(antHex.getNeighbors());
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

    /**
     * Центральный метод-помощник, инкапсулирующий полную логику создания валидной команды.
     * <p>
     * Процесс включает:
     * 1. Поиск полного пути с помощью {@link Pathfinder}.
     * 2. Обрезку пути в соответствии с очками передвижения юнита.
     * 3. Валидацию безопасности конечной точки маршрута.
     *
     * @param ant      Юнит, которому отдается приказ.
     * @param target   Целевой гекс.
     * @param state    Текущее состояние арены.
     * @param hexCosts Карта стоимостей передвижения.
     * @param hexTypes Карта типов гексов.
     * @return {@link Optional} с готовой и проверенной командой, либо пустой, если путь невозможен или небезопасен.
     */
    private Optional<MoveCommandDto> createPathCommand(ArenaStateDto.AntDto ant, Hex target, ArenaStateDto state, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Hex start = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> path = pathfinder.findPath(start, target, hexCosts, obstacles);
        if (path.isEmpty()) return Optional.empty();

        UnitType unitType = UnitType.fromApiId(ant.type());
        List<Hex> truncatedPath = truncatePathByMovementPoints(path, unitType.getSpeed(), hexCosts);
        if (truncatedPath.isEmpty()) return Optional.empty();

        // Валидация конечной точки пути.
        Hex finalDestination = truncatedPath.getLast();
        if (isUnsafeFinalDestination(finalDestination, ant, hexTypes)) {
            return Optional.empty(); // Путь ведет в опасное место, отменяем команду.
        }

        return Optional.of(new MoveCommandDto(ant.id(), truncatedPath));
    }

    /**
     * Формирует динамический набор препятствий для конкретного юнита.
     * Эти препятствия используются алгоритмом A* для построения маршрута.
     * <p>
     * Включает в себя: вражеских юнитов, дружественных юнитов того же типа
     * и непроходимые гексы (например, камень).
     *
     * @param ant   Юнит, для которого определяются препятствия.
     * @param state Текущее состояние арены.
     * @return {@link Set} гексов, которые следует считать непроходимыми.
     */
    private Set<Hex> getObstaclesFor(ArenaStateDto.AntDto ant, ArenaStateDto state) {
        Set<Hex> obstacles = new HashSet<>();
        // Враги
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));
        // Дружественные юниты того же типа
        state.ants().stream()
                .filter(other -> other.type() == ant.type() && !other.id().equals(ant.id()))
                .forEach(other -> obstacles.add(new Hex(other.q(), other.r())));
        // Непроходимые гексы (камни)
        state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .forEach(cell -> obstacles.add(new Hex(cell.q(), cell.r())));

        return obstacles;
    }

    /**
     * Производит валидацию конечной точки маршрута на предмет опасности.
     * В соответствии с правилами, опасность представляет только остановка на гексе с кислотой,
     * если у юнита недостаточно здоровья, чтобы пережить урон.
     *
     * @param destination Конечный гекс запланированного маршрута.
     * @param ant         Юнит, выполняющий движение.
     * @param hexTypes    Карта типов гексов.
     * @return {@code true}, если остановка на данном гексе приведет к гибели юнита, иначе {@code false}.
     */
    private boolean isUnsafeFinalDestination(Hex destination, ArenaStateDto.AntDto ant, Map<Hex, HexType> hexTypes) {
        HexType destinationType = hexTypes.get(destination);

        // Считаем кислоту препятствием, если она убьет юнита
        if (destinationType == HexType.ACID) {
            return ant.health() <= destinationType.getDamage();
        }

        return false;
    }

    /**
     * Обрезает полный путь до той части, которую юнит может пройти за один ход,
     * основываясь на его очках передвижения и стоимости гексов.
     *
     * @param path      Полный, идеальный путь от точки А до Б.
     * @param maxPoints Максимальное количество очков передвижения юнита.
     * @param hexCosts  Карта стоимостей передвижения.
     * @return Список гексов, представляющий достижимый за один ход сегмент пути.
     */
    private List<Hex> truncatePathByMovementPoints(List<Hex> path, int maxPoints, Map<Hex, Integer> hexCosts) {
        List<Hex> resultPath = new ArrayList<>();
        int pointsSpent = 0;
        for (Hex step : path) {
            int cost = hexCosts.getOrDefault(step, 1);
            if (pointsSpent + cost <= maxPoints) {
                pointsSpent += cost;
                resultPath.add(step);
            } else {
                break;
            }
        }
        return resultPath;
    }

    /**
     * Проверяет, несет ли юнит какой-либо ресурс.
     *
     * @param ant Юнит для проверки.
     * @return {@code true}, если юнит несет ресурс, иначе {@code false}.
     */
    private boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    /**
     * Находит ближайший к юниту гекс из набора гексов муравейника.
     *
     * @param from      Позиция юнита.
     * @param homeHexes Список гексов муравейника.
     * @return {@link Optional} с ближайшим гексом.
     */
    private Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream().min(Comparator.comparingInt(from::distanceTo));
    }

    /**
     * Находит ближайший к юниту доступный ресурс.
     *
     * @param from            Позиция юнита.
     * @param foods           Список всех видимых ресурсов.
     * @param assignedTargets Набор ресурсов, к которым уже направлены другие юниты.
     * @return {@link Optional} с позицией ближайшего свободного ресурса.
     */
    private Optional<Hex> findClosestAvailableFood(Hex from, List<ArenaStateDto.FoodDto> foods, Set<Hex> assignedTargets) {
        return foods.stream()
                .map(food -> new Hex(food.q(), food.r()))
                .filter(foodHex -> !assignedTargets.contains(foodHex))
                .min(Comparator.comparingInt(from::distanceTo));
    }
}
