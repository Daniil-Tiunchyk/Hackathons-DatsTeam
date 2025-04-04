package dev.datscity.model;

import lombok.*;

/**
 * Класс, представляющий слово из набора.
 * Хранит индекс слова, текст и его длину.
 */
@Data
@AllArgsConstructor
public class Word {
    private final int index;
    private final String text;
    private final int length;

    public Word(int index, String text) {
        this.index = index;
        this.text = text;
        this.length = text.length();
        System.out.println("[Word] Создано слово: index=" + index + ", text=" + text + ", length=" + length);
    }
}
