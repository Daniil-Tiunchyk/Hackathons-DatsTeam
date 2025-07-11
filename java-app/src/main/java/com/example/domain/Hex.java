package com.example.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Представляет гексагональную координату в аксиальной системе (q, r).
 * Является неизменяемым объектом-значением (immutable value object).
 * Основано на руководстве: <a href="https://www.redblobgames.com/grids/hexagons/">...</a>
 */
public record Hex(int q, int r) {

    /**
     * Внутренний record для представления кубических координат с плавающей точкой,
     * необходимый для алгоритма линейной интерполяции.
     */
    private record CubeFloat(double q, double r, double s) {
        public CubeFloat(double q, double r) {
            this(q, r, -q - r);
        }
    }

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

    /**
     * Генерирует прямую линию гексов от текущего до целевого.
     * Реализация алгоритма с redblobgames.com.
     *
     * @param destination Целевой гекс.
     * @return Список гексов, формирующих путь.
     */
    public List<Hex> lineTo(Hex destination) {
        int distance = this.distanceTo(destination);
        if (distance == 0) {
            return List.of(this);
        }

        CubeFloat startCube = new CubeFloat(this.q, this.r, this.s());
        CubeFloat endCube = new CubeFloat(destination.q, destination.r, destination.s());

        return IntStream.rangeClosed(0, distance)
                .mapToObj(i -> {
                    double t = 1.0 / distance * i;
                    CubeFloat interpolated = cubeLerp(startCube, endCube, t);
                    return roundCube(interpolated);
                })
                .distinct() // Устраняем дубликаты, которые могут возникнуть из-за округления
                .collect(Collectors.toList());
    }

    private CubeFloat cubeLerp(CubeFloat a, CubeFloat b, double t) {
        return new CubeFloat(
                lerp(a.q, b.q, t),
                lerp(a.r, b.r, t),
                lerp(a.s, b.s, t)
        );
    }

    private double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    private Hex roundCube(CubeFloat cube) {
        long rq = Math.round(cube.q);
        long rr = Math.round(cube.r);
        long rs = Math.round(cube.s);

        double q_diff = Math.abs(rq - cube.q);
        double r_diff = Math.abs(rr - cube.r);
        double s_diff = Math.abs(rs - cube.s);

        if (q_diff > r_diff && q_diff > s_diff) {
            rq = -rr - rs;
        } else if (r_diff > s_diff) {
            rr = -rq - rs;
        } else {
            rs = -rq - rr;
        }
        return new Hex((int) rq, (int) rr);
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
