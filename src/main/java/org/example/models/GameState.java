package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GameState {
    private Integer points;
    private List<List<Integer>> fences;

    private List<Snake> snakes;
    private List<Food> food;
    private SpecialFood specialFood;

    private List<String> errors;

}
