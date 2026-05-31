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

    public void loesche() {
        Platform.runLater(() -> gc.clearRect(0, 0, BREITE, HOEHE));
    }

    public void punktZeichnen(int x, int y) {
        Platform.runLater(() -> {
            gc.setFill(Color.BLUE);
            gc.fillOval(x - 5, y - 5, 10, 10);
        });
    }
}
