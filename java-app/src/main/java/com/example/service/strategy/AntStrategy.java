package com.example.service.strategy;

import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;

import java.util.List;

/**
 * Определяет общий контракт для всех стратегий поведения юнитов.
 * <p>
 * Каждый класс, реализующий этот интерфейс, инкапсулирует в себе
 * специфическую логику принятия решений для определенного типа (роли) юнитов.
 */
public interface AntStrategy {

    /**
     * Разрабатывает план действий для предоставленного списка юнитов.
     *
     * @param ants  Список юнитов, для которых необходимо принять решение.
     *              Предполагается, что все юниты в списке одного типа.
     * @param state Текущее состояние арены, содержащее всю необходимую контекстную информацию.
     * @return Список {@link MoveCommandDto} с командами для юнитов.
     */
    List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> ants, ArenaStateDto state);
}
