package racing.model;

import racing.datastructure.Liste;

public class Level {

    private Map map;
    private Liste gegenstaende;
    private Liste pool;

    public Level(Map map) {
        this.map = map;
        this.gegenstaende = new Liste();
        pool = new Liste();
    }

    public void platziereGegenstand(Gegenstand gegenstand, int x, int y) {
        // Entspricht in geg. Diagramm (vgl. lb): acquireInstance().
        Gegenstand objekt;
        if (!pool.istLeer()) {
            objekt = (Gegenstand) pool.entferneVorne();
        } else {
            objekt = gegenstand;
        }
        objekt.setzePosition(x, y);
        gegenstaende.fuegeHintenEin(objekt);
    }

    public Map gibMap() {
        return map;
    }

    public Liste gibGegenstaende() {
        return gegenstaende;
    }

    public void entferneGegenstand(Gegenstand gegenstand) {
        //entspricht in leebmanns diagramm: return instance()
        gegenstaende.entferneElement(gegenstand);
        pool.fuegeHintenEin(gegenstand);
    }
}
