package com.pixeldrift.track;

import com.pixeldrift.physics.Vector2D;
import com.pixeldrift.entities.Vehicle;

public class Checkpoint {
    private Vector2D position;
    private int index;
    private boolean isFinish;

    public boolean isPassed(Vehicle v) {
        return false;
    }
}
