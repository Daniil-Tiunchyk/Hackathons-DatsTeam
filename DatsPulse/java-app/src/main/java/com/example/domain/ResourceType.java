package com.example.domain;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Перечисление, представляющее типы ресурсов в игре.
 * Инкапсулирует бизнес-логику, связанную с ресурсами, такую как их
 * калорийность и возможность сбора с карты.
 */
public enum ResourceType {
    APPLE(1, 10, true),
    BREAD(2, 20, true),
    NECTAR(3, 60, false); // Нектар нельзя собрать с карты, он генерируется.

    private final int apiId;
    private final int calories;
    private final boolean collectible;

    private static final Map<Integer, ResourceType> ID_TO_TYPE_MAP =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(ResourceType::getApiId, Function.identity()));

    ResourceType(int apiId, int calories, boolean collectible) {
        this.apiId = apiId;
        this.calories = calories;
        this.collectible = collectible;
    }

    public static ResourceType fromApiId(int apiId) {

        return ID_TO_TYPE_MAP.get(apiId);
    }

    public int getApiId() {
        return apiId;
    }

    public int getCalories() {
        return calories;
    }

    /**
     * @return true, если этот ресурс можно найти и собрать на карте.
     */
    public boolean isCollectible() {
        return collectible;
    }
}
