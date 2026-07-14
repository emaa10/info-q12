package racing.model;

public class Spieler {

    private String name;
    private Auto auto;

    // rennfortschritt: pro spieler getrennt, damit checkpoints/runden nich geteilt werden
    private int naechsterCheckpoint = 0;
    private long lapStartZeit = -1;
    private int lapZaehler = 0;
    private long besteRunde = -1;
    private int kreuzungsCooldown = 300;
    private boolean aufStrecke = true;
    private int letzterScore = 0;

    public Spieler(String name, Auto auto) {
        this.name = name;
        this.auto = auto;
    }

    public String gibName() {
        return name;
    }

    public void setzeName(String name) {
        this.name = name;
    }

    public Auto gibAuto() {
        return auto;
    }

    public int gibNaechsterCheckpoint() {
        return naechsterCheckpoint;
    }

    public void setzeNaechsterCheckpoint(int naechsterCheckpoint) {
        this.naechsterCheckpoint = naechsterCheckpoint;
    }

    public long gibLapStartZeit() {
        return lapStartZeit;
    }

    public void setzeLapStartZeit(long lapStartZeit) {
        this.lapStartZeit = lapStartZeit;
    }

    public int gibLapZaehler() {
        return lapZaehler;
    }

    public void erhoeheLapZaehler() {
        lapZaehler++;
    }

    public long gibBesteRunde() {
        return besteRunde;
    }

    public void setzeBesteRunde(long besteRunde) {
        this.besteRunde = besteRunde;
    }

    public int gibKreuzungsCooldown() {
        return kreuzungsCooldown;
    }

    public void setzeKreuzungsCooldown(int kreuzungsCooldown) {
        this.kreuzungsCooldown = kreuzungsCooldown;
    }

    public boolean istAufStrecke() {
        return aufStrecke;
    }

    public void setzeAufStrecke(boolean aufStrecke) {
        this.aufStrecke = aufStrecke;
    }

    public int gibLetzterScore() {
        return letzterScore;
    }

    public void setzeLetzterScore(int letzterScore) {
        this.letzterScore = letzterScore;
    }
}
