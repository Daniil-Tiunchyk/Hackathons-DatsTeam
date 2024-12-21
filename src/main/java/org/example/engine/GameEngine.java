package org.example.engine;

import com.google.gson.Gson;
import org.example.models.GameState;
import org.example.service.FileService;
import org.example.service.FoodService;
import org.example.service.MovementService;

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

    public GameEngine(MovementService movementService,
                      FileService fileService,
                      FoodService foodService,
                      Gson gson) {
        this.movementService = movementService;
        this.fileService = fileService;
        this.foodService = foodService;
        this.gson = gson;
    }

    // Храним предыдущее количество очков — чтобы определить, были ли изменения
    private int previousPoints = -1;

    public void runGameCycle() {
        try {
            // 1. Получение карты (POST-запрос)
            String jsonResponse = movementService.fetchGameState();

            // 2. Запись карты в файл
            fileService.saveToFile(jsonResponse, "data/response.json");

            // 3. Парсинг карты
            GameState gameState = gson.fromJson(jsonResponse, GameState.class);

            // 4. Проверяем, изменилось ли количество очков
            if (previousPoints != -1 && gameState.getPoints() > previousPoints) {
                // Змейка (или змея) съела мандарин, очки выросли
                printSoupMessage(gameState.getPoints() - previousPoints);
            }

            // Запоминаем текущее количество очков
            previousPoints = gameState.getPoints();

            // 5. Вывод ошибок (если есть)
            displayErrors(gameState);

            // 6. Вывод игровой информации
            foodService.displayGameStateInfo(gameState);

            // 7. Генерация команды движения
            var moveRequest = movementService.buildMoveRequest(gameState);

            // 8. Отправка команды
            movementService.sendMoveRequest(moveRequest);

        } catch (Exception e) {
            e.printStackTrace();
        }
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
