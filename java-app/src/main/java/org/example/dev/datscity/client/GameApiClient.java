package org.example.dev.datscity.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dev.datscity.model.ApiResponses.*;
import org.example.dev.datscity.model.WordPlacement;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

/**
 * Класс, инкапсулирующий работу с API DatsCity:
 * - хранит базовый URL, токен
 * - выполняет HTTP-запросы (GET/POST)
 * - парсит JSON-ответы в модельные объекты
 * <p>
 * Требует Jackson для парсинга JSON (com.fasterxml.jackson.databind).
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
    }

    /**
     * Запрос списка слов и информации по текущему ходу.
     * GET /api/words
     */
    public PlayerExtendedWordsResponse getWords() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/words"))
                    .header("X-Auth-Token", apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("getWords failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            return objectMapper.readValue(response.body(), PlayerExtendedWordsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in getWords", e);
        }
    }

    /**
     * Отправка POST /api/build для размещения слов.
     *
     * @param placements список размещений слов
     * @param done       флаг завершения башни
     * @return ответ сервера с новым списком слов (или пустым при done)
     */
    public PlayerWordsResponse sendBuild(List<WordPlacement> placements, boolean done) {
        try {
            PlayerBuildRequest buildReq = new PlayerBuildRequest();
            buildReq.done = done;
            for (WordPlacement wp : placements) {
                TowerWordRequest req = new TowerWordRequest();
                req.id = wp.getIndex();
                req.pos = new int[]{wp.getX(), wp.getY(), wp.getZ()};

                // Преобразуем внутренние направления в API-коды:
                // 0 => 2 = [1,0,0] (X), 1 => 3 = [0,1,0] (Y)
                if (wp.getDir() == 0) {
                    req.dir = 2;
                } else if (wp.getDir() == 1) {
                    req.dir = 3;
                } else if (wp.getDir() == 2) {
                    req.dir = 1; // вертикально вниз (если реализовано)
                } else {
                    throw new ApiException("Unknown direction: " + wp.getDir());
                }

                buildReq.words.add(req);
            }

            String json = objectMapper.writeValueAsString(buildReq);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/build"))
                    .header("X-Auth-Token", apiToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("sendBuild failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            return objectMapper.readValue(response.body(), PlayerWordsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in sendBuild", e);
        }
    }

    /**
     * POST /api/shuffle
     * Запросить новый набор слов
     */
    public PlayerWordsResponse shuffleWords() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/shuffle"))
                    .header("X-Auth-Token", apiToken)
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("shuffleWords failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            return objectMapper.readValue(response.body(), PlayerWordsResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in shuffleWords", e);
        }
    }

    /**
     * GET /api/towers
     * Возвращает информацию по завершённым башням и текущей башне.
     */
    public PlayerResponse getTowers() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/towers"))
                    .header("X-Auth-Token", apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("getTowers failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            return objectMapper.readValue(response.body(), PlayerResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in getTowers", e);
        }
    }

    /**
     * GET /api/rounds
     * Возвращает информацию о раундах игры.
     */
    public RoundListResponse getRounds() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/rounds"))
                    .header("X-Auth-Token", apiToken)
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ApiException("getRounds failed, code: " + response.statusCode() + ", body: " + response.body());
            }

            return objectMapper.readValue(response.body(), RoundListResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new ApiException("Exception in getRounds", e);
        }
    }
}
