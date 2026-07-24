package racing.model;

public class Nitro extends Gegenstand {

    private static final double SKALIERUNG = 1.8;

    public static final int BREITE = (int) Math.round(36 * SKALIERUNG);
    public static final int HOEHE = (int) Math.round(36 * SKALIERUNG);

    private static final int HITBOX_RADIUS = (int) Math.round(26 * SKALIERUNG);

    public boolean kollidiertMit(int px, int py) {
        double cx = x + BREITE / 2.0;
        double cy = y + HOEHE / 2.0;
        double dx = px - cx;
        double dy = py - cy;
        return dx * dx + dy * dy <= HITBOX_RADIUS * HITBOX_RADIUS;
    }
}
