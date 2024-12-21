package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/*
 * Объект запроса на движение
 * direction: [dx, dy, dz]
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SnakeRequest {
    private List<SnakeCommand> snakes;

    @Data
    @AllArgsConstructor
    public static class SnakeCommand {
        private String id;
        private int[] direction;
    }
}
