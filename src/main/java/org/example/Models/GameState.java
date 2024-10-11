package org.example.Models;
import lombok.Data;

import java.util.List;
@Data
public class GameState {
    private List<Anomaly> anomalies;
    private int attackCooldownMs;
    private int attackDamage;
    private int attackExplosionRadius;
    private int attackRange;
    private List<Bounty> bounties;
    private List<Enemy> enemies;
    private MapSize mapSize;
    private int maxAccel;
    private int maxSpeed;
    private String name;
    private int points;
    private int reviveTimeoutSec;
    private int shieldCooldownMs;
    private int shieldTimeMs;
    private int transportRadius;
    private List<Transport1> transports;
    private List<Wanted> wantedList;

}


