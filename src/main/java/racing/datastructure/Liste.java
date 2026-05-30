package main.java.racing.datastructure;

public class Liste {
    private Listenelement anfang;

    public Liste() {
        this.anfang = new Abschluss(); // erstmal leer
    }

    public boolean istLeer() {
        return anfang.istAbschluss();
    }

    public void fuegeVorneEin(Datenelement daten) {
        Knoten neu = new Knoten(daten);
        neu.setzeNachfolger(anfang);
        anfang = neu;
    }

    public void fuegeHintenEin(Datenelement daten) {
        Knoten neu = new Knoten(daten);
        if (istLeer()) {
            neu.setzeNachfolger(anfang);
            anfang = neu;
            return;
        }
        Knoten aktuell = (Knoten) anfang;
        while (!aktuell.gebeNachfolger().istAbschluss()) {
            aktuell = (Knoten) aktuell.gebeNachfolger();
        }
        neu.setzeNachfolger(aktuell.gebeNachfolger());
        aktuell.setzeNachfolger(neu);
    }

    public Datenelement entferneVorne() {
        if (!istLeer()) {
            Datenelement daten = ((Knoten) anfang).gebeDaten();
            anfang = ((Knoten) anfang).gebeNachfolger();
            return daten;
        }
        return null;
    }

    public Datenelement entferneHinten() {
        if (!istLeer()) {
            Knoten aktuell = (Knoten) anfang;
            if (aktuell.gebeNachfolger().istAbschluss()) {
                Datenelement daten = aktuell.gebeDaten();
                anfang = new Abschluss();
                return daten;
            } else {
                while (!((Knoten) aktuell.gebeNachfolger()).gebeNachfolger().istAbschluss()) {
                    aktuell = (Knoten) aktuell.gebeNachfolger();
                }
                Datenelement daten = ((Knoten) aktuell.gebeNachfolger()).gebeDaten();
                aktuell.setzeNachfolger(new Abschluss());
                return daten;
            }
        }
        return null;
    }

    public Listenelement gibAnfang() {
        return anfang;
    }
}
