package org.example.dev.datscity.model;

/**
 * Описывает решение по размещению одного слова (Word) в башне:
 * - link to Word (или, по минимуму, хранит index)
 * - координаты (x, y, z)
 * - ориентация (dir)
 * <p>
 * dir может быть, например:
 * 0 - вдоль оси X
 * 1 - вдоль оси Y
 * 2 - вдоль оси Z (если поддерживается вертикальное размещение, но в задании явно
 * сказано 1=Z,2=X,3=Y или иным образом)
 * В условии используется 1=[0,0,-1],2=[1,0,0],3=[0,1,0], но вы можете
 * адаптировать под свою систему координат. Важно корректно передавать на сервер.
 */
public class WordPlacement {
    private int index;  // индекс слова (Word.getIndex())
    private String text;
    private int x, y, z;
    private int dir;

    public WordPlacement(int index, String text, int x, int y, int z, int dir) {
        this.index = index;
        this.text = text;
        this.x = x;
        this.y = y;
        this.z = z;
        this.dir = dir;
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public int getDir() {
        return dir;
    }
}
