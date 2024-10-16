package org.example.scripts;


import org.example.models.move.Enemy;
import org.example.models.move.GameState;
import org.example.models.move.TransportAction;
import org.example.models.move.TransportResponse;

import java.util.List;
import java.util.stream.Collectors;


public class ShieldScript {

    private final static int ENEMY_AROUND_MINIMAL_HEALTH_WOUT_SHIELD = 59;
    private final static int MINIMAL_HEALTH_WOUT_SHIELD = 30;


    public List<TransportAction> shieldSuitability(List<TransportAction> transports, GameState gameState) {
        return transports.stream()
                .map(transport -> decideToActivate(transport, gameState)) // Обновляем каждый транспорт
                .collect(Collectors.toList());
    }

    private TransportAction decideToActivate(TransportAction transport, GameState gameState) {

        TransportResponse currentTransport = getCurrentTransport(transport, gameState);
        transport.setActivateShield(checkShieldSuitability(currentTransport, gameState));
        return transport;
    }

    private boolean checkShieldSuitability(TransportResponse currentTransport, GameState gameState) {

        List<Enemy> enemies = gameState.getEnemies();
        long radiusEnemyCount = enemies.stream()
                .filter(enemy -> getDistance(currentTransport, enemy) <= gameState.getAttackRange() + gameState.getAttackExplosionRadius())
                .count();
        int currentHealth = currentTransport.getHealth();

        // 1 Если >=2 врагов, в радиусе стрельбы и хп <= 59хп, то ставим щит
        if (radiusEnemyCount >= 2 && currentHealth <= ENEMY_AROUND_MINIMAL_HEALTH_WOUT_SHIELD) {
            System.out.printf("Активация щита для: %s, Здоровье: %d, Врагов вокруг: %d, Координаты: (%.2f, %.2f)%n",
                    currentTransport.getId(),
                    currentTransport.getHealth(),
                    radiusEnemyCount,
                    currentTransport.getX(),
                    currentTransport.getY());
            return true;
        }
        // 2 Если враг = 1, то при хп <= 30 ставим щит
        return radiusEnemyCount == 1 && currentHealth <= MINIMAL_HEALTH_WOUT_SHIELD;
    }

    private double getDistance(TransportResponse currentTransport, Enemy enemy) {
        return Math.sqrt(Math.pow(enemy.getX() - currentTransport.getX(), 2) + Math.pow(enemy.getY() - currentTransport.getY(), 2));
    }

    private TransportResponse getCurrentTransport(TransportAction transport, GameState gameState) {
        return gameState.getTransports().stream()
                .filter(t1 -> t1.getId().equals(transport.getId()))
                .findFirst()
                .orElse(null);
    }

}
