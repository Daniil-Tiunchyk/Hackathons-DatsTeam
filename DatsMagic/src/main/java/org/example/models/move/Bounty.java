package org.example.models.move;

import lombok.Data;

@Data
public class Bounty {
    private int points;
    private double radius;
    private double x;
    private double y;

    public Vector2D getPosition() {
        return new Vector2D(x, y);
    }
}
