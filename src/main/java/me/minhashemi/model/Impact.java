package me.minhashemi.model;

import java.awt.Point;

public class Impact {
    public MovingPacket packet1;
    public MovingPacket packet2;
    public Point point;
    private boolean disabled;

    public Impact(MovingPacket packet1, MovingPacket packet2, Point point) {
        this.packet1 = packet1;
        this.packet2 = packet2;
        this.point = point;
        this.disabled = false;
    }

    public boolean contains(MovingPacket packet11, MovingPacket packet22) {
        return ((packet1 == packet11 && packet2 == packet22) || (packet1 == packet22 && packet2 == packet11));
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
