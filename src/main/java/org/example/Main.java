package org.example;

import org.example.game.SnakeMovementController;

public class Main {
    public static void main(String[] args) {
        SnakeMovementController controller = new SnakeMovementController(
                "https://games-test.datsteam.dev/play/snake3d",
                "a46f1665-024c-4742-a5c4-b38590830ca2"
        );

//        while (true) {
            controller.moveSnakesToNearestFood();

            // Задержка между тиками
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("[ERROR] Цикл прерван: " + e.getMessage());
//                break;
            }
//        }
    }
}
