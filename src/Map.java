// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)

import java.util.Date;

public class Map {

    private int[][] matrix;
    private int seed;

    // With seed as a parameter:
    public Map(int seed) {
        this.matrix = new int[64][64];
        this.seed = seed;

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

        System.out.println("Generating new Map with seed: " + this.seed);
    }
}
