package com.example.service;

import com.example.domain.Hex;
import com.example.dto.ArenaStateDto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Управляет персистентным (накопительным) состоянием карты на протяжении одного раунда.
 * Этот сервис является центральным источником знаний о мире для всех стратегических модулей.
 *
 * <p><b>Принцип работы:</b>
 * <ol>
 *     <li>Хранит в себе полную известную карту, которая дополняется с каждым новым ответом от API.</li>
 *     <li>Перезаписывает динамическую информацию (юниты, враги, еда) на каждом ходу.</li>
 *     <li>Автоматически определяет и хранит известные границы карты.</li>
 *     <li>Производит полный сброс состояния каждые 15 минут, чтобы соответствовать раундам игры.</li>
 * </ol>
 */
public class MapStateService {

    private final Map<Hex, ArenaStateDto.MapCellDto> knownMap = new HashMap<>();
    private final Set<Hex> knownBoundaries = new HashSet<>();
    private List<Hex> home = new ArrayList<>();
    private Hex spot = null;

    private LocalDateTime lastResetTime = LocalDateTime.now();

    /**
     * Обновляет внутреннее состояние на основе свежих данных от API и возвращает
     * объединенное, полное состояние мира.
     *
     * @param apiResponse Свежее состояние, полученное от игрового сервера.
     * @return Полное, накопленное состояние мира для использования в логике.
     */
    public ArenaStateDto updateAndGet(ArenaStateDto apiResponse) {
        checkForReset();

        apiResponse.map().forEach(cell -> knownMap.put(new Hex(cell.q(), cell.r()), cell));
        if (home.isEmpty()) {
            home.addAll(apiResponse.home());
        }
        if (spot == null) {
            spot = apiResponse.spot();
        }

        recalculateBoundaries();

        return new ArenaStateDto(
                apiResponse.ants(),
                apiResponse.enemies(),
                apiResponse.food(),
                home,
                new ArrayList<>(knownMap.values()),
                knownBoundaries,
                apiResponse.nextTurnIn(),
                apiResponse.score(),
                spot,
                apiResponse.turnNo()
        );
    }

    /**
     * Проверяет, не пора ли сбросить состояние карты. Сброс происходит в 0, 15, 30 и 45 минут
     * каждого часа, чтобы синхронизироваться с началом новых игровых раундов.
     */
    private void checkForReset() {
        LocalDateTime now = LocalDateTime.now();
        int currentMinute = now.getMinute();

        if ((currentMinute % 15 == 0) && (ChronoUnit.MINUTES.between(lastResetTime, now) >= 1)) {
            System.out.println("Обнаружено время сброса раунда. Очистка состояния карты...");
            reset();
            lastResetTime = now;
        }
    }

    private void reset() {
        knownMap.clear();
        knownBoundaries.clear();
        home.clear();
        spot = null;
    }

    /**
     * Пересчитывает известные границы карты.
     * Границей считается любой известный нам гекс, у которого хотя бы один
     * из шести соседей нам неизвестен (отсутствует в knownMap).
     */
    private void recalculateBoundaries() {
        knownBoundaries.clear();
        Set<Hex> allKnownHexes = knownMap.keySet();

        for (Hex hex : allKnownHexes) {
            for (Hex neighbor : hex.getNeighbors()) {
                if (!allKnownHexes.contains(neighbor)) {
                    knownBoundaries.add(hex);
                    break;
                }
            }
        }
    }
}
