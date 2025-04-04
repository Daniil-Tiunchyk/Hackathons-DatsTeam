package dev.datscity;

import dev.datscity.client.GameApiClient;
import dev.datscity.model.ApiResponses.PlayerExtendedWordsResponse;
import dev.datscity.model.ApiResponses.PlayerWordsResponse;
import dev.datscity.model.Word;
import dev.datscity.model.WordPlacement;
import dev.datscity.strategy.TowerBuilderStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Пример главного цикла,
 * где мы выставляем done = strategy.isTowerCompleted().
 */
public class Main {
    public static void main(String[] args) {
        String baseUrl = "https://games-test.datsteam.dev";
        String apiToken = "e2dcd786-b907-4984-97cd-5c34bf8edbd7";

        GameApiClient apiClient = new GameApiClient(baseUrl, apiToken);
        TowerBuilderStrategy strategy = new TowerBuilderStrategy();

        while (true) {
            try {
                System.out.println("\n[Main] Запрос /api/words...");
                PlayerExtendedWordsResponse wordsResp = apiClient.getWords();
                if (wordsResp == null || wordsResp.words == null) {
                    System.out.println("[Main] Нет данных от /api/words, завершаем.");
                    break;
                }

                List<Word> currentWords = new ArrayList<>();
                for (int i = 0; i < wordsResp.words.size(); i++) {
                    currentWords.add(new Word(i, wordsResp.words.get(i)));
                }

                // Если башня завершена, начинаем новую
                if (strategy.isTowerCompleted()) {
                    strategy.startNewTower(currentWords, wordsResp.mapSize);
                }

                // Планируем следующий уровень
                List<WordPlacement> placements = strategy.planNextMove(currentWords);
                boolean done = strategy.isTowerCompleted();

                System.out.println("[Main] Отправка запроса /api/build: placements="
                        + placements.size() + ", done=" + done
                        + ", внутренняя высота=" + strategy.getInternalTowerHeight());

                PlayerWordsResponse buildResp = apiClient.sendBuild(placements, done);

                // Ждём до следующего хода
                int waitMs = wordsResp.nextTurnSec * 1000;
                if (waitMs < 1000) waitMs = 1000; // минимум 1с
                Thread.sleep(waitMs);

            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
}
