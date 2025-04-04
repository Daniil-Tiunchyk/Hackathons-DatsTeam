package org.example.dev.datscity.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс, моделирующий текущее состояние строящейся башни в 3D пространстве.
 * Для простоты используем Set<Cell> занятых клеток. Cell - внутренняя структура.
 * Также есть методы проверки возможности размещения слова и его фактического "укладывания".
 */
public class TowerMap {
    private final int maxX;
    private final int maxY;
    private final int maxZ;

    // Множество занятых клеток
    private final Set<Cell> occupied = new HashSet<>();

    /**
     * Ячейка: координаты x,y,z.
     */
    private record Cell(int x, int y, int z) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Cell(int x1, int y1, int z1))) return false;
            return x == x1 && y == y1 && z == z1;
        }

    }

    public TowerMap(int maxX, int maxY, int maxZ) {
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    /**
     * Проверка, занята ли клетка (x,y,z).
     */
    public boolean isOccupied(int x, int y, int z) {
        return occupied.contains(new Cell(x, y, z));
    }

    /**
     * Проверить, можем ли мы разместить слово word (длиной length)
     * начиная с (startX, startY, startZ), по направлению dir.
     * Упрощённо считаем, что dir=0 -> вдоль X, dir=1 -> вдоль Y.
     * (Если требуется иная интерпретация, адаптируйте соответствующим образом.)
     */
    public boolean canPlaceWord(String text, int startX, int startY, int startZ, int dir) {
        int length = text.length();
        if (length == 0) return false;

        // Проверка границ
        if (dir == 0) {
            // вдоль X
            if (startX + length - 1 >= maxX) return false;
            if (startY < 0 || startY >= maxY) return false;
        } else if (dir == 1) {
            // вдоль Y
            if (startY + length - 1 >= maxY) return false;
            if (startX < 0 || startX >= maxX) return false;
        } else {
            // dir не поддерживается в данной демо-версии
            return false;
        }

        if (startZ < 0 || startZ >= maxZ) return false;

        // Проверка занятости клеток + опоры (если z>0)
        for (int i = 0; i < length; i++) {
            int x = (dir == 0) ? (startX + i) : startX;
            int y = (dir == 1) ? (startY + i) : startY;

            // Уже занято?
            if (isOccupied(x, y, startZ)) {
                return false;
            }

            // Проверка опоры (здесь упрощённо — требуем, чтобы клетка снизу была занята, если z>0)
            if (startZ > 0 && !isOccupied(x, y, startZ - 1)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Собственно занять клетки при размещении слова.
     * Вызывается после успешной canPlaceWord.
     */
    public void placeWord(String text, int startX, int startY, int startZ, int dir) {
        int length = text.length();
        for (int i = 0; i < length; i++) {
            int x = (dir == 0) ? (startX + i) : startX;
            int y = (dir == 1) ? (startY + i) : startY;
            occupied.add(new Cell(x, y, startZ));
        }
    }

    /**
     * Возвращает текущую высоту башни (макс z + 1 из занятых клеток).
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
