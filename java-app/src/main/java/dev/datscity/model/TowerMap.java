package dev.datscity.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс, представляющий карту (3D-модель) строящейся башни.
 * Отслеживает, какие клетки уже заняты, и позволяет проверить возможность размещения.
 */
public class TowerMap {
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final Set<Cell> occupied = new HashSet<>();

    /**
     * Внутренний класс для представления клетки.
     */
    private static class Cell {
        final int x, y, z;

        Cell(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell)) return false;
            Cell cell = (Cell) o;
            return x == cell.x && y == cell.y && z == cell.z;
        }

        @Override
        public int hashCode() {
            return (x * 31 + y) * 31 + z;
        }
    }

    public TowerMap(int maxX, int maxY, int maxZ) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        System.out.println("[TowerMap] Инициализирована карта башни размером: " + maxX + "x" + maxY + "x" + maxZ);
    }

    /**
     * Проверяет, занята ли клетка (x,y,z).
     */
    public boolean isOccupied(int x, int y, int z) {
        return occupied.contains(new Cell(x, y, z));
    }

    /**
     * Проверяет возможность размещения слова с данным текстом, начиная с (startX, startY, startZ)
     * в направлении dir (0 – вдоль X, 1 – вдоль Y).
     */
    public boolean canPlaceWord(String text, int startX, int startY, int startZ, int dir) {
        int length = text.length();
        if (length == 0) return false;

        // Проверка границ
        if (dir == 0) {
            if (startX + length - 1 >= maxX) {
                System.out.println("[TowerMap] Слово '" + text + "' не влезает по X");
                return false;
            }
            if (startY < 0 || startY >= maxY) return false;
        } else if (dir == 1) {
            if (startY + length - 1 >= maxY) {
                System.out.println("[TowerMap] Слово '" + text + "' не влезает по Y");
                return false;
            }
            if (startX < 0 || startX >= maxX) return false;
        } else {
            System.out.println("[TowerMap] Направление " + dir + " не поддерживается");
            return false;
        }
        if (startZ < 0 || startZ >= maxZ) return false;

        // Проверяем занятость клеток и наличие опоры (если z>0)
        for (int i = 0; i < length; i++) {
            int x = (dir == 0) ? (startX + i) : startX;
            int y = (dir == 1) ? (startY + i) : startY;
            int z = startZ;

            if (isOccupied(x, y, z)) {
                System.out.println("[TowerMap] Клетка (" + x + "," + y + "," + z + ") уже занята");
                return false;
            }

            // Если не на земле, требуется опора
            if (z > 0 && !isOccupied(x, y, z - 1)) {
                System.out.println("[TowerMap] Нет опоры для клетки (" + x + "," + y + "," + z + ")");
                return false;
            }
        }
        return true;
    }

    /**
     * Размещает слово, занимая клетки. Вызывается после успешной проверки canPlaceWord.
     */
    public void placeWord(String text, int startX, int startY, int startZ, int dir) {
        int length = text.length();
        for (int i = 0; i < length; i++) {
            int x = (dir == 0) ? (startX + i) : startX;
            int y = (dir == 1) ? (startY + i) : startY;
            int z = startZ;
            occupied.add(new Cell(x, y, z));
            System.out.println("[TowerMap] Занята клетка (" + x + "," + y + "," + z + ") для слова '" + text + "'");
        }
    }

    /**
     * Возвращает текущую высоту башни (максимальный z среди занятых клеток + 1).
     */
    public int getCurrentHeight() {
        int maxOccupiedZ = 0;
        for (Cell c : occupied) {
            if (c.z > maxOccupiedZ) {
                maxOccupiedZ = c.z;
            }
        }
        return maxOccupiedZ + 1;
    }
}
