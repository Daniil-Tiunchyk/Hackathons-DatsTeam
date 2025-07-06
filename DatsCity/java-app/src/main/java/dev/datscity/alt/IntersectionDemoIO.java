package dev.datscity.alt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IntersectionDemoIO {

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
        public Object doneTowers;  // здесь null
        public double score;
        public Tower tower;
    }

    public static class Tower {
        public double score;
        public List<WordData> words;
    }

    public static class WordData {
        // dir: 1 = [0, 0, -1] (вертикальное), 2 = [1, 0, 0] (горизонтальное по X), 3 = [0, 1, 0] (горизонтальное по Y)
        public int dir;
        public int[] pos;    // координаты [x, y, z]
        public String text;

        public WordData() { }
        public WordData(int dir, int[] pos, String text) {
            this.dir = dir;
            this.pos = pos;
            this.text = text;
        }
    }

    /**
     * Универсальный метод поиска пересечения двух слов.
     * Алгоритм сначала пытается найти вариант, когда у кандидата используется его первая буква (q == 0) и базовое слово – не первая (p > 0).
     * Если такого варианта нет, перебирает все пары, где p > 0 и q > 0.
     * Возвращает массив [p, q], где p – индекс в базовом слове, q – индекс в кандидатском, либо null, если пересечение не найдено.
     */
    public static int[] findIntersection(String base, String candidate) {
        // Попытка 1: кандидат начинается с первой буквы, а базовое слово – не первая
        for (int p = 1; p < base.length(); p++) {
            if (base.charAt(p) == candidate.charAt(0)) {
                return new int[]{p, 0};
            }
        }
        // Попытка 2: перебор всех пар, где и p>0, и q>0
        for (int p = 1; p < base.length(); p++) {
            for (int q = 1; q < candidate.length(); q++) {
                if (base.charAt(p) == candidate.charAt(q)) {
                    return new int[]{p, q};
                }
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        // Настройка ObjectMapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        // Считываем входной JSON из файла src/main/java/json/input.json
        File inputFile = new File("src/main/java/json/input.json");
        InputData inputData = mapper.readValue(inputFile, InputData.class);
        if (inputData.words == null || inputData.words.size() < 2) {
            System.err.println("Входной JSON должен содержать минимум 2 слова.");
            return;
        }

        // Фиксируем базовое слово — первое слово, например "банан"
        String baseWord = inputData.words.get(0);
        // Размещаем базовое слово горизонтально в позиции (0, R) на плоскости (z = 0)
        // Для этого примера R = 2 (то есть базовое слово начинается с (0,2,0))
        int R = 2;
        // Формируем список выходных слов
        List<WordData> outputWords = new ArrayList<>();
        // Базовое слово размещается горизонтально по оси X, поэтому его направление = 2 ([1,0,0])
        outputWords.add(new WordData(2, new int[]{0, R, 0}, baseWord));

        // Список слов, для которых пересечение не найдено
        List<String> noIntersection = new ArrayList<>();

        // Обрабатываем остальные слова
        for (int i = 1; i < inputData.words.size(); i++) {
            String candidate = inputData.words.get(i);
            int[] intersect = findIntersection(baseWord, candidate);
            if (intersect == null) {
                noIntersection.add(candidate);
                continue;
            }
            int p = intersect[0]; // индекс в базовом слове
            int q = intersect[1]; // индекс в кандидатском слове

            // Вычисляем координаты для кандидатского слова (вертикальное размещение).
            // Базовое слово размещено горизонтально с началом в (0, R, 0).
            // Его символ с индексом p находится в (p, R, 0).
            // Вертикальное слово кладется так, что его символ с индексом q совпадает с этой точкой.
            int candidateX = p;
            // Вычисляем y для кандидатского слова: y + q = R  =>  y = R - q.
            int candidateY = R - q;
            if (candidateY < 0) candidateY = 0;
            // Вертикальное слово должно иметь направление 1 ([0, 0, -1])
            outputWords.add(new WordData(1, new int[]{candidateX, candidateY, 0}, candidate));
        }

        // Выводим в лог слова, для которых пересечения не найдены
        if (!noIntersection.isEmpty()) {
            System.out.println("Слова без пересечений: " + noIntersection);
        }

        // Формируем итоговый объект TowerData
        Tower tower = new Tower();
        tower.score = outputWords.size();
        tower.words = outputWords;

        TowerData towerData = new TowerData();
        towerData.doneTowers = null;
        towerData.score = outputWords.size();
        towerData.tower = tower;

        // Записываем результат в output.json
        File outputFile = new File("src/main/java/json/output.json");
        mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, towerData);
    }
}
