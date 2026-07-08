package racing.model;

import racing.datastructure.Datenelement;

public class SpielstandEintrag implements Datenelement {

    private final String spielerName;
    private final int seed;
    private final int punkte;
    private final long zeitMs;
    private final String erstelltAm;

    public SpielstandEintrag(String spielerName, int seed, int punkte, long zeitMs, String erstelltAm) {
        this.spielerName = spielerName;
        this.seed = seed;
        this.punkte = punkte;
        this.zeitMs = zeitMs;
        this.erstelltAm = erstelltAm;
    }

    public String gibSpielerName() { return spielerName; }
    public int gibSeed()           { return seed; }
    public int gibPunkte()         { return punkte; }
    public long gibZeitMs()        { return zeitMs; }
    public String gibErstelltAm()  { return erstelltAm; }

    @Override
    public String toString() { // bloss für debug
        return spielerName + " | seed " + seed + " | " + punkte + " Punkte | " + (zeitMs / 1000.0) + "s";
    }
}
