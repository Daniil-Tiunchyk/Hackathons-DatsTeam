package dev.datscity.model.frontend;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Tower {
    private double score;
    private List<WordData> words;
}
