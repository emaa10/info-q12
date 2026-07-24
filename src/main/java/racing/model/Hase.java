package racing.model;

public class Hase extends Gegenstand {
    private static final double SKALIERUNG = 1.8;

    public static final int BREITE = (int) Math.round(40 * SKALIERUNG);
    public static final int HOEHE = (int) Math.round(35 * SKALIERUNG);
    public static final double GESCHWINDIGKEIT = 2.0 * SKALIERUNG;

    private final int hitboxOffsetX = 0;
    private final int hitboxOffsetY = 0;
    private final int hitboxBreite = BREITE;
    private final int hitboxHoehe = HOEHE;

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
