// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)
// See also:
// https://en.wikipedia.org/wiki/Random_number_generation
// https://bitesofcode.wordpress.com/2020/04/09/procedural-racetrack-generation/
// https://github.com/juangallostra/procedural-tracks/blob/master/main.py

package racing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import racing.model.util.geometry.ConvexHull;
import racing.model.util.geometry.Point;

public class Map {

    private static final int WIDTH = 960;
    private static final int HEIGHT = 600;
    private static final int MARGIN = 50;
    private static final int MIN_POINTS = 20;
    private static final int MAX_POINTS = 30;
    private static final int MIN_DISTANCE = 20;
    private static final int MAX_DISPLACEMENT = 80;
    private static final double DIFFICULTY = 0.1;
    private static final int DISTANCE_BETWEEN_POINTS = 20;
    private static final double MAX_ANGLE = 90.0;
    private static final int SPLINE_POINTS = 1000;
    static final int TRACK_WIDTH = 40;

    private racing.view.MapView view;
    private Point[] trackPoints;
    private List<int[]> centerline;
    private Random rng;

    public Map(racing.view.MapView mapView) {
        this.view = mapView;
        this.rng = new Random();

        Point[] pts = randomPoints();
        Point[] hull = new ConvexHull().jarvis(pts);
        this.trackPoints = shapeTrack(hull);
        this.centerline = smoothTrack(this.trackPoints);
        this.view.drawTrack(this.centerline, TRACK_WIDTH);
    }

    private Point[] randomPoints() {
        int count = MIN_POINTS + rng.nextInt(MAX_POINTS - MIN_POINTS + 1);
        List<Point> pts = new ArrayList<>();
        int attempts = 0;
        while (pts.size() < count && attempts < 10000) {
            attempts++;
            int x = MARGIN + rng.nextInt(WIDTH - 2 * MARGIN + 1);
            int y = MARGIN + rng.nextInt(HEIGHT - 2 * MARGIN + 1);
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
                Math.pow(rng.nextDouble(), DIFFICULTY) * MAX_DISPLACEMENT;
            double[] disp = randUnitVector(displacement);
            int mx = (int) ((hull[i].getX() + hull[(i + 1) % n].getX()) / 2.0 +
                disp[0]);
            int my = (int) ((hull[i].getY() + hull[(i + 1) % n].getY()) / 2.0 +
                disp[1]);
            track[i * 2] = new Point(hull[i].getX(), hull[i].getY());
            track[i * 2 + 1] = new Point(mx, my);
        }
        for (int iter = 0; iter < 3; iter++) {
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
        double x = rng.nextGaussian();
        double y = rng.nextGaussian();
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
            result.add(new int[] {
                (int) splineEval(t, x, u, mx),
                (int) splineEval(t, y, u, my),
            });
        }
        return result;
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

    public List<int[]> getCenterline() {
        return centerline;
    }
}
