package com.pixeldrift.powerups;

import com.pixeldrift.entities.Vehicle;
import com.pixeldrift.physics.Vector2D;

public abstract class PowerUp {
    protected Vector2D position;
    protected double duration;

    public abstract void applyTo(Vehicle v);

    public void update(double dt) {}
}
