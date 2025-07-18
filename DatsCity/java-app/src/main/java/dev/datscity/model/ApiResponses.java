package dev.datscity.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Классы-модели для парсинга JSON-ответов от сервера.
 * Все классы помечены @JsonIgnoreProperties(ignoreUnknown = true) для игнорирования лишних полей.
 */
public class ApiResponses {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerExtendedWordsResponse {
        public int[] mapSize;         // Пример: [30, 30, 100]
        public int nextTurnSec;       // Время до следующего хода
        public String roundEndsAt;    // Время окончания раунда
        public int shuffleLeft;       // Оставшиеся попытки перетасовки
        public int turn;              // Номер текущего хода
        public List<Integer> usedIndexes; // Использованные индексы
        public List<String> words;    // Текущий список слов

        public PlayerExtendedWordsResponse() {
            System.out.println("[ApiResponses] Получен PlayerExtendedWordsResponse");
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerWordsResponse {
        public int shuffleLeft;
        public List<String> words;

        public PlayerWordsResponse() {
            System.out.println("[ApiResponses] Получен PlayerWordsResponse");
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    public static class PlayerBuildRequest {
        public boolean done;
        public List<TowerWordRequest> words = new ArrayList<>();
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    public static class TowerWordRequest {
        public int dir;  // Направление
        public int id;   // Индекс слова
        public int[] pos; // Координаты [x, y, z]
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class TowerData {
        public List<DoneTower> doneTowers;
        public double score;
        public Tower tower;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    public static class DoneTower {
        public int id;
        public double score;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class Tower {
        public double score;
        public List<WordData> words;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class WordData {
        public int dir;
        public int[] pos; // [x, y, z]
        public String text;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    public static class RoundListResponse {
        public String eventId;
        public String now;
        public List<RoundResponse> rounds;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @RequiredArgsConstructor
    public static class RoundResponse {
        public int duration;
        public String endAt;
        public String name;
        public int repeat;
        public String startAt;
        public String status;
    }
}
