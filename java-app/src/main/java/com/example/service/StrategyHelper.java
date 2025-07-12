package com.example.service;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилитный класс, содержащий общие статические методы, используемые
 * различными стратегиями и сервисами.
 * <p>
 * Вынесение этих функций в отдельный класс позволяет избежать дублирования
 * кода и упрощает логику в основных классах, следуя принципу DRY (Don't Repeat Yourself).
 */
public final class StrategyHelper {

    private StrategyHelper() {
        // Предотвращаем инстанцирование утилитного класса
    }

    /**
     * Создает команду на передвижение, инкапсулируя полный цикл: поиск, обрезку и валидацию пути.
     *
     * @param ant        Юнит, которому отдается приказ.
     * @param target     Целевой гекс.
     * @param state      Текущее состояние арены.
     * @param pathfinder Экземпляр поисковика пути.
     * @param hexCosts   Карта стоимостей передвижения.
     * @param hexTypes   Карта типов гексов.
     * @return {@link Optional} с готовой и проверенной командой, либо пустой.
     */
    public static Optional<MoveCommandDto> createPathCommand(ArenaStateDto.AntDto ant, Hex target, ArenaStateDto state, Pathfinder pathfinder, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes) {
        Hex start = new Hex(ant.q(), ant.r());
        Set<Hex> obstacles = getObstaclesFor(ant, state);

        List<Hex> path = pathfinder.findPath(start, target, hexCosts, obstacles);
        if (path.isEmpty()) return Optional.empty();

        UnitType unitType = UnitType.fromApiId(ant.type());
        List<Hex> truncatedPath = truncatePathByMovementPoints(path, unitType.getSpeed(), hexCosts);
        if (truncatedPath.isEmpty()) return Optional.empty();

        Hex finalDestination = truncatedPath.getLast();
        if (isUnsafeFinalDestination(finalDestination, ant, hexTypes)) {
            return Optional.empty();
        }

        return Optional.of(new MoveCommandDto(ant.id(), truncatedPath));
    }

    /**
     * Формирует динамический набор препятствий для конкретного юнита.
     *
     * @param ant   Юнит, для которого определяются препятствия.
     * @param state Текущее состояние арены.
     * @return {@link Set} гексов, которые следует считать непроходимыми.
     */
    public static Set<Hex> getObstaclesFor(ArenaStateDto.AntDto ant, ArenaStateDto state) {
        Set<Hex> obstacles = new HashSet<>();
        state.enemies().forEach(e -> obstacles.add(new Hex(e.q(), e.r())));
        state.ants().stream()
                .filter(other -> other.type() == ant.type() && !other.id().equals(ant.id()))
                .forEach(other -> obstacles.add(new Hex(other.q(), other.r())));
        state.map().stream()
                .filter(cell -> HexType.fromApiId(cell.type()).isImpassable())
                .forEach(cell -> obstacles.add(new Hex(cell.q(), cell.r())));
        return obstacles;
    }

    /**
     * Производит валидацию конечной точки маршрута на предмет опасности.
     *
     * @param destination Конечный гекс запланированного маршрута.
     * @param ant         Юнит, выполняющий движение.
     * @param hexTypes    Карта типов гексов.
     * @return {@code true}, если остановка на данном гексе приведет к гибели юнита, иначе {@code false}.
     */
    public static boolean isUnsafeFinalDestination(Hex destination, ArenaStateDto.AntDto ant, Map<Hex, HexType> hexTypes) {
        HexType destinationType = hexTypes.get(destination);
        if (destinationType == HexType.ACID) {
            return ant.health() <= destinationType.getDamage();
        }
        return false;
    }

    /**
     * Обрезает полный путь до достижимого за один ход сегмента.
     *
     * @param path      Полный, идеальный путь.
     * @param maxPoints Максимальное количество очков передвижения юнита.
     * @param hexCosts  Карта стоимостей передвижения.
     * @return Достижимый за один ход сегмент пути.
     */
    public static List<Hex> truncatePathByMovementPoints(List<Hex> path, int maxPoints, Map<Hex, Integer> hexCosts) {
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
     */
    public static boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
    }

    /**
     * Находит ближайший к юниту гекс из набора гексов муравейника.
     */
    public static Optional<Hex> findClosestHomeHex(Hex from, List<Hex> homeHexes) {
        return homeHexes.stream().min(Comparator.comparingInt(from::distanceTo));
    }

    /**
     * Извлекает из состояния арены карту стоимостей передвижения по гексам.
     */
    public static Map<Hex, Integer> getHexCosts(ArenaStateDto state) {
        return state.map().stream().collect(Collectors.toMap(cell -> new Hex(cell.q(), cell.r()), ArenaStateDto.MapCellDto::cost, (a, b) -> a));
    }

    /**
     * Извлекает из состояния арены карту типов гексов.
     */
    public static Map<Hex, HexType> getHexTypes(ArenaStateDto state) {
        return state.map().stream().collect(Collectors.toMap(
                cell -> new Hex(cell.q(), cell.r()),
                cell -> HexType.fromApiId(cell.type()),
                (a, b) -> a
        ));
    }
}
