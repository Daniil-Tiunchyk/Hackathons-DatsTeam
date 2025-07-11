package com.example.domain;

import java.util.List;
import java.util.Objects;

/**
 * Представляет гексагональную координату в аксиальной системе (q, r).
 * Является неизменяемым объектом-значением (immutable value object).
 * Основано на руководстве: <a href="https://www.redblobgames.com/grids/hexagons/">...</a>
 */
public record Hex(int q, int r) {

    private static final List<Hex> AXIAL_DIRECTIONS = List.of(
            new Hex(1, 0), new Hex(1, -1), new Hex(0, -1),
            new Hex(-1, 0), new Hex(-1, 1), new Hex(0, 1)
    );

    /**
     * Третья кубическая координата 's' может быть вычислена из q и r.
     * Ограничение q + r + s = 0 должно всегда выполняться.
     *
     * @return Вычисленная координата 's'.
     */
    public int s() {
        return -q - r;
    }

    /**
     * Добавляет к текущему гексу другой гекс (вектор).
     *
     * @param other Гекс, который нужно прибавить.
     * @return Новый гекс, являющийся результатом сложения.
     */
    public Hex add(Hex other) {
        return new Hex(this.q + other.q, this.r + other.r);
    }

    /**
     * Возвращает список всех 6 смежных гексов в фиксированном порядке.
     * Возвращаемый список является неизменяемым.
     *
     * @return Неизменяемый список из 6 гексов-соседей.
     */
    public List<Hex> getNeighbors() {
        return AXIAL_DIRECTIONS.stream()
                .map(this::add)
                .toList();
    }

    /**
     * Вычисляет расстояние между этим гексом и другим.
     *
     * @param other Другая гексагональная координата.
     * @return Расстояние в количестве гексов.
     */
    public int distanceTo(Hex other) {
        int dq = Math.abs(this.q - other.q);
        int dr = Math.abs(this.r - other.r);
        int ds = Math.abs(this.s() - other.s());
        return (dq + dr + ds) / 2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hex hex = (Hex) o;
        return q == hex.q && r == hex.r;
    }

    @Override
    public int hashCode() {
        return Objects.hash(q, r);
    }
}
