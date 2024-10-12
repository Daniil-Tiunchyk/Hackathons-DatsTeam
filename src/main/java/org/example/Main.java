package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.POST.Attack;
import org.example.Script.TransportController;
import org.example.models.move.GameState;
import org.example.models.move.MoveResponse;
import org.example.models.move.TransportAction;
import org.example.models.move.Vector2D;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        String filePath = System.getProperty("user.dir") + "/response.json";

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));

            Gson gson = new GsonBuilder().create();

            GameState gameState = gson.fromJson(jsonContent, GameState.class);

            TransportController transportController = new TransportController();

            // Обработка состояния игры
            MoveResponse response = transportController.planTransportMovements(gameState);

            // Вывод результата в консоль
            System.out.println("Ответ на действия ковров:");

            for (TransportAction action : response.getTransports()) {
                System.out.println("ID ковра: " + action.getId());
                Vector2D acc = action.getAcceleration();
                if (acc != null) {
                    System.out.println("Ускорение: (" + acc.getX() + ", " + acc.getY() + ")");
                } else {
                    System.out.println("Ускорение: нет данных");
                }
                Attack attack = action.getAttack();
                if (attack != null) {
                    System.out.println("Атака по координатам: (" + attack.getX() + ", " + attack.getY() + ")");
                } else {
                    System.out.println("Атака: нет");
                }
                System.out.println("Активировать щит: " + (action.isActivateShield() ? "Да" : "Нет"));
                System.out.println("-----");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка при обработке данных: " + e.getMessage());
        }
    }
}
