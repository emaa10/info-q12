package com.pixeldrift.track;

import com.pixeldrift.entities.Vehicle;
import com.pixeldrift.physics.Vector2D;

import java.util.List;

public class Track {
    private List<TrackSegment> segments;
    private List<Checkpoint> checkpoints;
    private long seed;
    private double width;

    public boolean isOnTrack(Vector2D pos) {
        return false;
    }

    public Checkpoint nextCheckpoint(Vehicle v) {
        return null;
    }

    public Vector2D getStartPosition() {
        return null;
    }

    public double getLength() {
        return 0;
    }
}
