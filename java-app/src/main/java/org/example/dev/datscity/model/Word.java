package org.example.dev.datscity.model;

/**
 * Класс, представляющий слово из набора.
 * index - индекс слова в текущем списке (нужен для отправки в /api/build).
 * text  - собственно строка/текст слова.
 * length - длина слова (можно получить из text.length()).
 */
public class Word {
    private int index;
    private String text;
    private int length;

    public Word(int index, String text) {
        this.index = index;
        this.text = text;
        this.length = text.length();
    }

    public int getIndex() {
        return index;
    }

    public String getText() {
        return text;
    }

    public int getLength() {
        return length;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setText(String text) {
        this.text = text;
        this.length = (text != null) ? text.length() : 0;
    }
}
