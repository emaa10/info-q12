package racing.view;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapView {

    private final GraphicsContext gc;

    public MapView(GraphicsContext gc) {
        this.gc = gc;
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
