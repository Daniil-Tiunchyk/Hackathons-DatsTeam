package org.example.dev.datscity.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Содержит вложенные классы-модели для парсинга JSON-ответов от сервера.
 * (Взято из OpenAPI-спецификации и адаптировано.)
 */
public class ApiResponses {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerExtendedWordsResponse {
        public int[] mapSize;         // [x, y, z] например [30,30,100]
        public int nextTurnSec;       // время до следующего хода
        public String roundEndsAt;    // время окончания раунда
        public int shuffleLeft;       // сколько осталось перетасовок
        public int turn;              // номер текущего хода
        public List<Integer> usedIndexes; // индексы уже использованных слов
        public List<String> words;    // текущий список слов
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerWordsResponse {
        public int shuffleLeft;
        public List<String> words;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerBuildRequest {
        public boolean done;
        public List<TowerWordRequest> words = new ArrayList<>();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TowerWordRequest {
        public int dir;  // направление
        public int id;   // индекс слова
        public int[] pos; // [x, y, z]
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerResponse {
        public List<DoneTowerResponse> doneTowers;
        public double score;
        public PlayerTowerResponse tower;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DoneTowerResponse {
        public int id;
        public double score;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerTowerResponse {
        public double score;
        public List<PlayerWord> words;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerWord {
        public int dir;
        public int[] pos; // [x, y, z]
        public String text;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoundListResponse {
        public String eventId;
        public String now;
        public List<RoundResponse> rounds;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RoundResponse {
        public int duration;
        public String endAt;
        public String name;
        public int repeat;
        public String startAt;
        public String status;
    }
}
