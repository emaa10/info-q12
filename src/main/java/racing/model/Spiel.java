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

    private double[] startLinieA;
    private double[] startLinieB;
    private double[] startLinieTangent;
    private double[][] checkpoints;
    private int naechsterCheckpoint = 0;
    private long lapStartZeit = -1;
    private int lapZaehler = 0;
    private long besteRunde = -1;
    private int kreuzungsCooldown = 300;
    private boolean aufStrecke = true;
    private int letzterScore = 0;
    private long countdownStartZeit = -1;

    private static final int CHECKPOINT_ANZAHL = 8;
    private static final double STRECKEN_TOLERANZ = 38.0;
    private static final double START_ABSTAND = 45.0;
    private static final long COUNTDOWN_DAUER_MS = 3000;
    private static final long GO_ANZEIGE_DAUER_MS = 800;

    public Spiel(Oberflaeche oberflaeche) {
        this.oberflaeche = oberflaeche;
        this.oberflaeche.loesche();
        this.datenbank = new Datenbank();
        this.datenbank.verbinde();
        this.level = new Level(new Map(this.oberflaeche.getMapView()));

        Map map = this.level.gibMap();
        this.startLinieA = map.getStartLinieA();
        this.startLinieB = map.getStartLinieB();
        this.startLinieTangent = map.getStartLinieTangent();
        this.checkpoints = map.getCheckpoints(CHECKPOINT_ANZAHL);

        double[] startPt = map.getStartPunkt();
        double startAngle = Math.toDegrees(
            Math.atan2(startLinieTangent[1], startLinieTangent[0])
        );
        double autoX = startPt[0] - startLinieTangent[0] * START_ABSTAND;
        double autoY = startPt[1] - startLinieTangent[1] * START_ABSTAND;
        Auto auto = new Auto(autoX, autoY, startAngle);
        this.spieler = new Spieler[] { new Spieler("Spieler 1", auto) };
    }

    @Override
    public void run() {
        laeuft = true;
        countdownStartZeit = System.currentTimeMillis();
        spieleKreis();
    }

    // game loop ihr deutschen
    public void spieleKreis() {
        Map map = this.level.gibMap();
        int x = 150, y = 80;
        while (map.distanceToTrack(x, y) < 120) y += 10;
        level.platziereGegenstand(new Baum(), x, y);
        x = 780; y = 80;
        while (map.distanceToTrack(x, y) < 120) y += 10;
        level.platziereGegenstand(new Baum(), x, y);
        x = 480; y = 520;
        while (map.distanceToTrack(x, y) < 120) y -= 10;
        level.platziereGegenstand(new Baum(), x, y);

        // Spielschleife
        while (laeuft) {
            for (Spieler s : spieler) {
                Auto a = s.gibAuto();
                a.itr();
                double dist = level
                    .gibMap()
                    .distanceToTrack(a.gibX(), a.gibY());
                a.applyOffTrackFriction(dist);
                a.begrenze(960, 600);
            }

            Listenelement elKollision = level.gibGegenstaende().gibAnfang();
            while (!elKollision.istAbschluss()) {
                Gegenstand g = (Gegenstand) ((Knoten) elKollision).gebeDaten();
                if (g instanceof Baum) {
                    Baum b = (Baum) g;
                    for (Spieler s : spieler) {
                        Auto a = s.gibAuto();
                        if (b.kollidiertMit((int) a.gibX(), (int) a.gibY())) {
                            a.kollision();
                        }
                    }
                }
                elKollision = ((Knoten) elKollision).gebeNachfolger();
            }

            for (Spieler s : spieler) {
                Auto a = s.gibAuto();
                aufStrecke = level
                    .gibMap()
                    .istNahAnStrecke(a.gibX(), a.gibY(), STRECKEN_TOLERANZ);

                if (naechsterCheckpoint < checkpoints.length) {
                    double[] cp = checkpoints[naechsterCheckpoint];
                    if (
                        a.prüfeLapCrossing(
                            cp[0],
                            cp[1],
                            cp[2],
                            cp[3],
                            cp[4],
                            cp[5],
                            false
                        )
                    ) {
                        naechsterCheckpoint++;
                    }
                }
            }

            if (kreuzungsCooldown > 0) {
                kreuzungsCooldown--;
            } else {
                for (Spieler s : spieler) {
                    Auto a = s.gibAuto();
                    if (
                        a.prüfeLapCrossing(
                            startLinieA[0],
                            startLinieA[1],
                            startLinieB[0],
                            startLinieB[1],
                            startLinieTangent[0],
                            startLinieTangent[1],
                            true
                        )
                    ) {
                        long jetzt = System.currentTimeMillis();
                        if (lapStartZeit < 0) {
                            lapStartZeit = jetzt;
                        } else if (naechsterCheckpoint == checkpoints.length) {
                            long lapZeit = jetzt - lapStartZeit;
                            lapZaehler++;
                            if (
                                besteRunde < 0 || lapZeit < besteRunde
                            ) besteRunde = lapZeit;
                            int kollisionen = a.gibKollisionen();
                            letzterScore = Math.max(
                                0,
                                10000 -
                                    (int) (lapZeit / 100) -
                                    kollisionen * 500
                            );
                            a.resetKollisionen();
                            lapStartZeit = jetzt;
                        }
                        naechsterCheckpoint = 0;
                        kreuzungsCooldown = 180;
                    }
                }
            }

            // Szene neu zeichnen
            this.oberflaeche.loesche();

            // Track neu zeichnen (MapView übernimmt die komplette Streckenlogik)
            this.level.gibMap().draw();

            for (int i = naechsterCheckpoint; i < checkpoints.length; i++) {
                double[] cp = checkpoints[i];
                this.oberflaeche.checkpointZeichnen(
                    cp[0],
                    cp[1],
                    cp[2],
                    cp[3],
                    i == naechsterCheckpoint
                );
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
                this.oberflaeche.autoZeichnen(
                    a.gibX(),
                    a.gibY(),
                    a.gibWinkelDouble()
                );
            }

            long aktuelleZeit =
                lapStartZeit < 0
                    ? 0
                    : System.currentTimeMillis() - lapStartZeit;
            int kollisionen = spieler[0].gibAuto().gibKollisionen();
            this.oberflaeche.hudZeichnen(
                lapZaehler,
                aktuelleZeit,
                besteRunde,
                aufStrecke,
                naechsterCheckpoint,
                checkpoints.length,
                kollisionen,
                letzterScore
            );
            String countdownText = gibCountdownText();
            if (countdownText != null) {
                this.oberflaeche.countdownZeichnen(countdownText);
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

    public boolean istRennenGestartet() {
        return countdownStartZeit >= 0 &&
            System.currentTimeMillis() - countdownStartZeit >= COUNTDOWN_DAUER_MS;
    }

    private String gibCountdownText() {
        if (countdownStartZeit < 0) return null;

        long vergangen = System.currentTimeMillis() - countdownStartZeit;
        if (vergangen < 1000) return "3";
        if (vergangen < 2000) return "2";
        if (vergangen < COUNTDOWN_DAUER_MS) return "1";
        if (vergangen < COUNTDOWN_DAUER_MS + GO_ANZEIGE_DAUER_MS) return "GO";
        return null;
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
