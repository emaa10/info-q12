package racing.view;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;

// view-teil im MVC
public class Oberflaeche {

    private static final int BREITE = 960;
    private static final int HOEHE = 600;

    private final Pane wurzel;
    private final Canvas leinwand;
    private final GraphicsContext gc;
    private final Image baumBild;

    public Oberflaeche() {
        this.leinwand = new Canvas(BREITE, HOEHE);
        this.gc = leinwand.getGraphicsContext2D();
        this.wurzel = new Pane(leinwand);
        this.wurzel.setStyle("-fx-background-color: white;");
        this.baumBild = new Image(getClass().getResourceAsStream("/images/tree.png"));
    }

    public Parent gibWurzel() {
        return wurzel;
    }

    public void baumZeichnen(int x, int y) {
    Platform.runLater(() -> {
        gc.drawImage(baumBild, x, y, 100, 100);
        });
    }

    public void registriereEingabe() {
        // fehlt noch listener und so
    }

    // wird dann vom spiel aufgerufen zum aktualisieren
    public void aktualisiere() {
        // todo zeichnen
    }

    public void loesche() {
        Platform.runLater(() -> gc.clearRect(0, 0, BREITE, HOEHE));
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
                // bezierCurveTo(double xc1, double yc1, double xc2, double yc2, double x1, double y1)
            }
            gc.lineTo(startX, startY);
            gc.stroke();
        });
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
