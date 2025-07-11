package com.example.domain;

import java.util.Objects;

/**
 * Представляет гексагональную координату в аксиальной системе (q, r).
 * Является неизменяемым объектом-значением (immutable value object).
 * Основано на руководстве: <a href="https://www.redblobgames.com/grids/hexagons/">...</a>
 */
public record Hex(int q, int r) {

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
