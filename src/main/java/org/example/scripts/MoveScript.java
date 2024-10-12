package org.example.scripts;

import org.example.models.move.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MoveScript {

    private static final Vector2D TARGET_POINT = new Vector2D(5000, 5000); // Целевая точка (центр карты)
    private static final double BOUNTY_DETECTION_RADIUS = 400.0; // Радиус обнаружения баунти
    private static final double SAFE_DISTANCE_FROM_BOUNDARY = 200.0; // Безопасное расстояние от границы карты
    private static final double MAX_SPEED = 55.0; // Запас безопасности для аномалий
    private static final double ENEMY_COLLISION_RADIUS = 10.0; // Радиус столкновения с врагами
    private static final double AVOIDANCE_TIME_THRESHOLD = 2.0; // Время для предсказания столкновения (в секундах)

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

            // Проверяем, не находимся ли слишком близко к границе карты
            if (isNearBoundary(transport, gameState)) {
                // Если ковёр слишком близко к границе, поворачиваем его к центру карты
                Vector2D desiredAcceleration = calculateAccelerationTowardsCenter(transport, gameState.getMaxAccel());
                TransportAction command = createTransportCommand(transport, desiredAcceleration);
                commands.add(command);
                continue; // Переходим к следующему ковру
            }

            // Проверяем возможные столкновения с врагами через 2 секунды
            if (willCollideWithEnemies(transport, gameState)) {
                // Избегаем столкновения с врагами
                Vector2D avoidanceAcceleration = calculateAvoidanceFromEnemies(transport, gameState);
                TransportAction command = createTransportCommand(transport, avoidanceAcceleration);
                commands.add(command);
                continue; // Переходим к следующему ковру
            }

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

            // Вычисляем желаемое ускорение в направлении цели с учётом максимальной скорости и ускорения
            Vector2D desiredAcceleration = calculateDesiredAcceleration(
                    transport,
                    targetPosition,
                    transport.getAnomalyAcceleration(),
                    gameState.getMaxAccel()
            );

            // Проверяем, не приведет ли желаемое ускорение к столкновению
            if (willCollide(transport, desiredAcceleration, gameState)) {
                // Если столкновение возможно, корректируем ускорение
                desiredAcceleration = calculateSafeAcceleration(transport, targetPosition, transport.getAnomalyAcceleration(), gameState);
            }

            // Создаем команду для ковра
            TransportAction command = createTransportCommand(transport, desiredAcceleration);
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
     * Проверяет, столкнётся ли ковёр с врагами через заданное время.
     */
    private boolean willCollideWithEnemies(TransportResponse transport, GameState gameState) {
        Vector2D currentPosition = transport.getPosition();
        Vector2D currentVelocity = transport.getVelocity();

        for (Enemy enemy : gameState.getEnemies()) {
            if ("alive".equals(enemy.getStatus())) {
                Vector2D enemyPosition = new Vector2D(enemy.getX(), enemy.getY());
                Vector2D enemyVelocity = new Vector2D(enemy.getVelocity().getX(), enemy.getVelocity().getY());

                // Прогнозируем положение ковра и врага через AVOIDANCE_TIME_THRESHOLD секунд
                Vector2D futureTransportPosition = currentPosition.add(currentVelocity.scale(AVOIDANCE_TIME_THRESHOLD));
                Vector2D futureEnemyPosition = enemyPosition.add(enemyVelocity.scale(AVOIDANCE_TIME_THRESHOLD));

                // Проверяем, будет ли расстояние между ними меньше радиуса столкновения
                if (futureTransportPosition.subtract(futureEnemyPosition).magnitude() < ENEMY_COLLISION_RADIUS) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Проверяет, находится ли ковёр слишком близко к границе карты.
     */
    private boolean isNearBoundary(TransportResponse transport, GameState gameState) {
        Vector2D position = transport.getPosition();
        MapSize mapSize = gameState.getMapSize();

        return position.getX() < SAFE_DISTANCE_FROM_BOUNDARY ||
                position.getY() < SAFE_DISTANCE_FROM_BOUNDARY ||
                position.getX() > mapSize.getX() - SAFE_DISTANCE_FROM_BOUNDARY ||
                position.getY() > mapSize.getY() - SAFE_DISTANCE_FROM_BOUNDARY;
    }

    /**
     * Рассчитывает ускорение в сторону центра карты.
     */
    private Vector2D calculateAccelerationTowardsCenter(TransportResponse transport, double maxAccel) {
        Vector2D toCenter = TARGET_POINT.subtract(transport.getPosition()).normalize();
        return toCenter.scale(maxAccel); // Ускоряемся в сторону центра карты
    }


    /**
     * Рассчитывает безопасное направление, чтобы уйти от аномалий.
     */
    private String formatVector(Vector2D vector) {
        return String.format("(%.2f, %.2f)", vector.getX(), vector.getY());
    }

    /**
     * Рассчитывает ускорение для избегания столкновения с врагами.
     */
    private Vector2D calculateAvoidanceFromEnemies(TransportResponse transport, GameState gameState) {
        Vector2D currentPosition = transport.getPosition();
        Vector2D totalAvoidanceVector = new Vector2D(0, 0);

        for (Enemy enemy : gameState.getEnemies()) {
            if ("alive".equals(enemy.getStatus())) {
                Vector2D enemyPosition = new Vector2D(enemy.getX(), enemy.getY());
                Vector2D avoidanceVector = currentPosition.subtract(enemyPosition).normalize().scale(ENEMY_COLLISION_RADIUS);
                totalAvoidanceVector = totalAvoidanceVector.add(avoidanceVector);
            }
        }

        return totalAvoidanceVector.normalize().scale(gameState.getMaxAccel()); // Ускоряемся в безопасное направление
    }

    /**
     * Создаёт команду для ковра с указанным ускорением.
     */
    private TransportAction createTransportCommand(TransportResponse transport, Vector2D acceleration) {
        TransportAction command = new TransportAction();
        command.setId(transport.getId());
        command.setAcceleration(acceleration);
        return command;
    }

    /**
     * Построение жадного маршрута для сбора всех баунти в радиусе 400 метров.
     */
    private List<Bounty> buildGreedyRoute(TransportResponse transport, GameState gameState) {
        Set<Bounty> remainingBounties = new HashSet<>(gameState.getBounties());
        List<Bounty> route = new ArrayList<>();

        Vector2D currentPosition = transport.getPosition();
        Vector2D currentVelocity = transport.getVelocity();

        while (!remainingBounties.isEmpty()) {
            Bounty bestBounty = null;
            double bestScore = Double.MAX_VALUE; // Минимальный скоринг

            // Ищем баунти в радиусе 400 метров
            for (Bounty bounty : remainingBounties) {
                // Проверяем, находится ли баунти в радиусе 400 метров от текущего ковра
                if (bounty.getPosition().subtract(currentPosition).magnitude() > BOUNTY_DETECTION_RADIUS) {
                    continue; // Пропускаем баунти вне радиуса
                }

                if (isNearAnomaly(bounty.getPosition(), gameState.getAnomalies(), gameState.getTransportRadius())) {
                    continue; // Пропускаем баунти, которые слишком близки к аномалиям
                }

                // Оценка времени достижения баунти
                double estimatedTime = estimateTimeToReach(currentPosition, currentVelocity, bounty.getPosition(), bounty.getRadius(), gameState);

                // Оценка только по времени достижения
                if (estimatedTime < bestScore) {
                    bestScore = estimatedTime;
                    bestBounty = bounty;
                }
            }

            if (bestBounty != null) {
                // Добавляем ближайший баунти в маршрут
                route.add(bestBounty);
                remainingBounties.remove(bestBounty);
                // Обновляем текущую позицию
                currentPosition = bestBounty.getPosition();
            } else {
                // Если не осталось доступных баунти, выходим
                break;
            }
        }

        return route;
    }

    /**
     * Оценка времени достижения цели, учитывая текущее ускорение и скорость транспорта, и радиус баунти.
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
            // Если уже движемся с максимальной скоростью или быстрее
            return distance / velocityInTargetDirection;
        }

        // Шаг 1: Время для достижения максимальной скорости
        double timeToMaxSpeed = (maxSpeed - velocityInTargetDirection) / maxAcceleration;

        // Шаг 2: Расстояние, которое мы пройдем за время разгона до максимальной скорости
        double distanceToMaxSpeed = velocityInTargetDirection * timeToMaxSpeed + 0.5 * maxAcceleration * Math.pow(timeToMaxSpeed, 2);

        if (distanceToMaxSpeed >= distance) {
            // Если можем достичь цели до того, как наберем максимальную скорость
            return (-velocityInTargetDirection + Math.sqrt(velocityInTargetDirection * velocityInTargetDirection + 2 * maxAcceleration * distance)) / maxAcceleration;
        } else {
            // Если достигнем максимальной скорости и будем двигаться с ней
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
     * Вычисляет желаемое ускорение к цели, учитывая ограничение на максимальное ускорение и скорость.
     */
    private Vector2D calculateDesiredAcceleration(TransportResponse transport, Vector2D targetPosition, Vector2D anomalyAcceleration, double maxAccel) {
        Vector2D position = transport.getPosition();
        Vector2D velocity = transport.getVelocity();
        Vector2D toTarget = targetPosition.subtract(position);

        // Направление к цели
        Vector2D desiredDirection = toTarget.normalize();

        // Рассчитываем желаемую скорость в направлении цели
        Vector2D desiredVelocity = desiredDirection.scale(MAX_SPEED); // Вектор желаемой скорости

        // Разница между желаемой скоростью и текущей скоростью ковра
        Vector2D velocityDifference = desiredVelocity.subtract(velocity);

        // Рассчитываем необходимое ускорение для достижения желаемой скорости
        Vector2D requiredAcceleration = velocityDifference; // Предполагаем dt = 1 сек

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

        int i = 0;

        // Если безопасное ускорение все еще ведет к столкновению, уменьшаем его
        while (willCollide(transport, safeAcceleration, gameState) && i <= 50) {
            safeAcceleration = safeAcceleration.scale(0.9); // Постепенно уменьшаем ускорение
            i++;
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