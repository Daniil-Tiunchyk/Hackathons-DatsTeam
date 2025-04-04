package dev.datscity.strategy;

import dev.datscity.model.PairWord;
import dev.datscity.model.Word;
import dev.datscity.model.WordPlacement;
import dev.datscity.model.ApiResponses.*;
import java.util.Collections;
import java.util.List;

/**
 * Класс стратегии, стремящийся построить как минимум 2 уровня (forceHeight=2)
 * и дальше - до максимума, пока есть слова и/или есть смысл.
 *
 * Главное отличие: мы трактуем "текущий этаж" как z = -currentLevel,
 * чтобы второй этаж находился на z = -1, а не z=1.
 */
public class TowerBuilderStrategy {
    public static List<WordPlacement> getNextWordsPlacement(Tower currentTower, List<Word> words, List<PairWord> pairWordList){
        return Collections.emptyList();
    }
}
