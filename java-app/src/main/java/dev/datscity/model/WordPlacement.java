package dev.datscity.model;

import lombok.Getter;

/**
 * Класс для описания размещения слова в башне.
 * Содержит индекс слова, его текст, координаты (x,y,z) и направление (dir).
 */
@Getter
public class WordPlacement {
    private final int index;
    private final String text;
    private final int x;
    private final int y;
    private final int z;
    private final int dir;

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

}
