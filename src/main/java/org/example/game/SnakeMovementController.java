package org.example.game;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.models.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SnakeMovementController {
    private final String baseUrl;
    private final String token;
    private final Gson gson;

    public SnakeMovementController(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.gson = new Gson();
    }

    public void moveSnakesToNearestFood() {
        try {
            // Используем сохранённый JSON вместо вызова сервера
            String previousResponse = new String(Files.readAllBytes(Paths.get("response.json")));

            // Преобразуем JSON-ответ в объект GameState
            GameState initialState;
            try {
                initialState = gson.fromJson(previousResponse, GameState.class);
            } catch (JsonSyntaxException e) {
                System.err.println("[ERROR] Ошибка десериализации JSON.");
                System.err.println("[DEBUG] Ответ сервера: " + previousResponse);
                throw e;
            }

            // Вывод в консоль нужной информации
            displayGameState(initialState);

            // Формируем команды движения
            SnakeRequest moveRequest = buildMoveRequest(initialState);
            String moveRequestJson = gson.toJson(moveRequest);

            // Лог: Команда движения
            System.out.println("[INFO] Команда движения сформирована.");

        } catch (IOException e) {
            System.err.println("[ERROR] Ошибка ввода-вывода: " + e.getMessage());
        } catch (IllegalStateException e) {
            System.err.println("[ERROR] Некорректный формат JSON: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Неизвестная ошибка: " + e.getMessage());
        }
    }

    private void displayGameState(GameState gameState) {
        System.out.println("[INFO] Текущий счёт: " + gameState.getPoints());

        for (Snake snake : gameState.getSnakes()) {
            List<Integer> headCoordinates = snake.getGeometry().isEmpty() ? null : snake.getGeometry().get(0);
            String status = snake.getStatus();
            int length = snake.getGeometry().size();

            System.out.println("[INFO] Змейка ID: " + snake.getId());
            System.out.println("       Статус: " + status);
            System.out.println("       Длина: " + length);
            System.out.println("       Голова: " + (headCoordinates != null ? headCoordinates : "N/A"));

            if ("alive".equals(status) && headCoordinates != null) {
                Point3D head = convertToPoint3D(headCoordinates);
                Food nearestFood = findNearestFood(head, getVisibleFood(gameState.getFood(), head));

                if (nearestFood != null) {
                    Point3D foodPoint = convertToPoint3D(nearestFood.getC());
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

    private List<Food> getVisibleFood(List<Food> foodList, Point3D head) {
        List<Food> visibleFood = new ArrayList<>();
        for (Food food : foodList) {
            Point3D foodPoint = convertToPoint3D(food.getC());
            if (Math.abs(foodPoint.getX() - head.getX()) <= 30 &&
                    Math.abs(foodPoint.getY() - head.getY()) <= 30 &&
                    Math.abs(foodPoint.getZ() - head.getZ()) <= 30) {
                visibleFood.add(food);
            }
        }
        return visibleFood;
    }

    private SnakeRequest buildMoveRequest(GameState gameState) {
        SnakeRequest moveRequest = new SnakeRequest();

        // Инициализируем список змей, чтобы избежать null-pointer ошибки
        if (moveRequest.getSnakes() == null) {
            moveRequest.setSnakes(new ArrayList<>());
        }

        for (Snake snake : gameState.getSnakes()) {
            if ("alive".equals(snake.getStatus()) && !snake.getGeometry().isEmpty()) {
                List<Integer> headCoordinates = snake.getGeometry().get(0);
                Point3D head = convertToPoint3D(headCoordinates);
                Food nearestFood = findNearestFood(head, getVisibleFood(gameState.getFood(), head));

                if (nearestFood != null) {
                    Point3D foodPoint = convertToPoint3D(nearestFood.getC());
                    Direction3D direction = calculateDirection(head, foodPoint);
                    SnakeRequest.SnakeCommand command = new SnakeRequest.SnakeCommand(snake.getId(), direction);
                    moveRequest.getSnakes().add(command);
                }
            }
        }

        return moveRequest;
    }

    private Food findNearestFood(Point3D head, List<Food> foodList) {
        Food nearest = null;
        int minDistance = Integer.MAX_VALUE;

        for (Food food : foodList) {
            Point3D foodPoint = convertToPoint3D(food.getC());
            int distance = calculateManhattanDistance(head, foodPoint);

            if (distance < minDistance) {
                minDistance = distance;
                nearest = food;
            }
        }

        return nearest;
    }

    private Direction3D calculateDirection(Point3D head, Point3D target) {
        return new Direction3D(
                Integer.compare(target.getX(), head.getX()),
                Integer.compare(target.getY(), head.getY()),
                Integer.compare(target.getZ(), head.getZ())
        );
    }

    private int calculateManhattanDistance(Point3D p1, Point3D p2) {
        return Math.abs(p1.getX() - p2.getX()) +
                Math.abs(p1.getY() - p2.getY()) +
                Math.abs(p1.getZ() - p2.getZ());
    }

    private Point3D convertToPoint3D(List<Integer> coordinates) {
        if (coordinates.size() == 3) {
            return new Point3D(coordinates.get(0), coordinates.get(1), coordinates.get(2));
        } else {
            throw new IllegalStateException("Некорректные координаты: " + coordinates);
        }
    }
}
