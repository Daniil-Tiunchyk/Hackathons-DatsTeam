package com.example.service.strategy;

import com.example.domain.Hex;
import com.example.domain.HexType;
import com.example.dto.ArenaStateDto;
import com.example.dto.MoveCommandDto;
import com.example.service.Pathfinder;
import com.example.service.StrategyHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Реализация {@link AntStrategy} для юнитов-рабочих.
 * <p>
 * Основная задача рабочих — эффективный сбор ресурсов. Этот класс инкапсулирует
 * логику поиска ближайшей доступной еды и отправки рабочих на ее сбор.
 */
public class WorkerStrategy implements AntStrategy {

    private final Pathfinder pathfinder;

    public WorkerStrategy(Pathfinder pathfinder) {
        this.pathfinder = pathfinder;
    }

    @Override
    public List<MoveCommandDto> decideMoves(List<ArenaStateDto.AntDto> workers, ArenaStateDto state) {
        if (workers.isEmpty()) {
            return Collections.emptyList();
        }

        List<MoveCommandDto> commands = new ArrayList<>();
        Set<Hex> assignedFoodTargets = new HashSet<>();
        Map<Hex, Integer> hexCosts = StrategyHelper.getHexCosts(state);
        Map<Hex, HexType> hexTypes = StrategyHelper.getHexTypes(state);

        for (ArenaStateDto.AntDto worker : workers) {
            Hex workerHex = new Hex(worker.q(), worker.r());

            Optional<MoveCommandDto> command = StrategyHelper.findClosestAvailableFood(workerHex, state.food(), assignedFoodTargets)
                    .flatMap(target -> StrategyHelper.createPathCommand(worker, target, state, pathfinder, hexCosts, hexTypes));

            command.ifPresent(cmd -> {
                commands.add(cmd);
                // Резервируем цель, чтобы другие рабочие за ней не пошли
                cmd.path().stream().reduce((first, second) -> second).ifPresent(assignedFoodTargets::add);
            });
        }
        return commands;
    }
}
