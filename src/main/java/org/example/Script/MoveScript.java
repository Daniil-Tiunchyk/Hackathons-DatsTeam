package org.example.Script;


import org.example.Models.Bounty;
import org.example.Models.Transport1;

import org.example.Models.Velocity;
import org.example.POST.Acceleration;
import org.example.POST.Transport;

import java.util.ArrayList;
import java.util.List;

public class MoveScript {

    public List<Transport> move(List<Transport1> transportList, List<Bounty> bountyList) {
        List<Transport> transportsFinal = new ArrayList<>();
        for (Transport1 transport : transportList) {
            Transport transportFinal = new Transport();
            transportFinal.setId(transport.getId());
            Acceleration acceleration = calculateBestAccelerationToBounty(transport, bountyList);
            transportFinal.setAcceleration(acceleration);

            transportsFinal.add(transportFinal);
        }
        return transportsFinal;
    }

    public static Acceleration calculateBestAccelerationToBounty(Transport1 transport, List<Bounty> bounties) {
        Bounty bestBounty = null;
        double minTime = Double.MAX_VALUE;

        for (Bounty bounty : bounties) {
            double time = calculateTimeToBounty(transport, bounty);
            if (time < minTime) {
                minTime = time;
                bestBounty = bounty;
            }
        }

        if (bestBounty != null) {
            return computeAccelerationToBounty(transport, bestBounty);
        } else {
            return new Acceleration();
        }
    }

    private static double calculateTimeToBounty(Transport1 transport, Bounty bounty) {
        double distance = calculateDistance(transport.getVelocity(), bounty);
        double currentSpeed = transport.getVelocity().length(); // Метод для получения длины вектора скорости

        // Если расстояние до монеты меньше или равно скорости, то время до монеты 1 ход
        if (distance <= currentSpeed) {
            return 1;
        }

        // Рассчитываем максимальное ускорение
        double maxAcceleration = 10.0;

        // Определяем необходимую скорость, чтобы достичь монеты
        double desiredSpeed = Math.sqrt(distance); // Минимальная скорость, чтобы достичь монеты
        double timeToReach = (desiredSpeed - currentSpeed) / maxAcceleration;

        // Если мы уже достаточно быстры
        if (currentSpeed >= desiredSpeed) {
            return distance / currentSpeed; // Время, если мы движемся с текущей скоростью
        }

        // Если время отрицательное, значит, мы не сможем достичь монеты
        return timeToReach > 0 ? timeToReach + 1 : Double.MAX_VALUE;
    }

    private static double calculateDistance(Velocity velocity, Bounty bounty) {
        return Math.sqrt(Math.pow(velocity.getX() - bounty.getX(), 2) + Math.pow(velocity.getY() - bounty.getY(), 2));
    }

    private static Acceleration computeAccelerationToBounty(Transport1 transport, Bounty bounty) {
        double directionX = bounty.getX() - transport.getVelocity().getX();
        double directionY = bounty.getY() - transport.getVelocity().getY();
        double length = Math.sqrt(directionX * directionX + directionY * directionY);

        // Нормализуем вектор
        if (length != 0) {
            directionX /= length;
            directionY /= length;

            // Рассчитываем максимальное доступное ускорение
            double currentSpeed = transport.getVelocity().length();
            double maxAcceleration = 10.0;

            // Определяем желаемую скорость
            double desiredSpeed = Math.min(currentSpeed + maxAcceleration, length); // Не превышаем расстояние до монеты
            double accelerationMagnitude = desiredSpeed - currentSpeed;

            // Убедимся, что мы не превышаем максимальное ускорение
            if (accelerationMagnitude > maxAcceleration) {
                accelerationMagnitude = maxAcceleration;
            } else if (accelerationMagnitude < -maxAcceleration) {
                accelerationMagnitude = -maxAcceleration;
            }

            return new Acceleration(directionX * accelerationMagnitude, directionY * accelerationMagnitude);
        }

        return new Acceleration(0, 0);
    }
}

