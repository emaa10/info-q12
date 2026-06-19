package racing.model;

import java.util.List;

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
        this.oberflaeche.loesche();
        this.datenbank = new Datenbank();
        this.datenbank.verbinde();
        this.level = new Level(new Map(this.oberflaeche.getMapView()));

        // Spieler mit Auto am ersten Centerline-Punkt (Start-Ziel-Linie) platzieren
        List<int[]> centerline = this.level.gibMap().getCenterline();
        int[] startPt = centerline.get(0);
        int[] nextPt  = centerline.get(1);
        double startAngle = Math.toDegrees(Math.atan2(nextPt[1] - startPt[1], nextPt[0] - startPt[0]));
        Auto auto = new Auto(startPt[0], startPt[1], startAngle);
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

        // Spielschleife
        while (laeuft) {
            // Physik aller Autos aktualisieren
            for (Spieler s : spieler) {
                s.gibAuto().itr();
                s.gibAuto().begrenze(960, 600);
            }

            // Szene neu zeichnen
            this.oberflaeche.loesche();

            // Track neu zeichnen (MapView übernimmt die komplette Streckenlogik)
            this.level.gibMap().draw();

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
