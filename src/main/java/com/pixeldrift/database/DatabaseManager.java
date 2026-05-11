package com.pixeldrift.database;

import java.sql.Connection;

public class DatabaseManager {
    private Connection connection;

    public void saveScore(String player, long timeMs, String trackId) {}

    public Leaderboard loadLeaderboard(String trackId) {
        return null;
    }

    public void saveGame(SaveGame state) {}

    public SaveGame loadGame(String id) {
        return null;
    }
}
