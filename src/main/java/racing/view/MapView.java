package racing.view;

import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class MapView {

    private final GraphicsContext gc;
    private Image gridImg;
    private Image kerbImg;

    public MapView(GraphicsContext gc) {
        this.gc = gc;
        this.gridImg = new Image(
            getClass().getResourceAsStream("/images/grid.png")
        );
        this.kerbImg = new Image(
            getClass().getResourceAsStream("/images/kerb.png")
        );
    }

    public void drawPoint(String c, int x, int y, int r) {
        Platform.runLater(() -> {
            gc.setFill(Color.web(c, 1.0));
            gc.fillOval(x - r, y - r, 2 * r, 2 * r);
        });
    }

    public void drawPoints(String c, int[] xArr, int[] yArr, int r) {
        if (xArr.length != yArr.length) {
            System.out.println("Error: xArr.length != yArr.length");
            return;
        }

        for (int k = 0; k < xArr.length; k++) {
            drawPoint(c, xArr[k], yArr[k], r);
        }
    }

    public void drawLine(String c, int x1, int y1, int x2, int y2) {
        Platform.runLater(() -> {
            gc.setStroke(Color.web(c, 1.0));
            gc.beginPath();
            gc.setLineWidth(2.5);
            gc.moveTo(x1, y1);
            gc.lineTo(x2, y2);
            gc.stroke();
            gc.closePath();
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
            gc.closePath();
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
