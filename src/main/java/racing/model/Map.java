// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)
// See also:
// https://en.wikipedia.org/wiki/Random_number_generation
// https://en.wikipedia.org/wiki/Linear_congruential_generator
// https://bitesofcode.wordpress.com/2020/04/09/procedural-racetrack-generation/

package racing.model;

import java.util.Date;

public class Map {

    private racing.view.MapView view;
    private int[][] matrix;
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

    // With seed as a parameter:
    public Map(int seed, racing.view.MapView mapView) {
        this.matrix = new int[64][64];
        this.seed = seed;
        this.modulus = (1 << 16) + 1;
        this.multiplier = 75;
        this.increment = 0;
        this.numberOfPoints = 8;
        this.pointCoordinates = new int[2 * this.numberOfPoints];
        this.points = new Point[this.numberOfPoints];
        this.startFinishCoordinates = new int[2];
        this.view = mapView;

        lcg(
            this.seed,
            this.modulus,
            this.multiplier,
            this.increment,
            this.pointCoordinates,
            2 * this.numberOfPoints
        );
        mod65Arr(pointCoordinates);

        for (int k = 0, idx = 0; k < pointCoordinates.length; k += 2, idx++) {
            this.points[idx] = new Point(
                pointCoordinates[k],
                pointCoordinates[k + 1]
            );
        }

        lcg(
            this.seed,
            this.modulus,
            this.multiplier,
            this.increment,
            this.startFinishCoordinates,
            2
        );
        mod65Arr(this.startFinishCoordinates);
        this.startFinishPoint = new Point(
            this.startFinishCoordinates[0],
            this.startFinishCoordinates[1]
        );
    }

