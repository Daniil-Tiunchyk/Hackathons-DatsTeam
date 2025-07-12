package com.example.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.example.domain.Hex;
import com.example.domain.UnitType;
import com.example.dto.ArenaStateDto;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Управляет персистентным состоянием карты и ресурсов.
 * <p><b>Принцип работы:</b>
 * <ol>
 *     <li>При запуске "гидрирует" свое состояние из файла `main.json`.</li>
 *     <li>На каждом ходу сначала инвалидирует (удаляет) информацию о еде на тех клетках,
 *     которые видят наши юниты, но где еды по факту нет.</li>
 *     <li>Затем обогащает свою базу знаний новыми данными о карте и еде из свежего ответа API.</li>
 *     <li>После каждого обновления сохраняет актуальное состояние обратно в `main.json`.</li>
 * </ol>
 */
public class MapStateService {

    private static final String STATE_FILE_NAME = "main.json";
    private final Gson gson;

    private Map<Hex, ArenaStateDto.MapCellDto> knownMap = new HashMap<>();
    private Map<Hex, ArenaStateDto.FoodDto> knownFood = new HashMap<>();
    private List<Hex> home = new ArrayList<>();
    private Hex spot = null;

    private LocalDateTime lastResetTime = LocalDateTime.now();

    public MapStateService(Gson gson) {
        this.gson = gson;
        loadStateFromFile();
    }

    public ArenaStateDto updateAndGet(ArenaStateDto apiResponse) {
        checkForReset();

        // Шаг 1: Инвалидация старых данных о еде на основе текущей видимости
        invalidateStaleFood(apiResponse);

        // Шаг 2: Обогащение новыми данными
        apiResponse.map().forEach(cell -> knownMap.put(new Hex(cell.q(), cell.r()), cell));
        apiResponse.food().forEach(food -> knownFood.put(new Hex(food.q(), food.r()), food));

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
                new ArrayList<>(knownFood.values()), // Передаем полный список известной еды
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

    /**
     * Удаляет из памяти информацию о еде, если наш юнит видит гекс,
     * но API больше не сообщает о наличии еды на нем.
     */
    private void invalidateStaleFood(ArenaStateDto apiResponse) {
        Set<Hex> visibleHexes = calculateCurrentlyVisibleHexes(apiResponse);
        Set<Hex> foodHexesInResponse = apiResponse.food().stream()
                .map(f -> new Hex(f.q(), f.r()))
                .collect(Collectors.toSet());

        // Находим гексы, которые мы видим, где мы помнили еду, но ее там больше нет
        Set<Hex> hexesToClear = new HashSet<>();
        for (Hex visibleHex : visibleHexes) {
            if (knownFood.containsKey(visibleHex) && !foodHexesInResponse.contains(visibleHex)) {
                hexesToClear.add(visibleHex);
            }
        }

        hexesToClear.forEach(knownFood::remove);
    }

    private void loadStateFromFile() {
        try {
            if (Files.exists(Paths.get(STATE_FILE_NAME))) {
                System.out.println("Найден файл main.json. Загрузка сохраненного состояния...");
                JsonReader reader = new JsonReader(new FileReader(STATE_FILE_NAME));
                ArenaStateDto loadedState = gson.fromJson(reader, ArenaStateDto.class);

                if (loadedState != null) {
                    this.knownMap = loadedState.map().stream()
                            .collect(Collectors.toMap(c -> new Hex(c.q(), c.r()), c -> c));
                    this.knownFood = loadedState.food().stream()
                            .collect(Collectors.toMap(f -> new Hex(f.q(), f.r()), f -> f, (a, b) -> a));
                    this.home = new ArrayList<>(loadedState.home());
                    this.spot = loadedState.spot();
                    System.out.printf("Состояние успешно загружено. Гексов: %d, Еды: %d%n", this.knownMap.size(), this.knownFood.size());
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
        knownFood.clear();
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
