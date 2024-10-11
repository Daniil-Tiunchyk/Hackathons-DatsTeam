package org.example.models.move;

import lombok.Data;

@Data
public class Anomaly {
    private double effectiveRadius;
    private String id;
    private double radius;
    private double strength;
    private Vector2D velocity;
    private double x;
    private double y;
}
