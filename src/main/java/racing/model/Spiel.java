package racing.model;

import java.util.List;

import racing.datastructure.Knoten;
import racing.datastructure.Liste;
import racing.datastructure.Listenelement;
import racing.view.Oberflaeche;
import racing.model.Hase;

// model-teil
public class Spiel implements Runnable {

    private Spieler[] spieler;
    private Level level;
    private Datenbank datenbank;
    private Oberflaeche oberflaeche;

    private volatile boolean laeuft;
    private volatile boolean pausiert = false;
    private long pauseBeginn = -1;

    private int aktuellerSeed;
    private String spielerName = "Spieler 1";
    // neustart wird vom menü angefordert, aber im gamethread ausgefuehrt (thread-safe)
    private volatile boolean neustartAngefordert = false;
    private volatile int neustartSeed;

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
    private static final int NITRO_ANZAHL = 4;
    private static final int HASE_Y = 300;
    private static final double STRECKEN_TOLERANZ = 38.0;
    private static final double START_ABSTAND = 45.0;
    private static final long COUNTDOWN_DAUER_MS = 3000;
    private static final long GO_ANZEIGE_DAUER_MS = 800;

    public Spiel(Oberflaeche oberflaeche) {
        this.oberflaeche = oberflaeche;
        this.oberflaeche.loesche();
        this.datenbank = new Datenbank();
        this.datenbank.verbinde();
        initialisiereLevel(erzeugeSeed());
    }

    // baut map/level/auto neu auf und setzt alle renn-zustaende zurueck
    private void initialisiereLevel(int seed) {
        // seed in [1, 65536] bringen (laesst gueltige seeds unveraendert), sonst LCG-overflow
        seed = 1 + Math.floorMod(seed - 1, 65536);
        this.aktuellerSeed = seed;
        this.oberflaeche.loesche();
        this.level = new Level(new Map(this.oberflaeche.getMapView(), seed));

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
        this.spieler = new Spieler[] { new Spieler(spielerName, auto) };

        // zustand zuruecksetzen
        this.naechsterCheckpoint = 0;
        this.lapStartZeit = -1;
        this.lapZaehler = 0;
        this.besteRunde = -1;
        this.kreuzungsCooldown = 300;
        this.aufStrecke = true;
        this.letzterScore = 0;
        this.countdownStartZeit = -1;
        this.pausiert = false;
        this.pauseBeginn = -1;

        platziereBaeumeUndNitros();
        platziereHase();
    }

    // baeume an feste ecken, nitros auf die strecke
    private void platziereBaeumeUndNitros() {
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
        platziereNitros();
    }

    // seed in [1, 65536], den der spieler noch nicht hatte
    public int erzeugeSeed() {
        java.util.Set<Integer> gespielt = datenbank.ladeSeedsVon(spielerName);
        int seed = 1 + (int) Math.floorMod(System.nanoTime(), 65536L);
        int versuche = 0;
        while (gespielt.contains(seed) && versuche < 65536) {
            seed = 1 + (seed % 65536); // naechster seed, wrappt bei 65536 zu 1
            versuche++;
        }
        return seed;
    }

    public int gibAktuellenSeed() {
        return aktuellerSeed;
    }

    // vom menü (fx-thread): neues spiel mit diesem seed. gamethread macht den rest
    public void neuesSpielMitSeed(int seed) {
        neustartSeed = seed;
        neustartAngefordert = true;
        pausiert = false;
    }

    @Override
    public void run() {
        laeuft = true;
        // countdown erst bei starteRennen(), nich automatisch
        spieleKreis();
    }

    // start-button im menü -> countdown laeuft los
    public void starteRennen() {
        countdownStartZeit = System.currentTimeMillis();
    }

    // schon mal gestartet? (fuer fortsetzen nach pause)
    public boolean istGestartet() {
        return countdownStartZeit >= 0;
    }

    public void pausiere() {
        if (!pausiert) {
            pausiert = true;
            pauseBeginn = System.currentTimeMillis();
        }
    }

    public void fortsetzen() {
        if (pausiert) {
            pausiert = false;
            // zeitstempel um die pausendauer verschieben, sonst laeuft die uhr weiter
            long dauer = System.currentTimeMillis() - pauseBeginn;
            if (lapStartZeit >= 0) lapStartZeit += dauer;
            if (countdownStartZeit >= 0) countdownStartZeit += dauer;
            pauseBeginn = -1;
        }
    }

