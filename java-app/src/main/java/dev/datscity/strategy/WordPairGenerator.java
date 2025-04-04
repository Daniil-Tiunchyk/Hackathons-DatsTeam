package dev.datscity.strategy;

import dev.datscity.model.PairWord;
import dev.datscity.model.Word;

import java.util.ArrayList;
import java.util.List;

public class WordPairGenerator {
    /**
     * Генерация всех возможных пар слов с одинаковыми буквами
     */
    public static List<PairWord> generateAllWordPairs(List<Word> words) {
        List<PairWord> pairs = new ArrayList<>();

        for (int i = 0; i < words.size(); i++) {
            Word firstWord = words.get(i);
            String firstText = firstWord.getText();

            for (int j = i + 1; j < words.size(); j++) {

                Word secondWord = words.get(j);
                String secondText = secondWord.getText();

                for (int firstPos = 0; firstPos < firstText.length(); firstPos++) {
                    char firstChar = firstText.charAt(firstPos);

                    for (int secondPos = 0; secondPos < secondText.length(); secondPos++) {
                        if (firstChar == secondText.charAt(secondPos)) {
                            // Создаем пару в обоих направлениях
                            pairs.add(new PairWord(
                                    firstWord, firstPos - 1,
                                    secondWord, secondPos - 1,
                                    firstChar
                            ));

                            pairs.add(new PairWord(
                                    secondWord, secondPos - 1,
                                    firstWord, firstPos - 1,
                                    firstChar
                            ));
                        }
                    }
                }
            }
        }

        return pairs;
    }

    //TODO для теста, потом удалить
    /*public static void main(String[] args) {
        List<Word> words = List.of(
                new Word(1, "кошка"),
                new Word(2, "собака"),
                new Word(3, "дом")
        );

        List<PairWord> pairs = generateAllPairs(words);
        pairs.forEach(System.out::println);
    }*/
}
