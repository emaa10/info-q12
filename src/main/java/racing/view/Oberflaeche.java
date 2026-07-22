package racing.view;

import java.util.HashSet;
import java.util.Set;
import java.util.function.IntConsumer;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import racing.datastructure.Knoten;
import racing.datastructure.Liste;
import racing.datastructure.Listenelement;
import racing.model.Hase;

// view-teil im MVC
public class Oberflaeche {

    private static final int BREITE = 960;
    private static final int HOEHE = 600;

    // wurzel stapelt die ebenen uebereinander (erstes kind unten)
    private final StackPane wurzel;
    private final Pane spielEbene;      // canvas / rennen
    private final StackPane menueEbene; // menü drueber, per setVisible an/aus
    private final VBox hauptmenuPanel;
    private final VBox leaderboardPanel;
    private final VBox leaderboardZeilen; // hier kommen die eintraege rein
    private final Canvas leinwand;
    private final GraphicsContext gc;
    private final Image baumBild;
    private final Image autoBild;
    private final Image autoBild2;
    private final Image grasBild;
    private final Image nitroBild;
    private final Image haseBild;
    private final MapView mapView;

    private final Set<KeyCode> gedrueckteTasten = new HashSet<>();

    // callbacks, damit view das spiel nich direkt kennt (mvc)
    private Runnable startAktion;
    private Runnable pauseAktion;
    private Runnable leaderboardAktion;
    private Runnable fortsetzenAktion;
    private IntConsumer seedAktion; // klick auf leaderboard-eintrag -> seed spielen

    private TextField nameFeld;
    private TextField nameFeld2;
    private Button fortsetzenKnopf; // nur sichtbar wenn pausiert

    public Oberflaeche() {
        this.leinwand = new Canvas(BREITE, HOEHE);
        this.gc = leinwand.getGraphicsContext2D();
        this.spielEbene = new Pane(leinwand);

        // menü ebene: container fuer die einzelnen panels
        this.hauptmenuPanel = new VBox(24);
        this.hauptmenuPanel.setAlignment(Pos.CENTER);
        this.hauptmenuPanel.getStyleClass().add("panel");
        this.leaderboardPanel = new VBox(16);
        this.leaderboardPanel.setAlignment(Pos.CENTER);
        this.leaderboardPanel.getStyleClass().add("panel");
        this.leaderboardZeilen = new VBox(6);
        this.leaderboardZeilen.setAlignment(Pos.CENTER);

        this.menueEbene = new StackPane(hauptmenuPanel, leaderboardPanel);
        this.menueEbene.getStyleClass().add("menue-ebene");

        baueHauptmenue();
        baueLeaderboard();
        zeigeHauptmenue();

        this.wurzel = new StackPane(spielEbene, menueEbene);

        this.baumBild = new Image(
            getClass().getResourceAsStream("/images/tree.png")
        );
        this.autoBild = new Image(
            getClass().getResourceAsStream("/images/auto_m2.png")
        );
        this.autoBild2 = new Image(
            getClass().getResourceAsStream("/images/auto_bmw.png")
        );
        this.grasBild = new Image(
            getClass().getResourceAsStream("/images/grass.jpg")
        );
        this.nitroBild = new Image(
            getClass().getResourceAsStream("/images/nitro.png")
        );
        this.haseBild = new Image(
            getClass().getResourceAsStream("/images/hase.png")
        );
        this.mapView = new MapView(this.gc);
    }

    // titel + name feld + buttons
    private void baueHauptmenue() {
        Label titel = new Label("RACING GAME");
        titel.getStyleClass().add("titel");

        nameFeld = new TextField();
        nameFeld.setPromptText("Name Spieler 1 (WASD+B)");
        nameFeld.setMaxWidth(240);
        nameFeld.getStyleClass().add("name-feld");

        nameFeld2 = new TextField();
        nameFeld2.setPromptText("Name Spieler 2 (Pfeile+N)");
        nameFeld2.setMaxWidth(240);
        nameFeld2.getStyleClass().add("name-feld");

        fortsetzenKnopf = new Button("Fortsetzen");
        Button startKnopf = new Button("Spiel starten");
        Button leaderboardKnopf = new Button("Leaderboard");
        Button beendenKnopf = new Button("Beenden");

        for (Button b : new Button[] { fortsetzenKnopf, startKnopf, leaderboardKnopf, beendenKnopf }) {
            b.setPrefWidth(240);
            b.getStyleClass().add("menue-button");
        }
        startKnopf.getStyleClass().add("start-button");

        // fortsetzen anfangs versteckt (gibt noch nix zum fortsetzen)
        fortsetzenKnopf.setVisible(false);
        fortsetzenKnopf.setManaged(false);

        // klick feuert auch bei enter/space, macht javafx selbst
        fortsetzenKnopf.setOnAction(e -> {
            if (fortsetzenAktion != null) fortsetzenAktion.run();
        });
        startKnopf.setOnAction(e -> {
            if (startAktion != null) startAktion.run();
        });
        leaderboardKnopf.setOnAction(e -> {
            if (leaderboardAktion != null) leaderboardAktion.run();
        });
        beendenKnopf.setOnAction(e -> Platform.exit());

        hauptmenuPanel.getChildren().addAll(titel, fortsetzenKnopf, nameFeld, nameFeld2, startKnopf, leaderboardKnopf, beendenKnopf);
    }

