package org.example.service;

import com.google.gson.Gson;
import org.example.models.*;

import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class MovementService {
    private static final Logger logger = Logger.getLogger(MovementService.class.getName());

    private final String baseUrl;
    private final String token;
    private final Gson gson;
    private final FileService fileService;
    private final PathFindingService pathFindingService;
    private final FoodService foodService;

    public MovementService(String baseUrl, String token, Gson gson, FileService fileService,
                           PathFindingService pathFindingService, FoodService foodService) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.gson = gson;
        this.fileService = fileService;
        this.pathFindingService = pathFindingService;
        this.foodService = foodService;
    }

    public String fetchGameState() throws IOException, InterruptedException {
        return sendHttpRequest(baseUrl, "{}");
    }

    public SnakeRequest buildMoveRequest(GameState gameState) {
        SnakeRequest moveRequest = new SnakeRequest();

        // Убедиться, что поле snakes не null
        if (moveRequest.getSnakes() == null) {
            moveRequest.setSnakes(new ArrayList<>());
        }

        for (var snake : gameState.getSnakes()) {
            if ("alive".equals(snake.getStatus()) && snake.getHead() != null) {
                Point3D head = snake.getHead();
                Food nearestFood = foodService.findNearestFood(head, gameState.getFood());

                if (nearestFood != null) {
                    List<int[]> path = pathFindingService.findPath(
                            head,
                            nearestFood.getCoordinates(),
                            collectObstacles(gameState),
                            gameState.getMapSize(),
                            30
                    );

                    if (path != null && !path.isEmpty()) {
                        moveRequest.getSnakes().add(new SnakeRequest.SnakeCommand(snake.getId(), path.get(0)));
                    } else {
                        logger.info("Путь не найден для змейки " + snake.getId());
                    }
                }
            }
        }

        return moveRequest;
    }


    public void sendMoveRequest(SnakeRequest moveRequest) throws IOException, InterruptedException {
        String requestJson = gson.toJson(moveRequest);
        fileService.saveToFile(requestJson, "data/request.json");

        String response = sendHttpRequest(baseUrl, requestJson);
        fileService.saveToFile(response, "data/response.json");
        logger.info("Команда движения отправлена.");
    }

    private String sendHttpRequest(String endpoint, String body) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Сервер вернул ошибку: " + response.statusCode() + " " + response.body());
        }
        return response.body();
    }

    private Set<Point3D> collectObstacles(GameState gameState) {
        Set<Point3D> obstacles = new HashSet<>();

        // Добавляем заборы как препятствия
        gameState.getFences().forEach(f ->
                obstacles.add(new Point3D(f.get(0), f.get(1), f.get(2)))
        );

        // Добавляем тело своих змей как препятствия
        gameState.getSnakes().forEach(snake ->
                snake.getGeometry().forEach(segment ->
                        obstacles.add(new Point3D(segment.get(0), segment.get(1), segment.get(2)))
                )
        );

        // Добавляем тело врагов и окружение вокруг их голов
        gameState.getEnemies().forEach(enemy -> {
            // Добавляем тело врагов
            enemy.getGeometry().forEach(segment ->
                    obstacles.add(new Point3D(segment.get(0), segment.get(1), segment.get(2)))
            );

            // Добавляем клетки вокруг головы врагов
            if (!enemy.getGeometry().isEmpty()) {
                List<Integer> head = enemy.getGeometry().get(0); // Голова врага
                addSurroundingObstacles(obstacles, new Point3D(head.get(0), head.get(1), head.get(2)));
            }
        });

        return obstacles;
    }

    private void addSurroundingObstacles(Set<Point3D> obstacles, Point3D head) {
        int x = head.getX();
        int y = head.getY();
        int z = head.getZ();

        int[][] deltas = {
                {1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}
        };

        for (int[] delta : deltas) {
            obstacles.add(new Point3D(x + delta[0], y + delta[1], z + delta[2]));
        }
    }
}
