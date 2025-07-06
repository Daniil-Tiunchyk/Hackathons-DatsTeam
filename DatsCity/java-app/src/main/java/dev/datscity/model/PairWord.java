package dev.datscity.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class PairWord {
    private Word firstWord;
    private int firstIndex;
    private Word secondWord;
    private int secondIndex;
    private char commonChar;
    @Override
    public String toString() {
        return String.format("%s-%d <-> %s-%d ('%c')",
                firstWord.getText(), firstIndex + 1,
                secondWord.getText(), secondIndex + 1,
                commonChar);
    }
}
