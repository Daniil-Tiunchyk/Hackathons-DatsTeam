package com.example.simulation;

import com.example.client.DatsPulseApiClient;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;
import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Mock-реализация {@link DatsPulseApiClient} для локального тестирования и симуляции.
 * <p>
 * Вместо реальных HTTP-запросов, этот класс читает предопределенное состояние арены
 * из файла {@code test.json} в classpath. Это позволяет тестировать всю логику
 * принятия решений в изолированной и воспроизводимой среде.
 */
public class MockDatsPulseApiClient implements DatsPulseApiClient {

    private final ArenaStateDto arenaState;
    private final Gson gson = new Gson();

    public MockDatsPulseApiClient(String resourcePath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException("Не удалось найти тестовый файл в ресурсах: " + resourcePath);
            }
            this.arenaState = gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), ArenaStateDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при чтении или парсинге тестового JSON-файла", e);
        }
    }

    /**
     * Возвращает заранее загруженное состояние арены из файла.
     *
     * @return Объект {@link ArenaStateDto}.
     */
    @Override
    public ArenaStateDto getArenaState() {
        return this.arenaState;
    }

    /**
     * Симулирует отправку ходов, выводя их в консоль.
     *
     * @param moves Список команд на передвижение.
     */
    @Override
    public void sendMoves(List<MoveCommandDto> moves) {
        System.out.println("[MOCK] Получено " + moves.size() + " команд для отправки на сервер.");
    }

    /**
     * Возвращает стандартный успешный ответ для симуляции регистрации.
     *
     * @return Успешный {@link RegistrationResponseDto}.
     */
    @Override
    public RegistrationResponseDto register() {
        return new RegistrationResponseDto(0, "Успешная регистрация (симуляция)");
    }
}
