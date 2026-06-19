package racing.view;

import java.util.HashSet;
import java.util.Set;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

// view-teil im MVC
public class Oberflaeche {

    private static final int BREITE = 960;
    private static final int HOEHE  = 600;

    private final Pane wurzel;
    private final Canvas leinwand;
    private final GraphicsContext gc;
    private final Image baumBild;
    private final Image autoBild;
    private final MapView mapView;

    private final Set<KeyCode> gedrueckteTasten = new HashSet<>();

    public Oberflaeche() {
        this.leinwand = new Canvas(BREITE, HOEHE);
        this.gc       = leinwand.getGraphicsContext2D();
        this.wurzel   = new Pane(leinwand);
        this.wurzel.setStyle("-fx-background-color: white;");
        this.baumBild = new Image(getClass().getResourceAsStream("/images/tree.png"));
        this.autoBild = new Image(getClass().getResourceAsStream("/images/auto_m2.png"));
        this.mapView  = new MapView(this.gc);
    }

    public Parent gibWurzel() {
        return wurzel;
    }

    public MapView getMapView() {
        return this.mapView;
    }

    // Muss nach Scene-Erstellung aufgerufen werden
    public void registriereEingabe(Scene szene) {
        szene.setOnKeyPressed(e  -> gedrueckteTasten.add(e.getCode()));
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
        Platform.runLater(() -> gc.clearRect(0, 0, BREITE, HOEHE));
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
        int startX, int startY,
        int[] xKoord, int[] yKoord
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

    // zeichnet das hud oben rechts mit runde, zeit und bester runde
    // wenn aufStrecke false ist, wird eine rote warnung angezeigt
    public void hudZeichnen(int runde, long aktuelleZeitMs, long besteRundeMs,
                             boolean aufStrecke, int checkpointFortschritt, int totalCheckpoints) {
        Platform.runLater(() -> {
            // warnung wenn man von der strecke fährt
            if (!aufStrecke) {
                gc.setFill(Color.rgb(200, 0, 0, 0.85));
                gc.fillRoundRect(BREITE / 2.0 - 160, 12, 320, 34, 8, 8);
                gc.setFill(Color.WHITE);
                gc.setFont(Font.font("Monospace", FontWeight.BOLD, 14));
                gc.fillText("Nicht mehr auf der Strecke!", BREITE / 2.0 - 140, 35);
            }

            // hud panel
            double px = BREITE - 230;
            double py = 12;
            gc.setFill(Color.rgb(0, 0, 0, 0.55));
            gc.fillRoundRect(px, py, 218, 96, 10, 10);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Monospace", FontWeight.BOLD, 13));
            gc.fillText("Runde:   " + (runde == 0 ? "-" : runde), px + 12, py + 22);
            gc.fillText("Zeit:    " + formatZeit(aktuelleZeitMs), px + 12, py + 44);
            gc.fillText("Beste:   " + (besteRundeMs < 0 ? "--:--.---" : formatZeit(besteRundeMs)), px + 12, py + 66);

            // checkpoint fortschrittsbalken
            gc.setFill(Color.web("#ffffff", 0.25));
            gc.fillRoundRect(px + 12, py + 76, 194, 10, 5, 5);
            if (totalCheckpoints > 0) {
                double balken = 194.0 * checkpointFortschritt / totalCheckpoints;
                gc.setFill(checkpointFortschritt == totalCheckpoints ? Color.GREEN : Color.YELLOW);
                gc.fillRoundRect(px + 12, py + 76, balken, 10, 5, 5);
            }
        });
    }

    // zeichnet eine checkpoint-linie quer über die strecke
    // aktiv = nächster checkpoint (gelb), sonst grau
    public void checkpointZeichnen(double ax, double ay, double bx, double by, boolean aktiv) {
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
        long millis  = ms % 1000;
        return String.format("%d:%02d.%03d", minutes, seconds, millis);
    }

    public void streckeZeichnenBezier(
        int startX, int startY,
        int[] xKoord, int[] yKoord,
        double[] xC1Koord, double[] yC1Koord,
        double[] xC2Koord, double[] yC2Koord
    ) {
        Platform.runLater(() -> {
            gc.beginPath();
            gc.setLineWidth(2.5);
            gc.moveTo(startX, startY);
            for (int k = 0; k < xKoord.length; k++) {
                gc.bezierCurveTo(
                    xC1Koord[k], yC1Koord[k] - 100,
                    xC2Koord[k], yC2Koord[k] + 100,
                    xKoord[k],   yKoord[k]
                );
            }
            gc.lineTo(startX, startY);
            gc.closePath();
            gc.stroke();
        });
    }
}
