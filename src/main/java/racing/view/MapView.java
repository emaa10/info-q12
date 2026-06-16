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
            int radius = width / 2;

            double[] normalX = new double[n];
            double[] normalY = new double[n];
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
                normalX[i] = -ty;
                normalY[i] = tx;
            }

            boolean[] corners = detectCorners(centerline, n);
            for (int i = 0; i < n; i++) {
                if (!corners[i]) continue;
                double cx = centerline.get(i)[0];
                double cy = centerline.get(i)[1];
                int prev = (i - 1 + n) % n;
                int next = (i + 1) % n;
                double tx = centerline.get(next)[0] - centerline.get(prev)[0];
                double ty = centerline.get(next)[1] - centerline.get(prev)[1];
                placeKerbTile(
                    cx + normalX[i] * radius,
                    cy + normalY[i] * radius,
                    tx,
                    ty,
                    -8
                );
                placeKerbTile(
                    cx - normalX[i] * radius,
                    cy - normalY[i] * radius,
                    tx,
                    ty,
                    0
                );
            }

            gc.setFill(Color.web("#bab6a8"));
            for (int[] point : centerline) {
                gc.fillOval(
                    point[0] - radius,
                    point[1] - radius,
                    radius * 2,
                    radius * 2
                );
            }

            placeStartFinishLine(
                centerline.get(0)[0],
                centerline.get(0)[1],
                normalX[0],
                normalY[0],
                width
            );
        });
    }

    private boolean[] detectCorners(List<int[]> centerline, int n) {
        boolean[] sharp = new boolean[n];
        for (int i = 0; i < n; i++) {
            int prev = (i - 1 + n) % n;
            int next = (i + 1) % n;
            double t1x = centerline.get(i)[0] - centerline.get(prev)[0];
            double t1y = centerline.get(i)[1] - centerline.get(prev)[1];
            double t2x = centerline.get(next)[0] - centerline.get(i)[0];
            double t2y = centerline.get(next)[1] - centerline.get(i)[1];
            double l1 = Math.sqrt(t1x * t1x + t1y * t1y);
            double l2 = Math.sqrt(t2x * t2x + t2y * t2y);
            if (l1 > 0) {
                t1x /= l1;
                t1y /= l1;
            }
            if (l2 > 0) {
                t2x /= l2;
                t2y /= l2;
            }
            double dot = Math.max(-1.0, Math.min(1.0, t1x * t2x + t1y * t2y));
            sharp[i] = Math.acos(dot) > 0.08;
        }
        boolean[] result = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (sharp[i]) {
                for (int d = -5; d <= 5; d++) result[(i + d + n) % n] = true;
            }
        }
        return result;
    }

    private void placeKerbTile(
        double ex,
        double ey,
        double tx,
        double ty,
        int yOffset
    ) {
        if (tx == 0 && ty == 0) return;
        gc.save();
        gc.translate(ex, ey);
        gc.rotate(Math.toDegrees(Math.atan2(ty, tx)));
        gc.drawImage(kerbImg, -6, yOffset, 12, 8);
        gc.restore();
    }

    private void placeStartFinishLine(
        double cx,
        double cy,
        double nx,
        double ny,
        int width
    ) {
        double startX = cx - nx * (width / 2.0);
        double startY = cy - ny * (width / 2.0);
        double angle = Math.toDegrees(Math.atan2(ny, nx));
        int numTiles = (int) Math.ceil(width / 8.0);
        for (int t = 0; t < numTiles; t++) {
            gc.save();
            gc.translate(startX + nx * 8 * t, startY + ny * 8 * t);
            gc.rotate(angle);
            gc.drawImage(gridImg, 0, -4, 8, 8);
            gc.restore();
        }
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
