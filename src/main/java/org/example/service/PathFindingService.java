package org.example.service;

import org.example.models.Point3D;

import java.util.*;

public class PathFindingService {

    public List<int[]> findPath(Point3D head, Point3D target, Set<Point3D> obstacles, List<Integer> mapSize, Integer range) {
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

                if (!isWithinBounds(neighbor, mapSize) || obstacles.contains(neighbor) || closedSet.contains(neighbor)) {
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

        return null;
    }

    private List<int[]> reconstructPath(Node node) {
        List<int[]> path = new ArrayList<>();
        while (node.parent != null) {
            Point3D curr = node.point;
            Point3D prev = node.parent.point;
            path.add(new int[]{curr.getX() - prev.getX(), curr.getY() - prev.getY(), curr.getZ() - prev.getZ()});
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private int calculateManhattanDistance(Point3D a, Point3D b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY()) + Math.abs(a.getZ() - b.getZ());
    }

    private int[][] getDirections() {
        return new int[][]{{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
    }

    private boolean isWithinBounds(Point3D point, List<Integer> mapSize) {
        return point.getX() >= 0 && point.getX() < mapSize.get(0) &&
                point.getY() >= 0 && point.getY() < mapSize.get(1) &&
                point.getZ() >= 0 && point.getZ() < mapSize.get(2);
    }

    private static class Node {
        Point3D point;
        Node parent;
        int g, h;

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
