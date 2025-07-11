package com.example.ui;

import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Отвечает за отрисовку агрегированной и лаконичной сводки о состоянии игры
 * в консоли в виде информационного дашборда.
 */
public class ConsoleDisplay {

    /**
     * Неизменяемый объект-хранилище для всей статистики, необходимой для отрисовки одного кадра.
     */
    private record TurnStatistics(
            long workers, long fighters, long scouts, long totalAnts,
            long enemies,
            long returningHome, long movingToTarget, long idle,
            long totalFoodCarried
    ) {
    }

    public void render(ArenaStateDto state, List<MoveCommandDto> plannedMoves) {
        clearConsole();

        TurnStatistics stats = aggregateStatistics(state, plannedMoves);

        String output = """
                =======================================================
                | 🕹️ Клиент DatsPulse | Ход: %-5d | Счет: %-7d |
                =======================================================
                Время до следующего хода: %.2f сек.
                -------------------------------------------------------
                
                --[ 📊 Силы на карте ]--
                Наши юниты : 🐜 %-2d (Р:%-2d, Б:%-2d, Рз:%-2d)
                Враги      : 💀 %-2d (в зоне видимости)
                
                --[ 🎯 Текущие задачи ]--
                [🏠] Возвращаются с ресурсами: %-2d (несут %d ед.)
                [🗺️] Движутся к цели        : %-2d
                [💤] Ожидают приказа        : %-2d
                
                =======================================================
                """.formatted(
                state.turnNo(),
                state.score(),
                state.nextTurnIn(),
                stats.totalAnts(),
                stats.workers(), stats.fighters(), stats.scouts(),
                stats.enemies(),
                stats.returningHome(), stats.totalFoodCarried(),
                stats.movingToTarget(),
                stats.idle()
        );

        System.out.println(output);
    }

    /**
     * Агрегирует всю необходимую для отображения статистику из состояния игры.
     */
    private TurnStatistics aggregateStatistics(ArenaStateDto state, List<MoveCommandDto> plannedMoves) {
        Map<UnitType, Long> countsByType = state.ants().stream()
                .collect(Collectors.groupingBy(ant -> UnitType.fromApiId(ant.type()), Collectors.counting()));

        Set<String> antsWithMoves = plannedMoves.stream()
                .map(MoveCommandDto::ant)
                .collect(Collectors.toSet());

        long returningHome = 0;
        long movingToTarget = 0;
        long totalFoodCarried = 0;

        for (ArenaStateDto.AntDto ant : state.ants()) {
            if (antsWithMoves.contains(ant.id())) {
                if (ant.food() != null && ant.food().amount() > 0) {
                    returningHome++;
                    totalFoodCarried += ant.food().amount();
                } else {
                    movingToTarget++;
                }
            }
        }

        long totalAnts = state.ants().size();
        long idle = totalAnts - returningHome - movingToTarget;

        return new TurnStatistics(
                countsByType.getOrDefault(UnitType.WORKER, 0L),
                countsByType.getOrDefault(UnitType.FIGHTER, 0L),
                countsByType.getOrDefault(UnitType.SCOUT, 0L),
                totalAnts,
                state.enemies().size(),
                returningHome,
                movingToTarget,
                idle,
                totalFoodCarried
        );
    }

    public void showRegistrationAttempt() {
        clearConsole();
        String output = """
                =======================================================
                | ⌛ Состояние: Ожидание раунда                      |
                =======================================================
                Не зарегистрированы в раунде. Попытка регистрации...
                """;
        System.out.println(output);
    }

    public void showRegistrationResult(RegistrationResponseDto response) {
        if (response != null && response.message() != null) {
            System.out.printf("Ответ сервера: [Код: %d] %s%n", response.code(), response.message());
        } else {
            System.out.println("Не удалось получить внятный ответ от сервера регистрации.");
        }
        System.out.println("-------------------------------------------------------");
    }

    /**
     * Очищает экран консоли. Использует разные подходы для реального терминала
     * и для эмулированной консоли в IDE.
     */
    private void clearConsole() {
        try {
            if (System.console() == null) {
                // Если мы работаем в консоли вывода IDE (например, в IntelliJ),
                // этот трюк добавит пустые строки для имитации очистки.
                System.out.println("\n".repeat(5));
            } else {
                // Если мы в настоящем терминале
                if (System.getProperty("os.name").contains("Windows")) {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
            }
        } catch (IOException | InterruptedException ignored) {
            // Игнорируем ошибки, так как это некритичная для логики операция.
        }
    }
}
