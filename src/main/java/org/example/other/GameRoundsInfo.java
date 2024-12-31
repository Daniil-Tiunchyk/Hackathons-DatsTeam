package org.example.other;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GameRoundsInfo {
    private final String baseUrl;
    private final String token;
    private final Gson gson;

    public GameRoundsInfo(String baseUrl, String token) {
        this.baseUrl = baseUrl;
        this.token = token;
        this.gson = new Gson();
    }

    private String sendGet(String endpoint) throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .header("X-Auth-Token", token)
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public void printRoundsInfo() {
        try {
            System.out.println("=====================================");
            System.out.println("Загрузка информации о раундах...");
            System.out.println("=====================================");

            String response = sendGet("/rounds/snake3d");
            JsonObject responseObject = gson.fromJson(response, JsonObject.class);

            // Название игры
            String gameName = responseObject.get("gameName").getAsString();
            System.out.println("Название игры: " + gameName);
            System.out.println("-------------------------------------");

            // Массив раундов
            JsonArray rounds = responseObject.getAsJsonArray("rounds");
            System.out.println("Раунды:");
            for (int i = 0; i < rounds.size(); i++) {
                JsonObject roundObj = rounds.get(i).getAsJsonObject();
                System.out.printf("  Раунд %d:\n", i + 1);
                System.out.println("    Название: " + roundObj.get("name").getAsString());
                System.out.println("    Начало: " + roundObj.get("startAt").getAsString());
                System.out.println("    Конец: " + roundObj.get("endAt").getAsString());
                System.out.println("    Длительность: " + roundObj.get("duration").getAsInt() + " секунд");
                System.out.println("    Статус: " + roundObj.get("status").getAsString());
                System.out.println("    Повтор: " + roundObj.get("repeat").getAsInt());
                System.out.println("-------------------------------------");
            }

            // Текущее время сервера
            String currentTime = responseObject.get("now").getAsString();
            System.out.println("Текущее время сервера: " + currentTime);
            System.out.println("=====================================");

        } catch (IOException e) {
            System.err.println("Произошла ошибка ввода/вывода при получении информации о раундах.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Запрос был прерван.");
        } catch (Exception e) {
            System.err.println("Произошла непредвиденная ошибка: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        String baseUrl = "https://games-test.datsteam.dev/rounds/snake3d";
        String token = "a46f1665-024c-4742-a5c4-b38590830ca2";

        GameRoundsInfo roundsInfo = new GameRoundsInfo(baseUrl, token);
        roundsInfo.printRoundsInfo();
    }
}
