package dev.datscity;

import com.google.gson.Gson;

import java.io.FileReader;
import java.io.Reader;
import java.util.*;

public class CrosswordSolver {

    // Параметры сетки будут заданы из JSON-файла
    static int GRID_ROWS, GRID_COLS;
    static char[][] grid;
    static boolean[] used;

    // Глобальные переменные для отслеживания наилучшего результата
    static int bestScore = 0;
    static List<Placement> bestPlacement = new ArrayList<>();

    // Слова, загружаемые из JSON-файла
    static String[] words;

    // Класс для чтения конфигурации из JSON
    static class Config {
        List<Integer> mapSize;
        int nextTurnSec;
        String roundEndsAt;
        int shuffleLeft;
        int turn;
        List<String> words;
    }

    // Класс для хранения информации о размещении слова
    static class Placement {
        String word;
        int row, col;
        boolean horizontal; // true - горизонтальное, false - вертикальное

        public Placement(String word, int row, int col, boolean horizontal) {
            this.word = word;
            this.row = row;
            this.col = col;
            this.horizontal = horizontal;
        }
    }

    public static void main(String[] args) {
        // Чтение конфигурации из файла JSON
        Gson gson = new Gson();
        try (Reader reader = new FileReader("src/main/java/json/input.json")) {
            Config config = gson.fromJson(reader, Config.class);
            // Первые два числа массива mapSize задают размеры сетки
            GRID_ROWS = config.mapSize.get(0);
            GRID_COLS = config.mapSize.get(1);
            // Преобразуем список слов в массив
            words = config.words.toArray(new String[0]);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Инициализация сетки
        grid = new char[GRID_ROWS][GRID_COLS];
        resetGrid();
        used = new boolean[words.length];
        bestScore = 0;
        bestPlacement.clear();

        // Начинаем с размещения каждого слова как стартового (горизонтально в центре)
        int offsetRow = GRID_ROWS / 2;
        int offsetCol = GRID_COLS / 2;
        for (int i = 0; i < words.length; i++) {
            resetGrid();
            List<Placement> currentPlacement = new ArrayList<>();
            used[i] = true;
            if (placeWord(words[i], offsetRow, offsetCol, true)) {
                currentPlacement.add(new Placement(words[i], offsetRow, offsetCol, true));
                backtrack(currentPlacement, words[i].length());
                removeWord(words[i], offsetRow, offsetCol, true);
            }
            used[i] = false;
        }

        // Вывод результата: максимальная суммарная длина и список размещений
        System.out.println("Максимальная суммарная длина слов: " + bestScore);
        for (Placement p : bestPlacement) {
            System.out.println(p.word + " at (" + p.row + "," + p.col + ") " +
                    (p.horizontal ? "горизонтально" : "вертикально"));
        }

        // Формирование итоговой сетки для отображения кроссворда
        char[][] bestGrid = new char[GRID_ROWS][GRID_COLS];
        for (int i = 0; i < GRID_ROWS; i++) {
            Arrays.fill(bestGrid[i], '.');
        }
        for (Placement p : bestPlacement) {
            if (p.horizontal) {
                for (int j = 0; j < p.word.length(); j++) {
                    bestGrid[p.row][p.col + j] = p.word.charAt(j);
                }
            } else {
                for (int i = 0; i < p.word.length(); i++) {
                    bestGrid[p.row + i][p.col] = p.word.charAt(i);
                }
            }
        }

        // Определение границ области с буквами и вывод кроссворда
        int minRow = GRID_ROWS, maxRow = 0, minCol = GRID_COLS, maxCol = 0;
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                if (bestGrid[i][j] != '.') {
                    minRow = Math.min(minRow, i);
                    maxRow = Math.max(maxRow, i);
                    minCol = Math.min(minCol, j);
                    maxCol = Math.max(maxCol, j);
                }
            }
        }
        System.out.println("\nРезультат (кроссворд):");
        for (int i = minRow; i <= maxRow; i++) {
            for (int j = minCol; j <= maxCol; j++) {
                System.out.print(bestGrid[i][j]);
            }
            System.out.println();
        }
    }

    // Рекурсивный перебор (backtracking) с индексированным обходом размещённых слов
    // currentScore – суммарная длина размещённых слов
    static void backtrack(List<Placement> currentPlacement, int currentScore) {
        if (currentScore > bestScore) {
            bestScore = currentScore;
            bestPlacement = new ArrayList<>(currentPlacement);
        }

        // Индексированный цикл по уже размещённым словам
        for (int pi = 0; pi < currentPlacement.size(); pi++) {
            Placement placed = currentPlacement.get(pi);
            for (int pos1 = 0; pos1 < placed.word.length(); pos1++) {
                char letter = placed.word.charAt(pos1);
                for (int i = 0; i < words.length; i++) {
                    if (!used[i]) {
                        String word = words[i];
                        for (int pos2 = 0; pos2 < word.length(); pos2++) {
                            if (word.charAt(pos2) == letter) {
                                int newRow, newCol;
                                boolean horizontal;
                                if (placed.horizontal) {
                                    // Если уже размещённое слово горизонтальное, новое располагаем вертикально
                                    newRow = placed.row - pos2;
                                    newCol = placed.col + pos1;
                                    horizontal = false;
                                } else {
                                    // Если размещённое слово вертикальное, новое располагаем горизонтально
                                    newRow = placed.row + pos1;
                                    newCol = placed.col - pos2;
                                    horizontal = true;
                                }
                                if (canPlace(word, newRow, newCol, horizontal, pos2)) {
                                    placeWord(word, newRow, newCol, horizontal);
                                    used[i] = true;
                                    currentPlacement.add(new Placement(word, newRow, newCol, horizontal));
                                    backtrack(currentPlacement, currentScore + word.length());
                                    currentPlacement.remove(currentPlacement.size() - 1);
                                    removeWord(word, newRow, newCol, horizontal);
                                    used[i] = false;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Метод проверки возможности размещения слова без нежелательных касаний (за исключением пересечения)
    static boolean canPlace(String word, int row, int col, boolean horizontal, int intersectIndex) {
        if (horizontal) {
            if (col < 0 || col + word.length() > GRID_COLS) return false;
            if (row < 0 || row >= GRID_ROWS) return false;
            // Проверка слева и справа от слова
            if (col - 1 >= 0 && grid[row][col - 1] != '.') return false;
            if (col + word.length() < GRID_COLS && grid[row][col + word.length()] != '.') return false;
            for (int j = 0; j < word.length(); j++) {
                int cPos = col + j;
                char letter = word.charAt(j);
                char current = grid[row][cPos];
                if (j == intersectIndex) {
                    if (current != '.' && current != letter) return false;
                } else {
                    if (current != '.') return false;
                }
                // Проверка клеток сверху и снизу (если не точка пересечения)
                if (j != intersectIndex) {
                    if (row - 1 >= 0 && grid[row - 1][cPos] != '.') return false;
                    if (row + 1 < GRID_ROWS && grid[row + 1][cPos] != '.') return false;
                }
            }
        } else { // вертикальное размещение
            if (row < 0 || row + word.length() > GRID_ROWS) return false;
            if (col < 0 || col >= GRID_COLS) return false;
            // Проверка клеток над и под словом
            if (row - 1 >= 0 && grid[row - 1][col] != '.') return false;
            if (row + word.length() < GRID_ROWS && grid[row + word.length()][col] != '.') return false;
            for (int i = 0; i < word.length(); i++) {
                int rPos = row + i;
                char letter = word.charAt(i);
                char current = grid[rPos][col];
                if (i == intersectIndex) {
                    if (current != '.' && current != letter) return false;
                } else {
                    if (current != '.') return false;
                }
                // Проверка клеток слева и справа (если не точка пересечения)
                if (i != intersectIndex) {
                    if (col - 1 >= 0 && grid[rPos][col - 1] != '.') return false;
                    if (col + 1 < GRID_COLS && grid[rPos][col + 1] != '.') return false;
                }
            }
        }
        return true;
    }

    // Метод размещения слова в сетке
    static boolean placeWord(String word, int row, int col, boolean horizontal) {
        if (horizontal) {
            if (col < 0 || col + word.length() > GRID_COLS) return false;
            for (int j = 0; j < word.length(); j++) {
                grid[row][col + j] = word.charAt(j);
            }
        } else {
            if (row < 0 || row + word.length() > GRID_ROWS) return false;
            for (int i = 0; i < word.length(); i++) {
                grid[row + i][col] = word.charAt(i);
            }
        }
        return true;
    }

    // Удаление слова с сетки (для возврата в предыдущее состояние)
    static void removeWord(String word, int row, int col, boolean horizontal) {
        if (horizontal) {
            for (int j = 0; j < word.length(); j++) {
                grid[row][col + j] = '.';
            }
        } else {
            for (int i = 0; i < word.length(); i++) {
                grid[row + i][col] = '.';
            }
        }
    }

    // Инициализация сетки: заполняем точками
    static void resetGrid() {
        for (int i = 0; i < GRID_ROWS; i++) {
            Arrays.fill(grid[i], '.');
        }
    }
}
