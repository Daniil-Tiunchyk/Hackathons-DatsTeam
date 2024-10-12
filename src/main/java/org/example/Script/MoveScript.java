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

    // Константа радиуса поиска Bounty
    private static final double BOUNTY_SEARCH_RADIUS = 400.0;

    // Основной метод, который принимает объект GameState и возвращает массив Transport
    public static MoveResponse processGameState(GameState gameState) {
        List<TransportAction> transportActions = new ArrayList<>();

        for (Transport transport : gameState.getTransports()) {
            // Проверяем статус транспорта (только для "alive")
            if (!"alive".equals(transport.getStatus())) {
                continue;
            }

            // Вывод начальной информации по каждому транспорту
            System.out.println("Транспорт ID: " + transport.getId());
            System.out.println("Координаты: (" + transport.getX() + ", " + transport.getY() + ")");
            System.out.println("Ускорение от аномалии: (" + transport.getAnomalyAcceleration().getX() + ", " + transport.getAnomalyAcceleration().getY() + ")");
            System.out.println("Собственное ускорение: (" + transport.getSelfAcceleration().getX() + ", " + transport.getSelfAcceleration().getY() + ")");
            System.out.println("Скорость: (" + transport.getVelocity().getX() + ", " + transport.getVelocity().getY() + ")");
            System.out.println("Статус: " + transport.getStatus());

            // Найдём все Bounty в радиусе 400 от транспорта
            List<Bounty> nearbyBounties = findNearbyBounties(transport, gameState.getBounties());

            // Вывод найденных Bounty
            if (!nearbyBounties.isEmpty()) {
                System.out.println("Bounty в радиусе " + BOUNTY_SEARCH_RADIUS + ":");
                for (Bounty bounty : nearbyBounties) {
                    System.out.println("Bounty Points: " + bounty.getPoints());
                    System.out.println("Координаты: (" + bounty.getX() + ", " + bounty.getY() + ")");
                    System.out.println("Радиус Bounty: " + bounty.getRadius());
                    System.out.println("-----");
                }
            } else {
                System.out.println("В радиусе " + BOUNTY_SEARCH_RADIUS + " нет Bounty.");
            }

            System.out.println("=====\n");

            // Заглушка для действий транспорта
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

    // Метод для поиска Bounty в радиусе 400 от транспорта
    private static List<Bounty> findNearbyBounties(Transport transport, List<Bounty> bounties) {
        List<Bounty> nearbyBounties = new ArrayList<>();

        for (Bounty bounty : bounties) {
            // Рассчитываем Евклидово расстояние между транспортом и Bounty
            double distance = calculateDistance(transport.getX(), transport.getY(), bounty.getX(), bounty.getY());

            // Если Bounty находится в радиусе BOUNTY_SEARCH_RADIUS
            if (distance <= BOUNTY_SEARCH_RADIUS) {
                nearbyBounties.add(bounty);
            }
        }

        return nearbyBounties;
    }

    // Метод для расчета Евклидова расстояния между двумя точками (x1, y1) и (x2, y2)
    private static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
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
