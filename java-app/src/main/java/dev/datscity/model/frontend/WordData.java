package dev.datscity.model.frontend;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WordData {
    private int dir;
    private int[] pos;
    private String text;
}
