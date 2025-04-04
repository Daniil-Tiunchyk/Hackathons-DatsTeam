package org.example.dev.datscity.strategy;

import org.example.dev.datscity.model.TowerMap;
import org.example.dev.datscity.model.Word;
import org.example.dev.datscity.model.WordPlacement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Вспомогательный класс для укладки слов на одном "уровне" (этаж).
 * Жадно заполняет пространство row-by-row (или col-by-col),
 * используя методы TowerMap.canPlaceWord / placeWord.
 */
public class LevelPlanner {

    /**
     * Заполняет слой размерами (width x height), начиная с (offsetX, offsetY) на уровне z.
     * Ориентация слоя задаётся параметром dir (0=по X, 1=по Y).
     * <p>
     * Алгоритм жадный: идём по "строкам", стараемся уложить слова по порядку.
     * Возвращает список WordPlacement, которые удалось уложить на уровень.
     * При этом реальное заполнение клеток в TowerMap тоже выполняется.
     */
    public static List<WordPlacement> fillLayer(
            List<Word> words,
            TowerMap towerMap,
            int offsetX,
            int offsetY,
            int z,
            int width,
            int height,
            int dir
    ) {
        List<WordPlacement> placements = new ArrayList<>();

        if (words.isEmpty()) return placements;

        // для dir=0 (по X) - двигаемся по Y-строкам
        // для dir=1 (по Y) - двигаемся по X-строкам
        // Ниже реализуем вариант для dir=0, dir=1 аналогичен, просто поменять смысл координат.

        if (dir == 0) {
            // раскладываем слова построчно вдоль X
            for (int row = 0; row < height; row++) {
                int xCursor = 0;
                // идём по списку слов (внешний итератор)
                Iterator<Word> it = words.iterator();
                while (it.hasNext()) {
                    Word w = it.next();
                    int wLen = w.getLength();

                    if (wLen <= (width - xCursor)) {
                        // проверяем, можем ли мы физически разместить в towerMap
                        int placeX = offsetX + xCursor;
                        int placeY = offsetY + row;
                        if (towerMap.canPlaceWord(w.getText(), placeX, placeY, z, 0)) {
                            // Укладываем
                            towerMap.placeWord(w.getText(), placeX, placeY, z, 0);
                            placements.add(new WordPlacement(w.getIndex(), w.getText(), placeX, placeY, z, 0));
                            xCursor += wLen;
                            // удаляем слово из списка - мы его уложили
                            it.remove();
                        }
                    }
                }
            }
        } else if (dir == 1) {
            // раскладываем слова построчно вдоль Y
            for (int row = 0; row < width; row++) {
                int yCursor = 0;
                Iterator<Word> it = words.iterator();
                while (it.hasNext()) {
                    Word w = it.next();
                    int wLen = w.getLength();

                    if (wLen <= (height - yCursor)) {
                        int placeX = offsetX + row;
                        int placeY = offsetY + yCursor;
                        if (towerMap.canPlaceWord(w.getText(), placeX, placeY, z, 1)) {
                            towerMap.placeWord(w.getText(), placeX, placeY, z, 1);
                            placements.add(new WordPlacement(w.getIndex(), w.getText(), placeX, placeY, z, 1));
                            yCursor += wLen;
                            it.remove();
                        }
                    }
                }
            }
        }

        return placements;
    }
}
