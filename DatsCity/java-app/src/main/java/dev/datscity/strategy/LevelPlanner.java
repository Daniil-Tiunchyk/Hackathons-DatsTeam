package dev.datscity.strategy;

import dev.datscity.model.TowerMap;
import dev.datscity.model.Word;
import dev.datscity.model.WordPlacement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Вспомогательный класс для планирования размещения слов на одном уровне (этаж).
 * Алгоритм жадный: заполняем пространство row-by-row,
 * несколько раз проходя по списку, пока можно что-то уложить.
 * <p>
 * Модифицирован, чтобы не использовать один и тот же индекс слова несколько раз в одном запросе.
 */
public class LevelPlanner {

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
        // NEW: локальное множество индексов, уже размещённых в этом уровне
        Set<Integer> usedIndicesInThisLayer = new HashSet<>();

        if (words.isEmpty()) {
            System.out.println("[LevelPlanner] Нет слов для заполнения уровня.");
            return placements;
        }

        System.out.println("[LevelPlanner] Заполнение слоя: z=" + z + ", dir=" + dir
                + ", offset=(" + offsetX + "," + offsetY + "),"
                + " размер=" + width + "x" + height);

        if (dir == 0) {
            // Заполняем по рядам вдоль оси X
            for (int row = 0; row < height; row++) {
                int xCursor = 0;
                boolean placedInRow;
                do {
                    placedInRow = false;
                    Iterator<Word> it = words.iterator();
                    while (it.hasNext()) {
                        Word w = it.next();
                        // NEW: если этот индекс уже использован в рамках этого уровня, пропускаем
                        if (usedIndicesInThisLayer.contains(w.getIndex())) {
                            continue;
                        }

                        int wLen = w.getLength();
                        if (wLen <= (width - xCursor)) {
                            int placeX = offsetX + xCursor;
                            int placeY = offsetY + row;

                            if (towerMap.canPlaceWord(w.getText(), placeX, placeY, z, 0)) {
                                // Укладываем
                                towerMap.placeWord(w.getText(), placeX, placeY, z, 0);
                                placements.add(new WordPlacement(w.getIndex(), w.getText(), placeX, placeY, z, 0));
                                xCursor += wLen;
                                placedInRow = true;
                                // Удаляем слово из общего списка
                                it.remove();
                                // NEW: отмечаем индекс, чтобы не дублировать в этом запросе
                                usedIndicesInThisLayer.add(w.getIndex());

                                System.out.println("[LevelPlanner] Ряд " + row + ": уложено '" + w.getText()
                                        + "' (idx=" + w.getIndex() + ") xCursor -> " + xCursor);
                            }
                        }
                    }
                } while (placedInRow && xCursor < width);
            }
        } else if (dir == 1) {
            // Заполняем по колонкам вдоль оси Y
            for (int col = 0; col < width; col++) {
                int yCursor = 0;
                boolean placedInCol;
                do {
                    placedInCol = false;
                    Iterator<Word> it = words.iterator();
                    while (it.hasNext()) {
                        Word w = it.next();
                        // NEW: проверяем, не размещен ли уже этот индекс
                        if (usedIndicesInThisLayer.contains(w.getIndex())) {
                            continue;
                        }

                        int wLen = w.getLength();
                        if (wLen <= (height - yCursor)) {
                            int placeX = offsetX + col;
                            int placeY = offsetY + yCursor;

                            if (towerMap.canPlaceWord(w.getText(), placeX, placeY, z, 1)) {
                                towerMap.placeWord(w.getText(), placeX, placeY, z, 1);
                                placements.add(new WordPlacement(w.getIndex(), w.getText(), placeX, placeY, z, 1));
                                yCursor += wLen;
                                placedInCol = true;
                                it.remove();
                                usedIndicesInThisLayer.add(w.getIndex());

                                System.out.println("[LevelPlanner] Колонка " + col + ": уложено '"
                                        + w.getText() + "' (idx=" + w.getIndex()
                                        + ") yCursor -> " + yCursor);
                            }
                        }
                    }
                } while (placedInCol && yCursor < height);
            }
        } else {
            System.out.println("[LevelPlanner] Направление " + dir + " не поддерживается.");
        }

        System.out.println("[LevelPlanner] Слой (z=" + z + ") уложено слов: " + placements.size());
        return placements;
    }
}