    // leaderboard: titel, zeilen-container, zurueck
    private void baueLeaderboard() {
        Label titel = new Label("LEADERBOARD");
        titel.getStyleClass().add("untertitel");

        Button zurueck = new Button("Zurueck");
        zurueck.setPrefWidth(240);
        zurueck.getStyleClass().add("menue-button");
        zurueck.setOnAction(e -> zeigeHauptmenue());

        leaderboardPanel.getChildren().addAll(titel, leaderboardZeilen, zurueck);
    }

    public String gibSpielerName() {
        String name = nameFeld.getText().trim();
        return name.isEmpty() ? "Spieler 1" : name;
    }

    public String gibSpielerName2() {
        String name = nameFeld2.getText().trim();
        return name.isEmpty() ? "Spieler 2" : name;
    }

    public void setzeStartAktion(Runnable startAktion) {
        this.startAktion = startAktion;
    }

    public void setzePauseAktion(Runnable pauseAktion) {
        this.pauseAktion = pauseAktion;
    }

    public void setzeLeaderboardAktion(Runnable leaderboardAktion) {
        this.leaderboardAktion = leaderboardAktion;
    }

    public void setzeFortsetzenAktion(Runnable fortsetzenAktion) {
        this.fortsetzenAktion = fortsetzenAktion;
    }

    public void setzeSeedAktion(IntConsumer seedAktion) {
        this.seedAktion = seedAktion;
    }

    // fortsetzen-knopf an/aus (an wenn ein spiel pausiert ist)
    public void setzeFortsetzenSichtbar(boolean sichtbar) {
        fortsetzenKnopf.setVisible(sichtbar);
        fortsetzenKnopf.setManaged(sichtbar);
    }

    // je zeile ein button, klick spielt den seed
    public void zeigeLeaderboard(Liste zeilen) {
        leaderboardZeilen.getChildren().clear();
        if (zeilen.istLeer()) {
            Label leer = new Label("noch keine eintraege");
            leer.setStyle("-fx-text-fill: #6b7488; -fx-font-family: Monospace; -fx-font-size: 15px;");
            leaderboardZeilen.getChildren().add(leer);
        } else {
            Listenelement el = zeilen.gibAnfang();
            while (!el.istAbschluss()) {
                LeaderboardZeile zeile = (LeaderboardZeile) ((Knoten) el).gebeDaten();
                int seed = zeile.gibSeed();
                Button b = new Button(zeile.gibText());
                b.getStyleClass().add("eintrag");
                b.setPrefWidth(460);
                b.setOnAction(e -> {
                    if (seedAktion != null) seedAktion.accept(seed);
                });
                leaderboardZeilen.getChildren().add(b);
                el = ((Knoten) el).gebeNachfolger();
            }
        }
        hauptmenuPanel.setVisible(false);
        leaderboardPanel.setVisible(true);
    }

    public void zeigeHauptmenue() {
        leaderboardPanel.setVisible(false);
        hauptmenuPanel.setVisible(true);
    }

    // menü an, spiel dahinter versteckt
    public void zeigeMenue() {
        zeigeHauptmenue();
        menueEbene.setVisible(true);
    }

    // menü aus, canvas sichtbar
    public void zeigeSpiel() {
        menueEbene.setVisible(false);
    }

    public Parent gibWurzel() {
        return wurzel;
    }

    public TextField gibNameFeld() {
        return nameFeld;
    }

    public MapView getMapView() {
        return this.mapView;
    }

    // nach scene-erstellung aufrufen
    public void registriereEingabe(Scene szene) {
        szene.setOnKeyPressed(e -> {
            // esc im rennen -> pause menü
            if (e.getCode() == KeyCode.ESCAPE) {
                if (!menueEbene.isVisible() && pauseAktion != null) {
                    gedrueckteTasten.clear(); // sonst haengt ne taste
                    pauseAktion.run();
                }
                return;
            }
            gedrueckteTasten.add(e.getCode());
        });
        szene.setOnKeyReleased(e -> gedrueckteTasten.remove(e.getCode()));
    }

