package racing.controller;

import racing.model.Spiel;
import racing.view.Oberflaeche;

public class Kontrolleur implements Runnable {

    // Oberfläche und Spiel deklarieren
    private final Oberflaeche oberflaeche;
    private final Spiel spiel;

    private volatile boolean aktiv;

    // Konstruktur für den controller, Attribute darin initialisieren
    public Kontrolleur(Oberflaeche oberflaeche, Spiel spiel) {
        this.oberflaeche = oberflaeche;
        this.spiel = spiel;
    }

    // run methode, die den controller ausführt
    @Override
    public void run() {
        aktiv = true;
        while (aktiv) {
            pruefeEingabe();
            try {
                Thread.sleep(16);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                aktiv = false;
            }
        }
    }

    /**
     * Prueft die zuletzt gemeldeten Eingaben und leitet daraus Aktionen fuers
     * Spiel ab. Noch nicht implementiert.
     */
    public void pruefeEingabe() {
        // todo
    }

    public void stoppe() {
        aktiv = false;
    }
}
