package com.example;

import com.example.client.DatsPulseApiClient;
import com.example.client.HttpDatsPulseApiClient;
import com.example.config.GameConfig;
import com.example.service.GameService;
import com.example.service.Pathfinder;
import com.example.service.StrategyService;
import com.example.ui.ConsoleDisplay;

/**
 * Главная точка входа для клиентского приложения DatsPulse.
 * Инициализирует все необходимые компоненты и запускает игровой цикл.
 */
public class DatsPulseApplication {

    public static void main(String[] args) {
        try {
            System.out.println("Запуск клиента DatsPulse...");

            // --- Уровень Конфигурации и Инфраструктуры ---
            GameConfig config = new GameConfig();
            DatsPulseApiClient apiClient = new HttpDatsPulseApiClient(config);

            // --- Уровень Представления (UI) ---
            ConsoleDisplay consoleDisplay = new ConsoleDisplay();

            // --- Уровень Бизнес-логики ---
            Pathfinder pathfinder = new Pathfinder();
            StrategyService strategyService = new StrategyService(pathfinder);

            // --- Сборка и Запуск ---
            GameService gameService = new GameService(apiClient, consoleDisplay, strategyService);

            System.out.println("Клиент запущен. Вступаем в игру...");
            gameService.run();

        } catch (Exception e) {
            System.err.println("Критическая ошибка при запуске приложения: " + e.getMessage());
        }
    }
}
