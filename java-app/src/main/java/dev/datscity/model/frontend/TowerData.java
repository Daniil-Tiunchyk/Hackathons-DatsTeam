package dev.datscity.model.frontend;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class TowerData {
    private List<DoneTower> doneTowers;
    private double score;
    private Tower tower;
}
