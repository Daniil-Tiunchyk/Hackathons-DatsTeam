package org.example;

import org.example.dev.datscity.client.GameApiClient;
import org.example.dev.datscity.model.Word;
import org.example.dev.datscity.strategy.TowerBuilderStrategy;
import org.example.dev.datscity.model.ApiResponses.*;
import org.example.dev.datscity.model.WordPlacement;

import java.util.ArrayList;
import java.util.List;

/**
 * Главный класс, точка входа. Запускает стратегию, взаимодействует с API.
 * В реальном хакатоне вам нужно будет вызывать цикл каждые ~60с
 * или ориентироваться на nextTurnSec, пока раунд не завершится.
 */
public class Main {
    public static void main(String[] args) {
        // ПАРСИНГ АРГУМЕНТОВ (упрощённо)
        // Предположим, что:
        //   --test -> baseUrl = "https://games-test.datsteam.dev"
        //   иначе baseUrl = "https://games.datsteam.dev"
        //   --token <token> -> задаём apiToken
        String baseUrl = "https://games-test.datsteam.dev";
        String apiToken = "e2dcd786-b907-4984-97cd-5c34bf8edbd7";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--test":
                    baseUrl = "https://games-test.datsteam.dev";
                    break;
                case "--token":
                    if (i + 1 < args.length) {
                        apiToken = args[++i];
                    }
                    break;
            }
        }

        // Создаём клиент для API
        GameApiClient apiClient = new GameApiClient(baseUrl, apiToken);
        // Создаём стратегию
        TowerBuilderStrategy strategy = new TowerBuilderStrategy();

        System.out.println("[Main] Starting bot at " + baseUrl);

        // Основной цикл игры (примитивный, ориентирующийся на nextTurnSec).
        // В реальном сценарии нужно работать в течение всего раунда.
        while (true) {
            try {
                // 1. Получаем список слов и инфо о ходе
                PlayerExtendedWordsResponse wordsResp = apiClient.getWords();
                if (wordsResp == null) {
                    // Если сервер вернул пусто (бывает), выходим
                    System.out.println("[Main] No more data from server, exiting.");
                    break;
                }
                // 2. Преобразуем список слов в List<Word> с индексами
                List<Word> currentWords = new ArrayList<>();
                for (int i = 0; i < wordsResp.words.size(); i++) {
                    String text = wordsResp.words.get(i);
                    Word w = new Word(i, text);
                    currentWords.add(w);
                }

                // Если башня закончена по мнению стратегии, начнём новую:
                if (strategy.isTowerCompleted()) {
                    strategy.startNewTower(currentWords, wordsResp.mapSize);
                }

                // 3. Стратегия решает, какие слова ставить
                List<WordPlacement> placements = strategy.planNextMove(currentWords);

                // Если список placements пустой и стратегия говорит "всё, завершаем башню":
                boolean done = false;
                if (strategy.isTowerCompleted()) {
                    done = true;
                }

                // 4. Отправляем ход на сервер
                //    Если done=true, башня будет завершена
                System.out.println("[Main] Sending build request (words=" + placements.size() + ", done=" + done + ")");
                PlayerWordsResponse buildResp = apiClient.sendBuild(placements, done);

                // buildResp может содержать обновлённый список слов (если башня не завершена).
                // Но для простоты мы будем каждый раз заново вызывать getWords() на новом ходу.
                // Если башня завершена, сервер сбросит текущую башню.

                // 5. Если раунд подходит к концу, можно выйти из цикла
                //    (Например, анализировать roundEndsAt из wordsResp)
                //    Здесь условно ограничимся количеством итераций или сигналом
                //    nextTurnSec, если оно слишком велико/мало.
                if (wordsResp.roundEndsAt != null) {
                    // Проверяем, не вышло ли время, в реальной игре смотреть на текущее время
                    // Здесь для примера пропустим.
                }

                // 6. Ждём до следующего хода (примерно nextTurnSec)
                int waitSec = (wordsResp.nextTurnSec > 0) ? wordsResp.nextTurnSec : 60;
                System.out.println("[Main] Sleep " + waitSec + "s until next turn...");
                Thread.sleep(waitSec * 1000L);

            } catch (Exception e) {
                e.printStackTrace();
                // Попробуем продолжить или прервём
                break;
            }
        }

        System.out.println("[Main] Game loop finished.");
    }
}
