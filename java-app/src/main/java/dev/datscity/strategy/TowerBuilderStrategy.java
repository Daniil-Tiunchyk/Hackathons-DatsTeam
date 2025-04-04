package dev.datscity.strategy;

import dev.datscity.model.TowerMap;
import dev.datscity.model.Word;
import dev.datscity.model.WordPlacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Класс стратегии, стремящийся построить как минимум 2 уровня (forceHeight=2)
 * и дальше - до максимума, пока есть слова и/или есть смысл.
 *
 * Главное отличие: мы трактуем "текущий этаж" как z = -currentLevel,
 * чтобы второй этаж находился на z = -1, а не z=1.
 */
public class TowerBuilderStrategy {

    private TowerMap towerMap;          // Текущее состояние башни
    private boolean towerCompleted;     // Флаг завершения башни
    private int currentLevel;           // "Этаж" в нашей логике. Этаж 0 -> z=0, этаж 1 -> z=-1, ...
    private int layerSize;              // Размер "площади" слоя
    private int mapX = 30, mapY = 30, mapZ = 100;

    // Минимум этажей (2). Сервер иначе вернёт ошибку "tower's height is too low"
    private int forceHeight = 2;

    // Если true, бот старается строить выше, пока есть смысл
    private boolean forceMaxHeight = true;

    public TowerBuilderStrategy() {
        this.towerCompleted = true; // изначально башня не активна
        System.out.println("[TowerBuilderStrategy] Стратегия инициализирована.");
    }

    /**
     * Начать новую башню. Первый этаж будет на z=0, следующий - z=-1 и так далее.
     */
    public void startNewTower(List<Word> wordsList, int[] mapSize) {
        this.mapX = mapSize[0];
        this.mapY = mapSize[1];
        this.mapZ = mapSize[2];
        this.towerMap = new TowerMap(mapX, mapY, mapZ);
        this.towerCompleted = false;
        this.currentLevel = 0;

        // Просто эвристика: берем половину меньшей стороны как layerSize
        this.layerSize = Math.min(mapX, mapY) / 2;
        if (this.layerSize < 2) this.layerSize = 2;

        System.out.println("[TowerBuilderStrategy] Начата новая башня: layerSize=" + layerSize
                + ", mapZ=" + mapZ);
    }

