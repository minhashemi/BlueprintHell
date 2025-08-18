package dev.aminhashemi.blueprinthell.model;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.Wire;

import java.awt.*;
import java.util.List;

public class MovingPacket {

    private final Packet packet;
    private final Wire wire;
    private final List<Point> path;
    private int currentSegmentIndex;
    private double progressOnSegment;
    private boolean hasArrived = false;
    
    // Impact system properties
    private float noiseLevel = 0.0f;
    private boolean isLost = false;

    private static final double SPEED = 2.0; // Pixels per update tick

    public MovingPacket(Packet packet, Wire wire) {
        this.packet = packet;
        this.wire = wire;
        this.path = wire.getAllPoints();
        this.currentSegmentIndex = 0;
        this.progressOnSegment = 0.0;
        if (!path.isEmpty()) {
            packet.setPosition(path.get(0).x, path.get(0).y);
        }
    }

    public void update(GameEngine engine) {
        if (hasArrived || path.size() < 2) {
            return;
        }

        Point start = path.get(currentSegmentIndex);
        Point end = path.get(currentSegmentIndex + 1);
        double segmentLength = start.distance(end);
        if (segmentLength == 0) segmentLength = 1; // Avoid division by zero

        double progressThisUpdate = SPEED / segmentLength;
        progressOnSegment += progressThisUpdate;

        if (progressOnSegment >= 1.0) {
            currentSegmentIndex++;
            if (currentSegmentIndex >= path.size() - 1) {
                // Arrived at the final destination
                hasArrived = true;
                packet.setPosition(end.x - packet.getWidth() / 2, end.y - packet.getHeight() / 2);
                // Notify the engine of arrival
                engine.handlePacketArrival(this);
                return;
            }
            progressOnSegment = 0.0; // Reset progress for the new segment
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
        return hasArrived;
    }

    public Packet getPacket() {
        return packet;
    }

    public System getDestinationSystem() {
        return wire.getEndPort().getParentSystem();
    }
    
    // Impact system methods
    
    public Wire getWire() {
        return wire;
    }
    
    public void increaseNoise(float amount) {
        this.noiseLevel += amount;
        // If noise gets too high, packet is lost
        if (noiseLevel > 100.0f) {
            isLost = true;
        }
    }
    
    public void applyImpactEffect(Point impactPoint, float intensity) {
        // Calculate distance from impact
        Point packetPos = packet.getPosition();
        double distance = packetPos.distance(impactPoint);
        
        // Apply inverse square law for wave effects
        if (distance > 0) {
            float waveIntensity = intensity / (float)(distance * distance);
            increaseNoise(waveIntensity);
        }
    }
    
    public float getNoiseLevel() {
        return noiseLevel;
    }
    
    public void setNoiseLevel(float noiseLevel) {
        this.noiseLevel = noiseLevel;
    }
    
    public boolean isLost() {
        return isLost;
    }
    
    public void setLost(boolean lost) {
        this.isLost = lost;
    }
}
