package com.example.ui;

import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Отвечает за отрисовку агрегированной и лаконичной сводки о состоянии игры
 * в консоли в виде информационного дашборда.
 */
public class ConsoleDisplay {

    private static final String BORDER = "=======================================================";
    private static final String SEPARATOR = "-------------------------------------------------------";

    public void render(ArenaStateDto state, List<MoveCommandDto> plannedMoves) {
        clearConsole();

        StringBuilder sb = new StringBuilder();

        sb.append(BORDER).append("\n");
        sb.append(String.format("| Клиент DatsPulse | Ход: %-5d | Счет: %-7d |\n", state.turnNo(), state.score()));
        sb.append(BORDER).append("\n");
        sb.append(String.format("Время до следующего хода: %.2f сек.\n", state.nextTurnIn()));
        sb.append(SEPARATOR).append("\n\n");

        sb.append("ОБЩАЯ СВОДКА ЮНИТОВ\n");
        Map<UnitType, Long> countsByType = state.ants().stream()
                .collect(Collectors.groupingBy(ant -> UnitType.fromApiId(ant.type()), Collectors.counting()));

        long workers = countsByType.getOrDefault(UnitType.WORKER, 0L);
        long fighters = countsByType.getOrDefault(UnitType.FIGHTER, 0L);
        long scouts = countsByType.getOrDefault(UnitType.SCOUT, 0L);

        sb.append(String.format("Рабочие: %-2d | Бойцы: %-2d | Разведчики: %-2d | Всего: %d\n",
                workers, fighters, scouts, state.ants().size()));
        sb.append("\n");

        sb.append("ЗАДАЧИ НА ТЕКУЩИЙ ХОД\n");

        Map<String, ArenaStateDto.AntDto> antMap = state.ants().stream()
                .collect(Collectors.toMap(ArenaStateDto.AntDto::id, Function.identity()));

        long returningHomeCount = 0;
        long totalFoodCarried = 0;
        long movingToTargetCount = 0;

        for (MoveCommandDto move : plannedMoves) {
            ArenaStateDto.AntDto ant = antMap.get(move.ant());
            if (ant == null) continue;

            if (ant.food() != null && ant.food().amount() > 0) {
                returningHomeCount++;
                totalFoodCarried += ant.food().amount();
            } else {
                movingToTargetCount++;
            }
        }

        long idleCount = state.ants().size() - (returningHomeCount + movingToTargetCount);

        sb.append(String.format("[⌂] Несут ресурсы: %-2d (Всего: %d ед.)\n", returningHomeCount, totalFoodCarried));
        sb.append(String.format("[►] В движении:   %-2d\n", movingToTargetCount));
        sb.append(String.format("[–] Ожидают:      %-2d\n", idleCount));

        sb.append("\n").append(BORDER);

        System.out.println(sb.toString());
    }

    public void showRegistrationAttempt() {
        clearConsole();
        System.out.println(BORDER);
        System.out.println("| Состояние: Ожидание раунда                          |");
        System.out.println(BORDER);
        System.out.println("Не зарегистрированы в раунде. Попытка регистрации...");
    }

    public void showRegistrationResult(RegistrationResponseDto response) {
        if (response != null && response.message() != null) {
            System.out.printf("Ответ сервера: [Код: %d] %s\n", response.code(), response.message());
        } else {
            System.out.println("Не удалось получить внятный ответ от сервера регистрации.");
        }
        System.out.println(SEPARATOR);
    }

    /**
     * Очищает экран консоли. Использует разные подходы для реального терминала
     * и для эмулированной консоли в IDE.
     */
    private void clearConsole() {
        try {
            // *** КЛЮЧЕВОЕ ИЗМЕНЕНИЕ ***
            // Если System.console() возвращает null, значит, мы работаем в среде без
            // интерактивного терминала, например, в консоли вывода IDE.
            if (System.console() == null) {
                // В этом случае симулируем очистку, печатая много пустых строк.
                for (int i = 0; i < 50; ++i) {
                    System.out.println();
                }
            } else {
                // Иначе мы в настоящем терминале, где работают стандартные команды.
                String os = System.getProperty("os.name");
                if (os.contains("Windows")) {
                    new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
                } else {
                    System.out.print("\033[H\033[2J");
                    System.out.flush();
                }
            }
        } catch (IOException | InterruptedException ex) {
            // В случае ошибки просто игнорируем, это не критично.
        }
    }
}
