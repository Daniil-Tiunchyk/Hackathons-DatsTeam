package org.example.models.move;

import lombok.Data;

@Data
public class TransportResponse {
    private Vector2D anomalyAcceleration;
    private int attackCooldownMs;
    private int deathCount;
    private int health;
    private String id;
    private Vector2D selfAcceleration;
    private int shieldCooldownMs;
    private int shieldLeftMs;
    private String status;
    private Vector2D velocity;
    private double x;
    private double y;

    public Vector2D getPosition() {
        return new Vector2D(x, y);
    }
}
