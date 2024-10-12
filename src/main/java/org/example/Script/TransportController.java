package org.example.Script;

import org.example.models.move.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TransportController {

    private static final Vector2D TARGET_POINT = new Vector2D(5000, 5000); // Целевая точка

    public MoveResponse planTransportMovements(GameState gameState) {
        List<TransportAction> commands = new ArrayList<>();

        // Для каждого ковра
        for (TransportResponse transport : gameState.getTransports()) {
            if (!"alive".equals(transport.getStatus())) {
                continue; // Пропускаем уничтоженные ковры
            }

            // Используем готовое значение ускорения от аномалий
            Vector2D providedAnomalyAcceleration = transport.getAnomalyAcceleration();

            // Построение маршрута для сбора всех возможных баунти
            List<Bounty> route = buildGreedyRoute(transport, gameState);

            Vector2D targetPosition;
            if (!route.isEmpty()) {
                // Если есть баунти на маршруте, выбираем ближайший
                targetPosition = route.get(0).getPosition(); // Первая цель на маршруте
            } else {
                // Если баунти нет, целевая позиция — точка 5000:5000
                targetPosition = TARGET_POINT;
            }

            // Вычисляем желаемое ускорение в направлении цели
            Vector2D desiredAcceleration = calculateDesiredAcceleration(transport, targetPosition, providedAnomalyAcceleration, gameState.getMaxAccel());

            // Создаем команду для ковра
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

    /**
     * Построение жадного маршрута для сбора всех баунти.
     */
    private List<Bounty> buildGreedyRoute(TransportResponse transport, GameState gameState) {
        Set<Bounty> remainingBounties = new HashSet<>(gameState.getBounties());
        List<Bounty> route = new ArrayList<>();

        Vector2D currentPosition = transport.getPosition();
        Vector2D currentVelocity = transport.getVelocity();

        while (!remainingBounties.isEmpty()) {
            Bounty closestBounty = null;
            double minTime = Double.MAX_VALUE;

            // Ищем ближайший баунти (по времени достижения)
            for (Bounty bounty : remainingBounties) {
                if (isNearAnomaly(bounty.getPosition(), gameState.getAnomalies(), gameState.getTransportRadius())) {
                    continue; // Пропускаем баунти, которые слишком близки к аномалиям
                }

                double estimatedTime = estimateTimeToReach(currentPosition, currentVelocity, bounty.getPosition(), gameState);

                if (estimatedTime < minTime) {
                    minTime = estimatedTime;
                    closestBounty = bounty;
                }
            }

            if (closestBounty != null) {
                // Добавляем ближайший баунти в маршрут
                route.add(closestBounty);
                remainingBounties.remove(closestBounty);
                // Обновляем текущую позицию
                currentPosition = closestBounty.getPosition();
            } else {
                // Если не осталось доступных баунти, выходим
                break;
            }
        }

        return route;
    }

    /**
     * Оценка времени достижения цели, учитывая текущее ускорение и скорость транспорта.
     */
    private double estimateTimeToReach(Vector2D currentPosition, Vector2D currentVelocity, Vector2D targetPosition, GameState gameState) {
        Vector2D deltaPosition = targetPosition.subtract(currentPosition);
        double distance = deltaPosition.magnitude();

        // Максимальное ускорение и скорость
        double maxAcceleration = gameState.getMaxAccel();
        double maxSpeed = gameState.getMaxSpeed();

        // Направление к цели
        Vector2D directionToTarget = deltaPosition.normalize();

        // Проекция скорости на направление к цели
        double velocityInTargetDirection = currentVelocity.dot(directionToTarget);

        // Теперь определим, как быстро транспорт может ускориться к цели:
        if (velocityInTargetDirection >= maxSpeed) {
            // Если уже движемся быстрее или на максимальной скорости — просто время = расстояние / скорость
            return distance / velocityInTargetDirection;
        }

        // Шаг 1: Время для достижения максимальной скорости
        double timeToMaxSpeed = (maxSpeed - velocityInTargetDirection) / maxAcceleration;

        // Шаг 2: Расстояние, которое мы пройдем за время разгона до максимальной скорости
        double distanceToMaxSpeed = velocityInTargetDirection * timeToMaxSpeed + 0.5 * maxAcceleration * Math.pow(timeToMaxSpeed, 2);

        if (distanceToMaxSpeed >= distance) {
            // Если можем достичь цели до того, как наберем максимальную скорость
            double time = (-velocityInTargetDirection + Math.sqrt(velocityInTargetDirection * velocityInTargetDirection + 2 * maxAcceleration * distance)) / maxAcceleration;
            return time;
        } else {
            // Если не можем достичь цели до достижения максимальной скорости — учитываем движение на максимальной скорости
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
