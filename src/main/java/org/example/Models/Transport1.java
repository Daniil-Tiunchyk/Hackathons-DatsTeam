package org.example.Models;

import lombok.Data;

@Data
public class Transport1 {
    private Velocity anomalyAcceleration;
    private int attackCooldownMs;
    private int deathCount;
    private int health;
    private String id;
    private Velocity selfAcceleration;
    private int shieldCooldownMs;
    private int shieldLeftMs;
    private String status;
    private Velocity velocity;
    private int x;
    private int y;

}
