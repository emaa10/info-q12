package racing.model;

import racing.datastructure.Liste;

public class Level {

    private Karte karte;
    private Liste gegenstaende;

    public Level(Karte karte) {
        this.karte = karte;
        this.gegenstaende = new Liste();
    }

    public void platziereGegenstand(Gegenstand gegenstand, int x, int y) {
        // TODO: alles, auch object pool
    }

    public Karte gibKarte() {
        return karte;
    }

    public Liste gibGegenstaende() {
        return gegenstaende;
    }
}