    public Set<KeyCode> gibGedrueckteTasten() {
        return gedrueckteTasten;
    }

    // wird dann vom spiel aufgerufen zum aktualisieren
    public void aktualisiere() {
        // todo zeichnen
    }

    public void loesche() {
        Platform.runLater(() -> {
            int kachelGroesse = 200;
            for (int x = 0; x < BREITE; x += kachelGroesse) {
                for (int y = 0; y < HOEHE; y += kachelGroesse) {
                    gc.drawImage(grasBild, x, y, kachelGroesse, kachelGroesse);
                }
            }
        });
    }

    public void autoZeichnen(double x, double y, double winkel, int spielerIndex) {
        Platform.runLater(() -> {
            gc.save();
            gc.translate(x, y);
            // -90° damit das hochkante Bild (Fahrtrichtung unten) bei winkel=0 nach rechts zeigt
            // auto_bmw.png hat die front oben im bild -> zusaetzlich 180° drehen
            boolean istAuto2 = spielerIndex == 1;
            gc.rotate(winkel - 90 + (istAuto2 ? 180 : 0));
            double w = istAuto2 ? 34 : 28;
            double h = istAuto2 ? 68 : 56;
            Image bild = istAuto2 ? autoBild2 : autoBild;
            gc.drawImage(bild, -w / 2, -h / 2, w, h);
            gc.restore();
        });
    }

    public void baumZeichnen(int x, int y) {
        Platform.runLater(() -> {
            gc.drawImage(baumBild, x, y, 100, 100);
        });
    }

    public void nitroZeichnen(int x, int y) {
        Platform.runLater(() -> {
            gc.drawImage(nitroBild, x, y, 36, 36);
        });
    }

    public void haseZeichnen(int x, int y) {
        Platform.runLater(() -> {
            gc.drawImage(haseBild, x, y, Hase.BREITE, Hase.HOEHE);
        });
    }

    public void testSzene() {
        Platform.runLater(() -> {
            gc.clearRect(0, 0, BREITE, HOEHE);
            gc.drawImage(baumBild, 860, 500, 100, 100);
            gc.drawImage(baumBild, 120, 120, 100, 100);
            gc.drawImage(baumBild, 500, 500, 100, 100);
        });
    }

    public void punktZeichnen(int x, int y) {
        Platform.runLater(() -> {
            gc.setFill(Color.BLUE);
            gc.fillOval(x - 5, y - 5, 10, 10);
        });
    }

    public void startEndPunktZeichnen(int x, int y) {
        Platform.runLater(() -> {
            gc.setFill(Color.RED);
            gc.fillOval(x - 5, y - 5, 10, 10);
            gc.setFill(Color.BLACK);
            gc.fillText("Start-Ziel-Linie", x - 15, y - 15);
        });
    }

    public void streckeZeichnen(
        int startX,
        int startY,
        int[] xKoord,
        int[] yKoord
    ) {
        Platform.runLater(() -> {
            gc.beginPath();
            gc.setLineWidth(2.5);
            gc.moveTo(startX, startY);
            for (int k = 0; k < xKoord.length; k++) {
                gc.lineTo(xKoord[k], yKoord[k]);
            }
            gc.lineTo(startX, startY);
            gc.stroke();
        });
    }

    // seed-badge oben links, einmal pro frame
    public void topLeisteZeichnen(int seed) {
        Platform.runLater(() -> {
            gc.setFill(Color.rgb(0, 0, 0, 0.55));
            gc.fillRoundRect(12, 12, 190, 26, 8, 8);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            gc.fillText("Seed: " + seed, 22, 30);
        });
    }

    // warnung wenn ein spieler von der strecke abkommt. linkeSeite=false -> banner rechts der mitte
    public void streckenWarnungZeichnen(String spielerName, boolean linkeSeite) {
        Platform.runLater(() -> {
            double bx = linkeSeite ? BREITE / 2.0 - 330 : BREITE / 2.0 + 10;
            gc.setFill(Color.rgb(200, 0, 0, 0.85));
            gc.fillRoundRect(bx, 12, 320, 34, 8, 8);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
            gc.fillText(spielerName + ": Nicht mehr auf der Strecke!", bx + 12, 35);
        });
    }

