package dev.datscity.model.frontend;

public class DoneTower {
    private int id;
    private double score;

    public DoneTower(int id, double score) {
        this.id = id;
        this.score = score;
    }

    public int getId() {
        return id;
    }
    public double getScore() {
        return score;
    }
}
