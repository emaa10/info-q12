package racing.model.util;

import java.util.Date;

public class LCG {

    private int seed;
    private int modulus;
    private int multiplier;
    private int increment;

    public LCG(int s, int mod, int mul, int i) {
        this.seed = s;
        this.modulus = mod;
        this.multiplier = mul;
        this.increment = i;
    }

    public LCG(int s) {
        this.seed = s;
        this.modulus = (1 << 16) + 1;
        this.multiplier = 75;
        this.increment = 0;
    }

    public LCG() {
        // This will be in int range and is good until 18 Jan 2038.
        // Date.getTime() returns a long by default: until 18th of January 2038, dividing by 1000 is
        // enough to cast this into an int.
        this.seed = int i = (int) (new Date().getTime() / 1000);
        this.modulus = (1 << 16) + 1;
        this.multiplier = 75;
        this.increment = 0;
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
        randomNumbers[0] = this.seed;

        for (int i = 1; i < numberOfRandomNumbers; i++) {
            randomNumbers[i] = ((randomNumbers[i - 1] * a) + c) % m;
        }
    }

    public void randomNumbers()
}
