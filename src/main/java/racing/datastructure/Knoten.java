package main.java.racing.datastructure;

// speichert ein datenelement und verweist auf seinen nachfolger
public class Knoten implements Listenelement {

    private Datenelement daten;
    private Listenelement nachfolger;

    public Knoten(Datenelement daten) {
        this.daten = daten;
        this.nachfolger = new Abschluss();
    }

    @Override
    public boolean istAbschluss() {
        return false;
    }

    public Datenelement gebeDaten() {
        return daten;
    }

    public void setzeDaten(Datenelement daten) {
        this.daten = daten;
    }

    public Listenelement gebeNachfolger() {
        return nachfolger;
    }

    public void setzeNachfolger(Listenelement nachfolger) {
        this.nachfolger = nachfolger;
    }
}
