package com.example.service;

import com.example.client.DatsPulseApiClient;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;
import com.example.ui.ConsoleDisplay;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Оркестрирует основной игровой цикл, координируя взаимодействие между
 * API-клиентом, сервисом стратегии и пользовательским интерфейсом.
 * Управляет состоянием "в игре" / "ожидание регистрации" и корректно
 * обрабатывает прерывания потока.
 */
public class GameService {

    private static final int REGISTRATION_RETRY_DELAY_SECONDS = 30;
    private static final int ERROR_RETRY_DELAY_SECONDS = 5;
    /**
     * Минимальный интервал между запросами к API в миллисекундах.
     * Установлен в 500мс для гарантии не более 2 запросов в секунду,
     * что соответствует требованиям rate-лимита API (3 RPS).
     */
    private static final long MINIMUM_REQUEST_INTERVAL_MS = 500;


    private final DatsPulseApiClient apiClient;
    private final ConsoleDisplay consoleDisplay;
    private final StrategyService strategyService;


    public GameService(DatsPulseApiClient apiClient, ConsoleDisplay consoleDisplay, StrategyService strategyService) {
        this.apiClient = apiClient;
        this.consoleDisplay = consoleDisplay;
        this.strategyService = strategyService;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long turnStartTime = System.currentTimeMillis();
                ArenaStateDto currentState = apiClient.getArenaState();

                if (weAreNotInGame(currentState)) {
                    handleRegistration();
                    continue;
                }

                List<MoveCommandDto> moves = strategyService.createMoveCommands(currentState);
                if (!moves.isEmpty()) {
                    apiClient.sendMoves(moves);
                }

                consoleDisplay.render(currentState, moves);

                waitForNextTurn(currentState.nextTurnIn(), turnStartTime);

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

    private void waitForNextTurn(double secondsToNextTurn, long turnStartTime) throws InterruptedException {
        long apiWaitMillis = (long) (secondsToNextTurn * 1000);
        long processingTime = System.currentTimeMillis() - turnStartTime;
        long timeToWait = Math.max(0, apiWaitMillis - processingTime);
        long sleepMillis = Math.max(MINIMUM_REQUEST_INTERVAL_MS, timeToWait);

        TimeUnit.MILLISECONDS.sleep(sleepMillis);
    }

    private void waitFor(int seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }
}
