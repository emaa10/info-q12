package main.java.racing.model;

import main.java.racing.datastructure.Datenelement;

public class Auto implements Datenelement {

    private int x;
    private int y;
    private int winkel;

    public Auto(int x, int y, int winkel) {
        this.x = x;
        this.y = y;
        this.winkel = winkel;
    }

    public void fahre(int geschwindigkeit) {
        // todo
    }

    public void drehe(int gradAenderung) {
        // todo
    }

    public int[] gebePos() {
        return new int[] { x, y };
    }

    public int gibWinkel() {
        return winkel;
    }
}
