package org.example.service;

import org.example.models.Point3D;

import java.util.*;

public class PathFindingService {

    public List<int[]> findPath(Point3D head, Point3D target, Set<Point3D> obstacles, List<Integer> mapSize, int radius) {
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingInt(Node::getF));
        Set<Point3D> closedSet = new HashSet<>();

        Node startNode = new Node(head, null, 0, calculateManhattanDistance(head, target));
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            closedSet.add(current.point);

            if (current.point.equals(target)) {
                return reconstructPath(current);
            }

            for (int[] direction : getDirections()) {
                Point3D neighbor = new Point3D(
                        current.point.getX() + direction[0],
                        current.point.getY() + direction[1],
                        current.point.getZ() + direction[2]
                );

                if (!isWithinBounds(neighbor, mapSize)
                        || obstacles.contains(neighbor)
                        || closedSet.contains(neighbor)) {
                    continue;
                }

                int tentativeG = current.g + 1;
                Node neighborNode = new Node(neighbor, current, tentativeG, calculateManhattanDistance(neighbor, target));

                if (openSet.stream().anyMatch(node -> node.point.equals(neighbor) && node.g <= tentativeG)) {
                    continue;
                }

                openSet.add(neighborNode);
            }
        }

        return null; // Если путь не найден
    }

    private int[][] getDirections() {
        return new int[][]{
                {1, 0, 0}, {-1, 0, 0},
                {0, 1, 0}, {0, -1, 0},
                {0, 0, 1}, {0, 0, -1}
        };
    }

    private boolean isWithinBounds(Point3D p, List<Integer> mapSize) {
        return p.getX() >= 0 && p.getX() < mapSize.get(0)
                && p.getY() >= 0 && p.getY() < mapSize.get(1)
                && p.getZ() >= 0 && p.getZ() < mapSize.get(2);
    }

    private int calculateManhattanDistance(Point3D p1, Point3D p2) {
        return Math.abs(p1.getX() - p2.getX()) +
                Math.abs(p1.getY() - p2.getY()) +
                Math.abs(p1.getZ() - p2.getZ());
    }

    private List<int[]> reconstructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node.parent != null) {
            Point3D currP = node.point;
            Point3D parP = node.parent.point;
            path.add(new int[]{
                    currP.getX() - parP.getX(),
                    currP.getY() - parP.getY(),
                    currP.getZ() - parP.getZ()
            });
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static class Node {
        Point3D point;
        Node parent;
        int g;
        int h;

        Node(Point3D point, Node parent, int g, int h) {
            this.point = point;
            this.parent = parent;
            this.g = g;
            this.h = h;
        }

        int getF() {
            return g + h;
        }
    }
}
