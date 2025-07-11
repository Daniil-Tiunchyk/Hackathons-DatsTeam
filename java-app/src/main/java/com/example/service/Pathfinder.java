package com.example.service;

import com.example.domain.Hex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Реализует оптимизированный алгоритм поиска пути A* (A-star) для навигации по гексагональной карте.
 * Учитывает стоимость передвижения, избегает препятствий и имеет защиту от слишком долгих вычислений.
 */
public class Pathfinder {

    // Предохранитель для предотвращения бесконечных или слишком долгих циклов
    private static final int MAX_ITERATIONS = 10000;

    private record Node(Hex hex, Node parent, int gCost, int hCost) implements Comparable<Node> {
        public int fCost() {
            return gCost + hCost;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.fCost(), other.fCost());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Node node = (Node) o;
            return Objects.equals(hex, node.hex);
        }

        @Override
        public int hashCode() {
            return Objects.hash(hex);
        }
    }

    public List<Hex> findPath(Hex start, Hex goal, Map<Hex, Integer> hexCosts, Set<Hex> blockedHexes) {
        // Очередь с приоритетом для узлов, которые предстоит исследовать.
        PriorityQueue<Node> openSet = new PriorityQueue<>();

        // Множество для гексов, которые уже были исследованы с оптимальным путем.
        Set<Hex> closedSet = new HashSet<>();

        // Хранит для каждого гекса его лучшую найденную gCost (стоимость пути от старта).
        Map<Hex, Integer> gCostMap = new HashMap<>();

        Node startNode = new Node(start, null, 0, start.distanceTo(goal));
        openSet.add(startNode);
        gCostMap.put(start, 0);

        int iterations = 0;
        while (!openSet.isEmpty()) {
            if (++iterations > MAX_ITERATIONS) {
                return Collections.emptyList(); // Прерываем поиск, если он слишком затянулся
            }

            Node currentNode = openSet.poll();

            // Если мы уже нашли лучший путь к этому узлу, пропускаем его.
            // Это ключевая часть оптимизации "ленивого удаления" (lazy removal).
            if (closedSet.contains(currentNode.hex())) {
                continue;
            }

            if (currentNode.hex().equals(goal)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.hex());

            for (Hex neighborHex : currentNode.hex().getNeighbors()) {
                if (closedSet.contains(neighborHex) || blockedHexes.contains(neighborHex)) {
                    continue;
                }

                int moveCost = hexCosts.getOrDefault(neighborHex, 1);
                int newGCost = currentNode.gCost() + moveCost;

                // Если мы нашли более короткий путь к соседу, обновляем его
                if (newGCost < gCostMap.getOrDefault(neighborHex, Integer.MAX_VALUE)) {
                    gCostMap.put(neighborHex, newGCost);
                    int hCost = neighborHex.distanceTo(goal);
                    Node neighborNode = new Node(neighborHex, currentNode, newGCost, hCost);
                    openSet.add(neighborNode);
                }
            }
        }

        return Collections.emptyList(); // Путь не найден
    }

    private List<Hex> reconstructPath(Node goalNode) {
        List<Hex> path = new ArrayList<>();
        Node currentNode = goalNode;
        while (currentNode != null) {
            path.add(currentNode.hex());
            currentNode = currentNode.parent();
        }
        Collections.reverse(path);
        return path;
    }
}
