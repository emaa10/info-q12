package com.pixeldrift.core;

import com.pixeldrift.entities.Vehicle;
import com.pixeldrift.physics.PhysicsEngine;
import com.pixeldrift.track.Track;
import com.pixeldrift.ui.Renderer;
import com.pixeldrift.input.InputHandler;
import com.pixeldrift.audio.AudioManager;
import com.pixeldrift.database.DatabaseManager;

import java.util.List;

public class Game {
    private GameState state;
    private Track track;
    private List<Vehicle> vehicles;
    private PhysicsEngine physics;
    private Renderer renderer;
    private InputHandler input;
    private AudioManager audio;
    private DatabaseManager database;

    public void start() {}

    public void stop() {}

    public void update(double dt) {}

    public void render() {}

    public GameState getState() {
        return state;
    }
}
