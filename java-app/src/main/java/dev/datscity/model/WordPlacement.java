package dev.datscity.model;

/**
 * Класс для описания размещения слова в башне.
 * Содержит индекс слова, его текст, координаты (x,y,z) и направление (dir).
 */
public class WordPlacement {
    private int index;
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
        System.out.println("[WordPlacement] Размещаем слово '" + text + "' (index=" + index + ") по координатам ("
                + x + "," + y + "," + z + ") с направлением " + dir);
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
