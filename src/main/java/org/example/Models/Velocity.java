package org.example.Models;

import lombok.Data;

@Data
public class Velocity {
    private double x;
    private double y;
    public double length() {
        return Math.sqrt(x * x + y * y);
    }

}
