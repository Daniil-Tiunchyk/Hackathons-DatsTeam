package com.example;

import com.example.client.DatsPulseApiClient;
import com.example.client.HttpDatsPulseApiClient;
import com.example.config.GameConfig;
import com.example.service.GameService;
import com.example.ui.ConsoleDisplay;

/**
 * Главная точка входа для клиентского приложения DatsPulse.
 * Инициализирует все необходимые компоненты и запускает игровой цикл.
 */
public class DatsPulseApplication {

    public static void main(String[] args) {
        System.out.println("Запуск клиента DatsPulse...");

        GameConfig config = new GameConfig();
        DatsPulseApiClient apiClient = new HttpDatsPulseApiClient(config);
        ConsoleDisplay consoleDisplay = new ConsoleDisplay();

        // Это ручная форма Внедрения Зависимостей (Dependency Injection).
        GameService gameService = new GameService(apiClient, consoleDisplay);

        System.out.println("Клиент запущен. Запускаю игровой цикл...");
        gameService.run();
    }
}
