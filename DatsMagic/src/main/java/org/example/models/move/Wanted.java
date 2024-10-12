package org.example.models.move;

import lombok.Data;

@Data
public class Wanted {
    private int health;
    private int killBounty;
    private int shieldLeftMs;
    private String status;
    private Vector2D velocity;
    private double x;
    private double y;
}
