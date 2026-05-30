package racing.model;

// kommt gliech von jakob rein
public class Karte {

    private int seed;

    public Karte(int seed) {
        this.seed = seed;
    }

    public void lade(int seed) {
        this.seed = seed;
        // TODO: Strecke aus seed erzeugen
    }

    public void setze(int seed) {
        this.seed = seed;
    }

    public int gibSeed() {
        return seed;
    }
}
