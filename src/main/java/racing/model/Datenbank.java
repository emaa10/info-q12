package racing.model;

import java.sql.*;
import racing.datastructure.Liste;

public class Datenbank {

    private static final String DB_PFAD =
        System.getProperty("user.home") + "/racing_game.db";

    private Connection connection;

    public void verbinde() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_PFAD); // jdbc path: jdbc:sqlite:/home/user/racing_game.db (pfad nciht in git, absicht wegen git und so weiter)
            erstelleTabellen();
        } catch (SQLException e) {
            System.err.println("Datenbankfehler beim Verbinden: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Datenbankfehler: JDBC-Treiber nicht gefunden");
        }
    }

    private void erstelleTabellen() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate( // siehe https://www.sqlitetutorial.net/sqlite-create-table/ für syntax
                "CREATE TABLE IF NOT EXISTS spielstand (" +
                "  id           INTEGER PRIMARY KEY AUTOINCREMENT," +// automatischer primary key durch autoincrement, damit wir nicht selber hochzählen müssen
                "  spieler_name TEXT    NOT NULL," +
                "  level_id     INTEGER NOT NULL DEFAULT 0," +
                "  seed         INTEGER NOT NULL DEFAULT 0," +
                "  punkte       INTEGER NOT NULL," +
                "  zeit_ms      INTEGER NOT NULL," +
                "  erstellt_am  TEXT    DEFAULT (datetime('now','localtime'))" +
                ")"
            );
            // seed-spalte fuer alte dbs nachruesten, fehler wenn schon da -> egal
            try {
                stmt.executeUpdate("ALTER TABLE spielstand ADD COLUMN seed INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException schonDa) {
                // spalte existiert bereits
            }
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS level_fortschritt (" +
                "  id            INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  spieler_name  TEXT    NOT NULL," +
                "  level_id      INTEGER NOT NULL," +
                "  freigeschaltet INTEGER NOT NULL DEFAULT 0," +
                "  UNIQUE(spieler_name, level_id)" + //jeder spieler kann nur ein eintrag pro level haben
                ")"
            );
        }
    }

    // datenbank.speichereSpielstand("Emanuel", 1234, 3500, 63_000L);
    public void speichereSpielstand(String spielerName, int seed, int punkte, long zeitMs) {
        if (connection == null) return;
        String sql = "INSERT INTO spielstand (spieler_name, level_id, seed, punkte, zeit_ms) VALUES (?, 0, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) { // siehe https://www.sqlitetutorial.net/sqlite-java/insert/ für syntax, sql injection verhindert
            ps.setString(1, spielerName);
            ps.setInt(2, seed);
            ps.setInt(3, punkte);
            ps.setLong(4, zeitMs);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Datenbankfehler beim Speichern: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("unexpected error: " + e.getMessage());
        }
    }

    // welche seeds hat dieser spieler schon gespielt (abgeschlossen)
    public java.util.Set<Integer> ladeSeedsVon(String spielerName) {
        java.util.Set<Integer> seeds = new java.util.HashSet<>();
        if (connection == null) return seeds;
        String sql = "SELECT DISTINCT seed FROM spielstand WHERE spieler_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, spielerName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) seeds.add(rs.getInt("seed"));
        } catch (SQLException e) {
            System.err.println("Datenbankfehler beim Seed-Laden: " + e.getMessage());
        }
        return seeds;
    }

    // top10 ueber alle seeds
    public Liste ladeTopGlobal() {
        Liste ergebnisse = new Liste();
        if (connection == null) return ergebnisse; // könnte auch error raisen, mal schauen
        String sql =
            "SELECT spieler_name, seed, punkte, zeit_ms, erstellt_am " +
            "FROM spielstand " +
            "ORDER BY punkte DESC, zeit_ms ASC LIMIT 10"; // sortierung
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ergebnisse.fuegeHintenEin(new SpielstandEintrag(
                    rs.getString("spieler_name"),
                    rs.getInt("seed"),
                    rs.getInt("punkte"),
                    rs.getLong("zeit_ms"),
                    rs.getString("erstellt_am")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Datenbankfehler beim Laden: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("unexpected error: " + e.getMessage());
        }
        return ergebnisse; //format: spielerName | seed | punkte | zeitMs | erstelltAm (siehe constructor von SpielstandEintrag)
    }

    // datenbank.schalteFreiLevel("Emanuel", 2);
    public void schalteFreiLevel(String spielerName, int levelId) {
        if (connection == null) return;
        String sql =
            "INSERT OR REPLACE INTO level_fortschritt (spieler_name, level_id, freigeschaltet) " +
            "VALUES (?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, spielerName);
            ps.setInt(2, levelId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Datenbankfehler beim Freischalten: " + e.getMessage());
        }
    }

    // if (datenbank.istLevelFreigeschaltet("Emanuel", 2))
    public boolean istLevelFreigeschaltet(String spielerName, int levelId) {
        if (connection == null) return false;
        String sql =
            "SELECT freigeschaltet FROM level_fortschritt " +
            "WHERE spieler_name = ? AND level_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, spielerName);
            ps.setInt(2, levelId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("freigeschaltet") == 1;
        } catch (SQLException e) {
            System.err.println("Datenbankfehler beim Prüfen: " + e.getMessage());
        }
        return false;
    }

    public void trenneVerbindung() {
        if (connection == null) return;
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Datenbankfehler beim Trennen: " + e.getMessage());
        }
    }
}
