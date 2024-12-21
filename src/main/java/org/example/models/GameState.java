package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GameState {
    private Integer points;
    private List<List<Integer>> fences;

    // Остальные поля
    private List<Snake> snakes;
    private List<Food> food;
    private SpecialFood specialFood;
}
