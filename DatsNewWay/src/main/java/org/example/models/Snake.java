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
            List<Integer> head = geometry.get(0);
            return new Point3D(head.get(0), head.get(1), head.get(2));
        }
        return null;
    }

    public int getLength() {
        return geometry != null ? geometry.size() : 0;
    }
}
