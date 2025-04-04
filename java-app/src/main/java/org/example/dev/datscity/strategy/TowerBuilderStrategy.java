package org.example.dev.datscity.strategy;

import org.example.dev.datscity.model.TowerMap;
import org.example.dev.datscity.model.Word;
import org.example.dev.datscity.model.WordPlacement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Основной класс стратегии строительства башни.
 * Хранит текущее состояние "строящейся" башни (TowerMap),
 * решает, когда начать новую башню, когда завершить, как размещать слова.
 */
public class TowerBuilderStrategy {

    private TowerMap towerMap;          // Текущее состояние башни
    private boolean towerCompleted;     // Флаг: башня завершена
    private int currentLevel;           // Номер текущего этажа (условно z, но у нас towerMap может проверять z)
    private int layerSize;             // Планируемый "размер" слоя (ширина, глубина)
    private int minHeight = 2;         // Минимальная высота (пример)
    private int mapX = 30, mapY = 30, mapZ = 100; // по умолчанию

    public TowerBuilderStrategy() {
        // Инициируем стратегию, но реальную башню создадим при старте
        this.towerCompleted = true; // чтобы сигнализировать, что нет активной башни
    }

    /**
     * Инициализировать новую башню (называется, если предыдущая завершена).
     * @param wordsList - список слов текущего хода
     */
    public void startNewTower(List<Word> wordsList, int[] mapSize) {
        // Создаём пустую TowerMap (размер из mapSize)
        this.mapX = mapSize[0];
        this.mapY = mapSize[1];
        this.mapZ = mapSize[2];
        this.towerMap = new TowerMap(mapX, mapY, mapZ);

        this.towerCompleted = false;
        this.currentLevel = 0;

        // Определяем начальный размер слоя (layerSize).
        // Упрощённая эвристика: берем минимум(mapX, mapY)/2
        // или другое правило.
        this.layerSize = Math.min(mapX, mapY) / 2;
        if (this.layerSize < 2) this.layerSize = 2; // чтоб иметь хоть что-то

        System.out.println("[Strategy] Start new tower with layerSize=" + layerSize);
    }

    /**
     * Планируем очередное размещение слов в башне.
     * @param words - список доступных слов (неиспользованных)
     * @return список WordPlacement для отправки на /api/build
     */
    public List<WordPlacement> planNextMove(List<Word> words) {
        List<WordPlacement> result = new ArrayList<>();

        // Если башня уже завершена, ничего не строим
        if (towerCompleted) {
            return result;
        }

        // 1) Проверим высоту. Если уже достигли minHeight и мало подходящих слов, завершаем.
        int heightNow = towerMap.getCurrentHeight();
        if (heightNow >= minHeight && (words.isEmpty() || heightNow > 10)) {
            // Условие для завершения: высота >= minHeight, либо слишком высоко
            System.out.println("[Strategy] Decided to finish tower at height=" + heightNow);
            towerCompleted = true;
            return result; // пустой список + флаг done=true в вызывающем коде
        }

        // 2) Иначе пытаемся построить следующий уровень
        // Вычислим ориентацию: пусть четные уровни dir=0 (по X), нечетные dir=1 (по Y).
        int dir = (currentLevel % 2 == 0) ? 0 : 1;
        int zLevel = currentLevel; // упрощённо считаем, что уровень совпадает с currentLevel

        // Укладываем слова методом LevelPlanner
        List<Word> wordsCopy = new ArrayList<>(words);
        Collections.sort(wordsCopy, Comparator.comparingInt(Word::getLength).reversed());
        // Жадно пытаемся заполнить layerSize x layerSize
        int offsetX = 0, offsetY = 0; // можно центрировать, если нужно.

        List<WordPlacement> levelPlacements = LevelPlanner.fillLayer(wordsCopy, towerMap, offsetX, offsetY, zLevel, layerSize, layerSize, dir);

        // Уберём из "words" все использованные (сравним по index)
        for (WordPlacement wp : levelPlacements) {
            int idx = wp.getIndex();
            // Ищем слово с таким индексом и удаляем из входного списка
            words.removeIf(w -> w.getIndex() == idx);
        }

        // Обновим счётчик уровня
        currentLevel++;

        // Возвращаем размещения
        return levelPlacements;
    }

    /**
     * Проверяем, завершена ли башня или нет.
     */
    public boolean isTowerCompleted() {
        return towerCompleted;
    }

    /**
     * Установить явное завершение (когда отправили done=true на сервер).
     */
    public void setTowerCompleted(boolean towerCompleted) {
        this.towerCompleted = towerCompleted;
    }
}
