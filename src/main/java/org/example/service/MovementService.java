package org.example.service;

import org.example.models.*;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/* ---------------------------------------------------
 * MovementService — отправка/получение данных + сбор команды движения
 * --------------------------------------------------- */
public class MovementService {
    private static final Logger logger = Logger.getLogger(MovementService.class.getName());

    private final String baseUrl;
    private final String token;
    private final Gson gson;
    private final FileService fileService;

    // Для поиска пути и поиска еды подключаем сервисы (DI)
    private final PathFindingService pathFindingService;
    private final FoodService foodService;

    // Настройки для локального поиска
    private static final int SEARCH_RADIUS = 5;  // Радиус, в пределах которого учитываем препятствия
    private static final int DISTANCE_LIMIT = 50; // Максимальная дистанция до фрукта, чтобы пытаться найти путь

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
     * Получение текущего состояния игры (JSON).
     */
    public String fetchGameState() throws IOException, InterruptedException {
        // Отправляем пустое тело "{}" для получения состояния
        return sendHttpRequest(baseUrl, "{}");
    }

    /**
     * Сформировать команду движения для всех наших змей.
     * <p>
     * 1) Для каждой «живой» змейки ищем ближайший фрукт (смотрим расстояние).
     * 2) Если фрукт слишком далеко, пропускаем (во избежание долгого A*).
     * 3) Фильтруем препятствия по локальному радиусу SEARCH_RADIUS.
     * 4) Запускаем A* через PathFindingService.
     * 5) Если путь найден — берём первый шаг (direction).
     */
    public SnakeRequest buildMoveRequest(GameState gameState) {
        SnakeRequest moveRequest = new SnakeRequest();

        // Обратите внимание: убрали вызов addMapBoundaries(...)
        // и не используем глобальный obstacles со всеми границами.

        for (Snake snake : gameState.getSnakes()) {
            if ("alive".equals(snake.getStatus()) && snake.getHead() != null) {
                Point3D head = snake.getHead();

                // Находим ближайший фрукт (просто манхэттенская проверка,
                // без учёта препятствий)
                Food nearestFood = foodService.findNearestFood(head, gameState.getFood());
                if (nearestFood == null) {
                    logger.info("Нет фруктов или все уже съедены, пропускаем движение.");
                    continue;
                }

                // Проверяем, не слишком ли далеко фрукт
                int dist = distanceManhattan(head, nearestFood.getCoordinates());
                if (dist > DISTANCE_LIMIT) {
                    logger.info("Фрукт слишком далеко (dist=" + dist + "), пропускаем.");
                    continue;
                }

                // Собираем локальные препятствия (только в радиусе вокруг головы).
                Set<Point3D> localObstacles = collectLocalObstacles(
                        gameState,
                        head,
                        SEARCH_RADIUS
                );

                // Вызываем A*
                List<int[]> path = pathFindingService.findPath(
                        head,
                        nearestFood.getCoordinates(),
                        localObstacles,
                        gameState.getMapSize(),
                        SEARCH_RADIUS  // Параметр, который внутри findPath
                        // ограничит проверку диапазона
                );

                if (path != null && !path.isEmpty()) {
                    // Проверяем, не null ли список команд в moveRequest
                    if (moveRequest.getSnakes() == null) {
                        moveRequest.setSnakes(new ArrayList<>());
                    }
                    moveRequest.getSnakes().add(
                            new SnakeRequest.SnakeCommand(snake.getId(), path.get(0))
                    );
                } else {
                    logger.info("Путь не найден (или A* оборван) для змейки " + snake.getId());
                }

            }
        }

        return moveRequest;
    }

    /**
     * Отправить команду движения на сервер.
     */
    public void sendMoveRequest(SnakeRequest moveRequest) throws IOException, InterruptedException {
        String moveRequestJson = gson.toJson(moveRequest);

        // Сохраняем отправляемый JSON
        fileService.saveToFile(moveRequestJson, "data/request.json");

        // Отправляем запрос
        String response = sendHttpRequest(baseUrl, moveRequestJson);

        // Можно сохранить ответ сервера, если нужно
        fileService.saveToFile(response, "data/response_move.json");

        logger.info("[INFO] Команда движения успешно отправлена!");
    }

    /**
     * Приватный метод для отправки HTTP (POST).
     */
    private String sendHttpRequest(String endpoint, String body) throws IOException, InterruptedException {
        var request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("X-Auth-Token", token)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                .build();

        var client = java.net.http.HttpClient.newHttpClient();
        var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("Сервер вернул ошибку: " + response.statusCode()
                    + " | Тело ответа: " + response.body());
        }

        return response.body();
    }

    /**
     * Собираем препятствия в радиусе R вокруг головы змейки (и, возможно, вокруг цели).
     * <p>
     * - fences (заборы)
     * - geometry врагов
     * - (опционально) клетки вокруг головы врага, если хотим избегать лобового столкновения
     */
    private Set<Point3D> collectLocalObstacles(GameState gs, Point3D snakeHead, int radius) {
        Set<Point3D> result = new HashSet<>();

        // Добавляем заборы
        for (List<Integer> fence : gs.getFences()) {
            Point3D fenceP = new Point3D(fence.get(0), fence.get(1), fence.get(2));
            if (distanceManhattan(fenceP, snakeHead) <= radius) {
                result.add(fenceP);
            }
        }

        // Добавляем вражеских змей
        for (Enemy enemy : gs.getEnemies()) {
            List<List<Integer>> geom = enemy.getGeometry();
            if (geom != null) {
                for (List<Integer> seg : geom) {
                    Point3D segP = new Point3D(seg.get(0), seg.get(1), seg.get(2));
                    if (distanceManhattan(segP, snakeHead) <= radius) {
                        result.add(segP);
                    }
                }
                // При желании добавить клетки вокруг головы
                if (!geom.isEmpty()) {
                    List<Integer> head = geom.get(0);
                    Point3D headP = new Point3D(head.get(0), head.get(1), head.get(2));
                    if (distanceManhattan(headP, snakeHead) <= radius) {
                        addHeadSurroundings(result, headP);
                    }
                }
            }
        }

        return result;
    }

    private void addHeadSurroundings(Set<Point3D> obstacles, Point3D head) {
        int x = head.getX();
        int y = head.getY();
        int z = head.getZ();

        int[][] deltas = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };

        for (int[] d : deltas) {
            obstacles.add(new Point3D(x + d[0], y + d[1], z + d[2]));
        }
    }

    private int distanceManhattan(Point3D a, Point3D b) {
        return Math.abs(a.getX() - b.getX())
                + Math.abs(a.getY() - b.getY())
                + Math.abs(a.getZ() - b.getZ());
    }
}
