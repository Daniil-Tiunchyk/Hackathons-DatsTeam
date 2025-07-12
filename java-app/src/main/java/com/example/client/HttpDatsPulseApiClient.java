package com.example.client;

import com.google.gson.Gson;
import com.example.config.GameConfig;
import com.example.domain.Hex;
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
 * <p>
 * Этот класс является "Антикоррупционным слоем", отвечая за преобразование
 * систем координат (odd-r <-> axial) на границе приложения.
 */
public class HttpDatsPulseApiClient implements DatsPulseApiClient {

    private static final String AUTH_HEADER = "X-Auth-Token";
    private final GameConfig config;
    private final HttpClient httpClient;
    private final Gson gson;

    public HttpDatsPulseApiClient(GameConfig config, Gson gson) { // Принимаем Gson извне
        this.config = config;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = gson; // Используем предоставленный экземпляр
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
            ArenaStateDto rawState = gson.fromJson(response.body(), ArenaStateDto.class);
            return convertArenaStateToAxial(rawState);
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

        List<MoveCommandDto> convertedMoves = convertMoveCommandsToOddr(moves);
        MoveRequestDto requestBody = new MoveRequestDto(convertedMoves);
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

    private ArenaStateDto convertArenaStateToAxial(ArenaStateDto rawState) {
        if (rawState == null) {
            return null;
        }
        return new ArenaStateDto(
                rawState.ants().stream().map(this::convertAntToAxial).toList(),
                rawState.enemies().stream().map(this::convertEnemyToAxial).toList(),
                rawState.food().stream().map(this::convertFoodToAxial).toList(),
                rawState.home().stream().map(CoordinateConverter::oddrToAxial).toList(),
                rawState.map().stream().map(this::convertMapCellToAxial).toList(),
                null,
                null,
                rawState.nextTurnIn(),
                rawState.score(),
                CoordinateConverter.oddrToAxial(rawState.spot()),
                rawState.turnNo()
        );
    }

    private ArenaStateDto.AntDto convertAntToAxial(ArenaStateDto.AntDto ant) {
        Hex axialHex = CoordinateConverter.oddrToAxial(new Hex(ant.q(), ant.r()));
        return new ArenaStateDto.AntDto(ant.id(), ant.type(), axialHex.q(), axialHex.r(), ant.health(), ant.food());
    }

    private ArenaStateDto.EnemyDto convertEnemyToAxial(ArenaStateDto.EnemyDto enemy) {
        Hex axialHex = CoordinateConverter.oddrToAxial(new Hex(enemy.q(), enemy.r()));
        return new ArenaStateDto.EnemyDto(enemy.type(), axialHex.q(), axialHex.r(), enemy.health(), enemy.food());
    }

    private ArenaStateDto.FoodDto convertFoodToAxial(ArenaStateDto.FoodDto food) {
        Hex axialHex = CoordinateConverter.oddrToAxial(new Hex(food.q(), food.r()));
        return new ArenaStateDto.FoodDto(axialHex.q(), axialHex.r(), food.type(), food.amount());
    }

    private ArenaStateDto.MapCellDto convertMapCellToAxial(ArenaStateDto.MapCellDto cell) {
        Hex axialHex = CoordinateConverter.oddrToAxial(new Hex(cell.q(), cell.r()));
        return new ArenaStateDto.MapCellDto(axialHex.q(), axialHex.r(), cell.type(), cell.cost());
    }

    private List<MoveCommandDto> convertMoveCommandsToOddr(List<MoveCommandDto> axialMoves) {
        return axialMoves.stream()
                .map(cmd -> new MoveCommandDto(
                        cmd.ant(),
                        cmd.path().stream().map(CoordinateConverter::axialToOddr).toList()
                ))
                .toList();
    }
}
