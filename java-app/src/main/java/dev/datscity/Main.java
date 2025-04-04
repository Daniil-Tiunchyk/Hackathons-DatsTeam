package dev.datscity;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.datscity.model.ApiResponses.*;
import dev.datscity.model.PairWord;
import dev.datscity.model.Word;
import dev.datscity.model.WordPlacement;
import dev.datscity.strategy.TowerBuilderStrategy;
import dev.datscity.strategy.WordPairGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {
    // Список для накопления завершённых башен
    private static List<DoneTower> doneTowers = new ArrayList<>();
    private static int towerIdCounter = 1;

    /**
     * //TODO перенести в другой класс так же
     * Метод для решения завершать ли текущее строительство здания подссчитывая очки
     */
    public static boolean isDoneTower(List<WordPlacement> placements, List<WordPlacement> newPlacements, TowerData towerDataResponse) {
        return placements.size() <= newPlacements.size();
    }

    public static void main(String[] args) {


        ObjectMapper mapper = new ObjectMapper();

        while (true) {
            try {

                System.out.println("\n[Main] Чтение данных из файла json/shuffle.json...");
                // Читаем данные из файла, структура которого соответствует PlayerExtendedWordsResponse
                PlayerExtendedWordsResponse wordsResp = mapper.readValue(new File("src/main/java/json/shuffle.json"), PlayerExtendedWordsResponse.class);
                TowerData towerDataResponse = mapper.readValue(new File("src/main/java/json/towers.json"), TowerData.class);

                if (wordsResp == null || wordsResp.words == null) {
                    System.out.println("[Main] Нет данных из файла, завершаем работу.");
                    break;
                }

                List<Word> currentWords = new ArrayList<>();
                for (int i = 0; i < wordsResp.words.size(); i++) {
                    currentWords.add(new Word(i, wordsResp.words.get(i)));
                }

                List<PairWord> pairWordList = WordPairGenerator.generateAllWordPairs(currentWords);

                List<WordPlacement> placements = TowerBuilderStrategy.getNextWordsPlacement(towerDataResponse.getTower(), currentWords, pairWordList);
                List<WordPlacement> newPlacements = new ArrayList<>();

                if (!towerDataResponse.getTower().getWords().isEmpty()) {
                    newPlacements = TowerBuilderStrategy.getNextWordsPlacement(new Tower(0, new ArrayList<>()), currentWords, pairWordList);
                }

                boolean isDone = isDoneTower(placements, newPlacements, towerDataResponse);

                if (isDone) {
                    placements = newPlacements;
                }

                TowerData towerData = new TowerData();
                towerData.setDoneTowers(doneTowers);
                Tower tower = new Tower();
                List<WordData> words = new ArrayList<>();

                //TODO возможно заранее это делать, чтобы было удобнее сравнивать в isDoneTower()
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
