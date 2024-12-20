package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GameState {
    private List<Integer> mapSize;
    private String name;
    private int points;
    private List<Point3D> fences;
    private List<Snake> snakes;
    private List<Enemy> enemies;
    private List<Food> food;
    private SpecialFood specialFood;
    private int turn;
    private int reviveTimeoutSec;
    private int tickRemainMs;
    private List<String> errors;
}
