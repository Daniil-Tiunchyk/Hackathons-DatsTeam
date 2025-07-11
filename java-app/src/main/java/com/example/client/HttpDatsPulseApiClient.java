package com.example.client;

import com.google.gson.Gson;
import com.example.config.GameConfig;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.MoveRequestDto;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * Реализация {@link DatsPulseApiClient}, использующая встроенный в Java HttpClient
 * для взаимодействия с игровым сервером по протоколу HTTP.
 */
public class HttpDatsPulseApiClient implements DatsPulseApiClient {

    private final GameConfig config;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpDatsPulseApiClient(GameConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = new Gson();
    }

    @Override
    public ArenaStateDto getArenaState() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getApiBaseUrl() + "/api/arena"))
                .header("Authorization", "Bearer " + config.getApiToken())
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            // Надежная реализация должна обрабатывать здесь статусы ответа, отличные от 200.
            return gson.fromJson(response.body(), ArenaStateDto.class);
        } catch (IOException | InterruptedException e) {
            // На соревновании требуется более продуманная обработка ошибок.
            Thread.currentThread().interrupt();
            throw new RuntimeException("Не удалось получить состояние арены", e);
        }
    }

    @Override
    public void sendMoves(List<MoveCommandDto> moves) {
        if (moves == null || moves.isEmpty()) {
            return;
        }

        MoveRequestDto requestBody = new MoveRequestDto(moves);
        String jsonPayload = gson.toJson(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getApiBaseUrl() + "/api/move"))
                .header("Authorization", "Bearer " + config.getApiToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            // Мы игнорируем тело ответа, так как не ожидаем содержимого при успехе.
            // Надежный клиент проверил бы статус ответа.
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Не удалось отправить ходы", e);
        }
    }
}
