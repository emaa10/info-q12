package com.pixeldrift.core;

public class GameLoop implements Runnable {
    private final Game game;
    private boolean running;
    private int targetFPS = 60;

    public GameLoop(Game game) {
        this.game = game;
    }

    @Override
    public void run() {}

    public void stop() {
        running = false;
    }
}
