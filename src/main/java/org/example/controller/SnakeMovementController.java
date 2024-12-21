package org.example.controller;

import org.example.models.GameState;
import org.example.models.SnakeRequest;
import org.example.service.FileService;
import org.example.service.FoodService;
import org.example.service.MovementService;

import com.google.gson.Gson;

import java.io.IOException;

public class SnakeMovementController {
    private final String baseUrl;
    private final String token;
    private final Gson gson;
    private final FileService fileService;
    private final FoodService foodService;
    private final MovementService movementService;

    public SnakeMovementController(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.gson = new Gson();
        this.fileService = new FileService();
        this.foodService = new FoodService();
        this.movementService = new MovementService(baseUrl, token, gson, fileService);
    }

    public void runGameCycle() {
        try {
            // 1. Отправка POST-запроса для получения карты
            String jsonResponse = movementService.fetchGameState();

            // 2. Запись карты в файл
            fileService.saveToFile(jsonResponse, "data/response.json");

            // 3. Парсинг карты
            GameState gameState = gson.fromJson(jsonResponse, GameState.class);

            // 4. Вывод ошибок
            displayErrors(gameState);

            // 5. Вывод игровой информации
            foodService.displayGameStateInfo(gameState);

            // 6. Генерация команды движения
            SnakeRequest moveRequest = movementService.buildMoveRequest(gameState);

            // 7. Отправка команд
            movementService.sendMoveRequest(moveRequest);

        } catch (IOException e) {
            System.err.println("[ERROR] Ошибка ввода-вывода: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ERROR] Произошла ошибка: " + e.getMessage());
        }
    }

    private void displayErrors(GameState gameState) {
        if (gameState.getErrors() != null && !gameState.getErrors().isEmpty()) {
            System.err.println("[ERRORS] Найдены ошибки в игровом состоянии:");
            for (String error : gameState.getErrors()) {
                System.err.println(" - " + error);
            }
        } else {
            System.out.println("[INFO] Ошибок не обнаружено.");
        }
    }

}
