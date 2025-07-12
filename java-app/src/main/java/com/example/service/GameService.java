package com.example.service;

import com.example.client.DatsPulseApiClient;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;
import com.example.ui.ConsoleDisplay;
import com.example.util.Stopwatch;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Оркестрирует основной игровой цикл, координируя взаимодействие между
 * сервисами и измеряя производительность каждого этапа.
 */
public class GameService {

    private static final int REGISTRATION_RETRY_DELAY_SECONDS = 5;
    private static final int ERROR_RETRY_DELAY_SECONDS = 2;
    private static final long MINIMUM_REQUEST_INTERVAL_MS = 350;

    private final DatsPulseApiClient apiClient;
    private final ConsoleDisplay consoleDisplay;
    private final StrategyService strategyService;
    private final MapStateService mapStateService;
    private final Stopwatch turnTimer;

    public GameService(DatsPulseApiClient apiClient, ConsoleDisplay consoleDisplay, StrategyService strategyService, MapStateService mapStateService, Stopwatch stopwatch) {
        this.apiClient = apiClient;
        this.consoleDisplay = consoleDisplay;
        this.strategyService = strategyService;
        this.mapStateService = mapStateService;
        this.turnTimer = stopwatch;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                turnTimer.reset(); // Начинаем замер хода

                // --- Этап 1: API Call ---
                ArenaStateDto apiResponse = apiClient.getArenaState();
                long apiCallTime = turnTimer.getElapsedTimeMillis();

                if (weAreNotInGame(apiResponse)) {
                    handleRegistration();
                    continue;
                }

                // --- Этап 2: Обновление состояния карты ---
                turnTimer.reset();
                ArenaStateDto worldState = mapStateService.updateAndGet(apiResponse);
                long mapUpdateTime = turnTimer.getElapsedTimeMillis();

                // --- Этап 3: Расчет стратегии ---
                turnTimer.reset();
                List<MoveCommandDto> moves = strategyService.createMoveCommands(worldState);
                long strategyTime = turnTimer.getElapsedTimeMillis();

                if (!moves.isEmpty()) {
                    apiClient.sendMoves(moves);
                }

                // --- Отображение ---
                ConsoleDisplay.PerformanceMetrics metrics = new ConsoleDisplay.PerformanceMetrics(apiCallTime, mapUpdateTime, strategyTime);
                consoleDisplay.render(worldState, moves, metrics);
                consoleDisplay.renderDebugComparison(apiResponse, worldState);

                waitForNextTurn(worldState.nextTurnIn(), apiCallTime + mapUpdateTime + strategyTime);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Игровой цикл прерван. Выход.");
            } catch (Exception e) {
                System.err.println("Произошла критическая ошибка в игровом цикле: " + e.getMessage());
                e.printStackTrace();
                try {
                    waitFor(ERROR_RETRY_DELAY_SECONDS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    System.out.println("Ожидание после ошибки прервано. Выход.");
                }
            }
        }
    }

    private boolean weAreNotInGame(ArenaStateDto state) {
        return state == null;
    }

    private void handleRegistration() throws InterruptedException {
        consoleDisplay.showRegistrationAttempt();
        RegistrationResponseDto response = apiClient.register();
        consoleDisplay.showRegistrationResult(response);
        waitFor(REGISTRATION_RETRY_DELAY_SECONDS);
    }

    private void waitForNextTurn(double secondsToNextTurn, long totalProcessingTime) throws InterruptedException {
        long apiWaitMillis = (long) (secondsToNextTurn * 1000);
        long timeToWait = Math.max(0, apiWaitMillis - totalProcessingTime);
        long sleepMillis = MINIMUM_REQUEST_INTERVAL_MS;

        TimeUnit.MILLISECONDS.sleep(sleepMillis);
    }

    private void waitFor(int seconds) throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(seconds);
    }
}
