package racing.model;

import javafx.scene.image.Image;

public class Baum extends Gegenstand {

    public static final int BREITE = 100;
    public static final int HOEHE = 100;

    private static final Image BILD = new Image(
        Baum.class.getResourceAsStream("/images/tree.png")
    );

    // Hitbox für Kollisionserkennung, muss noch angepasst werden, wenn Elias die Assets fertig hat
    private final int hitboxOffsetX = 20;
    private final int hitboxOffsetY = 50;
    private final int hitboxBreite = 60;
    private final int hitboxHoehe = 45;

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
