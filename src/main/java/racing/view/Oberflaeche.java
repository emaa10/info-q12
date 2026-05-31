package racing.view;

import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

// view-teil im MVC
public class Oberflaeche {

    private static final int BREITE = 960;
    private static final int HOEHE = 600;

    private final Pane wurzel;
    private final Canvas leinwand;
    private final GraphicsContext gc;

    public Oberflaeche() {
        this.leinwand = new Canvas(BREITE, HOEHE);
        this.gc = leinwand.getGraphicsContext2D();
        this.wurzel = new Pane(leinwand);
        this.wurzel.setStyle("-fx-background-color: white;");
    }

    public Parent gibWurzel() {
        return wurzel;
    }

    public void registriereEingabe() {
        // fehlt noch listener und so
    }

    // wird dann vom spiel aufgerufen zum aktualisieren
    public void aktualisiere() {
        // todo zeichnen
    }

    public void punktZeichnen(int x, int y) {
        Platform.runLater(() -> {
            gc.clearRect(0, 0, BREITE, HOEHE);

            // 1. fahrbahn-markierung (zuerst -> ganz hinten)
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(2);
            gc.strokeLine(BREITE / 2.0, 0, BREITE / 2.0, HOEHE);

            // 2. hindernisse (graue blöcke)
            gc.setFill(Color.DARKGRAY);
            gc.fillRect(300, 150, 40, 40);
            gc.fillRect(600, 400, 40, 40);

            // 3. spieler (roter punkt, zuletzt -> ganz vorne)
            gc.setFill(Color.RED);
            gc.fillOval(x - 6, y - 6, 12, 12);

            // 4. text-overlay
            gc.setFill(Color.BLACK);
            gc.fillText("Punkt", x - 15, y - 12);
        });
    }
}
