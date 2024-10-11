package org.example.Models;

import lombok.Data;

@Data
public class Enemy {
    private int health;
    private int killBounty;
    private int shieldLeftMs;
    private String status;
    private Velocity velocity;
    private int x;
    private int y;

}