    // hud-panel fuer einen spieler. rechteSeite=true -> oben rechts (spieler 1), sonst oben links (spieler 2)
    public void spielerHudZeichnen(
        String spielerName,
        int runde,
        long aktuelleZeitMs,
        long besteRundeMs,
        int checkpointFortschritt,
        int totalCheckpoints,
        int kollisionen,
        int letzterScore,
        String nitroStatus,
        double nitroFortschritt,
        boolean rechteSeite
    ) {
        Platform.runLater(() -> {
            double px = rechteSeite ? BREITE - 230 : 12;
            double py = 48;
            gc.setFill(Color.rgb(0, 0, 0, 0.55));
            gc.fillRoundRect(px, py, 218, 178, 10, 10);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            gc.fillText(spielerName, px + 12, py + 18);
            gc.fillText(
                "Runde:   " + (runde == 0 ? "-" : runde),
                px + 12,
                py + 40
            );
            gc.fillText(
                "Zeit:    " + formatZeit(aktuelleZeitMs),
                px + 12,
                py + 62
            );
            gc.fillText(
                "Beste:   " +
                    (besteRundeMs < 0 ? "--:--.---" : formatZeit(besteRundeMs)),
                px + 12,
                py + 84
            );
            gc.fillText("Crashes: " + kollisionen, px + 12, py + 106);
            gc.fillText(
                "Score:   " + (letzterScore == 0 ? "-" : letzterScore),
                px + 12,
                py + 128
            );
            gc.fillText("Nitro:   " + nitroStatus, px + 12, py + 150);

            double nitroBalken =
                Math.max(0, Math.min(1, nitroFortschritt)) * 194.0;
            gc.setFill(Color.web("#ffffff", 0.25));
            gc.fillRoundRect(px + 12, py + 156, 194, 8, 4, 4);
            gc.setFill(
                nitroStatus.equals("BOOST") ? Color.CYAN : Color.web("#4aa3ff")
            );
            gc.fillRoundRect(px + 12, py + 156, nitroBalken, 8, 4, 4);

            // checkpoint fortschrittsbalken
            gc.setFill(Color.web("#ffffff", 0.25));
            gc.fillRoundRect(px + 12, py + 164, 194, 8, 4, 4);
            if (totalCheckpoints > 0) {
                double balken =
                    (194.0 * checkpointFortschritt) / totalCheckpoints;
                gc.setFill(
                    checkpointFortschritt == totalCheckpoints
                        ? Color.GREEN
                        : Color.YELLOW
                );
                gc.fillRoundRect(px + 12, py + 164, balken, 8, 4, 4);
            }
        });
    }

    public void countdownZeichnen(String text) {
        Platform.runLater(() -> {
            gc.save();
            gc.setFill(Color.rgb(0, 0, 0, 0.28));
            gc.fillRect(0, 0, BREITE, HOEHE);

            gc.setFill(Color.WHITE);
            gc.setFont(
                Font.font(
                    "Monospace",
                    FontWeight.EXTRA_BOLD,
                    text.equals("GO") ? 86 : 110
                )
            );
            double textBreite = gc.getFont().getSize() * text.length() * 0.62;
            gc.fillText(
                text,
                BREITE / 2.0 - textBreite / 2.0,
                HOEHE / 2.0 + 34
            );
            gc.restore();
        });
    }

    // zeichnet eine checkpoint-linie quer über die strecke
    // zielP1/zielP2 = naechster checkpoint fuer den jeweiligen spieler (je eigene farbe), sonst grau
    public void checkpointZeichnen(
        double ax,
        double ay,
        double bx,
        double by,
        boolean zielP1,
        boolean zielP2
    ) {
        Platform.runLater(() -> {
            gc.save();
            if (zielP1 && zielP2) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(3);
            } else if (zielP1) {
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(3);
            } else if (zielP2) {
                gc.setStroke(Color.CYAN);
                gc.setLineWidth(3);
            } else {
                gc.setStroke(Color.web("#ffffff", 0.35));
                gc.setLineWidth(2);
            }
            gc.strokeLine(ax, ay, bx, by);
            gc.restore();
        });
    }

    private String formatZeit(long ms) {
        long minutes = ms / 60000;
        long seconds = (ms % 60000) / 1000;
        long millis = ms % 1000;
        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }

    public void streckeZeichnenBezier(
        int startX,
        int startY,
        int[] xKoord,
        int[] yKoord,
        double[] xC1Koord,
        double[] yC1Koord,
        double[] xC2Koord,
        double[] yC2Koord
    ) {
        Platform.runLater(() -> {
            gc.beginPath();
            gc.setLineWidth(2.5);
            gc.moveTo(startX, startY);
            for (int k = 0; k < xKoord.length; k++) {
                gc.bezierCurveTo(
                    xC1Koord[k],
                    yC1Koord[k] - 100,
                    xC2Koord[k],
                    yC2Koord[k] + 100,
                    xKoord[k],
                    yKoord[k]
                );
            }
            gc.lineTo(startX, startY);
            gc.closePath();
            gc.stroke();
        });
    }
}
