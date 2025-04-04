package dev.datscity.model;

/**
 * Класс, представляющий слово из набора.
 * Хранит индекс слова, текст и его длину.
 */
public class Word {
    private int index;
    private String text;
    private int length;

    public Word(int index, String text) {
        this.index = index;
        this.text = text;
        this.length = text.length();
        System.out.println("[Word] Создано слово: index=" + index + ", text=" + text + ", length=" + length);
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
