package dev.aminhashemi.blueprinthell.model.world;

import dev.aminhashemi.blueprinthell.model.MovingPacket;
import java.awt.Point;

/**
 * Represents a collision between two MovingPackets.
 * Tracks the collision point and manages the impact state.
 */
public class Impact {
    private final MovingPacket packet1;
    private final MovingPacket packet2;
    private final Point collisionPoint;
    private boolean disabled;
    private final long creationTime;

    public Impact(MovingPacket packet1, MovingPacket packet2, Point collisionPoint) {
        this.packet1 = packet1;
        this.packet2 = packet2;
        this.collisionPoint = collisionPoint;
        this.disabled = false;
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * Checks if this impact involves the specified packets.
     */
    public boolean contains(MovingPacket p1, MovingPacket p2) {
        return ((packet1 == p1 && packet2 == p2) || (packet1 == p2 && packet2 == p1));
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public Point getCollisionPoint() {
        return collisionPoint;
    }

    public MovingPacket getPacket1() {
        return packet1;
    }

    public MovingPacket getPacket2() {
        return packet2;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
