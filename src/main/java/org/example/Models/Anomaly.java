package org.example.Models;

import lombok.Data;

@Data
public class Anomaly {
    private int effectiveRadius;
    private String id;
    private int radius;
    private int strength;
    private Velocity velocity;
    private int x;
    private int y;

}
