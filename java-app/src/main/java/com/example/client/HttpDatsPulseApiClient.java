package com.example.client;

import com.google.gson.Gson;
import com.example.config.GameConfig;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.MoveRequestDto;
import com.example.dto.RegistrationResponseDto;

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

    private static final String AUTH_HEADER = "X-Auth-Token";
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
                .header(AUTH_HEADER, config.getApiToken())
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200 || response.body() == null || response.body().isEmpty()) {
                return null;
            }
            return gson.fromJson(response.body(), ArenaStateDto.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Ошибка при получении состояния арены: " + e.getMessage());
            return null;
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
                .header(AUTH_HEADER, config.getApiToken())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Не удалось отправить ходы", e);
        }
    }

    @Override
    public RegistrationResponseDto register() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.getApiBaseUrl() + "/api/register"))
                .header(AUTH_HEADER, config.getApiToken())
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return gson.fromJson(response.body(), RegistrationResponseDto.class);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Не удалось отправить запрос на регистрацию", e);
        }
    }
}
