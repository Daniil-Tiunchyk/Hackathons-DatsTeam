package org.example.service;

import org.example.models.Food;
import org.example.models.GameState;
import org.example.models.Point3D;

import java.util.List;

public class FoodService {

    /**
     * Отображение информации об игре и о ближайших фруктах к каждой змейке.
     */
    public void displayGameStateInfo(GameState gameState) {
        System.out.println("[INFO] Текущий счёт: " + gameState.getPoints());
        System.out.println();

        for (var snake : gameState.getSnakes()) {
            Point3D head = snake.getHead();
            String status = snake.getStatus();
            int length = snake.getLength();

            System.out.println("Змейка ID: " + snake.getId());
            System.out.println("  Статус: " + status);
            System.out.println("  Длина: " + length);
            System.out.println("  Голова: " + head);

            if ("alive".equals(status) && head != null) {
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
            System.out.println();
        }
    }

    /**
     * Поиск ближайшего фрукта по Манхэттенскому расстоянию.
     */
    public Food findNearestFood(Point3D head, List<Food> foodList) {
        if (foodList == null || foodList.isEmpty()) return null;

        Food nearest = null;
        int minDist = Integer.MAX_VALUE;

        for (var food : foodList) {
            int dist = calculateManhattanDistance(head, food.getCoordinates());
            if (dist < minDist) {
                minDist = dist;
                nearest = food;
            }
        }
        return nearest;
    }

    private int calculateManhattanDistance(Point3D p1, Point3D p2) {
        return Math.abs(p1.getX() - p2.getX())
                + Math.abs(p1.getY() - p2.getY())
                + Math.abs(p1.getZ() - p2.getZ());
    }
}
