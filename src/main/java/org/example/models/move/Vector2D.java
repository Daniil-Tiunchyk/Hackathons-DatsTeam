package org.example.models.move;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Vector2D {
    private double x;
    private double y;

    // Add method
    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    // Subtract method
    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    // Magnitude (length) of the vector
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }

    // Normalize (unit vector in the same direction)
    public Vector2D normalize() {
        double mag = magnitude();
        if (mag == 0) {
            // Avoid division by zero
            return new Vector2D(0, 0);
        }
        return new Vector2D(x / mag, y / mag);
    }

    // Scale (multiply by a scalar)
    public Vector2D scale(double scalar) {
        return new Vector2D(x * scalar, y * scalar);
    }

    public double dot(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }
}
