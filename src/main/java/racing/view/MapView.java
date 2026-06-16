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
            System.out.println("Error: xArr.length != yArr.length!");
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

    public void drawLines(String c, int[] xArr, int[] yArr) {
        if (xArr.length != yArr.length) {
            System.out.println("Error: xArr.length != yArr.length!");
            return;
        }

        for (int k = 0; k < xArr.length - 1; k++) {
            drawLine(c, xArr[k], yArr[k], xArr[k + 1], yArr[k + 1]);
            if (k == xArr.length - 2) {
                drawLine(c, xArr[k + 1], yArr[k + 1], xArr[0], yArr[0]);
            }
        }
    }
}
