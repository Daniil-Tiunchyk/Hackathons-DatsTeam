package dev.datscity.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datscity.model.ApiResponses.*;
import dev.datscity.model.WordPlacement;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Класс для обращения к REST API игры.
 * Выполняет GET и POST запросы, парсит JSON-ответы через Jackson.
 * Логирует каждый шаг для отладки.
 */
public class GameApiClient {
    private final String baseUrl;
    private final String apiToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public GameApiClient(String baseUrl, String apiToken) {
        this.baseUrl = baseUrl;
        this.apiToken = apiToken;
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper();
        System.out.println("[GameApiClient] Инициализирован с baseUrl=" + baseUrl);
    }

    /**
     * GET /api/words - Получить список слов и информацию о ходе.
     */
    public PlayerExtendedWordsResponse getWords() {
        System.out.println("[GameApiClient] Выполняется GET /api/words");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/words"))
                    .header("X-Auth-Token", apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[GameApiClient] Ответ GET /api/words, код: " + response.statusCode());
            if (response.statusCode() != 200) {
                System.out.println("[GameApiClient] Ошибка в GET /api/words, тело: " + response.body());
                throw new ApiException("getWords failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            PlayerExtendedWordsResponse wordsResponse = objectMapper.readValue(response.body(), PlayerExtendedWordsResponse.class);
            System.out.println("[GameApiClient] Получено слов: " + wordsResponse.words.size());
            return wordsResponse;
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in getWords", e);
        }
    }

    /**
     * POST /api/build - Отправка запроса на строительство или достройку башни.
     *
     * @param placements список размещений слов.
     * @param done       флаг завершения башни.
     * @return PlayerWordsResponse с обновлённым списком слов.
     */
    public PlayerWordsResponse sendBuild(List<WordPlacement> placements, boolean done) {
        System.out.println("[GameApiClient] Выполняется POST /api/build, placements=" + placements.size() + ", done=" + done);
        try {
            PlayerBuildRequest buildReq = new PlayerBuildRequest();
            buildReq.done = done;
            // Формируем массив запросов для каждого слова
            for (WordPlacement wp : placements) {
                TowerWordRequest req = new TowerWordRequest();
                req.id = wp.getIndex();   // используем индекс слова
                // Здесь происходит маппинг внутреннего представления направления в допустимое API:
                // Внутреннее значение 0 (по X) -> 2 ([1,0,0]), 1 (по Y) -> 3 ([0,1,0])
                req.dir = mapDirection(wp.getDir());
                req.pos = new int[]{wp.getX(), wp.getY(), wp.getZ()};
                buildReq.words.add(req);
            }

            String json = objectMapper.writeValueAsString(buildReq);
            System.out.println("[GameApiClient] JSON запроса: " + json);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/build"))
                    .header("X-Auth-Token", apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[GameApiClient] Ответ POST /api/build, код: " + response.statusCode());
            if (response.statusCode() != 200) {
                System.out.println("[GameApiClient] Ошибка POST /api/build, тело: " + response.body());
                throw new ApiException("sendBuild failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            PlayerWordsResponse wordsResponse = objectMapper.readValue(response.body(), PlayerWordsResponse.class);
            System.out.println("[GameApiClient] После build, осталось shuffleLeft=" + wordsResponse.shuffleLeft);
            return wordsResponse;
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in sendBuild", e);
        }
    }

    /**
     * Приватный метод для маппинга внутреннего представления направления в допустимые значения API.
     * Внутреннее: 0 – по X, 1 – по Y.
     * API: 2 для [1,0,0] (по X), 3 для [0,1,0] (по Y).
     */
    private int mapDirection(int internalDir) {
        if (internalDir == 0) {
            return 2;
        } else if (internalDir == 1) {
            return 3;
        }
        return internalDir; // На случай, если появятся иные значения
    }

    /**
     * POST /api/shuffle - Запрос нового набора слов.
     */
    public PlayerWordsResponse shuffleWords() {
        System.out.println("[GameApiClient] Выполняется POST /api/shuffle");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/shuffle"))
                    .header("X-Auth-Token", apiToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[GameApiClient] Ответ POST /api/shuffle, код: " + response.statusCode());
            if (response.statusCode() != 200) {
                System.out.println("[GameApiClient] Ошибка в shuffle, тело: " + response.body());
                throw new ApiException("shuffleWords failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            PlayerWordsResponse wordsResponse = objectMapper.readValue(response.body(), PlayerWordsResponse.class);
            System.out.println("[GameApiClient] Получено слов после shuffle: " + wordsResponse.words.size());
            return wordsResponse;
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in shuffleWords", e);
        }
    }

    /**
     * GET /api/towers - Получить информацию о завершённых и текущей башне.
     */
    public TowerData getTowers() {
        System.out.println("[GameApiClient] Выполняется GET /api/towers");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/towers"))
                    .header("X-Auth-Token", apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[GameApiClient] Ответ GET /api/towers, код: " + response.statusCode());
            if (response.statusCode() != 200) {
                System.out.println("[GameApiClient] Ошибка в GET /api/towers, тело: " + response.body());
                throw new ApiException("getTowers failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            TowerData towerData = objectMapper.readValue(response.body(), TowerData.class);
            System.out.println("[GameApiClient] Получено общих очков: " + towerData.score);
            return towerData;
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in getTowers", e);
        }
    }

    /**
     * GET /api/rounds - Получить информацию о раундах.
     */
    public RoundListResponse getRounds() {
        System.out.println("[GameApiClient] Выполняется GET /api/rounds");
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/rounds"))
                    .header("X-Auth-Token", apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("[GameApiClient] Ответ GET /api/rounds, код: " + response.statusCode());
            if (response.statusCode() != 200) {
                System.out.println("[GameApiClient] Ошибка в GET /api/rounds, тело: " + response.body());
                throw new ApiException("getRounds failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            RoundListResponse roundsResponse = objectMapper.readValue(response.body(), RoundListResponse.class);
            System.out.println("[GameApiClient] Получено раундов: " + roundsResponse.rounds.size());
            return roundsResponse;
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in getRounds", e);
        }
    }
}
