package org.example.engine;

import com.google.gson.Gson;
import org.example.models.GameState;
import org.example.models.Point3D;
import org.example.models.Snake;
import org.example.models.SnakeRequest;
import org.example.service.FileService;
import org.example.service.FoodService;
import org.example.service.MovementService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameEngine {
    private static final Logger logger = Logger.getLogger(GameEngine.class.getName());

    private final MovementService movementService;
    private final FileService fileService;
    private final FoodService foodService;
    private final Gson gson;

    private int previousPoints = -1;
    private final Map<String, String> lastKnownStatus = new HashMap<>();
    private static final String SPAWN_POINTS_FILE = "spawn_points.csv";
    private boolean needHeader = false;

    public GameEngine(MovementService movementService,
                      FileService fileService,
                      FoodService foodService,
                      Gson gson) {
        this.movementService = movementService;
        this.fileService = fileService;
        this.foodService = foodService;
        this.gson = gson;
        initializeSpawnPointsFile();
    }

    public void runGameCycle() {
        try {
            // Получаем состояние игры
            String jsonResponse = movementService.fetchGameState();
            fileService.saveToFile(jsonResponse, "data/response.json");
            GameState gameState = gson.fromJson(jsonResponse, GameState.class);

            // Проверяем изменения очков
            if (previousPoints != -1 && gameState.getPoints() > previousPoints) {
                printSoupMessage(gameState.getPoints() - previousPoints);
            }
            previousPoints = gameState.getPoints();

            // Обрабатываем изменения статусов змей
            checkSnakeStatusChanges(gameState);

            // Вывод ошибок, если есть
            displayErrors(gameState);

            // Вывод информации о состоянии игры
            foodService.displayGameStateInfo(gameState.getPoints(), gameState.getSnakes(), gameState.getFood(), gameState.getMapSize(), gameState);

            // Генерация команды движения
            SnakeRequest moveRequest = movementService.buildMoveRequest(gameState);

            // Проверка на столкновения
            checkPotentialCollisions(gameState, moveRequest);

            // Отправка команды движения
            movementService.sendMoveRequest(moveRequest);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "[ERROR] Произошла непредвиденная ошибка: " + e.getMessage(), e);
        }
    }



    private void initializeSpawnPointsFile() {
        File file = new File(SPAWN_POINTS_FILE);
        needHeader = !file.exists();
        logger.info("spawn_points.csv: needHeader=" + needHeader + ", exists=" + file.exists());
    }

    private void checkSnakeStatusChanges(GameState gameState) {
        for (Snake snake : gameState.getSnakes()) {
            String currentStatus = snake.getStatus();
            String snakeId = snake.getId();

            if (!lastKnownStatus.containsKey(snakeId)) {
                lastKnownStatus.put(snakeId, currentStatus);
                if ("alive".equals(currentStatus)) {
                    recordSpawnCoordinates(snake);
                }
            } else {
                String oldStatus = lastKnownStatus.get(snakeId);
                if ("alive".equals(oldStatus) && "dead".equals(currentStatus)) {
                    logger.info("Змейка ID=" + snakeId + " умерла :(");
                }
                if ("dead".equals(oldStatus) && "alive".equals(currentStatus)) {
                    recordSpawnCoordinates(snake);
                }
                lastKnownStatus.put(snakeId, currentStatus);
            }
        }
    }

    private void recordSpawnCoordinates(Snake snake) {
        if (snake.getHead() == null) {
            logger.warning("У змейки " + snake.getId() + " нет координат головы для записи.");
            return;
        }

        long timestamp = System.currentTimeMillis();
        String line = snake.getId() + ","
                + snake.getHead().getX() + ","
                + snake.getHead().getY() + ","
                + snake.getHead().getZ() + ","
                + timestamp + "\n";

        try (FileWriter writer = new FileWriter(SPAWN_POINTS_FILE, true)) {
            if (needHeader) {
                writer.write("snakeId,x,y,z,timestamp\n");
                needHeader = false;
            }
            writer.write(line);
        } catch (IOException e) {
            logger.severe("Ошибка при записи координат возрождения для змейки " + snake.getId() + ": " + e.getMessage());
        }

        logger.info("Змейка ID=" + snake.getId() + " появилась (или возродилась) на координатах "
                + snake.getHead() + ". Записано в " + SPAWN_POINTS_FILE);
    }

    private void printSoupMessage(int pointsGained) {
        System.out.println();
        System.out.println("====================================================");
        System.out.println("           🎉 МАНДАРИН СЪЕДЕН! + " + pointsGained + " ОЧКОВ! 🎉           ");
        System.out.println("====================================================");
        System.out.println();
    }

    private void displayErrors(GameState gameState) {
        if (gameState.getErrors() != null && !gameState.getErrors().isEmpty()) {
            for (String err : gameState.getErrors()) {
                System.err.println("[GameState Error] " + err);
            }
        }
    }

    private void checkPotentialCollisions(GameState gameState, SnakeRequest moveRequest) {
        Set<Point3D> obstacles = gameState.getObstacles();
        StringBuilder collisionLog = new StringBuilder();
        boolean collisionDetected = false;

        for (var command : moveRequest.getSnakes()) {
            Snake snake = gameState.getSnakes().stream()
                    .filter(s -> s.getId().equals(command.getId()))
                    .findFirst()
                    .orElse(null);

            if (snake != null && "alive".equals(snake.getStatus())) {
                Point3D currentHead = snake.getHead();
                int[] direction = command.getDirection();

                // Новая позиция головы
                Point3D newHeadPosition = new Point3D(
                        currentHead.getX() + direction[0],
                        currentHead.getY() + direction[1],
                        currentHead.getZ() + direction[2]
                );

                // Проверяем, совпадает ли новая позиция с препятствием
                if (obstacles.contains(newHeadPosition)) {
                    collisionDetected = true;
                    collisionLog.append("Collision detected! Snake ID: ")
                            .append(command.getId())
                            .append(", New Position: ")
                            .append(newHeadPosition)
                            .append(", Obstacle Type: ")
                            .append(getObstacleType(newHeadPosition, gameState))
                            .append("\n");
                }
            }
        }

        if (collisionDetected) {
            // Логируем в консоль
            System.err.println(collisionLog);

            // Сохраняем в файл
            fileService.saveToFile(collisionLog.toString(), "data/collision_log.txt");
        }
    }
    private String getObstacleType(Point3D position, GameState gameState) {
        // Проверяем заборы
        if (gameState.getFences().stream()
                .anyMatch(f -> f.get(0) == position.getX() && f.get(1) == position.getY() && f.get(2) == position.getZ())) {
            return "Fence";
        }

        // Проверяем тело своих змей
        if (gameState.getSnakes().stream()
                .flatMap(s -> s.getGeometry().stream())
                .anyMatch(segment -> segment.get(0) == position.getX() &&
                        segment.get(1) == position.getY() &&
                        segment.get(2) == position.getZ())) {
            return "Own Snake Body";
        }

        // Проверяем тело врагов
        if (gameState.getEnemies().stream()
                .flatMap(e -> e.getGeometry().stream())
                .anyMatch(segment -> segment.get(0) == position.getX() &&
                        segment.get(1) == position.getY() &&
                        segment.get(2) == position.getZ())) {
            return "Enemy Snake Body";
        }

        // Проверяем окружение головы врагов
        for (var enemy : gameState.getEnemies()) {
            if (!enemy.getGeometry().isEmpty()) {
                List<Integer> head = enemy.getGeometry().get(0);
                Point3D headPoint = new Point3D(head.get(0), head.get(1), head.get(2));

                int[][] deltas = {
                        {1, 0, 0}, {-1, 0, 0},
                        {0, 1, 0}, {0, -1, 0},
                        {0, 0, 1}, {0, 0, -1}
                };

                for (int[] delta : deltas) {
                    Point3D surroundingPoint = new Point3D(
                            headPoint.getX() + delta[0],
                            headPoint.getY() + delta[1],
                            headPoint.getZ() + delta[2]
                    );

                    if (surroundingPoint.equals(position)) {
                        return "Enemy Head Surrounding";
                    }
                }
            }
        }

        return "Unknown Obstacle";
    }


}
