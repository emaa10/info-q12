package racing.model;

public class SpielstandEintrag {

    private final String spielerName;
    private final int levelId;
    private final int punkte;
    private final long zeitMs;
    private final String erstelltAm;

    public SpielstandEintrag(String spielerName, int levelId, int punkte, long zeitMs, String erstelltAm) {
        this.spielerName = spielerName;
        this.levelId = levelId;
        this.punkte = punkte;
        this.zeitMs = zeitMs;
        this.erstelltAm = erstelltAm;
    }

    public String gibSpielerName() { return spielerName; }
    public int gibLevelId()        { return levelId; }
    public int gibPunkte()         { return punkte; }
    public long gibZeitMs()        { return zeitMs; }
    public String gibErstelltAm()  { return erstelltAm; }

    @Override
    public String toString() { // bloss für debug
        return spielerName + " | Level " + levelId + " | " + punkte + " Punkte | " + (zeitMs / 1000.0) + "s";
    }
}
