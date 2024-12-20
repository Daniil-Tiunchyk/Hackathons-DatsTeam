package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Snake {
    private String id;
    private Direction3D direction;
    private Direction3D oldDirection;
    private List<Point3D> geometry;
    private int deathCount;
    private String status;
    private int reviveRemainMs;
}
