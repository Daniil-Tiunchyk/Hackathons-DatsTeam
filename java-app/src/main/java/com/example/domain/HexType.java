package com.example.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Перечисление, представляющее типы гексов на карте.
 * Обеспечивает централизованный доступ к свойствам гексов, таким как урон или непроходимость.
 */
public enum HexType {
    ANTHILL(1, 0, false),
    EMPTY(2, 0, false),
    DIRT(3, 0, false),
    ACID(4, 20, false),
    STONE(5, 0, true);

    private final int apiId;
    private final int damage;
    private final boolean impassable;

    private static final Map<Integer, HexType> ID_TO_TYPE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(HexType::getApiId, Function.identity()));

    HexType(int apiId, int damage, boolean impassable) {
        this.apiId = apiId;
        this.damage = damage;
        this.impassable = impassable;
    }

    /**
     * Возвращает экземпляр HexType по его API ID.
     *
     * @param apiId ID типа гекса из API.
     * @return Соответствующий HexType.
     * @throws IllegalArgumentException если ID неизвестен.
     */
    public static HexType fromApiId(int apiId) {
        HexType type = ID_TO_TYPE_MAP.get(apiId);
        if (type == null) {
            // Игнорируем неизвестные типы, чтобы не падать при возможных обновлениях API
            throw new IllegalArgumentException("Неизвестный API ID для типа гекса: " + apiId);
        }
        return type;
    }

    public int getApiId() {
        return apiId;
    }

    public int getDamage() {
        return damage;
    }

    public boolean isImpassable() {
        return impassable;
    }
}
