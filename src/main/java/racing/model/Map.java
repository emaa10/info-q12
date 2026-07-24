// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)
// See also:
// https://en.wikipedia.org/wiki/Random_number_generation
// https://bitesofcode.wordpress.com/2020/04/09/procedural-racetrack-generation/
// https://github.com/juangallostra/procedural-tracks/blob/master/main.py

package racing.model;

import java.util.ArrayList;
import java.util.List;
import racing.model.util.LCG;
import racing.model.util.geometry.ConvexHull;
import racing.model.util.geometry.Point;
import racing.view.MapView;

public class Map {

    private static final double SKALIERUNG = 1.8;
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int MARGIN = (int) Math.round(100 * SKALIERUNG);
    private static final int MIN_POINTS = 12;
    private static final int MAX_POINTS = 16;
    private static final int MIN_DISTANCE = (int) Math.round(55 * SKALIERUNG);
    private static final int MAX_DISPLACEMENT = (int) Math.round(65 * SKALIERUNG);
    private static final double DIFFICULTY = 0.1;
    private static final int DISTANCE_BETWEEN_POINTS = (int) Math.round(70 * SKALIERUNG);
    private static final double MAX_ANGLE = 80.0;
    private static final int SPLINE_POINTS = 1000;
    static final int TRACK_WIDTH = (int) Math.round(60 * SKALIERUNG);

    // 2^16 + 1 = 65537
    private static final int LCG_MODULUS = (1 << 16) + 1;

    private racing.view.MapView view;
    private Point[] trackPoints;
    private List<int[]> centerline;
    private int startFinishIndex;
    private boolean[] onTrackGrid;
    private int[] rngPool;
    private int rngIdx;
    private int seed;

    public Map(racing.view.MapView mapView, int seed) {
        this.view = mapView;
        this.seed = seed;
        LCG lcg = new LCG(seed);
        this.rngPool = new int[6000];
        lcg.randomNumbers(this.rngPool, 6000);
        this.rngIdx = 1;

        int attempts = 0;
        this.centerline = null;
        while (attempts < 30) {
            Point[] pts = randomPoints();
            Point[] hull = new ConvexHull().jarvis(pts);
            this.trackPoints = shapeTrack(hull);
            attempts++;
            if (trackPointsIntersect(this.trackPoints)) continue;
            List<int[]> candidate = smoothTrack(this.trackPoints);
            if (!splineIntersects(candidate) && !splineTooNarrow(candidate)) {
                this.centerline = candidate;
                break;
            }
        }
        if (this.centerline == null) this.centerline = smoothTrack(
            this.trackPoints
        );
        this.startFinishIndex = MapView.findStartFinishIndex(this.centerline);
        precomputeOnTrackGrid();
        this.view.drawTrack(
            this.centerline,
            TRACK_WIDTH,
            this.startFinishIndex
        );
    }

    private double nextDouble() {
        if (rngIdx >= rngPool.length) rngIdx = 1;
        return (double) rngPool[rngIdx++] / LCG_MODULUS;
    }

    private int nextInt(int n) {
        return (int) (nextDouble() * n);
    }

    private double nextGaussian() {
        double u1 = Math.max(1e-10, nextDouble());
        double u2 = nextDouble();
        return Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
    }

    private Point[] randomPoints() {
        int count = MIN_POINTS + nextInt(MAX_POINTS - MIN_POINTS + 1);
        List<Point> pts = new ArrayList<>();
        int attempts = 0;
        while (pts.size() < count && attempts < 10000) {
            attempts++;
            int x = MARGIN + nextInt(WIDTH - 2 * MARGIN + 1);
            int y = MARGIN + nextInt(HEIGHT - 2 * MARGIN + 1);
            boolean tooClose = false;
            for (Point p : pts) {
                double dx = p.getX() - x;
                double dy = p.getY() - y;
                if (Math.sqrt(dx * dx + dy * dy) < MIN_DISTANCE) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) pts.add(new Point(x, y));
        }
        return pts.toArray(new Point[0]);
    }

    private Point[] shapeTrack(Point[] hull) {
        int n = hull.length;
        Point[] track = new Point[n * 2];
        for (int i = 0; i < n; i++) {
            double displacement =
                Math.pow(nextDouble(), DIFFICULTY) * MAX_DISPLACEMENT;
            double[] disp = randUnitVector(displacement);
            int mx = (int) ((hull[i].getX() + hull[(i + 1) % n].getX()) / 2.0 +
                disp[0]);
            int my = (int) ((hull[i].getY() + hull[(i + 1) % n].getY()) / 2.0 +
                disp[1]);
            track[i * 2] = new Point(hull[i].getX(), hull[i].getY());
            track[i * 2 + 1] = new Point(mx, my);
        }
        for (int iter = 0; iter < 5; iter++) {
            fixAngles(track);
            pushPointsApart(track);
        }
        for (Point p : track) {
            p.setX(Math.max(MARGIN, Math.min(WIDTH - MARGIN, p.getX())));
            p.setY(Math.max(MARGIN, Math.min(HEIGHT - MARGIN, p.getY())));
        }
        return track;
    }

