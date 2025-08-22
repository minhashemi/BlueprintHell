package dev.aminhashemi.blueprinthell.model;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.systems.PortType;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.utils.Logger;

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

    // Phase 2: Enhanced movement behavior properties
    private float currentSpeed;
    private float acceleration;
    private boolean isReturningToSource;
    private Point sourcePosition;
    private int collisionCount;
    
    public MovingPacket(Packet packet, Wire wire) {
        this.packet = packet;
        this.wire = wire;
        this.path = wire.getAllPoints();
        this.currentSegmentIndex = 0;
        this.progressOnSegment = 0.0;
        
        // Initialize Phase 2 properties
        this.currentSpeed = calculateBaseSpeed();
        this.acceleration = 0.0f;
        this.isReturningToSource = false;
        this.sourcePosition = new Point(packet.getX(), packet.getY());
        this.collisionCount = 0;
        
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
    
    /**
     * Calculates the base speed based on packet type and port compatibility
     */
    private float calculateBaseSpeed() {
        float baseSpeed = 1.0f; // Default speed
        
        switch (packet.getType()) {
            case GREEN_DIAMOND_SMALL:
                baseSpeed = 0.5f; // Half speed from incompatible ports
                break;
            case GREEN_DIAMOND_LARGE:
                baseSpeed = 1.0f; // Constant speed
                break;
            case INFINITY_SYMBOL:
                baseSpeed = 1.0f; // Constant acceleration base
                break;
            case PADLOCK_ICON:
                baseSpeed = 0.8f; // Slightly slower due to size
                break;
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                baseSpeed = 1.0f; // Constant speed
                break;
            default:
                baseSpeed = 1.0f; // Phase 1 packets
        }
        
        return baseSpeed;
    }
    
    /**
     * Applies port compatibility effects on movement speed
     */
    public void applyPortCompatibilityEffect(PortType portType, boolean isCompatible) {
        switch (packet.getType()) {
            case GREEN_DIAMOND_SMALL:
                if (!isCompatible) {
                    currentSpeed = 0.5f; // Half speed from incompatible ports
                } else {
                    currentSpeed = 1.0f; // Normal speed from compatible ports
                }
                break;
                
            case GREEN_DIAMOND_LARGE:
                if (!isCompatible) {
                    currentSpeed = 1.5f; // Accelerated through incompatible ports
                } else {
                    currentSpeed = 1.0f; // Constant speed from compatible ports
                }
                break;
                
            case INFINITY_SYMBOL:
                if (!isCompatible) {
                    acceleration = -0.1f; // Deceleration through incompatible ports
                } else {
                    acceleration = 0.1f; // Constant acceleration from compatible ports
                }
                break;
                
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                // Check if passing by malicious, spy, or VPN systems
                if (portType == PortType.MALICIOUS || portType == PortType.SPY || portType == PortType.VPN) {
                    currentSpeed = Math.max(currentSpeed * 0.5f, 0.3f); // Slow down to avoid detection
                }
                break;
        }
    }
    
    /**
     * Handles collision behavior based on packet type
     */
    public void handleCollision() {
        collisionCount++;
        
        switch (packet.getType()) {
            case INFINITY_SYMBOL:
                // Return to source system on collision
                isReturningToSource = true;
                // Reverse the path to return to source
                reversePath();
                break;
                
            case PADLOCK_ICON:
                // Protected packets are more resilient to collisions
                increaseNoise(5.0f); // Very low noise increase for resilience
                break;
                
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                // Confidential packets are sensitive but not instantly destroyed
                increaseNoise(8.0f); // Moderate noise increase
                break;
                
            default:
                // Standard collision behavior - much lower noise
                increaseNoise(6.0f); // Reduced from 30.0f to 6.0f
        }
    }
    
    /**
     * Reverses the path to return to source (for INFINITY_SYMBOL packets)
     */
    private void reversePath() {
        // Implementation for path reversal
        // This would require more complex path management
        Logger.getInstance().info("INFINITY_SYMBOL packet returning to source after collision");
    }
    
    /**
     * Updates the packet's speed based on acceleration
     */
    private void updateSpeed() {
        if (packet.getType() == PacketType.INFINITY_SYMBOL) {
            currentSpeed += acceleration;
            currentSpeed = Math.max(0.1f, Math.min(2.0f, currentSpeed)); // Clamp speed
        }
    }
    
    public void increaseNoise(float amount) {
        this.noiseLevel += amount;
        // If noise gets too high (50%), packet is lost
        if (noiseLevel > 50.0f) {
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
