package org.example;

import org.example.game.SnakeMovementController;

public class Main {
    public static void main(String[] args) {
        String baseUrl = "https://games-test.datsteam.dev/play/snake3d";
        String token = "a46f1665-024c-4742-a5c4-b38590830ca2";

        SnakeMovementController movementController = new SnakeMovementController(baseUrl, token);

        // Цикл для управления змейками
        while (true) {
            movementController.moveSnakesToNearestFood();

            // Задержка между тиками (например, 1 секунда)
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Цикл был прерван.");
                break;
            }
        }
    }
}
