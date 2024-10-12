package org.example.Script;

import org.example.models.move.*;

import java.util.ArrayList;
import java.util.List;

public class TransportController {

    private static final Vector2D TARGET_POINT = new Vector2D(5000, 5000); // Целевая точка 5000:5000

    public MoveResponse planTransportMovements(GameState gameState) {
        List<TransportAction> commands = new ArrayList<>();

        // Для каждого ковра
        for (TransportResponse transport : gameState.getTransports()) {
            if (!"alive".equals(transport.getStatus())) {
                continue; // Пропускаем уничтоженные ковры
            }

            // Используем готовое значение ускорения от аномалий
            Vector2D providedAnomalyAcceleration = transport.getAnomalyAcceleration();

            // Шаг 2: Выбираем наиболее оптимальный Bounty или двигаемся к точке 5000:5000
            Bounty targetBounty = selectOptimalBounty(transport, gameState);

            Vector2D targetPosition;
            if (targetBounty != null) {
                // Если найден баунти, целевая позиция - это позиция баунти
                targetPosition = targetBounty.getPosition();
            } else {
                // Если баунти нет, целевая позиция - точка 5000:5000
                targetPosition = TARGET_POINT;
            }

            // Шаг 3: Вычисляем желаемое ускорение в направлении цели
            Vector2D desiredAcceleration = calculateDesiredAcceleration(transport, targetPosition, providedAnomalyAcceleration, gameState.getMaxAccel());

            // Шаг 4: Создаем команду для ковра
            TransportAction command = new TransportAction();
            command.setId(transport.getId());
            command.setAcceleration(desiredAcceleration);

            commands.add(command);
        }

        // Возвращаем объект MoveResponse с командами для ковров
        MoveResponse response = new MoveResponse();
        response.setTransports(commands);
        return response;
    }

    private Bounty selectOptimalBounty(TransportResponse transport, GameState gameState) {
        Bounty bestBounty = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (Bounty bounty : gameState.getBounties()) {
            // Пропускаем баунти, которые слишком близки к аномалиям (для безопасности)
            if (isNearAnomaly(bounty.getPosition(), gameState.getAnomalies(), gameState.getTransportRadius())) {
                continue;
            }

            // Оцениваем время для достижения баунти
            double estimatedTime = estimateTimeToReach(transport, bounty.getPosition(), gameState);

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

    private double estimateTimeToReach(TransportResponse transport, Vector2D targetPosition, GameState gameState) {
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

    private Vector2D calculateDesiredAcceleration(TransportResponse transport, Vector2D targetPosition, Vector2D anomalyAcceleration, double maxAccel) {
        Vector2D position = transport.getPosition();
        Vector2D velocity = transport.getVelocity();
        Vector2D toTarget = targetPosition.subtract(position);

        // Направление к цели
        Vector2D desiredDirection = toTarget.normalize();

        // Желаемая скорость (стремимся достичь максимального ускорения)
        Vector2D desiredVelocity = desiredDirection.scale(maxAccel);

        // Разница скоростей
        Vector2D velocityDifference = desiredVelocity.subtract(velocity);

        // Требуемое ускорение (предполагаем dt = 1с)
        Vector2D requiredAcceleration = velocityDifference;

        // Компенсируем ускорение от аномалий (используем предоставленное значение)
        Vector2D controlAcceleration = requiredAcceleration.subtract(anomalyAcceleration);

        // Ограничиваем ускорение до максимального значения
        if (controlAcceleration.magnitude() > maxAccel) {
            controlAcceleration = controlAcceleration.normalize().scale(maxAccel);
        }

        return controlAcceleration;
    }
}
