package org.example.Script;


import org.example.models.move.*;

import java.util.ArrayList;
import java.util.List;

public class TransportController {

    private static final double EPSILON = 1e-6;

    public MoveResponse planTransportMovements(GameState gameState) {
        List<TransportAction> commands = new ArrayList<>();

        // Для каждого ковра
        for (TransportResponse transport : gameState.getTransports()) {
            if (!"alive".equals(transport.getStatus())) {
                continue; // Пропускаем уничтоженные ковры
            }

            // Шаг 1: Вычисляем суммарное ускорение от аномалий в текущей позиции
            Vector2D position = transport.getPosition();
            Vector2D anomalyAcceleration = calculateAnomalyAcceleration(position, gameState.getAnomalies());

            // Шаг 2: Выбираем наиболее оптимальный Bounty
            Bounty targetBounty = selectOptimalBounty(transport, gameState, anomalyAcceleration);

            if (targetBounty != null) {
                // Шаг 3: Вычисляем желаемое ускорение в направлении баунти
                Vector2D desiredAcceleration = calculateDesiredAcceleration(transport, targetBounty, anomalyAcceleration, gameState.getMaxAccel());

                // Шаг 4: Создаем команду для ковра
                TransportAction command = new TransportAction();
                command.setId(transport.getId());
                command.setAcceleration(desiredAcceleration);

                commands.add(command);
            }
        }

        // Возвращаем объект MoveResponse с командами для ковров
        MoveResponse response = new MoveResponse();
        response.setTransports(commands);
        return response;
    }

    private Vector2D calculateAnomalyAcceleration(Vector2D position, List<Anomaly> anomalies) {
        Vector2D netAcceleration = new Vector2D(0, 0);

        for (Anomaly anomaly : anomalies) {
            Vector2D anomalyPosition = anomaly.getPosition();
            double dx = anomalyPosition.getX() - position.getX();
            double dy = anomalyPosition.getY() - position.getY();
            double distanceSquared = dx * dx + dy * dy;

            if (distanceSquared < EPSILON) {
                continue; // Избегаем деления на ноль
            }

            double distance = Math.sqrt(distanceSquared);
            if (distance > anomaly.getEffectiveRadius()) {
                continue; // Вне зоны действия аномалии
            }

            // Вычисляем величину ускорения от аномалии
            double strength = anomaly.getStrength();
            double accelerationMagnitude = (strength * strength) / distanceSquared;
            if (strength < 0) {
                accelerationMagnitude = -accelerationMagnitude; // Корректируем для отталкивающих аномалий
            }

            // Направление к центру аномалии
            Vector2D direction = new Vector2D(dx / distance, dy / distance);
            Vector2D acceleration = direction.scale(accelerationMagnitude);

            netAcceleration = netAcceleration.add(acceleration);
        }

        return netAcceleration;
    }

    private Bounty selectOptimalBounty(TransportResponse transport, GameState gameState, Vector2D anomalyAcceleration) {
        Bounty bestBounty = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Bounty bounty : gameState.getBounties()) {
            // Пропускаем баунти, которые слишком близки к аномалиям (для безопасности)
            if (isNearAnomaly(bounty.getPosition(), gameState.getAnomalies(), gameState.getTransportRadius())) {
                continue;
            }

            // Оцениваем время для достижения баунти
            double estimatedTime = estimateTimeToReach(transport, bounty.getPosition(), anomalyAcceleration, gameState);

            if (estimatedTime <= 0) {
                continue; // Невозможно достичь этот баунти
            }

            // Вычисляем оценку (например, ценность за единицу времени)
            double score = bounty.getPoints() / estimatedTime;

            if (score > bestScore) {
                bestScore = score;
                bestBounty = bounty;
            }
        }

        return bestBounty;
    }

    private double estimateTimeToReach(TransportResponse transport, Vector2D targetPosition, Vector2D anomalyAcceleration, GameState gameState) {
        // Упрощенная оценка: движение по прямой при максимальном ускорении
        Vector2D position = transport.getPosition();
        Vector2D deltaPosition = targetPosition.subtract(position);
        double distance = deltaPosition.magnitude();

        // Текущая скорость
        Vector2D velocity = transport.getVelocity();
        double currentSpeed = velocity.magnitude();

        // Максимальное ускорение и скорость
        double maxAcceleration = gameState.getMaxAccel();
        double maxSpeed = gameState.getMaxSpeed();

        // Упрощенная оценка времени при постоянном ускорении
        double timeToMaxSpeed = (maxSpeed - currentSpeed) / maxAcceleration;
        double distanceToMaxSpeed = (currentSpeed + maxSpeed) / 2 * timeToMaxSpeed;

        if (distanceToMaxSpeed >= distance) {
            // Можем достичь цели до достижения максимальной скорости
            double time = (-currentSpeed + Math.sqrt(currentSpeed * currentSpeed + 2 * maxAcceleration * distance)) / maxAcceleration;
            return time;
        } else {
            // Время для преодоления оставшегося расстояния на максимальной скорости
            double remainingDistance = distance - distanceToMaxSpeed;
            double timeAtMaxSpeed = remainingDistance / maxSpeed;
            return timeToMaxSpeed + timeAtMaxSpeed;
        }
    }

    private boolean isNearAnomaly(Vector2D position, List<Anomaly> anomalies, double safetyRadius) {
        for (Anomaly anomaly : anomalies) {
            Vector2D anomalyPosition = anomaly.getPosition();
            double dx = anomalyPosition.getX() - position.getX();
            double dy = anomalyPosition.getY() - position.getY();
            double distanceSquared = dx * dx + dy * dy;
            double safeDistance = anomaly.getRadius() + safetyRadius;

            if (distanceSquared <= safeDistance * safeDistance) {
                return true; // Слишком близко к аномалии
            }
        }
        return false;
    }

    private Vector2D calculateDesiredAcceleration(TransportResponse transport, Bounty targetBounty, Vector2D anomalyAcceleration, double maxAccel) {
        Vector2D position = transport.getPosition();
        Vector2D velocity = transport.getVelocity();
        Vector2D toTarget = targetBounty.getPosition().subtract(position);

        // Направление к цели
        Vector2D desiredDirection = toTarget.normalize();

        // Желаемая скорость (стремимся достичь максимального ускорения)
        Vector2D desiredVelocity = desiredDirection.scale(maxAccel);

        // Разница скоростей
        Vector2D velocityDifference = desiredVelocity.subtract(velocity);

        // Требуемое ускорение (предполагаем dt = 1с)
        Vector2D requiredAcceleration = velocityDifference;

        // Компенсируем ускорение от аномалий
        Vector2D controlAcceleration = requiredAcceleration.subtract(anomalyAcceleration);

        // Ограничиваем ускорение до максимального значения
        if (controlAcceleration.magnitude() > maxAccel) {
            controlAcceleration = controlAcceleration.normalize().scale(maxAccel);
        }

        return controlAcceleration;
    }
}