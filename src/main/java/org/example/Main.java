package org.example;

import org.example.controller.SnakeMovementController;

public class Main {
    public static void main(String[] args) {
        SnakeMovementController controller = new SnakeMovementController(
                "https://games-test.datsteam.dev/play/snake3d/player/move",
                "a46f1665-024c-4742-a5c4-b38590830ca2"
        );

        // Запуск цикла
        while (true) {
            controller.runGameCycle();

            // Задержка между тиками
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("[ERROR] Цикл прерван: " + e.getMessage());
                break;
            }
        }
    }
}
