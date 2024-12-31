package org.example.service;

import org.example.models.Food;
import org.example.models.GameState;
import org.example.models.Point3D;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class FoodService {
    private static final Logger logger = Logger.getLogger(FoodService.class.getName());

    public Food findNearestFood(Point3D head, List<Food> foodList, List<Integer> mapSize, Set<Point3D> obstacles) {
        if (foodList == null || foodList.isEmpty()) {
            logger.warning("Список фруктов пуст. Невозможно найти ближайший фрукт.");
            return createCenterFood(mapSize); // Возвращаем фиктивный фрукт в центре карты
        }

        // Исключаем фрукты, находящиеся в препятствиях
        List<Food> accessibleFood = foodList.stream()
                .filter(food -> !obstacles.contains(food.getCoordinates()))
                .filter(food -> food.getPoints() > 75) // Исключаем фрукты с ценностью 0
                .toList();

        // Если есть доступные ценные фрукты, ищем ближайший
        if (!accessibleFood.isEmpty()) {
            return accessibleFood.stream()
                    .min(Comparator.comparingInt(food -> calculateManhattanDistance(head, food.getCoordinates())))
                    .orElse(null);
        }

        // Если нет доступных ценных фруктов, направляемся к центру карты
        logger.info("Нет доступных ценных фруктов. Направляемся к центру карты.");
        return createCenterFood(mapSize);
    }


    /**
     * Создает фиктивный фрукт в центре карты.
     */
    private Food createCenterFood(List<Integer> mapSize) {
        Point3D center = getCenterPoint(mapSize);
        logger.info("Фиктивный фрукт в центре карты: " + center);
        return new Food(List.of(center.getX(), center.getY(), center.getZ()), 100);
    }

    /**
     * Вычисляет координаты центра карты.
     */
    private Point3D getCenterPoint(List<Integer> mapSize) {
        return new Point3D(
                mapSize.get(0) / 2,
                mapSize.get(1) / 2,
                mapSize.get(2) / 2
        );
    }

    /**
     * Вычисляет манхэттенское расстояние между двумя точками.
     */
    private int calculateManhattanDistance(Point3D p1, Point3D p2) {
        return Math.abs(p1.getX() - p2.getX())
                + Math.abs(p1.getY() - p2.getY())
                + Math.abs(p1.getZ() - p2.getZ());
    }

    /**
     * Выводит информацию о текущем состоянии игры и ближайших фруктах.
     *
     * @param points   Текущие очки игрока.
     * @param snakes   Список змей.
     * @param foodList Список фруктов.
     */
    public void displayGameStateInfo(int points, List<org.example.models.Snake> snakes, List<Food> foodList, List<Integer> mapSize, GameState gameState) {
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
                Food nearestFood = findNearestFood(head, foodList, mapSize, gameState.getObstacles());
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
}
