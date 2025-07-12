package com.example.service;

import com.google.gson.Gson;
import com.example.client.DatsPulseApiClient;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;
import com.example.ui.ConsoleDisplay;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Оркестрирует основной игровой цикл, координируя взаимодействие между
 * API-клиентом, сервисом состояния, сервисом стратегии и UI.
 * Также отвечает за сохранение состояния мира в файл для отладки.
 */
public class GameService {

    private static final String STATE_FILE_NAME = "main.json";
    private static final int REGISTRATION_RETRY_DELAY_SECONDS = 5;
    private static final int ERROR_RETRY_DELAY_SECONDS = 2;
    private static final long MINIMUM_REQUEST_INTERVAL_MS = 350;

    private final DatsPulseApiClient apiClient;
    private final ConsoleDisplay consoleDisplay;
    private final StrategyService strategyService;
    private final MapStateService mapStateService;
    private final Gson gson;

    public GameService(DatsPulseApiClient apiClient, ConsoleDisplay consoleDisplay, StrategyService strategyService, MapStateService mapStateService, Gson gson) {
        this.apiClient = apiClient;
        this.consoleDisplay = consoleDisplay;
        this.strategyService = strategyService;
        this.mapStateService = mapStateService;
        this.gson = gson;
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
                saveStateToFile(worldState); // Сохраняем обогащенное состояние

                List<MoveCommandDto> moves = strategyService.createMoveCommands(worldState);
                if (!moves.isEmpty()) {
                    apiClient.sendMoves(moves);
                }

                consoleDisplay.render(worldState, moves);

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

    /**
     * Сериализует текущее полное состояние мира в JSON и сохраняет в файл.
     * Это критически важно для отладки и анализа поведения бота.
     *
     * @param state Полное состояние мира для сохранения.
     */
    private void saveStateToFile(ArenaStateDto state) {
        try {
            String jsonState = gson.toJson(state);
            Files.writeString(Paths.get(STATE_FILE_NAME), jsonState);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить состояние в файл " + STATE_FILE_NAME + ": " + e.getMessage());
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
