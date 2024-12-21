package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Snake {
    private String id;
    private int[] direction;
    private int[] oldDirection;
    private List<List<Integer>> geometry;
    private String status;

    // Возвращает координаты головы змеи как Point3D
    public Point3D getHead() {
        if (geometry != null && !geometry.isEmpty()) {
            List<Integer> headCoordinates = geometry.get(0);
            if (headCoordinates.size() == 3) {
                return new Point3D(headCoordinates.get(0), headCoordinates.get(1), headCoordinates.get(2));
            }
        }
        return null;
    }

    // Возвращает длину змеи
    public int getLength() {
        return geometry != null ? geometry.size() : 0;
    }
}
