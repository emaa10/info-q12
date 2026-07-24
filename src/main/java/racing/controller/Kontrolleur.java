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
        if (!spiel.istRennenGestartet() || spiel.istPausiert() || spiel.istRennenBeendet()) return;

        Set<KeyCode> tasten = oberflaeche.gibGedrueckteTasten();
        Spieler[] spielerListe = spiel.gibSpieler();

        // Spieler 1: WASD + B fuer nitro
        if (spielerListe.length > 0) {
            Auto auto = spielerListe[0].gibAuto();
            auto.setzeNitroTaste(tasten.contains(KeyCode.B));
            if (tasten.contains(KeyCode.W)) auto.gibGas();
            if (tasten.contains(KeyCode.S)) auto.bremse();
            if (tasten.contains(KeyCode.A)) auto.dreheLinks();
            if (tasten.contains(KeyCode.D)) auto.dreheRechts();
        }

        // Spieler 2: Pfeiltasten + N fuer nitro
        if (spielerListe.length > 1) {
            Auto auto = spielerListe[1].gibAuto();
            auto.setzeNitroTaste(tasten.contains(KeyCode.N));
            if (tasten.contains(KeyCode.UP))    auto.gibGas();
            if (tasten.contains(KeyCode.DOWN))  auto.bremse();
            if (tasten.contains(KeyCode.LEFT))  auto.dreheLinks();
            if (tasten.contains(KeyCode.RIGHT)) auto.dreheRechts();
        }
    }

    public void stoppe() {
        aktiv = false;
    }
}
