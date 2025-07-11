package com.example.service;

import com.example.client.DatsPulseApiClient;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.ui.ConsoleDisplay;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Оркестрирует основной игровой цикл, координируя взаимодействие между
 * API-клиентом, сервисом стратегии и пользовательским интерфейсом.
 */
public class GameService {

    private final DatsPulseApiClient apiClient;
    private final ConsoleDisplay consoleDisplay;

    public GameService(DatsPulseApiClient apiClient, ConsoleDisplay consoleDisplay) {
        this.apiClient = apiClient;
        this.consoleDisplay = consoleDisplay;
    }

    public void run() {
        // Это упрощенный цикл. Более надежная версия должна обрабатывать регистрацию на раунд.
        while (true) {
            try {
                ArenaStateDto currentState = apiClient.getArenaState();

                // Здесь будет происходить преобразование DTO в нашу обогащенную доменную модель.
                // Для простоты пока передаем DTO напрямую.

                // Заглушка для принятия решения о ходах.
                List<MoveCommandDto> moves = decideNextMoves(currentState);

                apiClient.sendMoves(moves);

                consoleDisplay.render(currentState, moves);

                long sleepMillis = (long) (currentState.nextTurnIn() * 1000);
                // Добавляем небольшой буфер, чтобы не отправить запрос слишком рано.
                TimeUnit.MILLISECONDS.sleep(Math.max(50, sleepMillis + 100));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("Игровой цикл прерван. Выход.");
                break;
            } catch (Exception e) {
                // Примитивная обработка ошибок для хакатона.
                System.err.println("Произошла ошибка: " + e.getMessage());
                try {
                    TimeUnit.SECONDS.sleep(5); // Пауза перед повторной попыткой при ошибке.
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private List<MoveCommandDto> decideNextMoves(ArenaStateDto state) {
        // В реальной реализации здесь будет вызываться сервис стратегии (StrategyService).
        // Сервис стратегии будет оперировать доменной моделью, а не DTO.
        // Пока что возвращаем пустой список.
        System.out.println("Принятие решений для хода " + state.turnNo() + "...");
        return List.of();
    }
}
