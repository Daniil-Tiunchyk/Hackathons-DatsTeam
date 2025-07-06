package dev.datscity.alt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UniversalCrosswordSolver {

    // Модель входного JSON
    public static class InputData {
        public int[] mapSize;
        public int nextTurnSec;
        public String roundEndsAt;
        public int shuffleLeft;
        public int turn;
        public List<String> words;
    }

    // Модели для выходного JSON
    public static class TowerData {
        public Object doneTowers; // null
        public double score;
        public Tower tower;
    }

    public static class Tower {
        public double score;
        public List<WordData> words;
    }

    public static class WordData {
        // Используем коды:
        // 1 = вертикальное ([0, 0, -1])
        // 2 = горизонтальное ([1, 0, 0])
        public int dir;
        public int[] pos; // координаты [x, y, z] (z всегда 0)
        public String text;

        public WordData() {
        }

        public WordData(int dir, int[] pos, String text) {
            this.dir = dir;
            this.pos = pos;
            this.text = text;
        }
    }

    // Класс для представления поля (двумерная сетка)
    public static class Grid {
        public char[][] cells;
        public int width, height;

        public Grid(int width, int height) {
            this.width = width;
            this.height = height;
            cells = new char[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    cells[i][j] = '.';
                }
            }
        }

        // Проверяет, что слово можно разместить в позиции (x,y) в направлении dir (1 = вертикально, 2 = горизонтально)
        public boolean canPlace(String word, int x, int y, int dir) {
            int len = word.length();
            if (dir == 2) { // горизонтальное
                if (x + len > width) return false;
                for (int j = 0; j < len; j++) {
                    char c = cells[y][x + j];
                    if (c != '.' && c != word.charAt(j)) return false;
                }
            } else if (dir == 1) { // вертикальное
                if (y + len > height) return false;
                for (int i = 0; i < len; i++) {
                    char c = cells[y + i][x];
                    if (c != '.' && c != word.charAt(i)) return false;
                }
            } else {
                return false;
            }
            return true;
        }

        public void place(String word, int x, int y, int dir) {
            int len = word.length();
            if (dir == 2) {
                for (int j = 0; j < len; j++) {
                    cells[y][x + j] = word.charAt(j);
                }
            } else if (dir == 1) {
                for (int i = 0; i < len; i++) {
                    cells[y + i][x] = word.charAt(i);
                }
            }
        }

        public void remove(String word, int x, int y, int dir) {
            int len = word.length();
            if (dir == 2) {
                for (int j = 0; j < len; j++) {
                    cells[y][x + j] = '.';
                }
            } else if (dir == 1) {
                for (int i = 0; i < len; i++) {
                    cells[y + i][x] = '.';
                }
            }
        }

        public Grid copy() {
            Grid g = new Grid(width, height);
            for (int i = 0; i < height; i++) {
                System.arraycopy(this.cells[i], 0, g.cells[i], 0, width);
            }
            return g;
        }

        // В классе Grid (UniversalCrosswordSolver)
        public boolean canPlaceWithGap(String word, int x, int y, int dir) {
            int len = word.length();
            // Сначала стандартная проверка, что слово помещается и либо пустые клетки, либо совпадающие буквы
            if (!canPlace(word, x, y, dir)) return false;

            // Дополнительная проверка: необходимо, чтобы перед началом и после конца слова был зазор (если такие клетки есть)
            if (dir == 2) { // горизонтальное размещение
                // Проверяем клетку слева от слова
                if (x - 1 >= 0 && cells[y][x - 1] != '.') return false;
                // Проверяем клетку справа от слова
                if (x + len < width && cells[y][x + len] != '.') return false;
                // Проверяем вертикальные соседние клетки для каждой буквы (если не пересечение)
                for (int j = 0; j < len; j++) {
                    // Клетка сверху
                    if (y - 1 >= 0) {
                        // Если клетка заполнена, но не совпадает с буквой, то зазор отсутствует
                        if (cells[y - 1][x + j] != '.' && cells[y - 1][x + j] != word.charAt(j))
                            return false;
                    }
                    // Клетка снизу
                    if (y + 1 < height) {
                        if (cells[y + 1][x + j] != '.' && cells[y + 1][x + j] != word.charAt(j))
                            return false;
                    }
                }
            } else if (dir == 1) { // вертикальное размещение
                // Проверяем клетку над словом
                if (y - 1 >= 0 && cells[y - 1][x] != '.') return false;
                // Проверяем клетку под словом
                if (y + len < height && cells[y + len][x] != '.') return false;
                // Проверяем горизонтальные соседние клетки для каждой буквы
                for (int i = 0; i < len; i++) {
                    if (x - 1 >= 0) {
                        if (cells[y + i][x - 1] != '.' && cells[y + i][x - 1] != word.charAt(i))
                            return false;
                    }
                    if (x + 1 < width) {
                        if (cells[y + i][x + 1] != '.' && cells[y + i][x + 1] != word.charAt(i))
                            return false;
                    }
                }
            }
            return true;
        }

    }

    // Класс для хранения конфигурации (размещённых слов)
    public static class Placement {
        public List<WordData> words = new ArrayList<>();
        public int totalLetters = 0;

        public Placement() {
        }

        public Placement(Placement other) {
            this.words = new ArrayList<>(other.words);
            this.totalLetters = other.totalLetters;
        }
    }

    private static Placement bestPlacement = null;
    private static long nodesProcessed = 0;
    private static final long LOG_INTERVAL = 100000;
    private static int totalWords;

    /**
     * Универсальный метод, который перебирает все варианты размещения слов.
     * Каждое новое слово пытается присоединиться к уже размещённым словам по всем возможным парам букв.
     * Возвращает конфигурацию (Placement) с максимальным количеством размещённых букв.
     */
    private static void backtrack(List<String> remaining, Grid grid, Placement current) {
        nodesProcessed++;
        if (nodesProcessed % LOG_INTERVAL == 0) {
            double depthPercent = ((double) current.words.size() / totalWords) * 100.0;
            double remainPercent = ((double) remaining.size() / totalWords) * 100.0;
            System.out.printf("Processed nodes: %d, current depth: %d (%.1f%%), words remaining: %d (%.1f%%)%n",
                    nodesProcessed, current.words.size(), depthPercent, remaining.size(), remainPercent);
        }
        if (bestPlacement == null || current.totalLetters > bestPlacement.totalLetters) {
            bestPlacement = new Placement(current);
        }
        if (remaining.isEmpty()) return;
        for (String candidate : new ArrayList<>(remaining)) {
            boolean placedCandidate = false;
            for (WordData placedWD : new ArrayList<>(current.words)) {
                if (placedWD.dir == 2) {
                    for (int p = 1; p < placedWD.text.length(); p++) {
                        for (int q = 0; q < candidate.length(); q++) {
                            if (placedWD.text.charAt(p) == candidate.charAt(q)) {
                                int baseX = placedWD.pos[0];
                                int baseY = placedWD.pos[1];
                                int candidateX = baseX + p;
                                int candidateY = baseY - q;
                                if (candidateY < 0) candidateY = 0;
                                if (!grid.canPlaceWithGap(candidate, candidateX, candidateY, 1)) continue;
                                grid.place(candidate, candidateX, candidateY, 1);
                                WordData candidateWD = new WordData(1, new int[]{candidateX, candidateY, 0}, candidate);
                                current.words.add(candidateWD);
                                current.totalLetters += candidate.length();
                                List<String> next = new ArrayList<>(remaining);
                                next.remove(candidate);
                                backtrack(next, grid, current);
                                current.words.remove(current.words.size() - 1);
                                current.totalLetters -= candidate.length();
                                grid.remove(candidate, candidateX, candidateY, 1);
                                placedCandidate = true;
                            }
                        }
                    }
                } else if (placedWD.dir == 1) {
                    for (int p = 1; p < placedWD.text.length(); p++) {
                        for (int q = 0; q < candidate.length(); q++) {
                            if (placedWD.text.charAt(p) == candidate.charAt(q)) {
                                int baseX = placedWD.pos[0];
                                int baseY = placedWD.pos[1];
                                int candidateX = baseX - q;
                                int candidateY = baseY + p;
                                if (candidateX < 0) candidateX = 0;
                                if (!grid.canPlaceWithGap(candidate, candidateX, candidateY, 2)) continue;
                                grid.place(candidate, candidateX, candidateY, 2);
                                WordData candidateWD = new WordData(2, new int[]{candidateX, candidateY, 0}, candidate);
                                current.words.add(candidateWD);
                                current.totalLetters += candidate.length();
                                List<String> next = new ArrayList<>(remaining);
                                next.remove(candidate);
                                backtrack(next, grid, current);
                                current.words.remove(current.words.size() - 1);
                                current.totalLetters -= candidate.length();
                                grid.remove(candidate, candidateX, candidateY, 2);
                                placedCandidate = true;
                            }
                        }
                    }
                }
            }
            if (!placedCandidate) {
                System.out.println("No intersection found for: " + candidate);
            }
        }
    }


    public static TowerData buildCrossword(InputData inputData) {
        totalWords = inputData.words.size();
        // Инициализируем поле
        int width = 30, height = 30;
        if (inputData.mapSize != null && inputData.mapSize.length >= 2) {
            width = inputData.mapSize[0];
            height = inputData.mapSize[1];
        }
        Grid grid = new Grid(width, height);

        // Фиксируем базовое слово: первое слово размещаем горизонтально с позицией (0, height/2)
        int R = height / 2;
        String base = inputData.words.get(0);
        if (!grid.canPlace(base, 0, R, 2)) {
            System.err.println("Cannot place base word.");
            return null;
        }
        grid.place(base, 0, R, 2);
        Placement initial = new Placement();
        initial.words.add(new WordData(2, new int[]{0, R, 0}, base));
        initial.totalLetters += base.length();

        // Запускаем backtracking для оставшихся слов
        List<String> remaining = new ArrayList<>(inputData.words.subList(1, inputData.words.size()));
        backtrack(remaining, grid, initial);

        if (bestPlacement == null || bestPlacement.words.isEmpty()) {
            System.err.println("No valid configuration found.");
            return null;
        }

        // Формируем итоговый TowerData
        Tower tower = new Tower();
        tower.score = bestPlacement.words.size();
        tower.words = bestPlacement.words;
        TowerData towerData = new TowerData();
        towerData.doneTowers = null;
        towerData.score = bestPlacement.words.size();
        towerData.tower = tower;
        return towerData;
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        File inputFile = new File("src/main/java/json/input.json");
        InputData inputData = mapper.readValue(inputFile, InputData.class);
        if (inputData.words == null || inputData.words.isEmpty()) {
            System.err.println("Input JSON contains no words.");
            return;
        }

        TowerData result = buildCrossword(inputData);
        if (result == null) {
            System.err.println("Failed to build a crossword configuration.");
            return;
        }

        File outputFile = new File("src/main/java/json/output.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, result);
    }
}
