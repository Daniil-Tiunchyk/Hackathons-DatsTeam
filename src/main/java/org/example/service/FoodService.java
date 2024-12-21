package org.example.service;

import org.example.models.Food;
import org.example.models.GameState;
import org.example.models.Point3D;
import org.example.models.Snake;

import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/* ---------------------------------------------------
 * FoodService — логика работы с фруктами
 * --------------------------------------------------- */
public class FoodService {
    private static final Logger logger = Logger.getLogger(FoodService.class.getName());

    /**
     * Показывает общую информацию об игре и о ближайших фруктах к каждой змеике.
     */
    public void displayGameStateInfo(GameState gameState) {
        // Общая информация об игре
        System.out.println("[INFO] Текущий счёт: " + gameState.getPoints());
        System.out.println();

        for (Snake snake : gameState.getSnakes()) {
            Point3D head = snake.getHead();
            String status = snake.getStatus();
            int length = snake.getLength();

            // Информация о змейке
            System.out.println("Змейка ID: " + snake.getId());
            System.out.println("  Статус: " + status);
            System.out.println("  Длина: " + length);
            System.out.println("  Голова: " + head);

            // Если змея живая и есть фрукты на поле, найдём ближайший
            if ("alive".equals(status) && head != null && !gameState.getFood().isEmpty()) {
                Food nearestFood = findNearestFood(head, gameState.getFood());
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
     * Возвращает ближайший по манхэттенскому расстоянию фрукт (без учёта препятствий).
     * Используется в displayGameStateInfo() для подсказки.
     */
    public Food findNearestFood(Point3D head, List<Food> foodList) {
        if (foodList == null || foodList.isEmpty()) return null;

        Food nearest = null;
        int minDist = Integer.MAX_VALUE;

        for (Food f : foodList) {
            int dist = calculateManhattanDistance(head, f.getCoordinates());
            if (dist < minDist) {
                minDist = dist;
                nearest = f;
            }
        }
        return nearest;
    }

    /**
     * Манхэттенское расстояние (вспомогательный метод).
     */
    private int calculateManhattanDistance(Point3D p1, Point3D p2) {
        return Math.abs(p1.getX() - p2.getX())
                + Math.abs(p1.getY() - p2.getY())
                + Math.abs(p1.getZ() - p2.getZ());
    }
}
