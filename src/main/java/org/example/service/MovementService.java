package org.example.service;

import org.example.models.GameState;
import org.example.models.SnakeRequest;
import org.example.models.Point3D;
import org.example.models.Food;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class MovementService {
    private final String baseUrl;
    private final String token;
    private final Gson gson;
    private final FileService fileService;

    public MovementService(String baseUrl, String token, Gson gson, FileService fileService) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.gson = gson;
        this.fileService = fileService;
    }

    public String fetchGameState() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Сервер вернул ошибку: " + response.statusCode());
        }

        return response.body();
    }

    public SnakeRequest buildMoveRequest(GameState gameState) {
        SnakeRequest moveRequest = new SnakeRequest();
        moveRequest.setSnakes(new ArrayList<>());

        for (var snake : gameState.getSnakes()) {
            if ("alive".equals(snake.getStatus()) && snake.getHead() != null) {
                Food nearestFood = new FoodService().findNearestFood(snake.getHead(), gameState.getFood());
                if (nearestFood != null) {
                    Point3D foodPoint = nearestFood.getCoordinates();
                    int[] direction = calculateDirectionArray(snake.getHead(), foodPoint); // Преобразуем направление в массив
                    moveRequest.getSnakes().add(new SnakeRequest.SnakeCommand(snake.getId(), direction));
                }
            }
        }
        return moveRequest;
    }

    public void sendMoveRequest(SnakeRequest moveRequest) throws IOException, InterruptedException {
        String moveRequestJson = gson.toJson(moveRequest);

        // Сохраняем отправляемый JSON
        fileService.saveToFile(moveRequestJson, "data/request.json");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(moveRequestJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Сервер вернул ошибку: " + response.statusCode());
        }

        System.out.println("[INFO] Команда движения успешно отправлена!");
    }

    private int[] calculateDirectionArray(Point3D head, Point3D target) {
        int dx = Integer.compare(target.getX(), head.getX());
        int dy = Integer.compare(target.getY(), head.getY());
        int dz = Integer.compare(target.getZ(), head.getZ());

        // Движение только по одной оси
        if (dx != 0) {
            return new int[]{dx, 0, 0};
        } else if (dy != 0) {
            return new int[]{0, dy, 0};
        } else {
            return new int[]{0, 0, dz};
        }
    }
}
