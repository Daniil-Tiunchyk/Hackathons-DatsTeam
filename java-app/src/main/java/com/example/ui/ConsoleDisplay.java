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
 * в консоли, включая основной дашборд и диагностические таблицы.
 */
public class ConsoleDisplay {

    private record TurnStatistics(
            long workers, long fighters, long scouts, long totalAnts,
            long enemies,
            long returningHome, long movingToTarget, long idle,
            long totalFoodCarried
    ) {}

    /**
     * Выводит в консоль детальное сравнение "сырого" состояния от API
     * и обогащенного состояния из MapStateService.
     * Это ключевой инструмент для отладки логики накопления карты.
     *
     * @param raw      "Сырой" DTO, полученный напрямую от API.
     * @param enriched Финальный DTO после обработки в MapStateService.
     */
    public void renderDebugComparison(ArenaStateDto raw, ArenaStateDto enriched) {
        if (raw == null || enriched == null) {
            System.out.println("Невозможно отобразить сравнение: одно из состояний null.");
            return;
        }

        String header = String.format("======= СРАВНЕНИЕ СОСТОЯНИЙ (ХОД %d) =======%n", enriched.turnNo());
        String line =   "----------------------------------------------------------%n";
        String format = "| %-25s | %-12s | %-15s |%n";

        StringBuilder sb = new StringBuilder("\n");
        sb.append(header);
        sb.append(line);
        sb.append(String.format(format, "Параметр", "API (Raw)", "State (Enriched)"));
        sb.append(line);
        sb.append(String.format(format, "Наши юниты", raw.ants().size(), enriched.ants().size()));
        sb.append(String.format(format, "Враги (видимые)", raw.enemies().size(), enriched.enemies().size()));
        sb.append(String.format(format, "Еда (видимая)", raw.food().size(), enriched.food().size()));
        sb.append(String.format(format, "Гексы карты (в ответе)", raw.map().size(), enriched.map().size()));
        sb.append(String.format(format, "Известные границы", "N/A", enriched.knownBoundaries().size()));
        sb.append(String.format(format, "Видимые гексы (сейчас)", "N/A", enriched.currentlyVisibleHexes().size()));
        sb.append(line);

        System.out.print(sb);
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
                if (isCarryingFood(ant)) {
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
            System.out.printf("Ответ сервера: [Код: %d] %s%n", response.code(), response.message());
        } else {
            System.out.println("Не удалось получить внятный ответ от сервера регистрации.");
        }
        System.out.println("-------------------------------------------------------");
    }

    private void clearConsole() {
        try {
            if (System.console() == null) {
                // В средах без консоли (например, в IDE) просто добавляем отступы
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
