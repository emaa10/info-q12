package racing.controller;

import java.util.Set;

import javafx.scene.input.KeyCode;
import racing.model.Auto;
import racing.model.Spiel;
import racing.model.Spieler;
import racing.view.Oberflaeche;

public class Kontrolleur implements Runnable {

    private final Oberflaeche oberflaeche;
    private final Spiel spiel;

    private volatile boolean aktiv;

    public Kontrolleur(Oberflaeche oberflaeche, Spiel spiel) {
        this.oberflaeche = oberflaeche;
        this.spiel = spiel;
    }

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

    public void pruefeEingabe() {
        Set<KeyCode> tasten = oberflaeche.gibGedrueckteTasten();

        for (Spieler s : spiel.gibSpieler()) {
            Auto auto = s.gibAuto();

            if (tasten.contains(KeyCode.UP)    || tasten.contains(KeyCode.W)) auto.gibGas();
            if (tasten.contains(KeyCode.DOWN)  || tasten.contains(KeyCode.S)) auto.bremse();
            if (tasten.contains(KeyCode.LEFT)  || tasten.contains(KeyCode.A)) auto.dreheLinks();
            if (tasten.contains(KeyCode.RIGHT) || tasten.contains(KeyCode.D)) auto.dreheRechts();
        }
    }

    public void stoppe() {
        aktiv = false;
    }
}
