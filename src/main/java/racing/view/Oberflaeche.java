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
