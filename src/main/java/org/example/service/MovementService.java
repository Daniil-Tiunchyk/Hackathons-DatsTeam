package org.example.service;

import com.google.gson.Gson;
import org.example.models.Food;
import org.example.models.GameState;
import org.example.models.SnakeRequest;
import org.example.models.Point3D;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

public class MovementService {
    private static final Logger logger = Logger.getLogger(MovementService.class.getName());

    private final String baseUrl;
    private final String token;
    private final Gson gson;
    private final FileService fileService;

    private final PathFindingService pathFindingService;
    private final FoodService foodService;

    public MovementService(String baseUrl,
                           String token,
                           Gson gson,
                           FileService fileService,
                           PathFindingService pathFindingService,
                           FoodService foodService) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.gson = gson;
        this.fileService = fileService;
        this.pathFindingService = pathFindingService;
        this.foodService = foodService;
    }

    /**
     * Получить текущее состояние игры через POST-запрос.
     */
    public String fetchGameState() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString("{}"))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Сервер вернул ошибку: " + response.statusCode()
                    + " | Тело ответа: " + response.body());
        }

        // Логирование успешного получения состояния
        logger.info("[INFO] Состояние игры успешно получено.");

        return response.body();
    }

    /**
     * Сформировать и отправить команду движения змей.
     */
    public void sendMoveRequest(SnakeRequest moveRequest) throws IOException, InterruptedException {
        String moveRequestJson = gson.toJson(moveRequest);

        // Сохраняем отправляемый запрос
        fileService.saveToFile(moveRequestJson, "data/request.json");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(HttpRequest.BodyPublishers.ofString(moveRequestJson))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Сервер вернул ошибку: " + response.statusCode()
                    + " | Тело ответа: " + response.body());
        }

        // Сохраняем ответ сервера
        fileService.saveToFile(response.body(), "data/response_move.json");

        // Логирование успешной отправки команды
        logger.info("[INFO] Команда движения успешно отправлена!");
    }

    /**
     * Сформировать команду движения для всех змей.
     */
    public SnakeRequest buildMoveRequest(GameState gameState) {
        SnakeRequest moveRequest = new SnakeRequest();

        gameState.getSnakes().forEach(snake -> {
            if ("alive".equals(snake.getStatus()) && snake.getHead() != null) {
                Point3D head = snake.getHead();
                Food nearestFood = foodService.findNearestFood(head, gameState.getFood(), gameState.getMapSize(), gameState.getObstacles());
                Point3D nearestFoodPoint = nearestFood.getCoordinates();
                if (nearestFood != null) {
                    var path = pathFindingService.findPath(
                            head,
                            nearestFoodPoint,
                            gameState.getObstacles(),
                            gameState.getMapSize(),
                            30
                    );

                    if (path != null && !path.isEmpty()) {
                        moveRequest.getSnakes().add(new SnakeRequest.SnakeCommand(snake.getId(), path.get(0)));
                    } else {
                        logger.warning("Путь не найден для змейки: " + snake.getId());
                    }
                }
            }
        });

        return moveRequest;
    }
}
