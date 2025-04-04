package dev.datscity.model.frontend;

public class WordData {
    private int dir;
    private int[] pos;
    private String text;

    public WordData(int dir, int[] pos, String text) {
        this.dir = dir;
        this.pos = pos;
        this.text = text;
    }

    public int getDir() {
        return dir;
    }
    public int[] getPos() {
        return pos;
    }
    public String getText() {
        return text;
    }
}
