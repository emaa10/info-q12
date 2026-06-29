package racing.model;

import racing.datastructure.Datenelement;

public class Auto implements Datenelement {

    private double x;
    private double y;
    private double prevX;
    private double prevY;
    private double winkel; // in Grad (0 = rechts, 90 = unten)

    private double v_x;
    private double v_y;
    private double a_x;
    private double a_y;

    private final double a; // Beschleunigung pro Frame
    private final double m; // Masse in kg
    private final double Cv; // Luftwiderstandsbeiwert
    private final double Crr; // Rollwiderstandsbeiwert
    private final double A; // Frontfläche in m²
    private final double drehRate; // Grad pro Frame
    private final double traktion; // 0 = Eis, 1 = perfekter Grip ==> man könnte bereiche für verschiedene Untergründe definieren
    private final double heckSchlupf; // Lateralgrip unter Gas (heck-drift)
    private boolean gasAktiv = false;
    private int kollisionsAnzahl = 0;
    private int kollisionsCooldown = 0;
    private int nitroLadungTicks = 0;
    private boolean nitroTasteGedrueckt = false;

    private static final int NITRO_MAX_TICKS = 360;
    private static final int NITRO_PICKUP_TICKS = 180;
    private static final double NITRO_BESCHLEUNIGUNG = 2.2;
    private static final double NITRO_SCHUB = 0.08;

    public Auto(double x, double y, double winkel) {
        this.x = x;
        this.y = y;
        this.prevX = x;
        this.prevY = y;
        this.winkel = winkel;
        this.a = 0.1; // wir geben weniger gas, vorher 0.22
        this.m = 1000.0;
        this.Cv = 0.9; // Wir erhöhen die Friction vorher 0.3
        this.Crr = 0.01;
        this.A = 2.2;
        this.drehRate = 3.0;
        this.traktion = 0.65; // 75 % Seitengeschwindigkeit wird pro Frame gedämpft
        this.heckSchlupf = 0.3; // unter Gas nur 35 % Dämpfung → leichtes Übersteuern
    }

    // in jedem step wird diese Methode aufgerufen, um die Position zu aktualisieren und auch die beschleunigung zurückzusetzen
    public void itr() {
        prevX = x;
        prevY = y;
        if (kollisionsCooldown > 0) kollisionsCooldown--;
        if (nitroAktiv()) {
            double rad = Math.toRadians(winkel);
            a_x += Math.cos(rad) * NITRO_SCHUB;
            a_y += Math.sin(rad) * NITRO_SCHUB;
            nitroLadungTicks--;
        }
        x += v_x;
        y += v_y;

        v_x += a_x;
        v_y += a_y;

        applyAirDrag();
        applyTraktion();

        a_x = 0;
        a_y = 0;
    }

    // berechnet x und y beschleunigung basierend auf der aktuellen drehung des autos
    public void gibGas() {
        double rad = Math.toRadians(winkel);
        double faktor = nitroAktiv() ? NITRO_BESCHLEUNIGUNG : 1.0;
        a_x += a * faktor * Math.cos(rad);
        a_y += a * faktor * Math.sin(rad);
        gasAktiv = true;
    }

    // berechnet x und y beschleunigung basierend auf der aktuellen drehung des autos, aber in die entgegengesetzte richtung wie gas
    public void bremse() {
        double rad = Math.toRadians(winkel);
        a_x -= a * Math.cos(rad);
        a_y -= a * Math.sin(rad);
    }

    public void dreheLinks() {
        winkel = (winkel - drehRate + 360) % 360;
    }

    public void dreheRechts() {
        winkel = (winkel + drehRate) % 360;
    }

    // Luftwiderstand: F_drag = 0.5 * Cv * A * rho_luft * v^2, wirkt immer entgegen der Fahrtrichtung
    private void applyAirDrag() {
        double v_squared = v_x * v_x + v_y * v_y;
        double speed = Math.sqrt(v_squared);
        if (speed == 0) return;

        // F_drag = 0.5 * Cv * A * rho_luft * v^2 (rho = 1.293 kg/m^3 auf Meeresspiegel)
        double dragForce = 0.5 * Cv * A * 1.293 * v_squared;
        double maxDragForce = speed * m;
        if (dragForce > maxDragForce) dragForce = maxDragForce;

        v_x -= (v_x / speed) * (dragForce / m);
        v_y -= (v_y / speed) * (dragForce / m);
    }

