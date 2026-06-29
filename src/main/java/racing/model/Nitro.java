package racing.model;

public class Nitro extends Gegenstand {

    public static final int BREITE = 36;
    public static final int HOEHE = 36;

    private static final int HITBOX_RADIUS = 26;

    public boolean kollidiertMit(int px, int py) {
        double cx = x + BREITE / 2.0;
        double cy = y + HOEHE / 2.0;
        double dx = px - cx;
        double dy = py - cy;
        return dx * dx + dy * dy <= HITBOX_RADIUS * HITBOX_RADIUS;
    }
}
