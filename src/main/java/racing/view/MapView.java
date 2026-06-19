package racing.view;

import java.util.List;
import javafx.application.Platform;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class MapView {

    private static final int CANVAS_WIDTH = 960;
    private static final int CANVAS_HEIGHT = 600;

    private static final int BORDER_WIDTH = 8;
    private static final Color TRACK_SURFACE = Color.web("#c4c0b0");
    private static final Color TRACK_BORDER = Color.web("#8a8070");
    private static final Color GRASS_BASE = Color.web("#4a8c4a");
    private static final Color GRASS_STRIPE = Color.web("#5a9a5a");
    private static final Color KERB_RED = Color.web("#cc2200");

    private final GraphicsContext gc;

    public MapView(GraphicsContext gc) {
        this.gc = gc;
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

            double[] leftX = new double[n + 1];
            double[] leftY = new double[n + 1];
            double[] rightX = new double[n + 1];
            double[] rightY = new double[n + 1];
            for (int i = 0; i < n; i++) {
                leftX[i] = centerline.get(i)[0] + normalX[i] * radius;
                leftY[i] = centerline.get(i)[1] + normalY[i] * radius;
                rightX[i] = centerline.get(i)[0] - normalX[i] * radius;
                rightY[i] = centerline.get(i)[1] - normalY[i] * radius;
            }
            leftX[n] = leftX[0];
            leftY[n] = leftY[0];
            rightX[n] = rightX[0];
            rightY[n] = rightY[0];

            double[] trackPolyX = new double[2 * n];
            double[] trackPolyY = new double[2 * n];
            double[] borderPolyX = new double[2 * n];
            double[] borderPolyY = new double[2 * n];
            for (int i = 0; i < n; i++) {
                trackPolyX[i] = leftX[i];
                trackPolyY[i] = leftY[i];
                trackPolyX[n + i] = rightX[n - 1 - i];
                trackPolyY[n + i] = rightY[n - 1 - i];
                int ri = n - 1 - i;
                borderPolyX[i] =
                    centerline.get(i)[0] + normalX[i] * (radius + BORDER_WIDTH);
                borderPolyY[i] =
                    centerline.get(i)[1] + normalY[i] * (radius + BORDER_WIDTH);
                borderPolyX[n + i] =
                    centerline.get(ri)[0] -
                    normalX[ri] * (radius + BORDER_WIDTH);
                borderPolyY[n + i] =
                    centerline.get(ri)[1] -
                    normalY[ri] * (radius + BORDER_WIDTH);
            }

            // Per-point curvature (used for inner-corner gap patching and S/F placement)
            double[] angles = new double[n];
            for (int i = 0; i < n; i++) {
                int prev = (i - 10 + n) % n,
                    next = (i + 10) % n;
                double t1x = centerline.get(i)[0] - centerline.get(prev)[0];
                double t1y = centerline.get(i)[1] - centerline.get(prev)[1];
                double t2x = centerline.get(next)[0] - centerline.get(i)[0];
                double t2y = centerline.get(next)[1] - centerline.get(i)[1];
                double l1 = Math.sqrt(t1x * t1x + t1y * t1y),
                    l2 = Math.sqrt(t2x * t2x + t2y * t2y);
                if (l1 > 0) {
                    t1x /= l1;
                    t1y /= l1;
                }
                if (l2 > 0) {
                    t2x /= l2;
                    t2y /= l2;
                }
                angles[i] = Math.acos(
                    Math.max(-1.0, Math.min(1.0, t1x * t2x + t1y * t2y))
                );
            }

            // 1. Textured grass
            gc.setFill(GRASS_BASE);
            gc.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
            gc.setGlobalAlpha(0.13);
            gc.setFill(GRASS_STRIPE);
            for (int y = 0; y < CANVAS_HEIGHT; y += 5) gc.fillRect(
                0,
                y,
                CANVAS_WIDTH,
                2
            );
            gc.setGlobalAlpha(1.0);

            // 2. Drop shadow
            gc.save();
            gc.setGlobalAlpha(0.22);
            gc.setFill(Color.BLACK);
            gc.translate(4, 5);
            gc.fillPolygon(borderPolyX, borderPolyY, 2 * n);
            gc.restore();

            // 3. Border
            gc.setFill(TRACK_BORDER);
            gc.fillPolygon(borderPolyX, borderPolyY, 2 * n);
            // Patch inner-corner gaps in the border polygon
            gc.setFill(TRACK_BORDER);
            for (int i = 0; i < n; i++) {
                if (angles[i] > 0.4) {
                    double px = centerline.get(i)[0],
                        py = centerline.get(i)[1];
                    double r2 = radius + BORDER_WIDTH;
                    gc.fillOval(px - r2, py - r2, 2 * r2, 2 * r2);
                }
            }

            // 4. Track surface
            gc.setFill(TRACK_SURFACE);
            gc.fillPolygon(trackPolyX, trackPolyY, 2 * n);
            // Seal the polygon closing-edge seam at index 0 (fillPolygon can leave a gap there)
            gc.setStroke(TRACK_SURFACE);
            gc.setLineWidth(6.0);
            gc.strokeLine(leftX[0], leftY[0], rightX[0], rightY[0]);
            // Patch inner-corner gaps in the track surface polygon
            gc.setFill(TRACK_SURFACE);
            for (int i = 0; i < n; i++) {
                if (angles[i] > 0.4) {
                    double px = centerline.get(i)[0],
                        py = centerline.get(i)[1];
                    gc.fillOval(
                        px - radius,
                        py - radius,
                        2 * radius,
                        2 * radius
                    );
                }
            }

            double straightThreshold = 0.25;
            int bestStart = 0,
                bestLen = 0,
                curStart = 0,
                curLen = 0;
            for (int i = 0; i < 2 * n; i++) {
                if (angles[i % n] < straightThreshold) {
                    if (curLen == 0) curStart = i;
                    if (++curLen > bestLen && curLen <= n) {
                        bestLen = curLen;
                        bestStart = curStart;
                    }
                } else {
                    curLen = 0;
                }
            }
            int sfIndex = bestLen > 10 ? (bestStart + bestLen / 2) % n : 0;

            boolean[] corners = detectCorners(centerline, n);
            for (int d = -20; d <= 20; d++) corners[(d + sfIndex + n) % n] =
                false;
            for (int i = 0; i < n; i++) {
                int next = (i + 1) % n;
                if (corners[i]) {
                    gc.setLineWidth(3.5);
                    gc.setStroke((i / 4) % 2 == 0 ? KERB_RED : Color.WHITE);
                } else {
                    gc.setLineWidth(2.0);
                    gc.setStroke(Color.WHITE);
                }
                gc.strokeLine(leftX[i], leftY[i], leftX[next], leftY[next]);
                gc.strokeLine(rightX[i], rightY[i], rightX[next], rightY[next]);
            }

            int step = Math.max(1, n / 200);
            int cCount = n / step;
            double[] cxArr = new double[cCount + 1];
            double[] cyArr = new double[cCount + 1];
            for (int i = 0; i < cCount; i++) {
                cxArr[i] = centerline.get(i * step)[0];
                cyArr[i] = centerline.get(i * step)[1];
            }
            cxArr[cCount] = cxArr[0];
            cyArr[cCount] = cyArr[0];
            gc.save();
            gc.setStroke(Color.WHITE);
            gc.setLineDashes(8, 12);
            gc.setLineWidth(1.5);
            gc.strokePolyline(cxArr, cyArr, cCount + 1);
            gc.restore();

            // 8. Start/finish checkerboard at the straightest point on the track
            placeStartFinishLine(
                centerline.get(sfIndex)[0],
                centerline.get(sfIndex)[1],
                normalX[sfIndex],
                normalY[sfIndex],
                width
            );
        });
    }

    private void placeStartFinishLine(
        double cx,
        double cy,
        double nx,
        double ny,
        int width
    ) {
        int tileSize = 10;
        int cols = (int) Math.ceil(width / (double) tileSize);
        int rows = 3;
        double half = (rows * tileSize) / 2.0;
        double startX = cx - nx * (width / 2.0);
        double startY = cy - ny * (width / 2.0);
        gc.save();
        gc.translate(startX, startY);
        gc.rotate(Math.toDegrees(Math.atan2(ny, nx)));
        gc.setFill(TRACK_SURFACE);
        gc.fillRect(0, -half, cols * tileSize, rows * tileSize);
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                gc.setFill((col + row) % 2 == 0 ? Color.BLACK : Color.WHITE);
                gc.fillRect(
                    col * tileSize,
                    (row - rows / 2.0) * tileSize,
                    tileSize,
                    tileSize
                );
            }
        }
        gc.setFill(Color.WHITE);
        gc.fillRect(0, -half - 2, cols * tileSize, 2);
        gc.fillRect(0, half, cols * tileSize, 2);
        gc.restore();
    }

    private boolean[] detectCorners(List<int[]> centerline, int n) {
        int span = 10;
        boolean[] sharp = new boolean[n];
        for (int i = 0; i < n; i++) {
            int prev = (i - span + n) % n;
            int next = (i + span) % n;
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
            sharp[i] = Math.acos(dot) > 0.70;
        }
        boolean[] result = new boolean[n];
        for (int i = 0; i < n; i++) {
            if (sharp[i]) {
                for (int d = -8; d <= 8; d++) result[(i + d + n) % n] = true;
            }
        }
        return result;
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
        for (int k = 0; k < xArr.length; k++) drawPoint(c, xArr[k], yArr[k], r);
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
            if (k == xArr.length - 2) drawLine(
                c,
                xArr[k + 1],
                yArr[k + 1],
                xArr[0],
                yArr[0]
            );
        }
    }
}
