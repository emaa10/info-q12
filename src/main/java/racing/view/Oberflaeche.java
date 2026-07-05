package racing.view;

import java.util.HashSet;
import java.util.Set;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// view-teil im MVC
public class Oberflaeche {

    private static final int BREITE = 960;
    private static final int HOEHE = 600;

    // Die Wurzel ist ein StackPane: es stapelt seine Kinder wie Ebenen
    // uebereinander. Das erste Kind liegt unten, das letzte oben.
    private final StackPane wurzel;
    // Untere Ebene: hier laeuft das Rennen, gezeichnet auf den Canvas.
    private final Pane spielEbene;
    // Obere Ebene: das Menue aus echten JavaFX-Nodes (Schritt 2 fuellt sie).
    private final VBox menueEbene;
    private final Canvas leinwand;
    private final GraphicsContext gc;
    private final Image baumBild;
    private final Image autoBild;
    private final Image grasBild;
    private final Image nitroBild;
    private final MapView mapView;

    private final Set<KeyCode> gedrueckteTasten = new HashSet<>();

    public Oberflaeche() {
        // ----- Spiel-Ebene (unten): der Canvas, auf den das Rennen gemalt wird -----
        this.leinwand = new Canvas(BREITE, HOEHE);
        this.gc = leinwand.getGraphicsContext2D();
        this.spielEbene = new Pane(leinwand);

        // ----- Menue-Ebene (oben): echte JavaFX-Nodes statt Canvas-Text -----
        // VBox(24) = vertikale Anordnung mit 24px Abstand zwischen den Kindern.
        // Pos.CENTER zentriert die Kinder horizontal und vertikal.
        this.menueEbene = new VBox(24);
        this.menueEbene.setAlignment(Pos.CENTER);
        this.menueEbene.setStyle("-fx-background-color: white;");
        baueHauptmenue();

        // ----- Wurzel: beide Ebenen uebereinanderstapeln -----
        // Reihenfolge = Zeichenreihenfolge: spielEbene unten, menueEbene darueber.
        this.wurzel = new StackPane(spielEbene, menueEbene);

        this.baumBild = new Image(
            getClass().getResourceAsStream("/images/tree.png")
        );
        this.autoBild = new Image(
            getClass().getResourceAsStream("/images/auto_m2.png")
        );
        this.grasBild = new Image(
            getClass().getResourceAsStream("/images/grass.jpg")
        );
        this.nitroBild = new Image(
            getClass().getResourceAsStream("/images/nitro.png")
        );
        this.mapView = new MapView(this.gc);
    }

    // Baut die Inhalte der Menue-Ebene: Titel + klickbare Buttons.
    // Statt Text auf den Canvas zu malen, benutzen wir echte JavaFX-Nodes.
    private void baueHauptmenue() {
        Label titel = new Label("RENNSPIEL");
        titel.setFont(Font.font("Monospace", FontWeight.EXTRA_BOLD, 48));

        Button startKnopf = new Button("Spiel starten");
        Button leaderboardKnopf = new Button("Leaderboard");
        Button beendenKnopf = new Button("Beenden");

        // Allen Buttons die gleiche Breite geben, damit sie buendig sind.
        for (Button b : new Button[] { startKnopf, leaderboardKnopf, beendenKnopf }) {
            b.setPrefWidth(240);
            b.setFont(Font.font("Monospace", FontWeight.BOLD, 18));
        }

        // setOnAction() feuert bei Maus-Klick UND bei Enter/Space, wenn der Button
        // den Fokus hat. Das erledigt JavaFX automatisch - kein Tasten-Polling noetig.
        startKnopf.setOnAction(e -> System.out.println("Start geklickt (Schritt 3 startet das Rennen)"));
        leaderboardKnopf.setOnAction(e -> System.out.println("Leaderboard geklickt (Schritt 4)"));
        beendenKnopf.setOnAction(e -> Platform.exit());

        menueEbene.getChildren().addAll(titel, startKnopf, leaderboardKnopf, beendenKnopf);
    }

