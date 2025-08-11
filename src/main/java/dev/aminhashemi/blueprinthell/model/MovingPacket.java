package dev.aminhashemi.blueprinthell.model;

import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.world.Wire;

import java.awt.*;
import java.util.List;

/**
 * Represents a packet in transit along a wire.
 * This class handles the logic for moving the packet along the wire's path.
 */
public class MovingPacket {

    private final Packet packet;
    private final Wire wire;
    private final List<Point> path;
    private int currentSegmentIndex;
    private double progressOnSegment; // A value from 0.0 to 1.0

    private static final double SPEED = 2.0; // Pixels per update

    public MovingPacket(Packet packet, Wire wire) {
        this.packet = packet;
        this.wire = wire;
        this.path = wire.getAllPoints();
        this.currentSegmentIndex = 0;
        this.progressOnSegment = 0.0;
        // Set the packet's initial position to the start of the wire
        packet.setPosition(path.get(0).x, path.get(0).y);
    }

    public void update() {
        if (currentSegmentIndex >= path.size() - 1) {
            // Packet has reached the end of the wire
            return;
        }

        Point start = path.get(currentSegmentIndex);
        Point end = path.get(currentSegmentIndex + 1);
        double segmentLength = start.distance(end);

        // Calculate how much progress to make this frame
        double progressThisUpdate = SPEED / segmentLength;
        progressOnSegment += progressThisUpdate;

        if (progressOnSegment >= 1.0) {
            // Move to the next segment
            currentSegmentIndex++;
            progressOnSegment = 0.0; // Reset progress
            if (currentSegmentIndex >= path.size() - 1) {
                // Arrived at the final destination
                packet.setPosition(end.x - packet.getWidth() / 2, end.y - packet.getHeight() / 2);
                return;
            }
        }

        // Interpolate the position on the current segment
        int currentX = (int) (start.x + (end.x - start.x) * progressOnSegment);
        int currentY = (int) (start.y + (end.y - start.y) * progressOnSegment);
        packet.setPosition(currentX - packet.getWidth() / 2, currentY - packet.getHeight() / 2);
    }

    public void draw(Graphics2D g) {
        packet.draw(g);
    }

    public boolean hasArrived() {
        return currentSegmentIndex >= path.size() - 1;
    }
}
