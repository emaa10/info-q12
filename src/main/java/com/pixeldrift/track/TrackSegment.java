package com.pixeldrift.track;

import com.pixeldrift.physics.Vector2D;

public class TrackSegment {
    private Vector2D start, end;
    private SegmentType type;

    public boolean contains(Vector2D pos) {
        return false;
    }
}
