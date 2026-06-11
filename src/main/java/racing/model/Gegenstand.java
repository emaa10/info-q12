package racing.model;

import racing.datastructure.Datenelement;

// alle möglichen Gegenstände auf der Map (hazards, POIs, etc.)
public abstract class Gegenstand implements Datenelement {

    protected int x;
    protected int y;

    public void setzePosition(int x, int y) {
        this.x = x;
        this.y = y
    }

    public int[] gebePosition() {
        return new int[] { x, y };
    }
}
