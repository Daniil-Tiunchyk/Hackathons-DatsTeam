package org.example.game;

import com.google.gson.Gson;
import org.example.models.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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

    private String sendGet(String endpoint) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("X-Auth-Token", token)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private void sendPost(String endpoint, String jsonPayload) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public void moveSnakesToNearestFood() {
        try {
            // Получение текущего состояния игры
            String response = sendGet("/play/snake3d");
            GameState gameState = gson.fromJson(response, GameState.class);

            // Формирование команды движения змей
            String moveRequestJson = gson.toJson(buildMoveRequest(gameState));
            sendPost("/play/snake3d/player/move", moveRequestJson);

            System.out.println("Змейки направлены к ближайшим мандаринам!");

        } catch (IOException | InterruptedException e) {
            System.err.println("Ошибка при выполнении запроса: " + e.getMessage());
        }
    }

    private SnakeRequest buildMoveRequest(GameState gameState) {
        SnakeRequest moveRequest = new SnakeRequest();

        for (Snake snake : gameState.getSnakes()) {
            if ("alive".equals(snake.getStatus()) && !snake.getGeometry().isEmpty()) {
                // Голова змеи - первый элемент в geometry
                Point3D head = snake.getGeometry().get(0);

                // Поиск ближайшей еды
                Food nearestFood = findNearestFood(head, gameState.getFood());

                if (nearestFood != null) {
                    // Расчёт направления
                    Direction3D direction = calculateDirection(head, nearestFood.getC());

                    // Создание команды движения
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
            // Вычисление Манхэттенского расстояния
            int distance = calculateManhattanDistance(head, food.getC());

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
}
