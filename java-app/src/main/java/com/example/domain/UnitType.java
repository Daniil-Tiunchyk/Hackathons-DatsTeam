package com.example.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Перечисление, представляющее типы юнитов в игре.
 * Предоставляет централизованный и типобезопасный доступ к их характеристикам,
 * таким как скорость, здоровье и т.д., избавляя от "магических чисел".
 */
public enum UnitType {
    WORKER(0, 130, 30, 8, 1, 5, "Рабочий"),
    FIGHTER(1, 180, 70, 2, 1, 4, "Боец"),
    SCOUT(2, 80, 20, 2, 4, 7, "Разведчик");

    private final int apiId;
    private final int health;
    private final int attack;
    private final int capacity;
    private final int vision;
    private final int speed;
    private final String name;

    private static final Map<Integer, UnitType> ID_TO_TYPE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(UnitType::getApiId, Function.identity()));

    UnitType(int apiId, int health, int attack, int capacity, int vision, int speed, String name) {
        this.apiId = apiId;
        this.health = health;
        this.attack = attack;
        this.capacity = capacity;
        this.vision = vision;
        this.speed = speed;
        this.name = name;
    }

    /**
     * Возвращает экземпляр UnitType по его API ID.
     *
     * @param apiId ID типа юнита из API.
     * @return Соответствующий UnitType.
     * @throws IllegalArgumentException если ID неизвестен.
     */
    public static UnitType fromApiId(int apiId) {
        return ID_TO_TYPE_MAP.get(apiId);
    }

    public int getApiId() {
        return apiId;
    }

    public int getHealth() {
        return health;
    }

    public int getAttack() {
        return attack;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getVision() {
        return vision;
    }

    public int getSpeed() {
        return speed;
    }

    public String getName() {
        return name;
    }
}