    // Seitengeschwindigkeit dämpfen ==> Auto folgt seiner Fahrtrichtung
    // Unter Gas (Heckantrieb): Hinterräder verlieren Querhaftung → https://www.youtube.com/watch?v=7jItw5c_-I0
    private void applyTraktion() {
        double rad = Math.toRadians(winkel);
        double fwdX = Math.cos(rad);
        double fwdY = Math.sin(rad);

        double forwardSpeed = v_x * fwdX + v_y * fwdY;
        double lateralX = v_x - forwardSpeed * fwdX;
        double lateralY = v_y - forwardSpeed * fwdY;

        double grip = gasAktiv ? heckSchlupf : traktion;
        v_x -= lateralX * grip;
        v_y -= lateralY * grip;

        gasAktiv = false;
    }

    // je weiter das Auto vom Track entfernt ist, desto mehr Reibung (0.99 auf Track, 0.90 weit draußen)
    public void applyOffTrackFriction(double distVomTrack) {
        double faktor = 0.99 - 0.09 * (1.0 - Math.exp(-distVomTrack / 30.0));
        v_x *= faktor;
        v_y *= faktor;
    }

    // schaut dass das auto nicht aus der bahn rausfährt, wenn es die wand berührt wird die position korrigiert und die geschwindigkeit in diese richtung auf 0 gesetzt
    public void begrenze(double maxX, double maxY) {
        double halbBreite = 28;
        double halbHoehe = 14;

        if (x - halbBreite < 0) {
            x = halbBreite;
            if (v_x < 0) v_x = 0;
        }
        if (x + halbBreite > maxX) {
            x = maxX - halbBreite;
            if (v_x > 0) v_x = 0;
        }
        if (y - halbHoehe < 0) {
            y = halbHoehe;
            if (v_y < 0) v_y = 0;
        }
        if (y + halbHoehe > maxY) {
            y = maxY - halbHoehe;
            if (v_y > 0) v_y = 0;
        }
    }

    public double getSpeed() {
        return Math.sqrt(v_x * v_x + v_y * v_y);
    }

    public int[] gebePos() {
        return new int[] { (int) x, (int) y };
    }

    // wenn das auto einen baum trifft: geschwindigkeit stark reduzieren
    public void kollision() {
        if (kollisionsCooldown > 0) return;
        v_x *= 0.2;
        v_y *= 0.2;
        kollisionsAnzahl++;
        kollisionsCooldown = 40;
    }

    public int gibKollisionen() {
        return kollisionsAnzahl;
    }

    public void resetKollisionen() {
        kollisionsAnzahl = 0;
    }

    public boolean sammleNitro() {
        if (nitroLadungTicks >= NITRO_MAX_TICKS) return false;
        nitroLadungTicks = Math.min(
            NITRO_MAX_TICKS,
            nitroLadungTicks + NITRO_PICKUP_TICKS
        );
        return true;
    }

    public void setzeNitroTaste(boolean gedrueckt) {
        nitroTasteGedrueckt = gedrueckt;
    }

    public boolean nitroAktiv() {
        return nitroTasteGedrueckt && nitroLadungTicks > 0;
    }

    public double gibNitroFortschritt() {
        return (double) nitroLadungTicks / NITRO_MAX_TICKS;
    }

    public String gibNitroStatus() {
        if (nitroAktiv()) return "BOOST";
        if (nitroLadungTicks == 0) return "LEER";
        return "BEREIT";
    }

    public double gibX() {
        return x;
    }

    public double gibY() {
        return y;
    }

    public int gibWinkel() {
        return (int) winkel;
    }

    public double gibWinkelDouble() {
        return winkel;
    }

    // schaut ob das auto die linie von (ax,ay) nach (bx,by) überquert hat
    // richtungPruefen=true: rückwärts zählt nicht (für startlinie), false: für checkpoints
    public boolean prüfeLapCrossing(
        double ax,
        double ay,
        double bx,
        double by,
        double tx,
        double ty,
        boolean richtungPruefen
    ) {
        double dx = x - prevX;
        double dy = y - prevY;

        if (richtungPruefen && dx * tx + dy * ty <= 0) return false;

        // kreuzprodukt-test ob die bewegung die linie schneidet
        double d1 = (bx - ax) * (prevY - ay) - (by - ay) * (prevX - ax);
        double d2 = (bx - ax) * (y - ay) - (by - ay) * (x - ax);
        double d3 = dx * (ay - prevY) - dy * (ax - prevX);
        double d4 = dx * (by - prevY) - dy * (bx - prevX);

        return d1 * d2 < 0 && d3 * d4 < 0;
    }
}
