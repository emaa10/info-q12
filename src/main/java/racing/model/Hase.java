package racing.model;

public class Hase extends Gegenstand {
    public static final int BREITE = 40;
    public static final int HOEHE = 35;
    public static final double GESCHWINDIGKEIT = 2.0;

    private final int hitboxOffsetX = 0;
    private final int hitboxOffsetY = 0;
    private final int hitboxBreite = 40;
    private final int hitboxHoehe = 35;

    public int[] gibHitbox() {
        return new int[] {
            x + hitboxOffsetX,
            y + hitboxOffsetY,
            hitboxBreite,
            hitboxHoehe
        };
    }

    public boolean kollidiertMit(int px, int py) {
        int[] h = gibHitbox();
        return px >= h[0] && px <= h[0] + h[2]
            && py >= h[1] && py <= h[1] + h[3];
    }
}