    // Without seed as a parameter:
    public Map(racing.view.MapView mapView) {
        // This will be in int range and is good until 18 Jan 2038.
        // Date.getTime() returns a long by default: until 18th of January 2038, dividing by 1000 is
        // enough to cast this into an int.
        int i = (int) (new Date().getTime() / 1000);

        this.matrix = new int[64][64];
        this.seed = i;
        this.modulus = (1 << 16) + 1;
        this.multiplier = 75;
        this.increment = 0;
        this.numberOfPoints = 8;
        this.pointCoordinates = new int[2 * this.numberOfPoints];
        this.points = new Point[this.numberOfPoints];
        this.startFinishCoordinates = new int[2];
        this.view = mapView;

        lcg(
            this.seed,
            this.modulus,
            this.multiplier,
            this.increment,
            this.pointCoordinates,
            2 * this.numberOfPoints
        );
        mod65Arr(pointCoordinates);

        for (int k = 0, idx = 0; k < pointCoordinates.length; k += 2, idx++) {
            this.points[idx] = new Point(
                pointCoordinates[k],
                pointCoordinates[k + 1]
            );
        }

        lcg(
            this.seed,
            this.modulus,
            this.multiplier,
            this.increment,
            this.startFinishCoordinates,
            2
        );
        mod65Arr(this.startFinishCoordinates);
        this.startFinishPoint = new Point(
            this.startFinishCoordinates[0],
            this.startFinishCoordinates[1]
        );

        int[] xKoord;
        int[] yKoord;
        xKoord = new int[points.length];
        yKoord = new int[points.length];
        int k;
        k = 0;

        for (Point p : this.points) {
            xKoord[k] = 14 * p.getX() + 32;
            yKoord[k] = 7 * p.getY() + 44;
            k++;
        }
        this.view.streckeZeichnen(
            14 * this.startFinishPoint.getX() + 32,
            7 * this.startFinishPoint.getY() + 44,
            xKoord,
            yKoord
        );

        this.view.startEndPunktZeichnen(
            14 * this.startFinishPoint.getX() + 32,
            7 * this.startFinishPoint.getY() + 44
        );
        /*
        int[] xKoord;
        int[] yKoord;
        double[] xC1Koord;
        double[] yC1Koord;
        double[] xC2Koord;
        double[] yC2Koord;
        Point l;
        int k;
        k = 0;
        l = this.level.gibMap().getStartFinishPoint();

        for (Point p : this.level.gibMap().getPoints()) {
            this.oberflaeche
                .getMapView()
                .punktZeichnen(14 * p.getX() + 32, 7 * p.getY() + 44);
            xKoord[k] = 14 * p.getX() + 32;
            yKoord[k] = 7 * p.getY() + 44;

            double dx;
            double dy;
            double xc1;
            double yc1;
            double xc2;
            double yc2;

            if (k == 0) {
                dx =
                    (14 * p.getX() + 32) -
                    (14 * this.level.gibMap().getStartFinishPoint().getX() +
                        32);
                dy =
                    (7 * p.getY() + 44) -
                    (7 * this.level.gibMap().getStartFinishPoint().getY() + 44);

                xc1 = (14 * l.getX() + 32) + dx * 0.33;
                yc1 = (7 * l.getY() + 44) + dy * 0.33;

                xc2 = (14 * p.getX() + 32) - dx * 0.33;
                yc2 = (7 * p.getY() + 44) - dy * 0.33;
            } else if (k == this.level.gibMap().getPoints().length - 1) {
                dx =
                    (14 * this.level.gibMap().getStartFinishPoint().getX() +
                        32) - (14 * p.getX() + 32);
                dy =
                    (7 * this.level.gibMap().getStartFinishPoint().getY() +
                        44) - (7 * p.getY() + 44);

                xc1 = (14 * l.getX() + 32) + dx * 0.33;
                yc1 = (7 * l.getY() + 44) + dy * 0.33;

                xc2 = (14 * p.getX() + 32) - dx * 0.33;
                yc2 = (7 * p.getY() + 44) - dy * 0.33;
            } else {
                dx = (14 * p.getX() + 32) - (14 * l.getX() + 32);
                dy = (7 * p.getY() + 44) - (7 * l.getY() + 44);

                xc1 = (14 * l.getX() + 32) + dx * 0.33;
                yc1 = (7 * l.getY() + 44) + dy * 0.33;

                xc2 = (14 * p.getX() + 32) - dx * 0.33;
                yc2 = (7 * p.getY() + 44) - dy * 0.33;
            }
            xC1Koord[k] = xc1;
            yC1Koord[k] = yc1;
            xC2Koord[k] = xc2;
            yC2Koord[k] = yc2;
            k++;
            l = p;
        }

        this.oberflaeche
            .getMapView()
            .streckeZeichnenBezier(
                14 * this.level.gibMap().getStartFinishPoint().getX() + 32,
                7 * this.level.gibMap().getStartFinishPoint().getY() + 44,
                xKoord,
                yKoord,
                xC1Koord,
                yC1Koord,
                xC2Koord,
                yC2Koord
            );
        // Note:
        // Scale:
        // x_max = 960; x_rand,max = 64 => Faktor 14 (x_s,min = 0, x_s,max = 896)
        // y_max = 600; y_rand,max = 64 => Faktor 8 (y_s,min = 0, y_s,max = 512)
        // Transform:
        // x + 32 (x_st,min = 32, x_st,max = 928) -> genau um 1/2 * 64 = 32 = Abstanbd zum rechten Rand
        // y + 44 (y_st, min = 44, y_st,max = 556) -> vgl. oben, nur eben in vertikaler Richtung
        // Damit die Punkte nicht in irgendeinem Eck vergammeln
        // */
    }

    // "A linear congruential generator (LCG) is an algorithm that yields a sequence of pseudo-randomized
    // numbers calculated with a discontinuous piecewise linear equation. The method represents one of the
    // oldest and best-known pseudorandom number generator algorithms. The theory behind them is relatively
    // easy to understand, and they are easily implemented and fast, especially on computer hardware which
    // can provide modular arithmetic by storage-bit truncation."
    // (https://en.wikipedia.org/wiki/Linear_congruential_generator, 30th of May 2026, 10.31 CEST)
    // lcg(seed, modulus m, multiplier a, increment c)
    public void lcg(
        int seed,
        int m,
        int a,
        int c,
        int[] randomNumbers,
        int numberOfRandomNumbers
    ) {
        randomNumbers[0] = seed;

        for (int i = 1; i < numberOfRandomNumbers; i++) {
            randomNumbers[i] = ((randomNumbers[i - 1] * a) + c) % m;
        }
    }

    // Utility method.
    private void mod65Arr(int[] arr) {
        for (int k = 0; k < arr.length; k++) {
            arr[k] = arr[k] % 65;
        }
    }
}
