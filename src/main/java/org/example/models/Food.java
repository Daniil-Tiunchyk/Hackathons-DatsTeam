package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Food {
    private List<Integer> c;
    private int points;

    // Возвращает координаты фрукта как Point3D
    public Point3D getCoordinates() {
        if (c != null && c.size() == 3) {
            return new Point3D(c.get(0), c.get(1), c.get(2));
        }
        return null;
    }
}
