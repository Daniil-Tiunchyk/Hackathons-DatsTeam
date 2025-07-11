package com.example.client;

import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.dto.RegistrationResponseDto;

import java.util.List;

/**
 * Определяет контракт для взаимодействия с игровым API DatsPulse.
 * Эта абстракция позволяет легко создавать mock-объекты и тестировать сервисы,
 * которые зависят от API.
 */
public interface DatsPulseApiClient {

    /**
     * Запрашивает текущее состояние арены для нашей команды.
     *
     * @return Объект ArenaStateDto, представляющий игровой мир, или null при ошибке.
     */
    ArenaStateDto getArenaState();

    /**
     * Отправляет список команд на передвижение на сервер.
     *
     * @param moves Список команд на передвижение для наших муравьев.
     */
    void sendMoves(List<MoveCommandDto> moves);

    /**
     * Регистрирует команду для участия в раунде.
     *
     * @return Объект RegistrationResponseDto с результатом операции.
     */
    RegistrationResponseDto register();
}
