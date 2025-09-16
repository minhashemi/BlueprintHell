package dev.aminhashemi.blueprinthell.model.movement;

import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.world.Wire;

import java.awt.Point;
import java.util.List;

/**
 * Handles packet movement along wires.
 * Single responsibility: managing packet position and progress.
 */
public class PacketMovement {
    
    private final Packet packet;
    private final Wire wire;
    private final List<Point> path;
    private int currentSegmentIndex;
    private double progressOnSegment;
    private boolean hasArrived = false;
    
    // Movement properties
    private float currentSpeed;
    private float acceleration;
    private boolean isOnCurve = false;
    private int wirePassCount = 0;
    
    public PacketMovement(Packet packet, Wire wire) {
        this.packet = packet;
        this.wire = wire;
        this.path = wire.getAllPoints();
        this.currentSegmentIndex = 0;
        this.progressOnSegment = 0.0;
        this.currentSpeed = calculateBaseSpeed();
        this.acceleration = 0.0f;
        
        if (!path.isEmpty()) {
            packet.setPosition(path.get(0).x, path.get(0).y);
        }
    }
    
    /**
     * Updates the packet's position along the wire.
     */
    public void updatePosition() {
        if (hasArrived || path.size() < 2) {
            return;
        }
        
        // Move along current segment
        progressOnSegment += currentSpeed * 0.016; // Assuming 60 FPS
        
        // Check if we've reached the end of current segment
        if (progressOnSegment >= 1.0) {
            progressOnSegment = 0.0;
            currentSegmentIndex++;
            
            // Check if we've reached the end of the path
            if (currentSegmentIndex >= path.size() - 1) {
                hasArrived = true;
                packet.setPosition(path.get(path.size() - 1).x, path.get(path.size() - 1).y);
                return;
            }
        }
        
        // Update packet position
        updatePositionFromProgress();
    }
    
    /**
     * Updates packet position based on current progress.
     */
    private void updatePositionFromProgress() {
        if (path.size() < 2) return;
        
        int segmentIndex = Math.min(currentSegmentIndex, path.size() - 2);
        Point start = path.get(segmentIndex);
        Point end = path.get(segmentIndex + 1);
        
        double x = start.x + (end.x - start.x) * progressOnSegment;
        double y = start.y + (end.y - start.y) * progressOnSegment;
        
        packet.setPosition((int) x, (int) y);
    }
    
    /**
     * Calculates the base speed for this packet type.
     */
    private float calculateBaseSpeed() {
        return switch (packet.getType()) {
            case SQUARE_MESSENGER, TRIANGLE_MESSENGER -> 2.0f;
            case GREEN_DIAMOND_SMALL, GREEN_DIAMOND_LARGE -> 1.5f;
            case INFINITY_SYMBOL -> 3.0f;
            case BULK_PACKET_SMALL, BULK_PACKET_LARGE -> 0.8f;
            case PADLOCK_ICON -> 1.2f;
            case CAMOUFLAGE_ICON_SMALL, CAMOUFLAGE_ICON_LARGE -> 1.0f;
            default -> 1.0f;
        };
    }
    
    // Getters and setters
    public boolean hasArrived() {
        return hasArrived;
    }
    
    public double getProgress() {
        if (path.size() <= 1) return 0.0;
        return (currentSegmentIndex + progressOnSegment) / (path.size() - 1);
    }
    
    public void setProgress(double progress) {
        if (path.size() <= 1) return;
        double totalProgress = progress * (path.size() - 1);
        currentSegmentIndex = (int) totalProgress;
        progressOnSegment = totalProgress - currentSegmentIndex;
        updatePositionFromProgress();
    }
    
    public double getCurrentSpeed() {
        return currentSpeed;
    }
    
    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = (float) currentSpeed;
    }
    
    public double getAcceleration() {
        return acceleration;
    }
    
    public void setAcceleration(double acceleration) {
        this.acceleration = (float) acceleration;
    }
    
    public boolean isOnCurve() {
        return isOnCurve;
    }
    
    public void setOnCurve(boolean isOnCurve) {
        this.isOnCurve = isOnCurve;
    }
    
    public int getWirePassCount() {
        return wirePassCount;
    }
    
    public void setWirePassCount(int wirePassCount) {
        this.wirePassCount = wirePassCount;
    }
    
    public void incrementWirePassCount() {
        this.wirePassCount++;
    }
}
