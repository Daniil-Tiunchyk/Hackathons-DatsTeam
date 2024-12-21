package org.example.service;

import org.example.models.Food;
import org.example.models.GameState;
import org.example.models.Point3D;
import org.example.models.Snake;

import java.util.List;

public class FoodService {
    public void displayGameStateInfo(GameState gameState) {
        System.out.println("[INFO] Текущий счёт: " + gameState.getPoints());

        for (Snake snake : gameState.getSnakes()) {
            Point3D head = snake.getHead();
            String status = snake.getStatus();
            int length = snake.getLength();

            System.out.println("[INFO] Змейка ID: " + snake.getId());
            System.out.println("       Статус: " + status);
            System.out.println("       Длина: " + length);
            System.out.println("       Голова: " + head);

            if ("alive".equals(status) && head != null) {
                Food nearestFood = findNearestFood(head, gameState.getFood());
                if (nearestFood != null) {
                    Point3D foodPoint = nearestFood.getCoordinates();
                    int distance = calculateManhattanDistance(head, foodPoint);
                    System.out.println("       Ближайший фрукт: " + foodPoint);
                    System.out.println("       Расстояние до него: " + distance);
                    System.out.println("       Ценность: " + nearestFood.getPoints());
                } else {
                    System.out.println("       Ближайший фрукт: N/A");
                }
            }
        }
    }

    public Food findNearestFood(Point3D head, List<Food> foodList) {
        Food nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Food food : foodList) {
            int distance = calculateManhattanDistance(head, food.getCoordinates());
            if (distance < minDistance) {
                minDistance = distance;
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
