package racing.view;

import java.util.List;
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

    public void drawTrack(List<int[]> centerline, int width) {
        Platform.runLater(() -> {
            int n = centerline.size();
            double[] outerX = new double[n];
            double[] outerY = new double[n];
            double[] innerX = new double[n];
            double[] innerY = new double[n];

            for (int i = 0; i < n; i++) {
                int prev = (i - 1 + n) % n;
                int next = (i + 1) % n;
                double tx = centerline.get(next)[0] - centerline.get(prev)[0];
                double ty = centerline.get(next)[1] - centerline.get(prev)[1];
                double len = Math.sqrt(tx * tx + ty * ty);
                if (len > 0) {
                    tx /= len;
                    ty /= len;
                }
                double nx = -ty;
                double ny = tx;
                double half = width / 2.0;
                outerX[i] = centerline.get(i)[0] + nx * half;
                outerY[i] = centerline.get(i)[1] + ny * half;
                innerX[i] = centerline.get(i)[0] - nx * half;
                innerY[i] = centerline.get(i)[1] - ny * half;
            }

            double[] polyX = new double[2 * n];
            double[] polyY = new double[2 * n];
            for (int i = 0; i < n; i++) {
                polyX[i] = outerX[i];
                polyY[i] = outerY[i];
                polyX[n + i] = innerX[n - 1 - i];
                polyY[n + i] = innerY[n - 1 - i];
            }
            gc.setFill(Color.web("#4a4a4a"));
            gc.fillPolygon(polyX, polyY, 2 * n);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(3.0);
            gc.strokePolygon(outerX, outerY, n);
            gc.strokePolygon(innerX, innerY, n);
        });
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
