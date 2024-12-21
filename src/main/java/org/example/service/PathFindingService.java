package org.example.service;

import lombok.AllArgsConstructor;
import org.example.models.Point3D;

import java.util.*;
import java.util.logging.Logger;

/**
 * Сервис для поиска пути (A*) в трёхмерном пространстве.
 * <p>
 * Шаги алгоритма:
 * 1. Создаём стартовую ноду (голова змейки).
 * 2. Используем приоритетную очередь (openSet) для узлов, которые мы хотим исследовать.
 * 3. Храним costSoFar (стоимость пути) в Map для каждой посещённой координаты.
 * 4. На каждом шаге берём из очереди ноду с минимальным (g+h).
 * 5. Если достигли цели (target), восстанавливаем путь через parent-ссылки.
 * 6. Если нет, добавляем соседей (6 направлений для 3D), учитывая препятствия, карту и уже накопленные стоимости.
 * 7. Если путь не найден к тому моменту, когда очередь пуста (или превышен лимит итераций), возвращаем null.
 */
public class PathFindingService {
    private static final Logger logger = Logger.getLogger(PathFindingService.class.getName());

    // Максимальное количество итераций, чтобы не «зависать» на больших картах
    private static final int ITERATION_LIMIT = 50_000;

    /**
     * Поиск пути из точки head в точку target с учётом препятствий.
     *
     * @param head      координаты головы змейки (старт)
     * @param target    координаты фрукта (цель)
     * @param obstacles набор занятых клеток (препятствия), куда ходить нельзя
     * @param mapSize   [maxX, maxY, maxZ] - размеры карты
     * @param radius    радиус, в пределах которого позволяем исследовать (можно отключить, поставив большое число)
     * @return путь в виде списка направлений [dx, dy, dz]; или null, если путь не найден
     */
    public List<int[]> findPath(Point3D head,
                                Point3D target,
                                Set<Point3D> obstacles,
                                List<Integer> mapSize,
                                int radius) {

        // Если начальная или конечная точка некорректна
        if (head == null || target == null) {
            return null;
        }

        // Если старт == цель, путь "нуль" — змея уже на фрукте
        if (head.equals(target)) {
            return Collections.emptyList();
        }

        // Приоритетная очередь по возрастанию f = g + h
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getF));

        // Храним «лучшую стоимость достижения» каждой клетки (g-стоимость)
        Map<Point3D, Integer> costSoFar = new HashMap<>();

        // Стартовая нода
        Node startNode = new Node(head, null, 0, manhattan(head, target));
        openSet.add(startNode);
        costSoFar.put(head, 0);

        int iterationCount = 0;

        while (!openSet.isEmpty()) {
            iterationCount++;
            if (iterationCount > ITERATION_LIMIT) {
                logger.warning("A* aborted: iteration limit exceeded. openSet.size()=" + openSet.size());
                return null;
            }

            // Извлекаем ноду с наименьшей стоимостью (g + h)
            Node current = openSet.poll();

            // Если дошли до цели, восстанавливаем путь
            if (current.point.equals(target)) {
                return reconstructPath(current);
            }

            // Перебираем всех соседей (6 направлений в 3D)
            for (int[] dir : getDirections()) {
                int nx = current.point.getX() + dir[0];
                int ny = current.point.getY() + dir[1];
                int nz = current.point.getZ() + dir[2];

                Point3D neighbor = new Point3D(nx, ny, nz);

                // 1. Проверяем, что сосед в пределах карты
                if (!isWithinBounds(neighbor, mapSize)) {
                    continue;
                }

                // 2. Если нужно ограничить радиус поиска
                if (!isWithinRadius(neighbor, head, radius)) {
                    continue;
                }

                // 3. Проверяем препятствия
                if (obstacles.contains(neighbor)) {
                    continue;
                }

                // 4. Считаем стоимость перехода (шаг 1 = +1)
                int newCost = current.g + 1;

                // Если не посещали ещё или нашли путь дешевле
                if (!costSoFar.containsKey(neighbor) || newCost < costSoFar.get(neighbor)) {
                    costSoFar.put(neighbor, newCost);

                    // Эвристика (манхэттенское расстояние до цели)
                    int h = manhattan(neighbor, target);

                    Node neighborNode = new Node(neighbor, current, newCost, h);
                    openSet.add(neighborNode);
                }
            }
        }

        // Если очередь опустела — путь не найден
        return null;
    }

    /**
     * Восстанавливаем путь, идя по цепочке parent от конечной ноды к стартовой.
     */
    private List<int[]> reconstructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        Node current = node;

        // Пока у ноды есть предок, рассчитываем вектор [dx, dy, dz]
        while (current.parent != null) {
            Point3D currP = current.point;
            Point3D parP = current.parent.point;
            int dx = currP.getX() - parP.getX();
            int dy = currP.getY() - parP.getY();
            int dz = currP.getZ() - parP.getZ();
            path.add(new int[]{dx, dy, dz});
            current = current.parent;
        }

        // Нужно развернуть список, так как мы шли от конца к началу
        Collections.reverse(path);
        return path;
    }

    /**
     * Допустимые шаги в 3D: +/-1 по каждой из осей X, Y, Z. Итого 6 направлений.
     */
    private int[][] getDirections() {
        return new int[][]{
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };
    }

    /**
     * Проверка, что координаты внутри карты.
     */
    private boolean isWithinBounds(Point3D p, List<Integer> mapSize) {
        // mapSize = [maxX, maxY, maxZ]
        return p.getX() >= 0 && p.getX() < mapSize.get(0)
                && p.getY() >= 0 && p.getY() < mapSize.get(1)
                && p.getZ() >= 0 && p.getZ() < mapSize.get(2);
    }

    /**
     * Если включен «радиусный» поиск, проверяем, что точка не слишком далеко от головы.
     * Можно отключить, передав в findPath(...) большой radius.
     */
    private boolean isWithinRadius(Point3D p, Point3D head, int radius) {
        if (radius <= 0) return true;  // Отключили ограничение
        return manhattan(p, head) <= radius;
    }

    /**
     * Манхэттенское расстояние в 3D.
     */
    private int manhattan(Point3D a, Point3D b) {
        return Math.abs(a.getX() - b.getX())
                + Math.abs(a.getY() - b.getY())
                + Math.abs(a.getZ() - b.getZ());
    }

    /**
     * Узел для A*: хранит позицию, ссылку на родителя,
     * g (стоимость пути от старта) и h (эвристику).
     */
    @AllArgsConstructor
    private static class Node {
        Point3D point;
        Node parent;
        int g;  // пройденная стоимость (distance from start)
        int h;  // эвристика (distance to goal)

        int getF() {
            return g + h;
        }
    }
}
