package org.example.Models;

import lombok.Data;

@Data
public class Anomaly {
    private double effectiveRadius;
    private String id;
    private double radius;
    private int strength;
    private Velocity velocity;
    private int x;
    private int y;

}
