package racing;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    private boolean threadsGestartet = false;

    // gameThread/controllerThread/audioThread erst hier starten (nicht schon in
    // start()) -> die spiel-schleife lief sonst schon im hauptmenü mit voller
    // geschwindigkeit mit, was den javafx-application-thread so stark auslastete
    // (dauerhaft >100% cpu), dass maus-klicks und tastatureingaben im menü nicht
    // mehr verarbeitet wurden. erst bei tatsaechlichem spielstart noetig.
    private void starteHintergrundThreadsFallsNoetig() {
        if (threadsGestartet) return;
        threadsGestartet = true;
        gameThread.start();
        controllerThread.start();
        audioThread.start();
    }

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
            starteHintergrundThreadsFallsNoetig();
        });
        // fortsetzen = gleicher seed weiter
        oberflaeche.setzeFortsetzenAktion(() -> {
            oberflaeche.zeigeSpiel();
            spiel.fortsetzen();
            starteHintergrundThreadsFallsNoetig();
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

        // spiel-inhalt bleibt fest bei 960x600 (komplette restliche logik/koordinaten unangetastet)
        // -> nur der output wird per Scale-Transform auf die tatsaechliche fenster/vollbild-groesse skaliert
        Region innenWurzel = (Region) oberflaeche.gibWurzel();
        innenWurzel.setMinSize(BREITE, HOEHE);
        innenWurzel.setPrefSize(BREITE, HOEHE);
        innenWurzel.setMaxSize(BREITE, HOEHE);

        Scale skalierung = new Scale(1, 1, 0, 0);
        innenWurzel.getTransforms().add(skalierung);

        Pane aussenWurzel = new Pane(innenWurzel);
        aussenWurzel.setStyle("-fx-background-color: black;");

        Runnable aktualisiereSkalierung = () -> {
            double breiteVerfuegbar = aussenWurzel.getWidth();
            double hoeheVerfuegbar = aussenWurzel.getHeight();
            if (breiteVerfuegbar <= 0 || hoeheVerfuegbar <= 0) return;
            double faktor = Math.min(breiteVerfuegbar / BREITE, hoeheVerfuegbar / HOEHE);
            skalierung.setX(faktor);
            skalierung.setY(faktor);
            innenWurzel.setLayoutX((breiteVerfuegbar - BREITE * faktor) / 2.0);
            innenWurzel.setLayoutY((hoeheVerfuegbar - HOEHE * faktor) / 2.0);
        };
        aussenWurzel.widthProperty().addListener((obs, o, n) -> aktualisiereSkalierung.run());
        aussenWurzel.heightProperty().addListener((obs, o, n) -> aktualisiereSkalierung.run());

        Scene szene = new Scene(aussenWurzel, BREITE, HOEHE);
        szene.getStylesheets().add(getClass().getResource("/css/menu.css").toExternalForm());
        buehne.setTitle("Racing Game Info Q12");
        buehne.setScene(szene);
        buehne.setMinWidth(480);
        buehne.setMinHeight(300);

        // ESC bleibt fuers pause-menü reserviert -> vollbild nicht automatisch darüber verlassen
        buehne.setFullScreenExitHint("");
        buehne.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        // F11 schaltet vollbild um (z.b. fuer 1080p)
        szene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.F11) {
                buehne.setFullScreen(!buehne.isFullScreen());
            }
        });

        // Kiosk-start: zuerst normal (dekoriert) zeigen, damit der
        // window-manager dem Fenster X11-Input-Fokus gibt (undecorated
        // Fenster bekommen bei matchbox-window-manager nie Fokus -> Maus/
        // Tastatur kamen nie an). Danach programmatisch in echtes Vollbild
        // wechseln, das der Fokus bereits vorhanden ist -> kein Haengenbleiben
        // beim Start (das Haengen frueher passierte nur, wenn Vollbild schon
        // VOR dem ersten Fokus angefordert wurde).
        buehne.show();
        buehne.requestFocus();
        javafx.application.Platform.runLater(() -> {
            buehne.setFullScreen(true);
            // explizit fokussieren -> default focus-traversal greift wegen der
            // Scale-Transform-verschachtelung (siehe oben) offenbar nicht zuverlaessig
            oberflaeche.gibNameFeld().requestFocus();
        });

        // Tastatureingabe registrieren (braucht die fertige Scene)
        oberflaeche.registriereEingabe(szene);

        gameThread = new Thread(spiel, "GameThread");
        controllerThread = new Thread(kontrolleur, "ControllerThread");
        audio           = new AudioWiedergabe("/audio/Info-Soundtrack1-06-12-2026.wav");
        audioThread      = new Thread(audio, "AudioThread");
        gameThread.setDaemon(true);
        controllerThread.setDaemon(true);
        audioThread.setDaemon(true);
        // bewusst noch nicht gestartet -> siehe starteHintergrundThreadsFallsNoetig()
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
