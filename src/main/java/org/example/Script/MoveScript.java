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

    // Константа максимального ускорения
    private static final double MAX_ACCELERATION = 10.0;

    // Константа радиуса поиска Bounty
    private static final double BOUNTY_SEARCH_RADIUS = 400.0;

    // Координаты fallback-цели (9000, 9000)
    private static final double FALLBACK_X = 9000.0;
    private static final double FALLBACK_Y = 9000.0;

    // Основной метод, который принимает объект GameState и возвращает массив Transport
    public static MoveResponse processGameState(GameState gameState) {
        List<TransportAction> transportActions = new ArrayList<>();

        for (TransportResponse transport : gameState.getTransports()) {
            // Проверяем статус транспорта (только для "alive")
            if (!"alive".equals(transport.getStatus())) {
                continue;
            }

            // Найдём все Bounty в радиусе 400 от транспорта
            List<Bounty> nearbyBounties = findNearbyBounties(transport, gameState.getBounties());

            Vector2D optimalAcceleration;

            if (!nearbyBounties.isEmpty()) {
                // Если есть Bounty, находим лучший и целимся к нему
                Bounty bestBounty = findBestBounty(transport, nearbyBounties);
                optimalAcceleration = calculateOptimalAcceleration(transport, bestBounty.getX(), bestBounty.getY());
            } else {
                // Если Bounty нет, движемся к координатам (9000, 9000)
                System.out.println("Нет Bounty в радиусе действия для транспорта с ID: " + transport.getId() + ". Двигаемся к (9000, 9000).");
                optimalAcceleration = calculateOptimalAcceleration(transport, FALLBACK_X, FALLBACK_Y);
            }

            // Создаём действие с рассчитанным ускорением
            TransportAction action = new TransportAction();
            action.setId(transport.getId());
            action.setAcceleration(optimalAcceleration);

            // Для простоты оставляем атаку и щит выключенными
            action.setActivateShield(false);

            // Добавляем действие в список
            transportActions.add(action);
        }

        // Возвращаем объект MoveResponse с действиями Transport
        MoveResponse moveResponse = new MoveResponse();
        moveResponse.setTransports(transportActions);

        return moveResponse;
    }

    // Метод для поиска Bounty в радиусе 400 от транспорта
    private static List<Bounty> findNearbyBounties(TransportResponse transport, List<Bounty> bounties) {
        List<Bounty> nearbyBounties = new ArrayList<>();

        for (Bounty bounty : bounties) {
            double distance = calculateDistance(transport.getX(), transport.getY(), bounty.getX(), bounty.getY());
            if (distance <= BOUNTY_SEARCH_RADIUS) {
                nearbyBounties.add(bounty);
            }
        }

        return nearbyBounties;
    }

    // Метод для расчета наилучшего Bounty (пока заглушка, можно улучшить)
    private static Bounty findBestBounty(TransportResponse transport, List<Bounty> bounties) {
        // Выбираем ближайший баунти с наибольшей ценностью
        Bounty bestBounty = null;
        double bestScore = Double.MIN_VALUE;

        for (Bounty bounty : bounties) {
            double distance = calculateDistance(transport.getX(), transport.getY(), bounty.getX(), bounty.getY());
            // Простой коэффициент: ценность / расстояние
            double score = bounty.getPoints() / distance;

            if (score > bestScore) {
                bestScore = score;
                bestBounty = bounty;
            }
        }

        return bestBounty;
    }

    // Метод для расчета оптимального ускорения к указанной цели (цель может быть как Bounty, так и (9000, 9000))
    private static Vector2D calculateOptimalAcceleration(TransportResponse transport, double targetX, double targetY) {
        // Вектор от транспорта к цели (или к (9000, 9000))
        double dx = targetX - transport.getX();
        double dy = targetY - transport.getY();

        // Длина вектора до цели
        double distanceToTarget = Math.sqrt(dx * dx + dy * dy);

        // Текущая скорость транспорта
        double velocityX = transport.getVelocity().getX();
        double velocityY = transport.getVelocity().getY();

        // Вектор скорости
        double velocityMagnitude = Math.sqrt(velocityX * velocityX + velocityY * velocityY);

        // Вектор до цели
        Vector2D targetDirection = new Vector2D(dx, dy);

        // Если движение уже направлено к цели
        if (velocityMagnitude > 0) {
            // Нормализуем вектор скорости
            Vector2D velocityDirection = new Vector2D(velocityX / velocityMagnitude, velocityY / velocityMagnitude);
            // Рассчитываем угол между текущей скоростью и направлением на цель
            double dotProduct = velocityDirection.getX() * targetDirection.getX() +
                    velocityDirection.getY() * targetDirection.getY();
            double angle = Math.acos(dotProduct / (distanceToTarget * velocityMagnitude));

            // Если угол велик, корректируем ускорение
            if (angle > Math.PI / 6) { // 30 градусов
                // Корректируем направление ускорения в сторону цели
                targetDirection = new Vector2D(
                        targetDirection.getX() - velocityDirection.getX(),
                        targetDirection.getY() - velocityDirection.getY());
            }
        }

        // Нормализуем вектор ускорения к максимальному значению
        double scalingFactor = MAX_ACCELERATION / distanceToTarget;
        double scaledDx = targetDirection.getX() * scalingFactor;
        double scaledDy = targetDirection.getY() * scalingFactor;

        return new Vector2D(scaledDx, scaledDy);
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
                System.out.println("-----");
            }

        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Ошибка при обработке данных: " + e.getMessage());
        }
    }
}
