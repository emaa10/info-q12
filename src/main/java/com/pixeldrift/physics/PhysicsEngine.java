package com.pixeldrift.physics;

import com.pixeldrift.entities.Vehicle;
import com.pixeldrift.track.Track;

import java.util.List;

public class PhysicsEngine {
    private double gravity;
    private double friction;

    public void step(List<Vehicle> vs, Track t, double dt) {}

    private Collision checkCollision(Vehicle a, Vehicle b) {
        return null;
    }

    private void resolveCollision(Collision c) {}

    private void applyTrackBoundary(Vehicle v, Track t) {}
}
