// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)
// See also:
// https://en.wikipedia.org/wiki/Random_number_generation
// https://en.wikipedia.org/wiki/Linear_congruential_generator
// https://bitesofcode.wordpress.com/2020/04/09/procedural-racetrack-generation/

package racing.model;

import java.util.Date;

public class Map {

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
    public Map(int seed) {
        this.matrix = new int[64][64];
        this.seed = seed;
        this.modulus = (1 << 16) + 1;
        this.multiplier = 75;
        this.increment = 0;
        this.numberOfPoints = 8;
        this.pointCoordinates = new int[2 * this.numberOfPoints];
        this.points = new Point[this.numberOfPoints];
        this.startFinishCoordinates = new int[2];

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
    public Map() {
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
    public void mod65Arr(int[] arr) {
        for (int k = 0; k < arr.length; k++) {
            arr[k] = arr[k] % 65;
        }
    }

    public Point[] getPoints() {
        return this.points;
    }

    public Point getStartFinishPoint() {
        return this.startFinishPoint;
    }
}
