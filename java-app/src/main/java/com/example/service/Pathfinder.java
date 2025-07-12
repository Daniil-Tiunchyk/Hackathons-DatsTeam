package com.example.service;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.dto.ArenaStateDto;

import java.util.*;

/**
 * Реализует оптимизированный алгоритм поиска пути A* (A-star) для навигации по гексагональной карте.
 * Учитывает стоимость передвижения, избегает препятствий и имеет защиту от слишком долгих вычислений.
 * В новой версии также проверяет, не является ли шаг на кислотный гекс смертельным для юнита.
 */
public class Pathfinder {

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

    public List<Hex> findPath(ArenaStateDto.AntDto ant, Hex start, Hex goal, Map<Hex, Integer> hexCosts, Map<Hex, HexType> hexTypes, Set<Hex> blockedHexes) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Hex> closedSet = new HashSet<>();
        Map<Hex, Integer> gCostMap = new HashMap<>();

        Node startNode = new Node(start, null, 0, start.distanceTo(goal));
        openSet.add(startNode);
        gCostMap.put(start, 0);

        int iterations = 0;
        while (!openSet.isEmpty()) {
            if (++iterations > MAX_ITERATIONS) {
                return Collections.emptyList();
            }

            Node currentNode = openSet.poll();

            if (closedSet.contains(currentNode.hex())) {
                continue;
            }

            if (currentNode.hex().equals(goal)) {
                return reconstructPath(currentNode);
            }

            closedSet.add(currentNode.hex());

            for (Hex neighborHex : currentNode.hex().getNeighbors()) {
                if (closedSet.contains(neighborHex)
                        || blockedHexes.contains(neighborHex)
                        || isDeadlyAcid(neighborHex, ant, hexTypes)) {
                    continue;
                }

                int moveCost = hexCosts.getOrDefault(neighborHex, 1);
                int newGCost = currentNode.gCost() + moveCost;

                if (newGCost < gCostMap.getOrDefault(neighborHex, Integer.MAX_VALUE)) {
                    gCostMap.put(neighborHex, newGCost);
                    int hCost = neighborHex.distanceTo(goal);
                    Node neighborNode = new Node(neighborHex, currentNode, newGCost, hCost);
                    openSet.add(neighborNode);
                }
            }
        }

        return Collections.emptyList();
    }

    private boolean isDeadlyAcid(Hex hex, ArenaStateDto.AntDto ant, Map<Hex, HexType> hexTypes) {
        HexType type = hexTypes.get(hex);
        if (type == HexType.ACID) {
            return ant.health() <= type.getDamage();
        }
        return false;
    }

    private List<Hex> reconstructPath(Node goalNode) {
        List<Hex> path = new ArrayList<>();
        Node currentNode = goalNode;
        while (currentNode != null) {
            path.add(currentNode.hex());
            currentNode = currentNode.parent();
        }
        Collections.reverse(path);
        return path.isEmpty() ? path : path.subList(1, path.size());
    }
}
