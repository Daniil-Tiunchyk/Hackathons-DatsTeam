package org.example.Script;

import org.example.Models.Enemy;
import org.example.Models.GameState;
import org.example.Models.Transport1;
import org.example.POST.Transport;

import java.util.List;
import java.util.stream.Collectors;


public class ShieldScript {

    private final static int ENEMY_AROUND_MINIMAL_HEALTH_WOUT_SHIELD = 59;
    private final static int MINIMAL_HEALTH_WOUT_SHIELD = 30;


    public List<Transport> shieldSuitability(List<Transport> transports, GameState gameState) {
        return transports.stream()
                .map(transport -> decideToActivate(transport, gameState)) // Обновляем каждый транспорт
                .collect(Collectors.toList());
    }

    private Transport decideToActivate(Transport transport, GameState gameState) {

        Transport1 currentTransport = getCurrentTransport(transport, gameState);
        transport.setActivateShield(checkShieldSuitability(currentTransport, gameState));
        return transport;
    }

    private boolean checkShieldSuitability(Transport1 currentTransport, GameState gameState) {

        List<Enemy> enemies = gameState.getEnemies();
        long radiusEnemyCount = enemies.stream()
                .filter(enemy -> getDistance(currentTransport, enemy) <= gameState.getAttackRange() + gameState.getAttackExplosionRadius())
                .count();
        int currentHealth = currentTransport.getHealth();

        // 1 Если >=2 врагов, в радиусе стрельбы и хп <= 59хп, то ставим щит
        if (radiusEnemyCount >= 2 && currentHealth <= ENEMY_AROUND_MINIMAL_HEALTH_WOUT_SHIELD) {
            return true;
        }
        // 2 Если враг = 1, то при хп <= 30 ставим щит
        return radiusEnemyCount == 1 && currentHealth <= MINIMAL_HEALTH_WOUT_SHIELD;
    }

    private double getDistance(Transport1 currentTransport, Enemy enemy) {
        return Math.sqrt(Math.pow(enemy.getX() - currentTransport.getX(), 2) + Math.pow(enemy.getY() - currentTransport.getY(), 2));
    }

    private Transport1 getCurrentTransport(Transport transport, GameState gameState) {
        return gameState.getTransports().stream()
                .filter(t1 -> t1.getId().equals(transport.getId()))
                .findFirst()
                .orElse(null);
    }

}
