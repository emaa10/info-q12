// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)
// See also:
// https://en.wikipedia.org/wiki/Random_number_generation
// https://en.wikipedia.org/wiki/Linear_congruential_generator

import java.util.Date;

public class Map {

    private int[][] matrix;
    private int seed;
    private int modulus;
    private int multiplier;
    private int increment;

    // With seed as a parameter:
    public Map(int seed) {
        this.matrix = new int[64][64];
        this.seed = seed;
        this.modulus = (1 << 16) + 1;
        this.multiplier = 75;
        this.increment = 0;

        System.out.println("Generating new Map with seed: " + this.seed);
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

        System.out.println("Generating new Map with seed: " + this.seed);
    }

    // "A linear congruential generator (LCG) is an algorithm that yields a sequence of pseudo-randomized
    // numbers calculated with a discontinuous piecewise linear equation. The method represents one of the
    // oldest and best-known pseudorandom number generator algorithms. The theory behind them is relatively
    // easy to understand, and they are easily implemented and fast, especially on computer hardware which
    // can provide modular arithmetic by storage-bit truncation."
    // (https://en.wikipedia.org/wiki/Linear_congruential_generator, 30th of May 2026, 10.31 CEST)
    // lcm(seed, modulus m, multiplier a, increment c)
    public void lcm(
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
}
