package org.example.GET;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class Round {
    private String name;
    private String startAt;
    private String endAt;
    private int duration;
    private String status;
    private int repeat;
}

class GameRoundsResponse {
    private String gameName;
    private String now;
    private Round[] rounds;
}

public class GameRoundsClient {

    private static final String AUTH_TOKEN = "67038c0234b8867038c0234b8a";

    // Метод для выполнения запроса и получения объекта GameRoundsResponse
    public static GameRoundsResponse getGameRounds(String serverUrl) throws IOException {
        URL url = new URL(serverUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        connection.setRequestProperty("X-Auth-Token", AUTH_TOKEN);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }

            in.close();

            Gson gson = new GsonBuilder().create();
            return gson.fromJson(content.toString(), GameRoundsResponse.class);
        } else {
            throw new RuntimeException("Ошибка: " + responseCode + " - " + connection.getResponseMessage());
        }
    }

    // Метод для красивого вывода JSON
    public static String prettyPrintJson(GameRoundsResponse response) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(response);
    }

    // Основной метод для теста
    public static void main(String[] args) {
        // Основной сервер
        // String serverUrl = "https://games.datsteam.dev/rounds/magcarp";

        // Тестовый сервер
         String serverUrl = "https://games-test.datsteam.dev/rounds/magcarp";

        try {
            GameRoundsResponse response = getGameRounds(serverUrl);

            System.out.println("JSON:");
            System.out.println(prettyPrintJson(response));

        } catch (IOException e) {
            System.err.println("Произошла ошибка при выполнении запроса: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}
