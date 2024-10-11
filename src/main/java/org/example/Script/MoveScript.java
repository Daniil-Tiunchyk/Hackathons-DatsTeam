package org.example.Script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.move.GameState;
import org.example.models.move.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MoveScript {

    // Основной метод, который принимает объект GameState и возвращает массив Transport
    public static MoveResponse processGameState(GameState gameState) {
        List<TransportAction> transportActions = new ArrayList<>();

        for (Transport transport : gameState.getTransports()) {
            // Заглушку - действия для транспорта
            TransportAction action = new TransportAction();
            action.setId(transport.getId());

            // Заглушка для ускорения - берём текущее ускорение транспорта
            action.setAcceleration(new Vector2D(1.2, 1.2));

            // Заглушка для атаки - выбираем точку для атаки
            action.setAttack(new Vector2D(1, 1));

            // Заглушка для активации щита
            action.setActivateShield(true);

            transportActions.add(action);
        }

        // Возвращаем объект MoveResponse с действиями Transport
        MoveResponse moveResponse = new MoveResponse();
        moveResponse.setTransports(transportActions);

        return moveResponse;
    }

    // Метод для тестирования
    public static void main(String[] args) {
        String filePath = System.getProperty("user.dir") + "/response.json";

        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));

            Gson gson = new GsonBuilder().create();

            GameState gameState = gson.fromJson(jsonContent, GameState.class);

            // Обработка состояния игры
            MoveResponse response = processGameState(gameState);

            // Вывод результата в консоль
            System.out.println("Ответ на действия транспорта:");
            for (TransportAction action : response.getTransports()) {
                System.out.println("ID транспорта: " + action.getId());
                System.out.println("Ускорение: (" + action.getAcceleration().getX() + ", " + action.getAcceleration().getY() + ")");
                System.out.println("Атака: (" + action.getAttack().getX() + ", " + action.getAttack().getY() + ")");
                System.out.println("Активировать щит: " + action.isActivateShield());
                System.out.println("-----");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка при обработке данных: " + e.getMessage());
        }
    }
}
