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
        // Das gehört @jakobgraetz, bitte nicht anfassen; es ist heavily in progress.
        // TODO:
        int[] xKoord;
        int[] yKoord;
        double[] xC1Koord;
        double[] yC1Koord;
        double[] xC2Koord;
        double[] yC2Koord;
        Point l;
        int k;

        xKoord = new int[this.level.gibMap().getPoints().length];
        yKoord = new int[this.level.gibMap().getPoints().length];

        xC1Koord = new double[this.level.gibMap().getPoints().length];
        yC1Koord = new double[this.level.gibMap().getPoints().length];
        xC2Koord = new double[this.level.gibMap().getPoints().length];
        yC2Koord = new double[this.level.gibMap().getPoints().length];
        k = 0;
        l = this.level.gibMap().getStartFinishPoint();

        for (Point p : this.level.gibMap().getPoints()) {
            this.oberflaeche.punktZeichnen(
                14 * p.getX() + 32,
                7 * p.getY() + 44
            );
            xKoord[k] = 14 * p.getX() + 32;
            yKoord[k] = 7 * p.getY() + 44;

            double dx;
            double dy;
            double xc1;
            double yc1;
            double xc2;
            double yc2;

            if (k == 0) {
                dx =
                    (14 * p.getX() + 32) -
                    (14 * this.level.gibMap().getStartFinishPoint().getX() +
                        32);
                dy =
                    (7 * p.getY() + 44) -
                    (7 * this.level.gibMap().getStartFinishPoint().getY() + 44);

                xc1 = (14 * l.getX() + 32) + dx * 0.33;
                yc1 = (7 * l.getY() + 44) + dy * 0.33;

                xc2 = (14 * p.getX() + 32) - dx * 0.33;
                yc2 = (7 * p.getY() + 44) - dy * 0.33;
            } else if (k == this.level.gibMap().getPoints().length - 1) {
                dx =
                    (14 * this.level.gibMap().getStartFinishPoint().getX() +
                        32) - (14 * p.getX() + 32);
                dy =
                    (7 * this.level.gibMap().getStartFinishPoint().getY() +
                        44) - (7 * p.getY() + 44);

                xc1 = (14 * l.getX() + 32) + dx * 0.33;
                yc1 = (7 * l.getY() + 44) + dy * 0.33;

                xc2 = (14 * p.getX() + 32) - dx * 0.33;
                yc2 = (7 * p.getY() + 44) - dy * 0.33;
            } else {
                dx = (14 * p.getX() + 32) - (14 * l.getX() + 32);
                dy = (7 * p.getY() + 44) - (7 * l.getY() + 44);

                xc1 = (14 * l.getX() + 32) + dx * 0.33;
                yc1 = (7 * l.getY() + 44) + dy * 0.33;

                xc2 = (14 * p.getX() + 32) - dx * 0.33;
                yc2 = (7 * p.getY() + 44) - dy * 0.33;
            }
            xC1Koord[k] = xc1;
            yC1Koord[k] = yc1;
            xC2Koord[k] = xc2;
            yC2Koord[k] = yc2;
            k++;
            l = p;
        }
        this.oberflaeche.startEndPunktZeichnen(
            14 * this.level.gibMap().getStartFinishPoint().getX() + 32,
            7 * this.level.gibMap().getStartFinishPoint().getY() + 44
        );
        //this.oberflaeche.streckeZeichnen(
        //    14 * this.level.gibMap().getStartFinishPoint().getX() + 32,
        //    7 * this.level.gibMap().getStartFinishPoint().getY() + 44,
        //    xKoord,
        //    yKoord
        //);
        this.oberflaeche.streckeZeichnenBezier(
            14 * this.level.gibMap().getStartFinishPoint().getX() + 32,
            7 * this.level.gibMap().getStartFinishPoint().getY() + 44,
            xKoord,
            yKoord,
            xC1Koord,
            yC1Koord,
            xC2Koord,
            yC2Koord
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
