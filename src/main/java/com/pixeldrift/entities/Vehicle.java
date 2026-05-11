package com.pixeldrift.entities;

import com.pixeldrift.physics.Vector2D;

public abstract class Vehicle {
    protected Vector2D position;
    protected Vector2D velocity;
    protected double rotation;
    protected double maxSpeed;
    protected double acceleration;
    protected VehicleType type;

    public abstract void update(double dt);

    public void accelerate(double force) {}

    public void brake(double force) {}

    public void steer(double angle) {}

    public Vector2D getPosition() {
        return position;
    }
}
