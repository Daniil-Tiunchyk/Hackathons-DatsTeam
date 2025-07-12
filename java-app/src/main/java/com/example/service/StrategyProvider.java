package com.example.service;

import com.example.domain.UnitType;
import com.example.service.strategy.AntStrategy;
import com.example.service.strategy.FighterStrategy;
import com.example.service.strategy.ScoutStrategy;
import com.example.service.strategy.WorkerStrategy;

import java.util.EnumMap;
import java.util.Map;

/**
 * Предоставляет соответствующий объект-стратегию для каждого типа юнита.
 * <p>
 * Этот класс реализует паттерн "Фабрика" или "Поставщик", инкапсулируя
 * логику создания и сопоставления стратегий. Это позволяет централизованно
 * управлять всеми доступными стратегиями и упрощает DI в основном сервисе.
 */
public class StrategyProvider {

    private final Map<UnitType, AntStrategy> strategies;

    public StrategyProvider(Pathfinder pathfinder) {
        this.strategies = new EnumMap<>(UnitType.class);
        this.strategies.put(UnitType.WORKER, new WorkerStrategy(pathfinder));
        this.strategies.put(UnitType.FIGHTER, new FighterStrategy(pathfinder));
        this.strategies.put(UnitType.SCOUT, new ScoutStrategy(pathfinder));
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