    private double[] randUnitVector(double magnitude) {
        double x = nextGaussian();
        double y = nextGaussian();
        double mag = Math.sqrt(x * x + y * y);
        if (mag == 0) mag = 1;
        return new double[] { (magnitude * x) / mag, (magnitude * y) / mag };
    }

    private void fixAngles(Point[] pts) {
        int n = pts.length;
        for (int i = 0; i < n; i++) {
            int prev = (i - 1 + n) % n;
            int next = (i + 1) % n;
            double px = pts[i].getX() - pts[prev].getX();
            double py = pts[i].getY() - pts[prev].getY();
            double pl = Math.sqrt(px * px + py * py);
            if (pl > 0) {
                px /= pl;
                py /= pl;
            }
            double nx = -(pts[i].getX() - pts[next].getX());
            double ny = -(pts[i].getY() - pts[next].getY());
            double nl = Math.sqrt(nx * nx + ny * ny);
            if (nl > 0) {
                nx /= nl;
                ny /= nl;
            }
            double a = Math.atan2(px * ny - py * nx, px * nx + py * ny);
            if (Math.abs(Math.toDegrees(a)) <= MAX_ANGLE) continue;
            double diff = Math.toRadians(MAX_ANGLE * Math.signum(a)) - a;
            double c = Math.cos(diff);
            double s = Math.sin(diff);
            double newX = (nx * c - ny * s) * nl;
            double newY = (nx * s + ny * c) * nl;
            pts[next].setX((int) (pts[i].getX() + newX));
            pts[next].setY((int) (pts[i].getY() + newY));
        }
    }

