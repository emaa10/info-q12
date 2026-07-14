package racing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import racing.controller.Kontrolleur;
import racing.datastructure.Knoten;
import racing.datastructure.Liste;
import racing.datastructure.Listenelement;
import racing.model.Spiel;
import racing.model.SpielstandEintrag;
import racing.view.LeaderboardZeile;
import racing.view.Oberflaeche;

public class Main extends Application {

    private static final int BREITE = 960;
    private static final int HOEHE = 600;

    private Spiel spiel;
    private Kontrolleur kontrolleur;
    private AudioWiedergabe audio;
    private Thread gameThread;
    private Thread controllerThread;
    private Thread audioThread;

    @Override
    public void start(Stage buehne) {
        Oberflaeche oberflaeche = new Oberflaeche();

        spiel = new Spiel(oberflaeche);
        kontrolleur = new Kontrolleur(oberflaeche, spiel);

        // view und model verdrahten (mvc)
        // spiel starten = immer neuer seed
        oberflaeche.setzeStartAktion(() -> {
            spiel.setzeSpielerName(oberflaeche.gibSpielerName());
            spiel.setzeSpielerName2(oberflaeche.gibSpielerName2());
            oberflaeche.zeigeSpiel();
            spiel.neuesSpielMitSeed(spiel.erzeugeSeed());
        });
        // fortsetzen = gleicher seed weiter
        oberflaeche.setzeFortsetzenAktion(() -> {
            oberflaeche.zeigeSpiel();
            spiel.fortsetzen();
        });
        oberflaeche.setzePauseAktion(() -> {
            spiel.pausiere();
            oberflaeche.setzeFortsetzenSichtbar(true);
            oberflaeche.zeigeMenue();
        });
        // leaderboard: eintraege in text-zeilen + seeds umbauen
        oberflaeche.setzeLeaderboardAktion(() -> {
            Liste eintraege = spiel.gibLeaderboardEintraege();
            Liste zeilen = new Liste();
            Listenelement el = eintraege.gibAnfang();
            int platz = 1;
            while (!el.istAbschluss()) {
                SpielstandEintrag e = (SpielstandEintrag) ((Knoten) el).gebeDaten();
                String text = platz + ". " + e.gibSpielerName() + "   " + e.gibPunkte()
                    + " P   " + (e.gibZeitMs() / 1000.0) + " s   [seed " + e.gibSeed() + "]";
                zeilen.fuegeHintenEin(new LeaderboardZeile(text, e.gibSeed()));
                platz++;
                el = ((Knoten) el).gebeNachfolger();
            }
            oberflaeche.zeigeLeaderboard(zeilen);
        });
        // klick auf leaderboard-eintrag = dieses level (seed) spielen
        oberflaeche.setzeSeedAktion(seed -> {
            spiel.setzeSpielerName(oberflaeche.gibSpielerName());
            spiel.setzeSpielerName2(oberflaeche.gibSpielerName2());
            oberflaeche.zeigeSpiel();
            spiel.neuesSpielMitSeed(seed);
        });

        Scene szene = new Scene(oberflaeche.gibWurzel(), BREITE, HOEHE);
        szene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());
        buehne.setTitle("Racing Game Info Q12");
        buehne.setScene(szene);
        buehne.setMinHeight(HOEHE);
        buehne.setMinWidth(BREITE);
        buehne.setMaxHeight(HOEHE);
        buehne.setMaxWidth(BREITE);
        buehne.show();

        // Tastatureingabe registrieren (braucht die fertige Scene)
        oberflaeche.registriereEingabe(szene);

        gameThread = new Thread(spiel, "GameThread");
        controllerThread = new Thread(kontrolleur, "ControllerThread");
        audio           = new AudioWiedergabe("/audio/Info-Soundtrack1-06-12-2026.wav");
        audioThread      = new Thread(audio, "AudioThread");
        gameThread.setDaemon(true);
        controllerThread.setDaemon(true);
        audioThread.setDaemon(true);
        gameThread.start();
        controllerThread.start();
        audioThread.start();
    }

    @Override
    public void stop() {
        if (spiel != null) spiel.stoppe();
        if (kontrolleur != null) kontrolleur.stoppe();
        if (audio != null)       audio.stoppe();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
