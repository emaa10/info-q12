package racing.model;

import java.util.List;

import javafx.application.Platform;
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
    private String spielerName2 = "Spieler 2";
    // neustart wird vom menü angefordert, aber im gamethread ausgefuehrt (thread-safe)
    private volatile boolean neustartAngefordert = false;
    private volatile int neustartSeed;

    private double[] startLinieA;
    private double[] startLinieB;
    private double[] startLinieTangent;
    private double[][] checkpoints;
    private long countdownStartZeit = -1;

    // backpressure fuers zeichnen: GameThread laeuft mit fixem 16ms-sleep und
    // wartet NICHT auf Platform.runLater() (das ist fire-and-forget) -> unter
    // software-rendering (kein echtes gpu in qemu) kann der fx-application-
    // thread einen kompletten frame (strecke+checkpoints+gegenstaende+autos+hud)
    // oft nicht in 16ms zeichnen. ohne diese sperre wuerden neue runLater()-
    // aufrufe schneller ankommen als sie abgearbeitet werden -> die queue waechst
    // unbegrenzt (genau das problem, das die vorherige buendelung in EINEN
    // runLater() pro frame allein nicht loest). mit der sperre wird ein frame
    // einfach uebersprungen, wenn der vorherige noch nicht fertig gezeichnet ist.
    private final java.util.concurrent.atomic.AtomicBoolean zeichnenAusstehend =
        new java.util.concurrent.atomic.AtomicBoolean(false);

    private static final double SKALIERUNG = 1.8;
    private static final int BREITE = 1920;
    private static final int HOEHE = 1080;

    private static final int CHECKPOINT_ANZAHL = 8;
    private static final int NITRO_ANZAHL = 4;
    private static final int HASE_Y = (int) Math.round(300 * SKALIERUNG);
    private static final double STRECKEN_TOLERANZ = 38.0 * SKALIERUNG;
    private static final double START_ABSTAND = 45.0 * SKALIERUNG;
    private static final double AUTO_KOLLISIONS_RADIUS = 30.0 * SKALIERUNG;
    private static final long COUNTDOWN_DAUER_MS = 3000;
    private static final long GO_ANZEIGE_DAUER_MS = 800;
    private static final int RUNDEN_ZUM_SIEG = 3;
    // erklaerungsfenster (regeln/steuerung) laeuft VOR dem 3-2-1-GO countdown,
    // deshalb verschiebt sich countdownStartZeit-basierte logik (gibCountdownText(),
    // istRennenGestartet()) unten um genau diese dauer
    private static final long ERKLAERUNG_DAUER_MS = 10000;

    // true sobald ein spieler RUNDEN_ZUM_SIEG runden voll hat -> simulation
    // friert ein (siehe spieleKreis()), der sieger-bildschirm wird per
    // Platform.runLater() genau einmal beim uebergang false->true angestossen
    private boolean rennBeendet = false;
    private String siegerName = null;

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
        // kann auch vom gamethread aus aufgerufen werden (neustart waehrend
        // des spiels) -> nicht auf dem fx-thread garantiert, deshalb hier
        // noch einzeln gewrapped (im gegensatz zum haupt-zeichenblock unten)
        Platform.runLater(this.oberflaeche::loesche);
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

        // normale zur tangente, damit die beiden autos nebeneinander starten statt uebereinander
        double normalX = -startLinieTangent[1];
        double normalY = startLinieTangent[0];
        double seitenAbstand = 18.0 * SKALIERUNG;

        Auto auto1 = new Auto(
            autoX + normalX * seitenAbstand,
            autoY + normalY * seitenAbstand,
            startAngle
        );
        Auto auto2 = new Auto(
            autoX - normalX * seitenAbstand,
            autoY - normalY * seitenAbstand,
            startAngle
        );
        this.spieler = new Spieler[] {
            new Spieler(spielerName, auto1),
            new Spieler(spielerName2, auto2),
        };

        // zustand zuruecksetzen (rennfortschritt steckt in den frischen Spieler-objekten)
        this.countdownStartZeit = -1;
        this.pausiert = false;
        this.pauseBeginn = -1;
        this.rennBeendet = false;
        this.siegerName = null;

        platziereBaeumeUndNitros();
        platziereHase();
    }

    // baeume an feste ecken, nitros auf die strecke
    // rasterwerte hier sind auf der urspruenglichen 960x600-flaeche
    // handgetunt -> mit SKALIERUNG hochrechnen statt neu abzustimmen
    private void platziereBaeumeUndNitros() {
        Map map = this.level.gibMap();
        int mindestAbstand = (int) Math.round(100 * SKALIERUNG);
        int[] reihenX = { 30, 130, 230, 330, 430, 530, 630, 730, 830 };
        for (int xRoh : reihenX) {
            int x = (int) Math.round(xRoh * SKALIERUNG);
            int y = (int) Math.round(20 * SKALIERUNG);
            if (map.distanceToTrack(x, y) >= mindestAbstand) level.platziereGegenstand(new Baum(), x, y);
            y = (int) Math.round(470 * SKALIERUNG);
            if (map.distanceToTrack(x, y) >= mindestAbstand) level.platziereGegenstand(new Baum(), x, y);
        }
        int[] reihenY = { 30, 130, 230, 330, 430 };
        for (int yRoh : reihenY) {
            int y = (int) Math.round(yRoh * SKALIERUNG);
            int x = (int) Math.round(20 * SKALIERUNG);
            if (map.distanceToTrack(x, y) >= mindestAbstand) level.platziereGegenstand(new Baum(), x, y);
            x = (int) Math.round(840 * SKALIERUNG);
            if (map.distanceToTrack(x, y) >= mindestAbstand) level.platziereGegenstand(new Baum(), x, y);
        }
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
        // regeln/steuerung-fenster: EINMALIG hier ausloesen statt jeden frame
        // neu zu pruefen (wie zuvor) -> die pro-frame-zeichenschleife kann
        // unter last frames ueberspringen (backpressure, siehe
        // zeichnenAusstehend), ein rein zeit-basiertes "nur in den ersten
        // ERKLAERUNG_DAUER_MS zeichnen" haette das fenster dadurch komplett
        // verpassen koennen, ohne je sichtbar gewesen zu sein
        if (spieler != null && spieler.length >= 2) {
            final String erklaerungText = RUNDEN_ZUM_SIEG +
                " Runden fahren - wer zuerst fertig ist, gewinnt!\n" +
                spieler[0].gibName() + " (links): W A S D + B\n" +
                spieler[1].gibName() + " (rechts): Pfeiltasten + N";
            Platform.runLater(() -> this.oberflaeche.zeigeErklaerung(erklaerungText));
        }
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
            for (Spieler s : spieler) {
                if (s.gibLapStartZeit() >= 0) s.setzeLapStartZeit(s.gibLapStartZeit() + dauer);
            }
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

    public void setzeSpielerName2(String name) {
        this.spielerName2 = name;
        if (spieler != null && spieler.length > 1) {
            spieler[1].setzeName(name);
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
            // rennen vorbei (jemand hat RUNDEN_ZUM_SIEG runden voll) -> genau wie
            // pause: nix mehr simulieren, letztes bild bleibt hinter dem
            // sieger-panel (siehe Oberflaeche.zeigeSieger()) stehen
            if (rennBeendet) {
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
                a.begrenze(BREITE, HOEHE);
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
                } else if (g instanceof Hase) {
                    Hase hase = (Hase) g;
                    for (Spieler s : spieler) {
                        Auto a = s.gibAuto();
                        if (hase.kollidiertMit((int) a.gibX(), (int) a.gibY())) {
                            a.kollision();
                        }
                    }
                }
                elKollision = ((Knoten) elKollision).gebeNachfolger();
            }

            // auto-gegen-auto kollision: beide werden langsamer, beide crash-counter hoch
            for (int i = 0; i < spieler.length; i++) {
                for (int j = i + 1; j < spieler.length; j++) {
                    Auto a1 = spieler[i].gibAuto();
                    Auto a2 = spieler[j].gibAuto();
                    double dx = a1.gibX() - a2.gibX();
                    double dy = a1.gibY() - a2.gibY();
                    double abstand = Math.sqrt(dx * dx + dy * dy);
                    if (abstand < AUTO_KOLLISIONS_RADIUS) {
                        a1.kollision();
                        a2.kollision();
                    }
                }
            }

            Listenelement elHase = level.gibGegenstaende().gibAnfang();
            while (!elHase.istAbschluss()) {
                Gegenstand g = (Gegenstand) ((Knoten) elHase).gebeDaten();
                if (g instanceof Hase) {
                    Hase hase = (Hase) g;
                    int[] pos = hase.gebePosition();
                    int nx = pos[0] + (int) Hase.GESCHWINDIGKEIT;
                    int ny = pos[1];
                    if (spieler.length > 0) {
                        int autoY = (int) spieler[0].gibAuto().gibY();
                        ny += (autoY - ny) / 30;
                    }
                    if (nx > BREITE) {
                        nx = 0;
                    }
                    hase.setzePosition(nx, ny);
                    break;
                }
                elHase = ((Knoten) elHase).gebeNachfolger();
            }

            for (Spieler s : spieler) {
                Auto a = s.gibAuto();
                s.setzeAufStrecke(
                    level.gibMap().istNahAnStrecke(a.gibX(), a.gibY(), STRECKEN_TOLERANZ)
                );

                int naechsterCp = s.gibNaechsterCheckpoint();
                if (naechsterCp < checkpoints.length) {
                    double[] cp = checkpoints[naechsterCp];
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
                        s.setzeNaechsterCheckpoint(naechsterCp + 1);
                    }
                }

                int cooldown = s.gibKreuzungsCooldown();
                if (cooldown > 0) {
                    s.setzeKreuzungsCooldown(cooldown - 1);
                    continue;
                }

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
                    long lapStart = s.gibLapStartZeit();
                    if (lapStart < 0) {
                        s.setzeLapStartZeit(jetzt);
                    } else if (s.gibNaechsterCheckpoint() == checkpoints.length) {
                        long lapZeit = jetzt - lapStart;
                        s.erhoeheLapZaehler();
                        long beste = s.gibBesteRunde();
                        if (beste < 0 || lapZeit < beste) s.setzeBesteRunde(lapZeit);
                        int kollisionen = a.gibKollisionen();
                        int score = Math.max(
                            0,
                            10000 - (int) (lapZeit / 100) - kollisionen * 500
                        );
                        s.setzeLetzterScore(score);
                        a.resetKollisionen();
                        s.setzeLapStartZeit(jetzt);
                        // score + seed in die db fuers leaderboard
                        datenbank.speichereSpielstand(s.gibName(), aktuellerSeed, score, lapZeit);
                        // runde durch -> nitros wieder auffuellen
                        erneuereNitros();
                        // sieg: erster spieler mit RUNDEN_ZUM_SIEG runden gewinnt sofort,
                        // rest der schleife (physik/kollisionen) friert naechsten frame ein
                        if (!rennBeendet && s.gibLapZaehler() >= RUNDEN_ZUM_SIEG) {
                            rennBeendet = true;
                            siegerName = s.gibName();
                            final String siegerFuerUi = siegerName;
                            Platform.runLater(() -> this.oberflaeche.zeigeSieger(siegerFuerUi));
                        }
                    }
                    s.setzeNaechsterCheckpoint(0);
                    s.setzeKreuzungsCooldown(180);
                }
            }

            // Szene neu zeichnen -> ALLES in EINEN runLater() gebuendelt, statt
            // vorher pro einzelnem zeichen-aufruf (baum/nitro/hase/checkpoint/...)
            // einen eigenen. bei vielen objekten kamen da pro frame leicht 20-30+
            // einzelne Platform.runLater()-aufrufe zusammen, 60x/sekunde -> die
            // fx-event-queue lief unter software-rendering (kein echtes gpu,
            // siehe -Dprism.order=es2,sw) schneller voll als sie abgearbeitet
            // werden konnte, was den fx-application-thread dauerhaft blockierte
            // und dazu fuehrte, dass maus/tastatur-eingaben nicht mehr verarbeitet
            // wurden (bzw. wie hier: der countdown/die anzeige einfror).
            final String countdownTextFuerFrame = gibCountdownText();
            if (zeichnenAusstehend.compareAndSet(false, true)) {
            Platform.runLater(() -> {
              try {
                this.oberflaeche.loesche();

                // Track neu zeichnen (MapView übernimmt die komplette Streckenlogik)
                this.level.gibMap().draw();

                // checkpoint-anzeige: jeder spieler hat sein eigenes ziel (eigene farbe)
                int zielCpP1 = spieler[0].gibNaechsterCheckpoint();
                int zielCpP2 = spieler.length > 1
                    ? spieler[1].gibNaechsterCheckpoint()
                    : checkpoints.length;
                int anzeigeAb = Math.min(zielCpP1, zielCpP2);
                for (int i = anzeigeAb; i < checkpoints.length; i++) {
                    double[] cp = checkpoints[i];
                    this.oberflaeche.checkpointZeichnen(
                        cp[0],
                        cp[1],
                        cp[2],
                        cp[3],
                        i == zielCpP1,
                        i == zielCpP2
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
                for (int i = 0; i < spieler.length; i++) {
                    Auto a = spieler[i].gibAuto();
                    this.oberflaeche.autoZeichnen(
                        a.gibX(),
                        a.gibY(),
                        a.gibWinkelDouble(),
                        i
                    );
                }

                this.oberflaeche.topLeisteZeichnen(aktuellerSeed);

                for (int i = 0; i < spieler.length; i++) {
                    Spieler s = spieler[i];
                    if (!s.istAufStrecke()) {
                        this.oberflaeche.streckenWarnungZeichnen(s.gibName(), i == 0);
                    }
                }

                for (int i = 0; i < spieler.length; i++) {
                    Spieler s = spieler[i];
                    Auto a = s.gibAuto();
                    long zeit = s.gibLapStartZeit() < 0
                        ? 0
                        : System.currentTimeMillis() - s.gibLapStartZeit();
                    this.oberflaeche.spielerHudZeichnen(
                        s.gibName(),
                        s.gibLapZaehler(),
                        zeit,
                        s.gibBesteRunde(),
                        s.gibNaechsterCheckpoint(),
                        checkpoints.length,
                        a.gibKollisionen(),
                        s.gibLetzterScore(),
                        a.gibNitroStatus(),
                        a.gibNitroFortschritt(),
                        i != 0
                    );
                }
                if (countdownTextFuerFrame != null) {
                    this.oberflaeche.countdownZeichnen(countdownTextFuerFrame);
                }
              } finally {
                zeichnenAusstehend.set(false);
              }
            });
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
            System.currentTimeMillis() - countdownStartZeit >=
                ERKLAERUNG_DAUER_MS + COUNTDOWN_DAUER_MS;
    }

    public boolean istRennenBeendet() {
        return rennBeendet;
    }

    public String gibSiegerName() {
        return siegerName;
    }

    private String gibCountdownText() {
        if (countdownStartZeit < 0) return null;

        long vergangen = System.currentTimeMillis() - countdownStartZeit - ERKLAERUNG_DAUER_MS;
        if (vergangen < 0) return null;
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