    private void pushPointsApart(Point[] pts) {
        int n = pts.length;
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                double dx = pts[j].getX() - pts[i].getX();
                double dy = pts[j].getY() - pts[i].getY();
                double dl = Math.sqrt(dx * dx + dy * dy);
                if (dl >= DISTANCE_BETWEEN_POINTS) continue;
                if (dl == 0) dl = 1;
                dx /= dl;
                dy /= dl;
                double dif = DISTANCE_BETWEEN_POINTS - dl;
                dx *= dif;
                dy *= dif;
                pts[j].setX((int) (pts[j].getX() + dx));
                pts[j].setY((int) (pts[j].getY() + dy));
                pts[i].setX((int) (pts[i].getX() - dx));
                pts[i].setY((int) (pts[i].getY() - dy));
            }
        }
    }

    public List<int[]> smoothTrack(Point[] pts) {
        int n = pts.length + 1;
        double[] x = new double[n];
        double[] y = new double[n];
        for (int k = 0; k < pts.length; k++) {
            x[k] = pts[k].getX();
            y[k] = pts[k].getY();
        }
        x[pts.length] = x[0];
        y[pts.length] = y[0];

        double[] u = new double[n];
        for (int i = 1; i < n; i++) {
            double dx = x[i] - x[i - 1];
            double dy = y[i] - y[i - 1];
            u[i] = u[i - 1] + Math.sqrt(dx * dx + dy * dy);
        }
        for (int i = 0; i < n; i++) u[i] /= u[n - 1];

        double[] mx = periodicSplineSecondDerivatives(x, u);
        double[] my = periodicSplineSecondDerivatives(y, u);

        List<int[]> result = new ArrayList<>();
        for (int k = 0; k < SPLINE_POINTS; k++) {
            double t = (double) k / SPLINE_POINTS;
            int px = Math.max(
                MARGIN,
                Math.min(WIDTH - MARGIN - 1, (int) splineEval(t, x, u, mx))
            );
            int py = Math.max(
                MARGIN,
                Math.min(HEIGHT - MARGIN - 1, (int) splineEval(t, y, u, my))
            );
            result.add(new int[] { px, py });
        }
        return result;
    }

    private void precomputeOnTrackGrid() {
        onTrackGrid = new boolean[WIDTH * HEIGHT];
        int halfWidth = TRACK_WIDTH / 2;
        int hwSq = halfWidth * halfWidth;
        for (int[] pt : centerline) {
            for (int dy = -halfWidth; dy <= halfWidth; dy++) {
                int py = pt[1] + dy;
                if (py < 0 || py >= HEIGHT) continue;
                for (int dx = -halfWidth; dx <= halfWidth; dx++) {
                    if (dx * dx + dy * dy > hwSq) continue;
                    int px = pt[0] + dx;
                    if (px < 0 || px >= WIDTH) continue;
                    onTrackGrid[py * WIDTH + px] = true;
                }
            }
        }
    }

    public boolean isOnTrack(int x, int y) {
        if (x < 0 || x >= WIDTH || y < 0 || y >= HEIGHT) return false;
        return onTrackGrid[y * WIDTH + x];
    }

    static double[] periodicSplineSecondDerivatives(double[] x, double[] u) {
        int n = x.length;

        double[] h = new double[n];
        for (int i = 0; i < n - 1; i++) {
            h[i] = u[i + 1] - u[i];
        }
        h[n - 1] = u[0] + 1 - u[n - 1];

        double[] alpha = new double[n];
        for (int i = 1; i < n - 1; i++) {
            alpha[i] =
                (3 / h[i]) * (x[i + 1] - x[i]) -
                (3 / h[i - 1]) * (x[i] - x[i - 1]);
        }

        alpha[0] =
            (3 / h[0]) * (x[1] - x[0]) - (3 / h[n - 1]) * (x[0] - x[n - 1]);

        alpha[n - 1] = alpha[0];

        double[] l = new double[n];
        double[] mu = new double[n];
        double[] z = new double[n];
        double[] c = new double[n];

        l[0] = 1;
        mu[0] = 0;
        z[0] = 0;

        for (int i = 1; i < n - 1; i++) {
            l[i] = 2 * (u[i + 1] - u[i - 1]) - h[i - 1] * mu[i - 1];
            mu[i] = h[i] / l[i];
            z[i] = (alpha[i] - h[i - 1] * z[i - 1]) / l[i];
        }

        l[n - 1] = 1;

        for (int j = n - 2; j >= 0; j--) {
            c[j] = z[j] - mu[j] * c[j + 1];
        }

        return c;
    }

    static double splineEval(double t, double[] x, double[] u, double[] M) {
        int n = x.length;

        int i = n - 2;
        for (int j = 0; j < n - 1; j++) {
            if (t >= u[j] && t < u[j + 1]) {
                i = j;
                break;
            }
        }

        double h = u[i + 1] - u[i];
        double a = (u[i + 1] - t) / h;
        double b = (t - u[i]) / h;

        return (
            a * x[i] +
            b * x[i + 1] +
            (((a * a * a - a) * M[i] + (b * b * b - b) * M[i + 1]) * (h * h)) /
                6.0
        );
    }

    private boolean splineTooNarrow(List<int[]> line) {
        int step = 5;
        int m = line.size() / step;
        int skip = 15;
        int thresh = (int) (TRACK_WIDTH * 1.5);
        int threshSq = thresh * thresh;
        for (int i = 0; i < m; i++) {
            int[] a = line.get(i * step);
            for (int j = i + skip; j < m - skip; j++) {
                int[] b = line.get(j * step);
                int dx = a[0] - b[0];
                int dy = a[1] - b[1];
                if (dx * dx + dy * dy < threshSq) return true;
            }
        }
        return false;
    }

    private boolean splineIntersects(List<int[]> line) {
        int step = 5;
        int m = line.size() / step;
        for (int i = 0; i < m; i++) {
            int[] a1 = line.get(i * step);
            int[] a2 = line.get(((i + 1) % m) * step);
            for (int j = i + 2; j < m; j++) {
                if (i == 0 && j == m - 1) continue;
                int[] b1 = line.get(j * step);
                int[] b2 = line.get(((j + 1) % m) * step);
                if (
                    segmentsIntersect(
                        a1[0],
                        a1[1],
                        a2[0],
                        a2[1],
                        b1[0],
                        b1[1],
                        b2[0],
                        b2[1]
                    )
                ) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean trackPointsIntersect(Point[] pts) {
        int n = pts.length;
        for (int i = 0; i < n; i++) {
            int ax1 = pts[i].getX(),
                ay1 = pts[i].getY();
            int ax2 = pts[(i + 1) % n].getX(),
                ay2 = pts[(i + 1) % n].getY();
            for (int j = i + 2; j < n; j++) {
                if (i == 0 && j == n - 1) continue;
                int bx1 = pts[j].getX(),
                    by1 = pts[j].getY();
                int bx2 = pts[(j + 1) % n].getX(),
                    by2 = pts[(j + 1) % n].getY();
                if (segmentsIntersect(ax1, ay1, ax2, ay2, bx1, by1, bx2, by2)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean segmentsIntersect(
        int ax1,
        int ay1,
        int ax2,
        int ay2,
        int bx1,
        int by1,
        int bx2,
        int by2
    ) {
        long d1 = cross(bx2 - bx1, by2 - by1, ax1 - bx1, ay1 - by1);
        long d2 = cross(bx2 - bx1, by2 - by1, ax2 - bx1, ay2 - by1);
        long d3 = cross(ax2 - ax1, ay2 - ay1, bx1 - ax1, by1 - ay1);
        long d4 = cross(ax2 - ax1, ay2 - ay1, bx2 - ax1, by2 - ay1);
        return (
            ((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))
        );
    }

    private long cross(long dx, long dy, long px, long py) {
        return dx * py - dy * px;
    }

    public int getSeed() {
        return seed;
    }

    public List<int[]> getCenterline() {
        return centerline;
    }

    // fahrbahn-halbbreite inkl. border (30 asphalt + 8 border), passt zur sichtbaren strecke
    private static final double FAHRBAHN_RAND = TRACK_WIDTH / 2.0 + 8.0 * SKALIERUNG;

    public double distanceToTrack(double x, double y) {
        // abstand zum naechsten SEGMENT (nicht nur punkt), passt zum gezeichneten band
        double minDist = Double.MAX_VALUE;
        int n = centerline.size();
        for (int i = 0; i < n; i++) {
            int[] a = centerline.get(i);
            int[] b = centerline.get((i + 1) % n);
            double d = distanzPunktZuSegment(x, y, a[0], a[1], b[0], b[1]);
            if (d < minDist) minDist = d;
        }
        return Math.max(0.0, minDist - FAHRBAHN_RAND);
    }

    // kuerzester abstand von punkt (px,py) zur strecke a-b
    private double distanzPunktZuSegment(double px, double py, double ax, double ay, double bx, double by) {
        double dx = bx - ax, dy = by - ay;
        double laengeSq = dx * dx + dy * dy;
        double t = laengeSq == 0 ? 0 : ((px - ax) * dx + (py - ay) * dy) / laengeSq;
        t = Math.max(0, Math.min(1, t));
        double projX = ax + t * dx, projY = ay + t * dy;
        double ex = px - projX, ey = py - projY;
        return Math.sqrt(ex * ex + ey * ey);
    }

    public void draw() {
        this.view.drawTrack(
            this.centerline,
            TRACK_WIDTH,
            this.startFinishIndex
        );
    }

    private double[] getTangentAt(int idx) {
        int n = centerline.size();
        int prev = (idx - 1 + n) % n;
        int next = (idx + 1) % n;
        double tx = centerline.get(next)[0] - centerline.get(prev)[0];
        double ty = centerline.get(next)[1] - centerline.get(prev)[1];
        double len = Math.sqrt(tx * tx + ty * ty);
        if (len > 0) {
            tx /= len;
            ty /= len;
        }
        return new double[] { tx, ty };
    }

    public double[] getStartLinieTangent() {
        return getTangentAt(startFinishIndex);
    }

    public double[] getStartPunkt() {
        int[] c = centerline.get(startFinishIndex);
        return new double[] { c[0], c[1] };
    }

    // linker endpunkt der startlinie
    public double[] getStartLinieA() {
        double[] t = getStartLinieTangent();
        int[] c = centerline.get(startFinishIndex);
        // normal ist senkrecht zur tangente
        return new double[] {
            c[0] - (-t[1] * TRACK_WIDTH) / 2.0,
            c[1] - (t[0] * TRACK_WIDTH) / 2.0,
        };
    }

    // rechter endpunkt der startlinie
    public double[] getStartLinieB() {
        double[] t = getStartLinieTangent();
        int[] c = centerline.get(startFinishIndex);
        return new double[] {
            c[0] + (-t[1] * TRACK_WIDTH) / 2.0,
            c[1] + (t[0] * TRACK_WIDTH) / 2.0,
        };
    }

    // gibt checkpoints gleichmäßig verteilt auf der strecke zurück
    // jeder eintrag: ax, ay, bx, by, tangentX, tangentY
    public double[][] getCheckpoints(int anzahl) {
        int n = centerline.size();
        double[][] result = new double[anzahl][];
        for (int i = 0; i < anzahl; i++) {
            int idx = (startFinishIndex + ((i + 1) * n) / (anzahl + 1)) % n;
            double[] t = getTangentAt(idx);
            double tx = t[0];
            double ty = t[1];
            double nx = -ty;
            double ny = tx;
            double cx = centerline.get(idx)[0];
            double cy = centerline.get(idx)[1];
            // etwas breiter als die strecke damit man sie sicher trifft
            double breite = TRACK_WIDTH * 0.75;
            result[i] = new double[] {
                cx - nx * breite,
                cy - ny * breite,
                cx + nx * breite,
                cy + ny * breite,
                tx,
                ty,
            };
        }
        return result;
    }

    // schaut ob der punkt nah genug an der strecke ist
    public boolean istNahAnStrecke(double x, double y, double maxAbstand) {
        for (int[] p : centerline) {
            double dx = p[0] - x;
            double dy = p[1] - y;
            if (Math.sqrt(dx * dx + dy * dy) < maxAbstand) return true;
        }
        return false;
    }
}
