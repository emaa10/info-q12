// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)
// See also:
// https://en.wikipedia.org/wiki/Random_number_generation
// https://en.wikipedia.org/wiki/Linear_congruential_generator
// https://bitesofcode.wordpress.com/2020/04/09/procedural-racetrack-generation/

package racing.model;

import racing.model.util.LCG;
import racing.model.util.geometry.ConvexHull;
import racing.model.util.geometry.Point;

public class Map {

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
        LCG lcg = new LCG();
        lcg.randomNumbers(this.pointCoordinates, 2 * this.numberOfPoints);
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
        this.convexHull = new ConvexHull().jarvis(this.points);

        int[] xArr = new int[this.convexHull.length];
        int[] yArr = new int[this.convexHull.length];

        this.trackPoints = new Point[this.convexHull.length];

        for (int k = 0; k < this.convexHull.length; k++) {
            xArr[k] = this.convexHull[k].getX();
            yArr[k] = this.convexHull[k].getY();
            this.trackPoints[k] = this.convexHull[k];
        }
        this.view.drawLines("red", xArr, yArr);
        int[] xArrP = new int[this.points.length];
        int[] yArrP = new int[this.points.length];
        for (int k = 0; k < this.points.length; k++) {
            xArrP[k] = this.points[k].getX();
            yArrP[k] = this.points[k].getY();
            System.out.println(this.points[k].toString());
        }
        this.view.drawPoints("red", xArrP, yArrP, 5);
    }

    // Utility method.
    private void mod65Arr(int[] arr) {
        for (int k = 0; k < arr.length; k++) {
            arr[k] = arr[k] % 65;
        }
    }
}
