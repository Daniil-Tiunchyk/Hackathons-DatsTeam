package org.example.service;

import org.example.models.Food;
import org.example.models.Point3D;

import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

public class FoodService {
    private static final Logger logger = Logger.getLogger(FoodService.class.getName());

    /**
     * Возвращает ближайший фрукт для заданной точки.
     *
     * @param head     Координаты головы змейки.
     * @param foodList Список доступных фруктов.
     * @return Ближайший фрукт или null, если фрукты отсутствуют.
     */
    public Food findNearestFood(Point3D head, List<Food> foodList) {
        if (foodList == null || foodList.isEmpty()) {
            logger.warning("Список фруктов пуст. Невозможно найти ближайший фрукт.");
            return null;
        }

        return foodList.stream()
                .min(Comparator.comparingInt(food -> calculateManhattanDistance(head, food.getCoordinates())))
                .orElse(null);
    }

    /**
     * Исключает фрукты, находящиеся внутри препятствий или окруженные препятствиями.
     *
     * @param foodList  Список доступных фруктов.
     * @param obstacles Набор координат препятствий.
     * @return Отфильтрованный список фруктов.
     */
    public List<Food> filterUnreachableFood(List<Food> foodList, List<Point3D> obstacles) {
        return foodList.stream()
                .filter(food -> isReachable(food.getCoordinates(), obstacles))
                .toList();
    }

    /**
     * Проверяет, доступен ли фрукт для сбора (не окружен препятствиями).
     *
     * @param food      Координаты фрукта.
     * @param obstacles Набор координат препятствий.
     * @return true, если фрукт доступен, false — если фрукт окружен препятствиями.
     */
    private boolean isReachable(Point3D food, List<Point3D> obstacles) {
        // Проверяем все соседние клетки фрукта
        int[][] deltas = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };

        for (int[] delta : deltas) {
            Point3D neighbor = new Point3D(
                    food.getX() + delta[0],
                    food.getY() + delta[1],
                    food.getZ() + delta[2]
            );

            // Если хотя бы одна соседняя клетка не является препятствием, фрукт доступен
            if (!obstacles.contains(neighbor)) {
                return true;
            }
        }

        logger.warning("Фрукт в точке " + food + " окружен препятствиями и недоступен.");
        return false;
    }

    /**
     * Выводит информацию о текущем состоянии игры и ближайших фруктах.
     *
     * @param points   Текущие очки игрока.
     * @param snakes   Список змей.
     * @param foodList Список фруктов.
     */
    public void displayGameStateInfo(int points, List<org.example.models.Snake> snakes, List<Food> foodList) {
        System.out.println("[INFO] Текущий счёт: " + points);
        System.out.println();

        for (var snake : snakes) {
            Point3D head = snake.getHead();
            String status = snake.getStatus();
            int length = snake.getLength();

            // Информация о змейке
            System.out.println("Змейка ID: " + snake.getId());
            System.out.println("  Статус: " + status);
            System.out.println("  Длина: " + length);
            System.out.println("  Голова: " + head);

            // Если змея живая, ищем ближайший фрукт
            if ("alive".equals(status) && head != null && !foodList.isEmpty()) {
                Food nearestFood = findNearestFood(head, foodList);
                if (nearestFood != null) {
                    Point3D foodPoint = nearestFood.getCoordinates();
                    int distance = calculateManhattanDistance(head, foodPoint);

                    System.out.println("    Ближайший фрукт: " + foodPoint);
                    System.out.println("      Расстояние: " + distance);
                    System.out.println("      Ценность: " + nearestFood.getPoints());
                } else {
                    System.out.println("    Ближайший фрукт: N/A");
                }
            }
            System.out.println(); // Пустая строка между змеями
        }
    }

    /**
     * Вычисляет манхэттенское расстояние между двумя точками.
     *
     * @param p1 Первая точка.
     * @param p2 Вторая точка.
     * @return Манхэттенское расстояние.
     */
    private int calculateManhattanDistance(Point3D p1, Point3D p2) {
        return Math.abs(p1.getX() - p2.getX())
                + Math.abs(p1.getY() - p2.getY())
                + Math.abs(p1.getZ() - p2.getZ());
    }
}
