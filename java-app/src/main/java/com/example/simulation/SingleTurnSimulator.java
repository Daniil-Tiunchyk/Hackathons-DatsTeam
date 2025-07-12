package com.example.simulation;

import com.example.client.DatsPulseApiClient;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyProvider;
import com.example.service.StrategyService;
import com.example.ui.ConsoleDisplay;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Точка входа для запуска симуляции одного игрового хода.
 * <p>
 * Этот класс инициализирует все необходимые сервисы, используя {@link MockDatsPulseApiClient}
 * для загрузки данных из локального файла вместо обращения к реальному серверу.
 * Он позволяет наглядно увидеть, какой вывод сгенерирует {@link ConsoleDisplay}
 * и какие конкретно команды примет {@link StrategyService} для заданного сценария.
 * <p>
 * Также включает в себя замер времени выполнения основного алгоритма принятия решений.
 */
public class SingleTurnSimulator {

    public static void main(String[] args) {
        System.out.println("--- Запуск симулятора одного хода ---");

        try {
            // 1. Инициализация компонентов с использованием Mock-клиента
            DatsPulseApiClient mockApiClient = new MockDatsPulseApiClient("test.json");
            ConsoleDisplay consoleDisplay = new ConsoleDisplay();
            Pathfinder pathfinder = new Pathfinder();
            StrategyProvider strategyProvider = new StrategyProvider(pathfinder);
            StrategyService strategyService = new StrategyService(strategyProvider, pathfinder);

            // 2. Получение тестового состояния арены
            ArenaStateDto currentState = mockApiClient.getArenaState();
            if (currentState == null) {
                System.err.println("Не удалось загрузить состояние арены из test.json");
                return;
            }

            // 3. Вызов сервиса стратегии для принятия решений с замером времени
            long startTime = System.nanoTime(); // <<< НАЧАЛО ИЗМЕРЕНИЯ ВРЕМЕНИ
            List<MoveCommandDto> moves = strategyService.createMoveCommands(currentState);
            long endTime = System.nanoTime();   // <<< КОНЕЦ ИЗМЕРЕНИЯ ВРЕМЕНИ

            double durationMillis = (double) (endTime - startTime) / TimeUnit.MILLISECONDS.toNanos(1);

            // 4. Отрисовка дашборда, как в реальном приложении
            System.out.println("\n--- [ Начало вывода ConsoleDisplay ] ---\n");
            consoleDisplay.render(currentState, moves);
            System.out.println("--- [ Конец вывода ConsoleDisplay ] ---\n");

            // 5. Вывод информации о производительности
            System.out.println("--- [ Производительность алгоритма ] ---");
            System.out.printf("Логика принятия решений отработала за: %.4f мс%n", durationMillis);
            System.out.println("----------------------------------------\n");


            // 6. Детальный вывод сгенерированных команд для анализа
            System.out.println("--- [ Анализ сгенерированных команд (" + moves.size() + ") ] ---");
            if (moves.isEmpty()) {
                System.out.println("Ни одной команды не было сгенерировано.");
            } else {
                moves.forEach(move -> System.out.printf(
                        " -> Юнит ID: %-12s | Команда: двигаться по пути %s%n",
                        move.ant(),
                        move.path()
                ));
            }
            System.out.println("----------------------------------------------");


        } catch (Exception e) {
            System.err.println("\n--- Произошла ошибка во время симуляции ---");
            e.printStackTrace(System.err);
        }
    }
}
