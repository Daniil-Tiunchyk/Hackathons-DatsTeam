package com.example.ui;

import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;

import java.util.List;

/**
 * Отвечает за отрисовку состояния игры и запланированных действий
 * в консоли в человекочитаемом формате.
 */
public class ConsoleDisplay {

    private static final String BORDER = "=======================================================";
    private static final String SEPARATOR = "-------------------------------------------------------";

    public void render(ArenaStateDto state, List<MoveCommandDto> plannedMoves) {
        clearConsole();
        System.out.println(BORDER);
        System.out.printf("| Клиент DatsPulse | Ход: %-5d | Счет: %-7d |\n", state.turnNo(), state.score());
        System.out.println(BORDER);

        System.out.printf("Время до следующего хода: %.2f сек.\n", state.nextTurnIn());
        System.out.println(SEPARATOR);

        System.out.printf("Мои муравьи (%d):\n", state.ants().size());
        if (state.ants().isEmpty()) {
            System.out.println("  Нет.");
        } else {
            state.ants().forEach(ant ->
                    System.out.printf("  - Муравей %s [Тип: %d, HP: %d, Поз: (%d, %d)]\n",
                            ant.id().substring(0, 8), ant.type(), ant.health(), ant.q(), ant.r())
            );
        }
        System.out.println(SEPARATOR);

        System.out.printf("Запланированные ходы (%d):\n", plannedMoves.size());
        if (plannedMoves.isEmpty()) {
            System.out.println("  Остаемся на месте.");
        } else {
            plannedMoves.forEach(move ->
                    System.out.printf("  - Муравей %s -> Путь из %d шагов\n",
                            move.ant().substring(0, 8), move.path().size())
            );
        }
        System.out.println(BORDER);
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

    private void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
