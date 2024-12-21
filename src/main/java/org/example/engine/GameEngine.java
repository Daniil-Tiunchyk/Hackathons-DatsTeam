package org.example.engine;

import com.google.gson.Gson;
import org.example.models.GameState;
import org.example.models.Snake;
import org.example.models.SnakeRequest;
import org.example.service.FileService;
import org.example.service.FoodService;
import org.example.service.MovementService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/* ---------------------------------------------------
 * GameEngine — основной цикл игры
 * --------------------------------------------------- */
public class GameEngine {
    private static final Logger logger = Logger.getLogger(GameEngine.class.getName());

    private final MovementService movementService;
    private final FileService fileService;
    private final FoodService foodService;
    private final Gson gson;

    // Храним предыдущее количество очков — чтобы определить, были ли изменения (поедание мандаринов)
    private int previousPoints = -1;

    // Храним последний известный статус каждой змейки, чтобы отследить смену alive <-> dead
    private final Map<String, String> lastKnownStatus = new HashMap<>();

    // Файл, куда записываем координаты возрождения змей
    private static final String SPAWN_POINTS_FILE = "spawn_points.csv";

    // Флаг, указывающий нужно ли записать заголовок CSV (если файл ещё не существует)
    private boolean needHeader = false;

    public GameEngine(MovementService movementService,
                      FileService fileService,
                      FoodService foodService,
                      Gson gson) {
        this.movementService = movementService;
        this.fileService = fileService;
        this.foodService = foodService;
        this.gson = gson;

        // Инициализируем логику работы с файлом (определяем, нужно ли записать заголовок)
        initializeSpawnPointsFile();
    }

    /**
     * Выполняет один цикл игры (запрос состояния, вывод, генерация команд).
     */
    public void runGameCycle() {
        try {
            // 1. Получение карты (POST-запрос)
            String jsonResponse = movementService.fetchGameState();

            // 2. Запись карты в файл
            fileService.saveToFile(jsonResponse, "data/response.json");

            // 3. Парсинг карты
            GameState gameState = gson.fromJson(jsonResponse, GameState.class);

            // 4. Проверка очков (съедены ли мандарины)
            if (previousPoints != -1 && gameState.getPoints() > previousPoints) {
                // Змейка (или змея) съела мандарин, очки выросли
                printSoupMessage(gameState.getPoints() - previousPoints);
            }
            previousPoints = gameState.getPoints();

            // 5. Проверяем смену статуса змей (смерть/возрождение)
            checkSnakeStatusChanges(gameState);

            // 6. Вывод ошибок (если есть)
            displayErrors(gameState);

            // 7. Вывод игровой информации
            foodService.displayGameStateInfo(gameState);

            // 8. Генерация команды движения
            SnakeRequest moveRequest = movementService.buildMoveRequest(gameState);

            // 9. Отправка команды
            movementService.sendMoveRequest(moveRequest);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "[ERROR] Произошла непредвиденная ошибка: " + e.getMessage(), e);
        }
    }

    /**
     * Проверяем: если файл уже есть — будем его дополнять;
     * если нет — создаём и в следующий раз пишем заголовок CSV.
     */
    private void initializeSpawnPointsFile() {
        File file = new File(SPAWN_POINTS_FILE);
        if (!file.exists()) {
            // Файл не существует => надо будет записать заголовок
            needHeader = true;
        } else {
            // Файл уже существует => просто дополняем без заголовка
            needHeader = false;
        }
        logger.info("spawn_points.csv: needHeader=" + needHeader
                + ", exists=" + file.exists());
    }

    /**
     * Проверяем, не сменился ли статус змей (alive <-> dead).
     * Если змейка возродилась — записываем координаты головы в файл.
     * Если змейка умерла — выводим сообщение в лог.
     */
    private void checkSnakeStatusChanges(GameState gameState) {
        for (Snake snake : gameState.getSnakes()) {
            String currentStatus = snake.getStatus(); // "alive" или "dead"
            String snakeId = snake.getId();

            // Если раньше мы не видели эту змею, значит это её первое появление
            if (!lastKnownStatus.containsKey(snakeId)) {
                lastKnownStatus.put(snakeId, currentStatus);
                // Если она сразу "alive", запишем координаты первого появления
                if ("alive".equals(currentStatus)) {
                    recordSpawnCoordinates(snake);
                }
            } else {
                String oldStatus = lastKnownStatus.get(snakeId);

                // Если была alive и стала dead => змея умерла
                if ("alive".equals(oldStatus) && "dead".equals(currentStatus)) {
                    logger.info("Змейка ID=" + snakeId + " умерла :(");
                }

                // Если была dead и стала alive => змея возродилась
                if ("dead".equals(oldStatus) && "alive".equals(currentStatus)) {
                    recordSpawnCoordinates(snake);
                }

                // Обновляем статус
                lastKnownStatus.put(snakeId, currentStatus);
            }
        }
    }

    /**
     * Записываем координаты головы змейки в CSV-файл (момент возрождения или первого появления).
     */
    private void recordSpawnCoordinates(Snake snake) {
        if (snake.getHead() == null) {
            logger.warning("У змейки " + snake.getId() + " нет координат головы для записи.");
            return;
        }

        // Формируем строку CSV
        long timestamp = System.currentTimeMillis();
        String line = snake.getId() + ","
                + snake.getHead().getX() + ","
                + snake.getHead().getY() + ","
                + snake.getHead().getZ() + ","
                + timestamp
                + "\n";

        // Дописываем в файл
        try (FileWriter writer = new FileWriter(SPAWN_POINTS_FILE, true)) {
            // Если нужно заголовок — пишем и сбрасываем needHeader=false
            if (needHeader) {
                writer.write("snakeId,x,y,z,timestamp\n");
                needHeader = false;
            }
            writer.write(line);
        } catch (IOException e) {
            logger.severe("Ошибка при записи координат возрождения для змейки "
                    + snake.getId() + ": " + e.getMessage());
        }

        logger.info("Змейка ID=" + snake.getId() + " появилась (или возродилась) на координатах "
                + snake.getHead() + ". Записано в " + SPAWN_POINTS_FILE);
    }

    /**
     * Тут выводим «особое» сообщение при помощи условного «soup».
     * Можно оформить ASCII-баннером, логом и т.д. — по вашему вкусу.
     */
    private void printSoupMessage(int pointsGained) {
        // Пример текстового «баннера»:
        System.out.println();
        System.out.println("====================================================");
        System.out.println("     Э К С П Р Е С С   (soup)   А Л Е Р Т           ");
        System.out.println("====================================================");
        System.out.println("        М А Н Д А Р И Н   С Ъ Е Д Е Н  !!!          ");
        System.out.println("               + " + pointsGained + " очков!                ");
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
}
