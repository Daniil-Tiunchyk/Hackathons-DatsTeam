package com.example.service;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.example.domain.Hex;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Управляет персистентным состоянием карты.
 * <p><b>Принцип работы:</b>
 * <ol>
 *     <li>При запуске "гидрирует" свое состояние из файла `main.json`, если он существует.</li>
 *     <li>Во время работы обновляет состояние в памяти, обеспечивая высокую производительность.</li>
 *     <li>После каждого обновления сохраняет актуальное состояние обратно в `main.json` для выживания после перезапусков.</li>
 *     <li>Автоматически сбрасывает состояние каждые 15 минут для синхронизации с игровыми раундами.</li>
 * </ol>
 */
public class MapStateService {

    private static final String STATE_FILE_NAME = "main.json";
    private final Gson gson;

    private Map<Hex, ArenaStateDto.MapCellDto> knownMap = new HashMap<>();
    private List<Hex> home = new ArrayList<>();
    private Hex spot = null;

    private LocalDateTime lastResetTime = LocalDateTime.now();

    public MapStateService(Gson gson) {
        this.gson = gson;
        loadStateFromFile();
    }

    public ArenaStateDto updateAndGet(ArenaStateDto apiResponse) {
        checkForReset();

        apiResponse.map().forEach(cell -> knownMap.put(new Hex(cell.q(), cell.r()), cell));
        if (home.isEmpty() && !apiResponse.home().isEmpty()) {
            home = new ArrayList<>(apiResponse.home());
        }
        if (spot == null && apiResponse.spot() != null) {
            spot = apiResponse.spot();
        }

        Set<Hex> knownBoundaries = recalculateBoundaries();
        Set<Hex> visibleHexes = calculateCurrentlyVisibleHexes(apiResponse);

        ArenaStateDto worldState = new ArenaStateDto(
                apiResponse.ants(),
                apiResponse.enemies(),
                apiResponse.food(),
                home,
                new ArrayList<>(knownMap.values()),
                knownBoundaries,
                visibleHexes,
                apiResponse.nextTurnIn(),
                apiResponse.score(),
                spot,
                apiResponse.turnNo()
        );

        saveStateToFile(worldState);
        return worldState;
    }

    private void loadStateFromFile() {
        try {
            if (Files.exists(Paths.get(STATE_FILE_NAME))) {
                System.out.println("Найден файл main.json. Загрузка сохраненного состояния карты...");
                JsonReader reader = new JsonReader(new FileReader(STATE_FILE_NAME));
                ArenaStateDto loadedState = gson.fromJson(reader, ArenaStateDto.class);

                if (loadedState != null) {
                    this.knownMap = loadedState.map().stream()
                            .collect(Collectors.toMap(c -> new Hex(c.q(), c.r()), c -> c));
                    this.home = new ArrayList<>(loadedState.home());
                    this.spot = loadedState.spot();
                    System.out.println("Состояние карты успешно загружено. Известных гексов: " + this.knownMap.size());
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке состояния из файла " + STATE_FILE_NAME + ". Начинаем с чистого листа. Ошибка: " + e.getMessage());
            reset();
        }
    }

    private void saveStateToFile(ArenaStateDto state) {
        try {
            String jsonState = gson.toJson(state);
            Files.writeString(Paths.get(STATE_FILE_NAME), jsonState);
        } catch (IOException e) {
            System.err.println("Не удалось сохранить состояние в файл " + STATE_FILE_NAME + ": " + e.getMessage());
        }
    }

    private void checkForReset() {
        LocalDateTime now = LocalDateTime.now();
        int currentMinute = now.getMinute();

        if ((currentMinute % 15 == 0) && (ChronoUnit.MINUTES.between(lastResetTime, now) >= 1)) {
            System.out.println("Обнаружено время сброса раунда. Очистка состояния карты...");
            reset();
            saveStateToFile(createEmptyState());
            lastResetTime = now;
        }
    }

    private void reset() {
        knownMap.clear();
        home.clear();
        spot = null;
    }

    private Set<Hex> recalculateBoundaries() {
        Set<Hex> boundaries = new HashSet<>();
        Set<Hex> allKnownHexes = knownMap.keySet();
        for (Hex hex : allKnownHexes) {
            for (Hex neighbor : hex.getNeighbors()) {
                if (!allKnownHexes.contains(neighbor)) {
                    boundaries.add(hex);
                    break;
                }
            }
        }
        return boundaries;
    }

    private Set<Hex> calculateCurrentlyVisibleHexes(ArenaStateDto state) {
        Set<Hex> visibleHexes = new HashSet<>();
        for (ArenaStateDto.AntDto ant : state.ants()) {
            UnitType type = UnitType.fromApiId(ant.type());
            visibleHexes.addAll(getHexesInRange(new Hex(ant.q(), ant.r()), type.getVision()));
        }
        if (state.spot() != null) {
            visibleHexes.addAll(getHexesInRange(state.spot(), 2));
        }
        return visibleHexes;
    }

    private Set<Hex> getHexesInRange(Hex center, int radius) {
        Set<Hex> results = new HashSet<>();
        for (int q = -radius; q <= radius; q++) {
            for (int r = Math.max(-radius, -q - radius); r <= Math.min(radius, -q + radius); r++) {
                results.add(center.add(new Hex(q, r)));
            }
        }
        return results;
    }

    private ArenaStateDto createEmptyState() {
        return new ArenaStateDto(
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptySet(),
                Collections.emptySet(), 0, 0, null, 0
        );
    }
}
