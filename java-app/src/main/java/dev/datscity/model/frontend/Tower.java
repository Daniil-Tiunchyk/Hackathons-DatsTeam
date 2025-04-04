package dev.datscity.model.frontend;

import java.util.List;

public class Tower {
    private double score;
    private List<WordData> words;

    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public List<WordData> getWords() {
        return words;
    }
    public void setWords(List<WordData> words) {
        this.words = words;
    }
}
