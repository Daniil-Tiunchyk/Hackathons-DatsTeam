package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GameState {
    private List<Integer> mapSize; // [maxX, maxY, maxZ]
    private Integer points;
    private List<List<Integer>> fences;

    private List<Enemy> enemies;

    private List<Snake> snakes;
    private List<Food> food;
    private SpecialFood specialFood;

    private List<String> errors;

}
