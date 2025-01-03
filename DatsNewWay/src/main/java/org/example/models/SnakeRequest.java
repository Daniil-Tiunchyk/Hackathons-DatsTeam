package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/*
 * Объект запроса на движение
 * direction: [dx, dy, dz]
 */
@Data
public class SnakeRequest {
    private List<SnakeCommand> snakes = new ArrayList<>();

    @Data
    @AllArgsConstructor
    public static class SnakeCommand {
        private String id;
        private int[] direction;
    }
}
