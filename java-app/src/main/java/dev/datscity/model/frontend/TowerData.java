package dev.datscity.model.frontend;

import java.util.List;

public class TowerData {
    private List<DoneTower> doneTowers;
    private double score;
    private Tower tower;

    public List<DoneTower> getDoneTowers() {
        return doneTowers;
    }
    public void setDoneTowers(List<DoneTower> doneTowers) {
        this.doneTowers = doneTowers;
    }
    public double getScore() {
        return score;
    }
    public void setScore(double score) {
        this.score = score;
    }
    public Tower getTower() {
        return tower;
    }
    public void setTower(Tower tower) {
        this.tower = tower;
    }
}