    public Parent gibWurzel() {
        return wurzel;
    }

    public MapView getMapView() {
        return this.mapView;
    }

    // Muss nach Scene-Erstellung aufgerufen werden
    public void registriereEingabe(Scene szene) {
        szene.setOnKeyPressed(e -> gedrueckteTasten.add(e.getCode()));
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

    public void autoZeichnen(double x, double y, double winkel) {
        Platform.runLater(() -> {
            gc.save();
            gc.translate(x, y);
            // -90° damit das hochkante Bild (Fahrtrichtung unten) bei winkel=0 nach rechts zeigt
            gc.rotate(winkel - 90);
            double w = 28;
            double h = 56;
            gc.drawImage(autoBild, -w / 2, -h / 2, w, h);
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

    // zeichnet das hud oben rechts
    // wenn aufStrecke false ist, wird eine rote warnung angezeigt
    public void hudZeichnen(
        int runde,
        long aktuelleZeitMs,
        long besteRundeMs,
        boolean aufStrecke,
        int checkpointFortschritt,
        int totalCheckpoints,
        int kollisionen,
        int letzterScore,
        String nitroStatus,
        double nitroFortschritt
    ) {
        Platform.runLater(() -> {
            // warnung wenn man von der strecke fährt
            if (!aufStrecke) {
                gc.setFill(Color.rgb(200, 0, 0, 0.85));
                gc.fillRoundRect(BREITE / 2.0 - 160, 12, 320, 34, 8, 8);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
                gc.fillText(
                    "Nicht mehr auf der Strecke!",
                    BREITE / 2.0 - 140,
                    35
                );
            }

            // hud panel
            double px = BREITE - 230;
            double py = 12;
            gc.setFill(Color.rgb(0, 0, 0, 0.55));
            gc.fillRoundRect(px, py, 218, 158, 10, 10);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            gc.fillText(
                "Runde:   " + (runde == 0 ? "-" : runde),
                px + 12,
                py + 22
            );
            gc.fillText(
                "Zeit:    " + formatZeit(aktuelleZeitMs),
                px + 12,
                py + 44
            );
            gc.fillText(
                "Beste:   " +
                    (besteRundeMs < 0 ? "--:--.---" : formatZeit(besteRundeMs)),
                px + 12,
                py + 66
            );
            gc.fillText("Crashes: " + kollisionen, px + 12, py + 88);
            gc.fillText(
                "Score:   " + (letzterScore == 0 ? "-" : letzterScore),
                px + 12,
                py + 110
            );
            gc.fillText("Nitro:   " + nitroStatus, px + 12, py + 132);

            double nitroBalken =
                Math.max(0, Math.min(1, nitroFortschritt)) * 194.0;
            gc.setFill(Color.web("#ffffff", 0.25));
            gc.fillRoundRect(px + 12, py + 138, 194, 8, 4, 4);
            gc.setFill(
                nitroStatus.equals("BOOST") ? Color.CYAN : Color.web("#4aa3ff")
            );
            gc.fillRoundRect(px + 12, py + 138, nitroBalken, 8, 4, 4);

            // checkpoint fortschrittsbalken
            gc.setFill(Color.web("#ffffff", 0.25));
            gc.fillRoundRect(px + 12, py + 146, 194, 8, 4, 4);
            if (totalCheckpoints > 0) {
                double balken =
                    (194.0 * checkpointFortschritt) / totalCheckpoints;
                gc.setFill(
                    checkpointFortschritt == totalCheckpoints
                        ? Color.GREEN
                        : Color.YELLOW
                );
                gc.fillRoundRect(px + 12, py + 146, balken, 8, 4, 4);
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
    // aktiv = nächster checkpoint (gelb), sonst grau
    public void checkpointZeichnen(
        double ax,
        double ay,
        double bx,
        double by,
        boolean aktiv
    ) {
        Platform.runLater(() -> {
            gc.save();
            if (aktiv) {
                gc.setStroke(Color.YELLOW);
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
