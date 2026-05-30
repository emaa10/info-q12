package racing.model;

public class Spieler {

    private String name;
    private Auto auto;

    public Spieler(String name, Auto auto) {
        this.name = name;
        this.auto = auto;
    }

    public String gibName() {
        return name;
    }

    public Auto gibAuto() {
        return auto;
    }
}
