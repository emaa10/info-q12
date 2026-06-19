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

    // Lap-Timer & Checkpoints
    private double[] startLinieA;
    private double[] startLinieB;
    private double[] startLinieTangent;
    private double[][] checkpoints;
    private int  naechsterCheckpoint = 0;
    private long lapStartZeit = -1;
    private int  lapZaehler   = 0;
    private long besteRunde   = -1;
    private int  kreuzungsCooldown = 300; // ~5s Startcooldown
    private boolean aufStrecke = true;

    private static final int  CHECKPOINT_ANZAHL = 8;
    private static final double STRECKEN_TOLERANZ = 38.0; // px vom Mittelpunkt

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

        Map map = this.level.gibMap();
        this.startLinieA       = map.getStartLinieA();
        this.startLinieB       = map.getStartLinieB();
        this.startLinieTangent = map.getStartLinieTangent();
        this.checkpoints       = map.getCheckpoints(CHECKPOINT_ANZAHL);
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

            // Off-Track und Checkpoint-Logik
            for (Spieler s : spieler) {
                Auto a = s.gibAuto();
                aufStrecke = level.gibMap().istNahAnStrecke(a.gibX(), a.gibY(), STRECKEN_TOLERANZ);

                // Nächsten Checkpoint prüfen
                if (naechsterCheckpoint < checkpoints.length) {
                    double[] cp = checkpoints[naechsterCheckpoint];
                    if (a.prüfeLapCrossing(cp[0], cp[1], cp[2], cp[3], cp[4], cp[5])) {
                        naechsterCheckpoint++;
                    }
                }
            }

            // Startlinie prüfen
            if (kreuzungsCooldown > 0) {
                kreuzungsCooldown--;
            } else {
                for (Spieler s : spieler) {
                    Auto a = s.gibAuto();
                    if (a.prüfeLapCrossing(
                            startLinieA[0], startLinieA[1],
                            startLinieB[0], startLinieB[1],
                            startLinieTangent[0], startLinieTangent[1])) {
                        long jetzt = System.currentTimeMillis();
                        if (lapStartZeit < 0) {
                            // Erste Überquerung: Timer starten
                            lapStartZeit = jetzt;
                        } else if (naechsterCheckpoint == checkpoints.length) {
                            // Gültige Runde: alle Checkpoints passiert
                            long lapZeit = jetzt - lapStartZeit;
                            lapZaehler++;
                            if (besteRunde < 0 || lapZeit < besteRunde) besteRunde = lapZeit;
                            lapStartZeit = jetzt;
                        }
                        // Checkpoints immer zurücksetzen (auch bei ungültiger Runde)
                        naechsterCheckpoint = 0;
                        kreuzungsCooldown = 180;
                    }
                }
            }

            // Szene neu zeichnen
            this.oberflaeche.loesche();

            // Track neu zeichnen (MapView übernimmt die komplette Streckenlogik)
            this.level.gibMap().draw();

            // Checkpoints zeichnen (bereits passierte ausblenden)
            for (int i = naechsterCheckpoint; i < checkpoints.length; i++) {
                double[] cp = checkpoints[i];
                this.oberflaeche.checkpointZeichnen(cp[0], cp[1], cp[2], cp[3], i == naechsterCheckpoint);
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

            // HUD zeichnen
            long aktuelleZeit = lapStartZeit < 0 ? 0 : System.currentTimeMillis() - lapStartZeit;
            this.oberflaeche.hudZeichnen(lapZaehler, aktuelleZeit, besteRunde,
                    aufStrecke, naechsterCheckpoint, checkpoints.length);

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
