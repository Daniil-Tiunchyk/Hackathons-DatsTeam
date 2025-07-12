package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.example.client.DatsPulseApiClient;
import com.example.client.HttpDatsPulseApiClient;
import com.example.config.GameConfig;
import com.example.service.*;
import com.example.ui.ConsoleDisplay;

/**
 * Главная точка входа для клиентского приложения DatsPulse.
 * Инициализирует все необходимые компоненты и запускает игровой цикл.
 */
public class DatsPulseApplication {

    public static void main(String[] args) {
        try {
            System.out.println("Запуск клиента DatsPulse...");

            // Создаем единый, настроенный экземпляр Gson для всего приложения.
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            GameConfig config = new GameConfig();
            // Передаем Gson в API клиент.
            DatsPulseApiClient apiClient = new HttpDatsPulseApiClient(config, gson);
            ConsoleDisplay consoleDisplay = new ConsoleDisplay();
            MapStateService mapStateService = new MapStateService();

            Pathfinder pathfinder = new Pathfinder();
            StrategyProvider strategyProvider = new StrategyProvider(pathfinder);
            StrategyService strategyService = new StrategyService(strategyProvider, pathfinder);
            // Передаем все зависимости, включая Gson, в GameService.
            GameService gameService = new GameService(apiClient, consoleDisplay, strategyService, mapStateService, gson);

            System.out.println("Клиент запущен. Вступаем в игру...");
            gameService.run();

        } catch (Exception e) {
            System.err.println("Критическая ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
