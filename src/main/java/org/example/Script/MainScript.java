package org.example.Script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.models.move.GameState;
import org.example.models.move.MoveResponse;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainScript {

    // Токен авторизации
    private static final String AUTH_TOKEN = "67038c0234b8867038c0234b8a";

    // Метод для отправки POST-запроса с пустым массивом и получения ответа
    public static String sendMoveRequest(String serverUrl, MoveResponse moveRequest) throws IOException {


        URL url = new URL(serverUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        // Устанавливаем заголовки
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("X-Auth-Token", AUTH_TOKEN);
        connection.setDoOutput(true);

        // Преобразуем объект MoveRequest с пустым массивом в JSON
        Gson gson = new GsonBuilder().create();

        String jsonInputString = gson.toJson(moveRequest);

        try (OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
            writer.write(jsonInputString);
            writer.flush();
        }

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return content.toString();
        } else {
            // Если сервер вернул ошибку, читаем и возвращаем её
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            throw new RuntimeException("Ошибка: " + responseCode + " - " + content);
        }

    }

    // Метод для форматированного вывода JSON
    public static String prettyPrintJson(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Object jsonObject = gson.fromJson(json, Object.class);
        return gson.toJson(jsonObject);
    }

    // Метод для сохранения JSON в файл
    public static void saveJsonToFile(String json, String filePath) throws IOException {
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(prettyPrintJson(json));  // Записываем красиво отформатированный JSON в файл
        }
    }

    // Основной метод
    public static void main(String[] args) {

        // Основной сервер
        // String serverUrl = "https://games.datsteam.dev/";

        // Тестовый сервер
        String serverUrl = "https://games-test.datsteam.dev/play/magcarp/player/move";

        try {
            while (true) {

                // Отправляем запрос с пустым массивом и получаем ответ
                String response = sendMoveRequest(serverUrl, new MoveResponse());
                Gson gson = new Gson();
                GameState gameState = gson.fromJson(response, GameState.class);
                MoveResponse moveResponse;

                ShootScript shootScript = new ShootScript();
                ShieldScript shieldScript = new ShieldScript();

                //moveResponse = MoveScript.processGameState(gameState);
                TransportController transportController = new TransportController();
                moveResponse = transportController.planTransportMovements(gameState);

                moveResponse.setTransports(shootScript.shoot(moveResponse.getTransports(), gameState));
                moveResponse.setTransports(shieldScript.shieldSuitability(moveResponse.getTransports(), gameState));
                response = sendMoveRequest(serverUrl, moveResponse);

                // Сохраняем ответ в файл в корневой папке проекта
                String filePath = System.getProperty("user.dir") + "/response.json";
                String fileRequestPath = System.getProperty("user.dir") + "/request.json";
                saveJsonToFile(response, filePath);

                saveJsonToFile(gson.toJson(moveResponse), fileRequestPath);

                // System.out.println("JSON-ответ сохранен в файл: " + filePath);
                Thread.sleep(1000);
            }
        } catch (IOException e) {
            System.err.println("Произошла ошибка при выполнении запроса: " + e.getMessage());
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
