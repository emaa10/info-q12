package racing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import racing.controller.Kontrolleur;
import racing.model.Spiel;
import racing.view.Oberflaeche;

/**
 * Einstiegspunkt des Spiels.
 *
 * Hier wird das MVC-Muster verdrahtet:
 * - Oberflaeche (View) laeuft auf dem JavaFX-Thread,
 * - Spiel (Model) laeuft in einem eigenen Thread,
 * - Kontrolleur (Controller) laeuft in einem eigenen Thread.
 *
 * Aktuell oeffnet sich nur ein leeres, weisses Fenster. Die Spiel- und
 * Kontroll-Threads laufen bereits, tun aber noch nichts (Grundgeruest).
 */
public class Main extends Application {

    private static final int BREITE = 960;
    private static final int HOEHE = 600;

    private Spiel spiel;
    private Kontrolleur kontrolleur;
    private Thread spielThread;
    private Thread kontrollThread;

    @Override
    public void start(Stage buehne) {
        // --- View ---
        Oberflaeche oberflaeche = new Oberflaeche();

        // --- Model & Controller ---
        spiel = new Spiel(oberflaeche);
        kontrolleur = new Kontrolleur(oberflaeche, spiel);

        // --- Fenster aufbauen ---
        Scene szene = new Scene(oberflaeche.gibWurzel(), BREITE, HOEHE);
        buehne.setTitle("Racing-Projekt");
        buehne.setScene(szene);
        buehne.show();

        // --- Eigene Threads fuer Model und Controller starten ---
        spielThread = new Thread(spiel, "Spiel-Thread");
        kontrollThread = new Thread(kontrolleur, "Kontrolleur-Thread");
        spielThread.setDaemon(true);
        kontrollThread.setDaemon(true);
        spielThread.start();
        kontrollThread.start();
    }

    @Override
    public void stop() {
        // Threads beim Schliessen des Fensters sauber beenden.
        if (spiel != null) {
            spiel.stoppe();
        }
        if (kontrolleur != null) {
            kontrolleur.stoppe();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
