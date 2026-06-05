package racing.model;

import racing.view.Oberflaeche;

// model-teil
public class Spiel implements Runnable {

    private Spieler[] spieler;
    private Level level;
    private Datenbank datenbank;
    private Oberflaeche oberflaeche;

    private volatile boolean laeuft;

    public Spiel(Oberflaeche oberflaeche) {
        this.oberflaeche = oberflaeche;
        this.datenbank = new Datenbank();
        this.spieler = new Spieler[0];
        this.level = new Level(new Map());
        // level wird später z. B. mit einer Map erzeugt
    }

    @Override
    public void run() {
        laeuft = true;
        spieleKreis();
    }

    // game loop ihr deutschen
    public void spieleKreis() {
        this.oberflaeche.loesche();

        int[] xKoord;
        int[] yKoord;
        int k;

        xKoord = new int[this.level.gibMap().getPoints().length];
        yKoord = new int[this.level.gibMap().getPoints().length];
        k = 0;

        for (Point p : this.level.gibMap().getPoints()) {
            this.oberflaeche.punktZeichnen(
                14 * p.getX() + 32,
                7 * p.getY() + 44
            );
            xKoord[k] = 14 * p.getX() + 32;
            yKoord[k] = 7 * p.getY() + 44;
            k++;
        }
        this.oberflaeche.startEndPunktZeichnen(
            14 * this.level.gibMap().getStartFinishPoint().getX() + 32,
            7 * this.level.gibMap().getStartFinishPoint().getY() + 44
        );
        this.oberflaeche.streckeZeichnen(
            14 * this.level.gibMap().getStartFinishPoint().getX() + 32,
            7 * this.level.gibMap().getStartFinishPoint().getY() + 44,
            xKoord,
            yKoord
        );
        // Note:
        // Scale:
        // x_max = 960; x_rand,max = 64 => Faktor 14 (x_s,min = 0, x_s,max = 896)
        // y_max = 600; y_rand,max = 64 => Faktor 8 (y_s,min = 0, y_s,max = 512)
        // Transform:
        // x + 32 (x_st,min = 32, x_st,max = 928) -> genau um 1/2 * 64 = 32 = Abstanbd zum rechten Rand
        // y + 44 (y_st, min = 44, y_st,max = 556) -> vgl. oben, nur eben in vertikaler Richtung
        // Damit die Punkte nicht in irgendeinem Eck vergammeln

        while (laeuft) {
            try {
                Thread.sleep(16); // ca. 60 Bilder pro Sekunde
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                laeuft = false;
            }
        }
    }

    public void stoppe() {
        laeuft = false;
    }

    public Spieler[] gibSpieler() {
        return spieler;
    }

    public Level gibLevel() {
        return level;
    }

    public Datenbank gibDatenbank() {
        return datenbank;
    }
}
