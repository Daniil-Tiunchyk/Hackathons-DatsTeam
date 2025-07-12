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
 * API-клиентом, сервисом состояния, сервисом стратегии и UI.
 */
public class GameService {

    private static final int REGISTRATION_RETRY_DELAY_SECONDS = 1;
    private static final int ERROR_RETRY_DELAY_SECONDS = 1;
    private static final long MINIMUM_REQUEST_INTERVAL_MS = 500;

    private final DatsPulseApiClient apiClient;
    private final ConsoleDisplay consoleDisplay;
    private final StrategyService strategyService;
    private final MapStateService mapStateService;

    public GameService(DatsPulseApiClient apiClient, ConsoleDisplay consoleDisplay, StrategyService strategyService, MapStateService mapStateService) {
        this.apiClient = apiClient;
        this.consoleDisplay = consoleDisplay;
        this.strategyService = strategyService;
        this.mapStateService = mapStateService;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                long turnStartTime = System.currentTimeMillis();
                ArenaStateDto apiResponse = apiClient.getArenaState();

                if (weAreNotInGame(apiResponse)) {
                    handleRegistration();
                    continue;
                }

                ArenaStateDto worldState = mapStateService.updateAndGet(apiResponse);

                List<MoveCommandDto> moves = strategyService.createMoveCommands(worldState);
                if (!moves.isEmpty()) {
                    apiClient.sendMoves(moves);
                }

                // Сначала выводим основной дашборд...
                consoleDisplay.render(worldState, moves);
                // ...а затем детальную отладочную информацию.
                consoleDisplay.renderDebugComparison(apiResponse, worldState);


                waitForNextTurn(worldState.nextTurnIn(), turnStartTime);

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