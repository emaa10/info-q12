package racing.model;

import javafx.scene.image.Image;

public class Baum extends Gegenstand {

    private static final double SKALIERUNG = 1.8;

    public static final int BREITE = (int) Math.round(100 * SKALIERUNG);
    public static final int HOEHE = (int) Math.round(100 * SKALIERUNG);

    private static final Image BILD = new Image(
        Baum.class.getResourceAsStream("/images/tree.png")
    );

    // Hitbox für Kollisionserkennung, muss noch angepasst werden, wenn Elias die Assets fertig hat
    private final int hitboxOffsetX = (int) Math.round(20 * SKALIERUNG);
    private final int hitboxOffsetY = (int) Math.round(50 * SKALIERUNG);
    private final int hitboxBreite = (int) Math.round(60 * SKALIERUNG);
    private final int hitboxHoehe = (int) Math.round(45 * SKALIERUNG);

    public Image gibBild() {
        return BILD;
    }

    // Gibt [x, y, breite, hoehe] der Hitbox zurück, hier aufrufen für Auto (pls use)
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
