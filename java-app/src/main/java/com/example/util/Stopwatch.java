package com.example.util;

import java.util.concurrent.TimeUnit;

/**
 * Простая утилита для измерения прошедшего времени с высокой точностью.
 * Использует System.nanoTime() для защиты от изменений системного времени.
 */
public final class Stopwatch {

    private long startTime;

    private Stopwatch() {
        this.startTime = System.nanoTime();
    }

    /**
     * Создает и немедленно запускает новый экземпляр таймера.
     *
     * @return Запущенный Stopwatch.
     */
    public static Stopwatch start() {
        return new Stopwatch();
    }

    /**
     * Сбрасывает таймер, начиная отсчет заново с текущего момента.
     */
    public void reset() {
        this.startTime = System.nanoTime();
    }

    /**
     * @return Прошедшее время в миллисекундах с момента старта или последнего сброса.
     */
    public long getElapsedTimeMillis() {
        long elapsedNanos = System.nanoTime() - startTime;
        return TimeUnit.NANOSECONDS.toMillis(elapsedNanos);
    }
}