    public boolean istPausiert() {
        return pausiert;
    }

    public void setzeSpielerName(String name) {
        this.spielerName = name;
        if (spieler != null && spieler.length > 0) {
            spieler[0].setzeName(name);
        }
    }

    // top10 aus der db (main baut daraus die menü-zeilen)
    public Liste gibLeaderboardEintraege() {
        return datenbank.ladeTopGlobal();
    }

    // game loop ihr deutschen
    public void spieleKreis() {
        // Spielschleife
        while (laeuft) {
            // neustart mit neuem seed, im gamethread damit thread-safe
            if (neustartAngefordert) {
                neustartAngefordert = false;
                initialisiereLevel(neustartSeed);
                starteRennen();
                continue;
            }
            // pause -> nix simulieren, bild bleibt stehen
            if (pausiert) {
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    laeuft = false;
                }
                continue;
            }

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
                } else if (g instanceof Nitro) {
                    Nitro nitro = (Nitro) g;
                    for (Spieler s : spieler) {
                        Auto a = s.gibAuto();
                        if (
                            nitro.kollidiertMit((int) a.gibX(), (int) a.gibY()) &&
                            a.sammleNitro()
                        ) {
                            level.entferneGegenstand(nitro);
                            break;
                        }
                    }
                }
                elKollision = ((Knoten) elKollision).gebeNachfolger();
            }

            Listenelement elHase = level.gibGegenstaende().gibAnfang();
            while (!elHase.istAbschluss()) {
                Gegenstand g = (Gegenstand) ((Knoten) elHase).gebeDaten();
                if (g instanceof Hase) {
                    Hase hase = (Hase) g;
                    int[] pos = hase.gebePosition();
                    hase.setzePosition(pos[0] + (int) Hase.GESCHWINDIGKEIT, pos[1]);
                    if (pos[0] > 960) {
                        level.entferneGegenstand(hase);
                        platziereHase();
                    }
                    break;
                }
                elHase = ((Knoten) elHase).gebeNachfolger();
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
                            // score + seed in die db fuers leaderboard
                            datenbank.speichereSpielstand(s.gibName(), aktuellerSeed, letzterScore, lapZeit);
                            // runde durch -> nitros wieder auffuellen
                            erneuereNitros();
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
                if (g instanceof Baum) {
                    this.oberflaeche.baumZeichnen(pos[0], pos[1]);
                } else if (g instanceof Nitro) {
                    this.oberflaeche.nitroZeichnen(pos[0], pos[1]);
                } else if (g instanceof Hase) {
                    this.oberflaeche.haseZeichnen(pos[0], pos[1]);
                }
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
            Auto hudAuto = spieler[0].gibAuto();
            this.oberflaeche.hudZeichnen(
                lapZaehler,
                aktuelleZeit,
                besteRunde,
                aufStrecke,
                naechsterCheckpoint,
                checkpoints.length,
                kollisionen,
                letzterScore,
                hudAuto.gibNitroStatus(),
                hudAuto.gibNitroFortschritt(),
                aktuellerSeed
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

    // alte nitros raus, neue rein (pool nutzt die alten wieder)
    private void erneuereNitros() {
        // erst sammeln, dann entfernen (nich waehrend iteration loeschen)
        List<Gegenstand> zuEntfernen = new java.util.ArrayList<>();
        Listenelement el = level.gibGegenstaende().gibAnfang();
        while (!el.istAbschluss()) {
            Gegenstand g = (Gegenstand) ((Knoten) el).gebeDaten();
            if (g instanceof Nitro) zuEntfernen.add(g);
            el = ((Knoten) el).gebeNachfolger();
        }
        for (Gegenstand g : zuEntfernen) {
            level.entferneGegenstand(g);
        }
        platziereNitros();
    }

    private void platziereNitros() {
        List<int[]> centerline = level.gibMap().getCenterline();
        int n = centerline.size();
        for (int i = 0; i < NITRO_ANZAHL; i++) {
            int idx = ((i + 1) * n) / (NITRO_ANZAHL + 1);
            int[] p = centerline.get(idx);
            level.platziereGegenstand(
                new Nitro(),
                p[0] - Nitro.BREITE / 2,
                p[1] - Nitro.HOEHE / 2
            );
        }
    }

    private void platziereHase() {
        level.platziereGegenstand(new Hase(), 0, HASE_Y);
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