    /**
     * Планирует размещение слов на очередном "уровне".
     * Уровень 0 -> z=0, уровень 1 -> z=-1, уровень 2 -> z=-2 и т.д.
     */
    public List<WordPlacement> planNextMove(List<Word> words) {
        List<WordPlacement> result = new ArrayList<>();

        // 1. Если башня уже завершена (решением стратегии), не строим дальше
        if (towerCompleted) {
            System.out.println("[TowerBuilderStrategy] Башня помечена завершённой, не строим дальше.");
            return result;
        }

        // 2. Текущая "высота" башни по внутренним представлениям TowerMap (считает z>?)
        //    Но помните, TowerMap может думать, что z=0 - занято, z=-1 - тоже занято...
        //    getCurrentHeight() возможно просто ищет maxZ+1. В официальных правилах
        //    мы должны смотреть, сколько есть "этажей" в отрицательном z.
        //    Здесь оставляем как есть, главное - не выставлять done=true, если <2 этажей.
        int currentHeight = towerMap.getCurrentHeight();
        System.out.println("[TowerBuilderStrategy] Внутренняя высота башни (max z+1): " + currentHeight
                + ", currentLevel=" + currentLevel);

        // 3. Если достали высоту >= mapZ (по внутренней логике TowerMap),
        //    завершим строительство. (В реальности, z<0 не конфликтует с mapZ,
        //    но пусть будет проверка.)
        if (currentHeight >= mapZ) {
            System.out.println("[TowerBuilderStrategy] Достигнута предельная высота Z=" + mapZ
                    + ", завершаем башню.");
            towerCompleted = true;
            return result;
        }

        // 4. Если текущая (внутренняя) высота уже >= forceHeight=2
        //    и слов нет, можем завершить
        if (currentHeight >= forceHeight && words.isEmpty()) {
            System.out.println("[TowerBuilderStrategy] Высота >= " + forceHeight
                    + ", а слов нет -> завершаем башню.");
            towerCompleted = true;
            return result;
        }

        // 5. Определяем z-координату для нового "этажа".
        //    Первый уровень: currentLevel=0 -> z=0
        //    Второй уровень: currentLevel=1 -> z=-1
        //    Третий: currentLevel=2 -> z=-2, и т.д.
        int zLevel = -currentLevel;

        // 6. Выбираем направление (горизонтальное) по чётности уровня.
        //    В этом демо мы не показываем vertical (dir=1=[0,0,-1]),
        //    но по правилам нужно как-то вставлять вертикальные слова,
        //    иначе не будет пересечений.
        //    Пока оставляем 0 -> dir=0, 1 -> dir=1,
        //    а в GameApiClient mapDirection(0) = 2 ([1,0,0]) и mapDirection(1)=3 ([0,1,0]).
        //    Это горизонтальные укладки, что не даёт настоящих 2 этажей по правилам пересечений,
        //    но хотя бы z=-1 отличит "второй этаж".
        int dir = (currentLevel % 2 == 0) ? 0 : 1;

        System.out.println("[TowerBuilderStrategy] Планируем уровень=" + currentLevel
                + " (z=" + zLevel + "), dir=" + dir + ", layerSize=" + layerSize);

        // 7. Сортируем слова по длине
        List<Word> copy = new ArrayList<>(words);
        Collections.sort(copy, Comparator.comparingInt(Word::getLength).reversed());

        // 8. Пытаемся уложить на этот этаж
        List<WordPlacement> placed = LevelPlanner.fillLayer(copy, towerMap, 0, 0,
                zLevel, layerSize, layerSize, dir);

        // 9. Удаляем из original списка уложенные слова
        for (WordPlacement wp : placed) {
            words.removeIf(w -> w.getIndex() == wp.getIndex());
        }

        System.out.println("[TowerBuilderStrategy] На уровне " + currentLevel
                + " (z=" + zLevel + ") размещено слов: " + placed.size());

        // 10. Если ничего не уложили на этот уровень, пытаемся понять, что делать
        if (placed.isEmpty()) {
            System.out.println("[TowerBuilderStrategy] Не удалось разместить слова на уровне " + currentLevel
                    + " (z=" + zLevel + ").");
            // Если высота (по TowerMap) >= forceHeight, завершаем
            if (currentHeight >= forceHeight) {
                System.out.println("[TowerBuilderStrategy] Уже набрали высоту " + currentHeight
                        + ", завершаем башню.");
                towerCompleted = true;
            } else {
                // Высота 1, и мы ничего не смогли выложить на второй "этаж"?
                // Возможно тупик, нужно shuffle или ещё что-то.
                // Пока просто не выставляем done=true, чтобы не ловить ошибку.
                System.out.println("[TowerBuilderStrategy] Высота=" + currentHeight
                        + " < " + forceHeight + ", слов разместить не смогли. "
                        + "Не завершаем (done=false). Возможно нужно shuffle.");
            }
            return result;
        }

        // 11. Иначе мы что-то уложили, двигаемся к следующему уровню
        currentLevel++;

        int newHeight = towerMap.getCurrentHeight();
        System.out.println("[TowerBuilderStrategy] После укладки высота=" + newHeight);

        // Возвращаем список слов, которые надо отправить на сервер
        return placed;
    }

    public boolean isTowerCompleted() {
        return towerCompleted;
    }

    public void setTowerCompleted(boolean towerCompleted) {
        this.towerCompleted = towerCompleted;
    }

    /**
     * Возвращает внутреннюю оценку высоты из TowerMap (maxZ + 1).
     * Внимание: towerMap хранит занятые клетки, где z может быть 0, -1, -2...
     * maxZ может быть 0, поэтому getCurrentHeight() вернёт 1.
     * Если есть клетки на z=-1, maxZ может оставаться 0 (ведь -1 < 0),
     * так что towerMap.getCurrentHeight() в таком виде
     * может не отражать реальное количество этажей по логике DatsCity.
     */
    public int getInternalTowerHeight() {
        return (towerMap != null) ? towerMap.getCurrentHeight() : 0;
    }
}
