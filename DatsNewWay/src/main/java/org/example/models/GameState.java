package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * Собирает и возвращает множество всех препятствий.
     */
    public Set<Point3D> getObstacles() {
        Set<Point3D> obstacles = new HashSet<>();

        // Добавляем заборы
        fences.forEach(f -> obstacles.add(new Point3D(f.get(0), f.get(1), f.get(2))));

        // Добавляем тело своих змей
        snakes.forEach(snake -> snake.getGeometry().forEach(segment ->
                obstacles.add(new Point3D(segment.get(0), segment.get(1), segment.get(2)))
        ));

        // Добавляем тело врагов и окружение вокруг их голов
        enemies.forEach(enemy -> {
            // Тело врага
            enemy.getGeometry().forEach(segment ->
                    obstacles.add(new Point3D(segment.get(0), segment.get(1), segment.get(2)))
            );

            // Окружение вокруг головы врага
            if (!enemy.getGeometry().isEmpty()) {
                List<Integer> head = enemy.getGeometry().get(0);
                addSurroundingObstacles(obstacles, new Point3D(head.get(0), head.get(1), head.get(2)));
            }
        });

        return obstacles;
    }

    /**
     * Добавляет клетки вокруг головы змейки (со смещением на 1 по каждой оси).
     */
    private void addSurroundingObstacles(Set<Point3D> obstacles, Point3D head) {
        int x = head.getX();
        int y = head.getY();
        int z = head.getZ();

        int[][] deltas = {
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };

        for (int[] d : deltas) {
            obstacles.add(new Point3D(x + d[0], y + d[1], z + d[2]));
        }
    }
}
