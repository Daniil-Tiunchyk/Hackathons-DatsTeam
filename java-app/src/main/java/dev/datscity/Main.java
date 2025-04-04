package dev.datscity;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datscity.model.ApiResponses.PlayerExtendedWordsResponse;
import dev.datscity.model.Word;
import dev.datscity.model.WordPlacement;
import dev.datscity.model.frontend.TowerData;
import dev.datscity.model.frontend.Tower;
import dev.datscity.model.frontend.WordData;
import dev.datscity.model.frontend.DoneTower;
import dev.datscity.strategy.TowerBuilderStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Список для накопления завершённых башен
    private static List<DoneTower> doneTowers = new ArrayList<>();
    private static int towerIdCounter = 1;

    public static void main(String[] args) {
        TowerBuilderStrategy strategy = new TowerBuilderStrategy();
        ObjectMapper mapper = new ObjectMapper();

        while (true) {
            try {
                System.out.println("\n[Main] Чтение данных из файла json/shuffle.json...");
                // Читаем данные из файла, структура которого соответствует PlayerExtendedWordsResponse
                PlayerExtendedWordsResponse wordsResp = mapper.readValue(new File("src/main/java/json/shuffle.json"), PlayerExtendedWordsResponse.class);
                if (wordsResp == null || wordsResp.words == null) {
                    System.out.println("[Main] Нет данных из файла, завершаем работу.");
                    break;
                }

                List<Word> currentWords = new ArrayList<>();
                for (int i = 0; i < wordsResp.words.size(); i++) {
                    currentWords.add(new Word(i, wordsResp.words.get(i)));
                }

                // Если башня завершена, сохраняем данные о ней и начинаем новую башню
                if (strategy.isTowerCompleted()) {
                    double completedTowerScore = strategy.getInternalTowerHeight();  // здесь можно вычислить итоговый счёт
                    doneTowers.add(new DoneTower(towerIdCounter++, completedTowerScore));
                    strategy.startNewTower(currentWords, wordsResp.mapSize);
                }

                // Вычисляем следующий ход
                List<WordPlacement> placements = strategy.planNextMove(currentWords);
                boolean done = strategy.isTowerCompleted();
                double currentScore = strategy.getInternalTowerHeight();

                // Формируем объект TowerData, соответствующий требуемой JSON-структуре
                TowerData towerData = new TowerData();
                towerData.setDoneTowers(doneTowers);
                towerData.setScore(currentScore);

                Tower tower = new Tower();
                tower.setScore(currentScore);
                List<WordData> words = new ArrayList<>();
                for (WordPlacement placement : placements) {
                    // Для каждого WordPlacement создаём объект с полями: dir, pos (x,y,z) и text
                    words.add(new WordData(
                            placement.getDir(),
                            new int[]{placement.getX(), placement.getY(), placement.getZ()},
                            placement.getText()
                    ));
                }
                tower.setWords(words);
                towerData.setTower(tower);

                // Сохраняем объект towerData в JSON-файл
                mapper.writeValue(new File("towerData.json"), towerData);
                System.out.println("[Main] Данные сохранены в towerData.json");

                // Задержка до следующего хода
                int waitMs = wordsResp.nextTurnSec * 1000;
                if (waitMs < 1000) {
                    waitMs = 1000; // минимум 1 секунда
                }
                Thread.sleep(waitMs);

            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
    }
}
