package dev.datscity.model;

import lombok.Getter;

/**
 * Класс, представляющий слово из набора.
 * Хранит индекс слова, текст и его длину.
 */
@Getter
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
