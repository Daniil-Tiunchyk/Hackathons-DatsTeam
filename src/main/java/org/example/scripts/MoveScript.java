package org.example.scripts;

import org.example.models.move.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoveScript {

    private static final Vector2D TARGET_POINT = new Vector2D(5000, 5000); // Целевая точка

    public MoveResponse planTransportMovements(GameState gameState) {
        List<TransportAction> commands = new ArrayList<>();

        // Заголовок таблицы
        System.out.printf("%-10s %-25s %-25s %-25s %-25s %-25s\n",
                "ID",
                "Current Position",
                "Current Velocity",
                "Self Acceleration",
                "Target Position",
                "Desired Acceleration");
        System.out.println("------------------------------------------------------------------------------------------------------------");

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

            // Используем текущее ускорение из TransportResponse
            Vector2D selfAcceleration = transport.getSelfAcceleration();

            // Вычисляем желаемое ускорение в направлении цели
            Vector2D desiredAcceleration = calculateDesiredAcceleration(transport, targetPosition, providedAnomalyAcceleration, gameState.getMaxAccel());

            // Проверяем, не приведет ли желаемое ускорение к столкновению
            if (willCollide(transport, desiredAcceleration, gameState)) {
                // Если столкновение возможно, корректируем ускорение
                desiredAcceleration = calculateSafeAcceleration(transport, targetPosition, providedAnomalyAcceleration, gameState);
            }

            // Создаем команду для ковра
            TransportAction command = new TransportAction();
            command.setId(transport.getId());
            command.setAcceleration(desiredAcceleration);
            commands.add(command);

            // Выводим информацию о текущем транспортном средстве в виде строки таблицы
            System.out.printf("%-10s %-25s %-25s %-25s %-25s %-25s\n",
                    transport.getId(),
                    formatVector(transport.getPosition()),
                    formatVector(transport.getVelocity()),
                    formatVector(selfAcceleration),
                    formatVector(targetPosition),
                    formatVector(desiredAcceleration)
            );
        }

        // Возвращаем объект MoveResponse с командами для ковров
        MoveResponse response = new MoveResponse();
        response.setTransports(commands);
        return response;
    }

    /**
     * Форматирует вектор для вывода в виде строки.
     */
    private String formatVector(Vector2D vector) {
        return String.format("(%.2f, %.2f)", vector.getX(), vector.getY());
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

                double estimatedTime = estimateTimeToReach(currentPosition, currentVelocity, bounty.getPosition(), bounty.getRadius(), gameState);

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
     * Учитываем радиус баунти.
     */
    private double estimateTimeToReach(Vector2D currentPosition, Vector2D currentVelocity, Vector2D targetPosition, double targetRadius, GameState gameState) {
        Vector2D deltaPosition = targetPosition.subtract(currentPosition);
        double distance = deltaPosition.magnitude();

        // Учитываем радиус баунти
        distance -= targetRadius;

        if (distance <= 0) {
            return 0; // Если уже находимся в радиусе баунти
        }

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

    /**
     * Проверяет, приведет ли текущее ускорение к столкновению с границами карты, коврами или аномалиями.
     */
    private boolean willCollide(TransportResponse transport, Vector2D desiredAcceleration, GameState gameState) {
        Vector2D predictedPosition = predictPosition(transport, desiredAcceleration);

        // Проверка на границы карты
        if (isOutOfBounds(predictedPosition, gameState.getMapSize())) {
            return true;
        }

        // Проверка на другие ковры
        for (TransportResponse other : gameState.getTransports()) {
            if (!other.getId().equals(transport.getId()) && "alive".equals(other.getStatus())) {
                if (isCollision(predictedPosition, other.getPosition(), gameState.getTransportRadius())) {
                    return true;
                }
            }
        }

        // Проверка на аномалии
        for (Anomaly anomaly : gameState.getAnomalies()) {
            if (isNearAnomaly(predictedPosition, anomaly, gameState.getTransportRadius())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Вычисляет желаемое ускорение к цели, учитывая ограничение на максимальное ускорение.
     */
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

    /**
     * Вычисляет безопасное ускорение, избегая столкновений.
     */
    private Vector2D calculateSafeAcceleration(TransportResponse transport, Vector2D targetPosition, Vector2D anomalyAcceleration, GameState gameState) {
        Vector2D safeAcceleration = calculateDesiredAcceleration(transport, targetPosition, anomalyAcceleration, gameState.getMaxAccel());

        // Если безопасное ускорение все еще ведет к столкновению, уменьшаем его
        while (willCollide(transport, safeAcceleration, gameState)) {
            safeAcceleration = safeAcceleration.scale(0.9); // Постепенно уменьшаем ускорение
        }

        return safeAcceleration;
    }

    /**
     * Прогнозирует следующую позицию ковра на основе текущего ускорения.
     */
    private Vector2D predictPosition(TransportResponse transport, Vector2D acceleration) {
        Vector2D currentPosition = transport.getPosition();
        Vector2D currentVelocity = transport.getVelocity();
        return currentPosition.add(currentVelocity).add(acceleration); // Предполагаем, что dt = 1 сек
    }

    /**
     * Проверяет, находится ли позиция за границами карты.
     */
    private boolean isOutOfBounds(Vector2D position, MapSize mapSize) {
        return position.getX() < 0 || position.getY() < 0 || position.getX() > mapSize.getX() || position.getY() > mapSize.getY();
    }

    /**
     * Проверяет столкновение двух ковров.
     */
    private boolean isCollision(Vector2D pos1, Vector2D pos2, double radius) {
        return pos1.subtract(pos2).magnitude() <= 2 * radius;
    }

    /**
     * Проверяет, находится ли позиция ковра слишком близко к аномалии.
     */
    private boolean isNearAnomaly(Vector2D position, Anomaly anomaly, double safetyRadius) {
        Vector2D anomalyPosition = anomaly.getPosition();
        double distanceSquared = position.subtract(anomalyPosition).magnitude();
        double safeDistance = anomaly.getRadius() + safetyRadius;

        return distanceSquared <= safeDistance;
    }

    /**
     * Проверяет, находится ли позиция ковра слишком близко к аномалиям.
     */
    private boolean isNearAnomaly(Vector2D position, List<Anomaly> anomalies, double safetyRadius) {
        for (Anomaly anomaly : anomalies) {
            if (isNearAnomaly(position, anomaly, safetyRadius)) {
                return true;
            }
        }
        return false;
    }
}
