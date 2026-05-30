// Map.java
// (c) 2026 - Jakob Grätz (@jakobgraetz)

public class Map {

    private int[][] matrix;

    public Map(int seed) {
        this.matrix = new int[64][64];
        System.out.println("Generating new Map with seed: " + seed);
    }
}
