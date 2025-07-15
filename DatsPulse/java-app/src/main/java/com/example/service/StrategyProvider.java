package com.example.service;

import com.example.config.GameConfig;
import com.example.domain.UnitType;
import com.example.service.strategy.AntStrategy;
import com.example.service.strategy.FighterStrategy;
import com.example.service.strategy.ScoutStrategy;
import com.example.service.strategy.WorkerStrategy;

import java.util.EnumMap;
import java.util.Map;

/**
 * Предоставляет соответствующий объект-стратегию для каждого типа юнита,
 * основываясь на настройках из {@link GameConfig}.
 * <p>
 * Этот класс реализует паттерн "Фабрика", инкапсулируя логику создания
 * и сопоставления стратегий, включая применение фича-флагов.
 */
public class StrategyProvider {

    private final Map<UnitType, AntStrategy> strategies;

    public StrategyProvider(Pathfinder pathfinder, GameConfig config) {
        this.strategies = new EnumMap<>(UnitType.class);

        // Стандартные стратегии
        this.strategies.put(UnitType.WORKER, new WorkerStrategy(pathfinder));
        this.strategies.put(UnitType.SCOUT, new ScoutStrategy(pathfinder));

        // Конфигурируемая стратегия для Бойцов
        if (config.isFighterUseWorkerLogic()) {
            System.out.println("[CONFIG] Бойцы используют логику Рабочих.");
            this.strategies.put(UnitType.FIGHTER, new WorkerStrategy(pathfinder));
        } else {
            System.out.println("[CONFIG] Бойцы используют стандартную боевую логику.");
            this.strategies.put(UnitType.FIGHTER, new FighterStrategy(pathfinder));
        }
    }

    /**
     * Возвращает стратегию для указанного типа юнита.
     *
     * @param unitType Тип юнита.
     * @return Соответствующий объект {@link AntStrategy}.
     * @throws IllegalStateException если для данного типа юнита не найдена стратегия.
     */
    public AntStrategy getStrategy(UnitType unitType) {
        AntStrategy strategy = strategies.get(unitType);
        if (strategy == null) {
            throw new IllegalStateException("Не найдена стратегия для типа юнита: " + unitType);
        }
        return strategy;
    }
}
