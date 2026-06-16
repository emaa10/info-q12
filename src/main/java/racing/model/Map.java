// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)
// See also:
// https://en.wikipedia.org/wiki/Random_number_generation
// https://en.wikipedia.org/wiki/Linear_congruential_generator
// https://bitesofcode.wordpress.com/2020/04/09/procedural-racetrack-generation/

package racing.model;

import java.util.ArrayList;
import java.util.List;
import racing.model.util.LCG;
import racing.model.util.geometry.ConvexHull;
import racing.model.util.geometry.Point;

public class Map {

    private static final int TRACK_WIDTH = 60;

    private racing.view.MapView view;
    private int seed;
    private int modulus;
    private int multiplier;
    private int increment;
    private int numberOfPoints;
    private int[] pointCoordinates;
    private Point[] points;
    // Die Ziellinie in der Formel 1 wird offiziell als Start-Ziel-Linie bezeichnet.
    // Sie dient als Startpunkt des Rennens sowie als Zeitmesslinie für die Rundenzeiten
    // und markiert das Ende jeder gefahrenen Runde und des gesamten Rennens. (vgl. Wikipedia)
    private Point startFinishPoint;
    private int[] startFinishCoordinates;
    private Point[] convexHull;
    private Point[] trackPoints;
    private List<int[]> centerline;
    private LCG lcg;
    private double difficulty;

    // With seed as a parameter:
    public Map(int seed, racing.view.MapView mapView) {
        this.numberOfPoints = 8;
        this.pointCoordinates = new int[2 * this.numberOfPoints];
        this.points = new Point[this.numberOfPoints];
        this.startFinishCoordinates = new int[2];
        this.view = mapView;

        mod65Arr(pointCoordinates);

        for (int k = 0, idx = 0; k < pointCoordinates.length; k += 2, idx++) {
            this.points[idx] = new Point(
                14 * pointCoordinates[k] + 32,
                7 * pointCoordinates[k + 1] + 44
            );
        }

        mod65Arr(this.startFinishCoordinates);
        this.startFinishPoint = new Point(
            14 * this.startFinishCoordinates[0] + 32,
            7 * this.startFinishCoordinates[1] + 44
        );
    }

    // Without seed as a parameter:
    public Map(racing.view.MapView mapView) {
        this.numberOfPoints = 8;
        this.pointCoordinates = new int[2 * this.numberOfPoints];
        this.points = new Point[this.numberOfPoints];
        this.startFinishCoordinates = new int[2];
        this.view = mapView;
        this.difficulty = 0.1;
        this.lcg = new LCG();

        lcg.randomNumbers(this.pointCoordinates, 2 * this.numberOfPoints);
        mod65Arr(pointCoordinates);

        for (int k = 0, idx = 0; k < pointCoordinates.length; k += 2, idx++) {
            this.points[idx] = new Point(
                14 * pointCoordinates[k] + 32,
                7 * pointCoordinates[k + 1] + 44
            );
        }

        lcg.randomNumbers(this.startFinishCoordinates, 2);
        mod65Arr(this.startFinishCoordinates);
        this.startFinishPoint = new Point(
            14 * this.startFinishCoordinates[0] + 32,
            7 * this.startFinishCoordinates[1] + 44
        );

        this.convexHull = new ConvexHull().jarvis(this.points);

        Point[] midpoints = computeRandomlyDisplacedMidpoints();

        this.trackPoints = new Point[this.convexHull.length * 2];
        for (int k = 0; k < this.convexHull.length; k++) {
            this.trackPoints[2 * k] = this.convexHull[k];
            this.trackPoints[2 * k + 1] = midpoints[k];
        }

        this.centerline = smoothTrack();
        this.view.drawTrack(this.centerline, TRACK_WIDTH);
    }

    public List<int[]> smoothTrack() {
        int n = this.trackPoints.length + 1;
        double[] x = new double[n];
        double[] y = new double[n];

        for (int k = 0; k < this.trackPoints.length; k++) {
            x[k] = this.trackPoints[k].getX();
            y[k] = this.trackPoints[k].getY();
        }

        x[this.trackPoints.length] = x[0];
        y[this.trackPoints.length] = y[0];

        double[] u = new double[n];
        u[0] = 0;

        for (int i = 1; i < n; i++) {
            double dx = x[i] - x[i - 1];
            double dy = y[i] - y[i - 1];
            u[i] = u[i - 1] + Math.sqrt(dx * dx + dy * dy);
        }

        for (int i = 0; i < n; i++) {
            u[i] /= u[n - 1];
        }

        double[] mx = periodicSplineSecondDerivatives(x, u);
        double[] my = periodicSplineSecondDerivatives(y, u);

        int N = 200;
        List<int[]> result = new ArrayList<>();

        for (int k = 0; k < N; k++) {
            double t = (double) k / N;
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

    private void mod65Arr(int[] arr) {
        for (int k = 0; k < arr.length; k++) {
            arr[k] = arr[k] % 65;
        }
    }

    private void x05Arr(int[] arr) {
        for (int k = 0; k < arr.length; k++) {
            arr[k] = (int) Math.floor(arr[k] * 0.5);
        }
    }

    private Point[] computeRandomlyDisplacedMidpoints() {
        Point[] midpoints = new Point[this.convexHull.length];
        int[] dx = new int[this.convexHull.length];
        int[] dy = new int[this.convexHull.length];

        this.lcg.randomNumbers(dx, this.convexHull.length);
        this.lcg.randomNumbers(dy, this.convexHull.length);
        mod65Arr(dx);
        mod65Arr(dy);
        x05Arr(dx);
        x05Arr(dy);

        for (int k = 0; k < this.convexHull.length; k++) {
            int next = (k + 1) % this.convexHull.length;

            midpoints[k] = new Point(
                (this.convexHull[k].getX() +
                    dx[k] +
                    this.convexHull[next].getX() +
                    dx[next]) / 2,
                (this.convexHull[k].getY() +
                    dy[k] +
                    this.convexHull[next].getY() +
                    dy[next]) / 2
            );
        }

        return midpoints;
    }

    public List<int[]> getCenterline() {
        return centerline;
    }
}
