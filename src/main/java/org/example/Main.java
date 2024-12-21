package org.example;

import com.google.gson.Gson;
import org.example.engine.GameEngine;
import org.example.service.FileService;
import org.example.service.FoodService;
import org.example.service.MovementService;
import org.example.service.PathFindingService;

public class Main {
    public static void main(String[] args) {
        // 1. Создаем необходимые сервисы
        Gson gson = new Gson();
        FileService fileService = new FileService();
        FoodService foodService = new FoodService();
        PathFindingService pathFindingService = new PathFindingService();

        // 2. Конфигурируем MovementService
        //    К примеру, вот такой URL и token (можно менять под вашу игру)
        String baseUrl = "https://games.datsteam.dev/play/snake3d/player/move";
        String token = "a46f1665-024c-4742-a5c4-b38590830ca2";

        MovementService movementService = new MovementService(
                baseUrl,
                token,
                gson,
                fileService,
                pathFindingService,
                foodService
        );

        // 3. Создаем GameEngine
        GameEngine gameEngine = new GameEngine(
                movementService,
                fileService,
                foodService,
                gson
        );

        // 4. Запускаем игровой цикл
        while (true) {
            gameEngine.runGameCycle();

            // Задержка между тиками
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("[ERROR] Цикл прерван: " + e.getMessage());
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
