package racing.model;

import racing.datastructure.Knoten;
import racing.datastructure.Listenelement;
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
        this.datenbank.verbinde();
        this.level = new Level(new Map());

        // Spieler mit Auto an der Start-Ziel-Linie platzieren
        Point sfp = this.level.gibMap().getStartFinishPoint();
        double startX = 14 * sfp.getX() + 32;
        double startY = 7  * sfp.getY() + 44;
        Auto auto = new Auto(startX, startY, 0);
        this.spieler = new Spieler[] { new Spieler("Spieler 1", auto) };
    }

    @Override
    public void run() {
        laeuft = true;
        spieleKreis();
    }

    // game loop ihr deutschen
    public void spieleKreis() {
        level.platziereGegenstand(new Baum(), 200, 200);
        level.platziereGegenstand(new Baum(), 400, 300);
        level.platziereGegenstand(new Baum(), 600, 150);

        // Streckendaten einmalig berechnen
        int[] xKoord    = new int[this.level.gibMap().getPoints().length];
        int[] yKoord    = new int[this.level.gibMap().getPoints().length];
        double[] xC1Koord = new double[this.level.gibMap().getPoints().length];
        double[] yC1Koord = new double[this.level.gibMap().getPoints().length];
        double[] xC2Koord = new double[this.level.gibMap().getPoints().length];
        double[] yC2Koord = new double[this.level.gibMap().getPoints().length];

        Point l = this.level.gibMap().getStartFinishPoint();
        int k = 0;

        for (Point p : this.level.gibMap().getPoints()) {
            xKoord[k] = 14 * p.getX() + 32;
            yKoord[k] = 7  * p.getY() + 44;

            double dx, dy, xc1, yc1, xc2, yc2;

            if (k == 0) {
                dx = (14 * p.getX() + 32) - (14 * this.level.gibMap().getStartFinishPoint().getX() + 32);
                dy = (7  * p.getY() + 44) - (7  * this.level.gibMap().getStartFinishPoint().getY() + 44);
            } else if (k == this.level.gibMap().getPoints().length - 1) {
                dx = (14 * this.level.gibMap().getStartFinishPoint().getX() + 32) - (14 * p.getX() + 32);
                dy = (7  * this.level.gibMap().getStartFinishPoint().getY() + 44) - (7  * p.getY() + 44);
            } else {
                dx = (14 * p.getX() + 32) - (14 * l.getX() + 32);
                dy = (7  * p.getY() + 44) - (7  * l.getY() + 44);
            }

            xc1 = (14 * l.getX() + 32) + dx * 0.33;
            yc1 = (7  * l.getY() + 44) + dy * 0.33;
            xc2 = (14 * p.getX() + 32) - dx * 0.33;
            yc2 = (7  * p.getY() + 44) - dy * 0.33;

            xC1Koord[k] = xc1;
            yC1Koord[k] = yc1;
            xC2Koord[k] = xc2;
            yC2Koord[k] = yc2;
            k++;
            l = p;
        }

        int sfX = 14 * this.level.gibMap().getStartFinishPoint().getX() + 32;
        int sfY = 7  * this.level.gibMap().getStartFinishPoint().getY() + 44;

        // Spielschleife
        while (laeuft) {
            // Physik aller Autos aktualisieren
            for (Spieler s : spieler) {
                s.gibAuto().itr();
                s.gibAuto().begrenze(960, 600);
            }

            // Szene neu zeichnen
            this.oberflaeche.loesche();

            this.oberflaeche.startEndPunktZeichnen(sfX, sfY);

            this.oberflaeche.streckeZeichnenBezier(
                sfX, sfY,
                xKoord, yKoord,
                xC1Koord, yC1Koord,
                xC2Koord, yC2Koord
            );

            for (Point p : this.level.gibMap().getPoints()) {
                this.oberflaeche.punktZeichnen(14 * p.getX() + 32, 7 * p.getY() + 44);
            }

            Listenelement el = level.gibGegenstaende().gibAnfang();
            while (!el.istAbschluss()) {
                Gegenstand g = (Gegenstand) ((Knoten) el).gebeDaten();
                int[] pos = g.gebePosition();
                this.oberflaeche.baumZeichnen(pos[0], pos[1]);
                el = ((Knoten) el).gebeNachfolger();
            }

            // Autos zeichnen (über alles andere)
            for (Spieler s : spieler) {
                Auto a = s.gibAuto();
                this.oberflaeche.autoZeichnen(a.gibX(), a.gibY(), a.gibWinkelDouble());
            }

            try {
                Thread.sleep(16); // ca. 60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                laeuft = false;
            }
        }
    }

    public void stoppe() {
        laeuft = false;
        datenbank.trenneVerbindung();
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
