package racing.model;

import racing.datastructure.Liste;

public class Level {

    private Map map;
    private Liste gegenstaende;

    public Level(Map map) {
        this.map = map;
        this.gegenstaende = new Liste();
    }

    public void platziereGegenstand(Gegenstand gegenstand, int x, int y) {
        // TODO: alles, auch object pool
    }

    public Map gibMap() {
        return map;
    }

    public Liste gibGegenstaende() {
        return gegenstaende;
    }
}
