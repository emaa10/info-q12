package com.pixeldrift.entities;

import com.pixeldrift.track.Track;
import com.pixeldrift.track.Checkpoint;

public class AIVehicle extends Vehicle {
    private Track track;
    private Checkpoint currentTarget;

    @Override
    public void update(double dt) {}

    private void planPath() {}
}
