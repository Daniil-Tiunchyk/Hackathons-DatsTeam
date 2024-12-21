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
        if (geometry == null || geometry.isEmpty()) return null;
        List<Integer> headCoords = geometry.get(0);
        return new Point3D(headCoords.get(0), headCoords.get(1), headCoords.get(2));
    }

    // Возвращает длину змеи
    public int getLength() {
        return geometry != null ? geometry.size() : 0;
    }
}
