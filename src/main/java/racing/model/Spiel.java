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
        while (laeuft) {
            // loop hier - oberfläche und backend (zustände)
            for (Point p : this.level.gibMap().getPoints()) {
                this.oberflaeche.punktZeichnen(p.getX(), p.getY());
                System.out.println("Drawing point: " + p.toString());
            }

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
