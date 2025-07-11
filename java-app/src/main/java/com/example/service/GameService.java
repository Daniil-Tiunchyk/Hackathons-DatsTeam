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

    private final DatsPulseApiClient apiClient;
    private final ConsoleDisplay consoleDisplay;

    public GameService(DatsPulseApiClient apiClient, ConsoleDisplay consoleDisplay) {
        this.apiClient = apiClient;
        this.consoleDisplay = consoleDisplay;
    }

    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                ArenaStateDto currentState = apiClient.getArenaState();

                if (weAreNotInGame(currentState)) {
                    handleRegistration();
                    continue;
                }

                // Заглушка для принятия решения о ходах.
                List<MoveCommandDto> moves = decideNextMoves(currentState);

                apiClient.sendMoves(moves);

                consoleDisplay.render(currentState, moves);

                waitForNextTurn(currentState.nextTurnIn());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
                System.out.println("Игровой цикл прерван. Выход.");
                // Цикл завершится благодаря проверке !Thread.currentThread().isInterrupted()
            } catch (Exception e) {
                System.err.println("Произошла критическая ошибка в игровом цикле: " + e.getMessage());
                e.printStackTrace();
                try {
                    waitFor(ERROR_RETRY_DELAY_SECONDS);
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt(); // Восстанавливаем флаг прерывания
                    System.out.println("Ожидание после ошибки прервано. Выход.");
                }
            }
        }
    }

    private boolean weAreNotInGame(ArenaStateDto state) {
        // Если состояние не пришло или в нем нет поля ants (которое теперь не может быть null
        // из-за нашего защитного DTO, но сама переменная state может быть null),
        // мы считаем, что не зарегистрированы или раунд еще не начался.
        return state == null;
    }

    private void handleRegistration() throws InterruptedException {
        consoleDisplay.showRegistrationAttempt();
        RegistrationResponseDto response = apiClient.register();
        consoleDisplay.showRegistrationResult(response);
        waitFor(REGISTRATION_RETRY_DELAY_SECONDS);
    }

    private void waitForNextTurn(double seconds) throws InterruptedException {
        long sleepMillis = (long) (seconds * 1000);
        // Добавляем небольшой буфер, чтобы не отправить запрос слишком рано.
        TimeUnit.MILLISECONDS.sleep(Math.max(50, sleepMillis + 100));
    }

    private void waitFor(int seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }

    private List<MoveCommandDto> decideNextMoves(ArenaStateDto state) {
        // В реальной реализации здесь будет вызываться сервис стратегии (StrategyService).
        // Сервис стратегии будет оперировать доменной моделью, а не DTO.
        // Пока что возвращаем пустой список.
        System.out.println("Принятие решений для хода " + state.turnNo() + "...");
        return List.of();
    }
}
