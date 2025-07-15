package com.example.ui;

import com.example.domain.Hex;
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
 * в консоли, включая основной дашборд и диагностические таблицы.
 */
public class ConsoleDisplay {

    private static final double WORKER_FULL_CAPACITY_THRESHOLD = 0.7;

    /**
     * Агрегирует всю статистику за ход для последующего отображения.
     */
    private record TurnStatistics(
            // Общие данные
            int turnNo, int teamScore, double nextTurnIn,
            // Сводка по юнитам
            long totalAnts, long totalMoving, long totalIdle,
            long totalWorkers, long totalFighters, long totalScouts,
            // Сводка по врагам
            long visibleEnemies,
            // Сводка по экономике
            long totalCarriedFoodAmount,
            // Детализация по задачам рабочих
            long workersReturningFull,
            long workersToppingOff,
            long workersCollecting,
            long workersExploring,
            long workersIdle
    ) {
    }

    public void renderDebugComparison(ArenaStateDto raw, ArenaStateDto enriched) {
        if (raw == null || enriched == null) {
            System.out.println("Невозможно отобразить сравнение: одно из состояний null.");
            return;
        }

        String header = String.format("======= СРАВНЕНИЕ СОСТОЯНИЙ (ХОД %d) =======\n", enriched.turnNo());
        String line = "----------------------------------------------------------\n";
        String format = "| %-25s | %-12s | %-15s |%n";

        String sb = "\n" + header +
                line +
                String.format(format, "Параметр", "API (Raw)", "State (Enriched)") +
                line +
                String.format(format, "Наши юниты", raw.ants().size(), enriched.ants().size()) +
                String.format(format, "Враги (видимые)", raw.enemies().size(), enriched.enemies().size()) +
                String.format(format, "Еда (видимая)", raw.food().size(), enriched.food().size()) +
                String.format(format, "Гексы карты (в ответе)", raw.map().size(), enriched.map().size()) +
                String.format(format, "Известные границы", "N/A", enriched.knownBoundaries().size()) +
                String.format(format, "Видимые гексы (сейчас)", "N/A", enriched.currentlyVisibleHexes().size()) +
                line;

        System.out.print(sb);
    }

    public void render(ArenaStateDto state, List<MoveCommandDto> plannedMoves) {
        clearConsole();

        TurnStatistics stats = aggregateStatistics(state, plannedMoves);

        String output = """
                ================================================================
                | 🕹️ Клиент DatsPulse | Ход: %-5d | 🌟 Счет: %-8d |
                ================================================================
                Время до следующего хода: %.2f сек.
                
                --[ 🐜 Наши Юниты: %d | 🏃 Движутся: %d | 💤 Ожидают: %d ]--
                - Рабочие (%d):
                  [🏠] Несут домой (полные): %d
                  [🧺] Добирают ресурсы:      %d
                  [💰] Идут за едой:          %d
                  [🗺️] Исследуют:             %d
                - Бойцы (%d) / Разведчики (%d)
                
                --[ 💰 Экономика и Разведка ]--
                Ресурсов в пути: %d
                Врагов в зоне видимости: %d
                ----------------------------------------------------------------
                """.formatted(
                stats.turnNo(), stats.teamScore(), stats.nextTurnIn(),
                stats.totalAnts(), stats.totalMoving(), stats.totalIdle(),
                stats.totalWorkers(),
                stats.workersReturningFull(),
                stats.workersToppingOff(),
                stats.workersCollecting(),
                stats.workersExploring(),
                stats.totalFighters(), stats.totalScouts(),
                stats.totalCarriedFoodAmount(),
                stats.visibleEnemies()
        );

        System.out.println(output);
    }

    private TurnStatistics aggregateStatistics(ArenaStateDto state, List<MoveCommandDto> plannedMoves) {
        Map<String, MoveCommandDto> movesById = plannedMoves.stream()
                .collect(Collectors.toMap(MoveCommandDto::ant, move -> move));

        Set<Hex> foodHexes = state.food().stream()
                .map(f -> new Hex(f.q(), f.r()))
                .collect(Collectors.toSet());

        long totalWorkers = 0, totalFighters = 0, totalScouts = 0;
        long workersReturningFull = 0, workersToppingOff = 0, workersCollecting = 0, workersExploring = 0, workersIdle = 0;
        long totalCarriedFoodAmount = 0;

        for (ArenaStateDto.AntDto ant : state.ants()) {
            UnitType type = UnitType.fromApiId(ant.type());
            boolean hasMove = movesById.containsKey(ant.id());

            if (isCarryingFood(ant)) {
                totalCarriedFoodAmount += ant.food().amount();
            }

            switch (type) {
                case WORKER -> {
                    totalWorkers++;
                    if (isCarryingFood(ant)) {
                        double capacity = type.getCapacity();
                        if (ant.food().amount() / capacity >= WORKER_FULL_CAPACITY_THRESHOLD) {
                            workersReturningFull++;
                        } else {
                            workersToppingOff++;
                        }
                    } else if (hasMove) {
                        Hex targetHex = movesById.get(ant.id()).path().getLast();
                        if (foodHexes.contains(targetHex)) {
                            workersCollecting++;
                        } else {
                            workersExploring++;
                        }
                    } else {
                        workersIdle++;
                    }
                }
                case FIGHTER -> totalFighters++;
                case SCOUT -> totalScouts++;
            }
        }

        long totalAnts = state.ants().size();
        long totalMoving = movesById.size();
        long totalIdle = totalAnts - totalMoving;

        return new TurnStatistics(
                state.turnNo(), state.score(), state.nextTurnIn(),
                totalAnts, totalMoving, totalIdle,
                totalWorkers, totalFighters, totalScouts,
                state.enemies().size(),
                totalCarriedFoodAmount,
                workersReturningFull,
                workersToppingOff,
                workersCollecting,
                workersExploring,
                workersIdle
        );
    }

    private boolean isCarryingFood(ArenaStateDto.AntDto ant) {
        return ant.food() != null && ant.food().amount() > 0;
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
            System.out.printf("Ответ сервера: [Код: %d] %s\n", response.code(), response.message());
        } else {
            System.out.println("Не удалось получить внятный ответ от сервера регистрации.");
        }
        System.out.println("-------------------------------------------------------");
    }

    private void clearConsole() {
        try {
            if (System.console() == null) {
                System.out.println("\n".repeat(20));
            } else {
                if (System.getProperty("os.name").contains("Windows")) {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
            }
        } catch (IOException | InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
