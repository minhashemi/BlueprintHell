package dev.aminhashemi.blueprinthell.model;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.systems.PortType;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ConfidentialPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.BulkPacket;
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

    // Enhanced movement behavior
    private float currentSpeed;
    private float acceleration;
    private boolean spawnProtection = false; // Prevents immediate destruction
    private long spawnTime; // Spawn timestamp
    
    public MovingPacket(Packet packet, Wire wire) {
        this.packet = packet;
        this.wire = wire;
        this.path = wire.getAllPoints();
        this.currentSegmentIndex = 0;
        this.progressOnSegment = 0.0;
        
        // Initialize movement properties
        this.currentSpeed = calculateBaseSpeed();
        this.acceleration = 0.0f;
        this.spawnTime = java.lang.System.currentTimeMillis();
        
        if (!path.isEmpty()) {
            packet.setPosition(path.get(0).x, path.get(0).y);
        }
    }

    public void update(GameEngine engine) {
        if (hasArrived || path.size() < 2) {
            return;
        }

        // Update packet-specific behavior first
        packet.update(engine);
        
        // Update speed based on packet type and current conditions
        updateSpeed();
        
        // Check if packet is on a curve (has arc points)
        boolean isOnCurve = isOnCurvedSegment();
        
        // Apply curve-specific behavior for bulk packets
        if (packet instanceof BulkPacket) {
            ((BulkPacket) packet).setOnCurve(isOnCurve);
        }

        Point start = path.get(currentSegmentIndex);
        Point end = path.get(currentSegmentIndex + 1);
        double segmentLength = start.distance(end);
        if (segmentLength == 0) segmentLength = 1; // Avoid division by zero

        // Calculate movement based on current speed and packet type
        double effectiveSpeed = calculateEffectiveSpeed();
        double progressThisUpdate = effectiveSpeed / segmentLength;
        progressOnSegment += progressThisUpdate;

        if (progressOnSegment >= 1.0) {
            currentSegmentIndex++;
            if (currentSegmentIndex >= path.size() - 1) {
                // Packet arrived at destination
                hasArrived = true;
                packet.setPosition(end.x - packet.getWidth() / 2, end.y - packet.getHeight() / 2);
                Logger.getInstance().info("Packet " + packet.getType() + " arrived at destination system");
                engine.handlePacketArrival(this);
                return;
            }
            progressOnSegment = 0.0; // Reset for next segment
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
    
    /**
     * Gets the source system where this packet originated
     */
    public System getSourceSystem() {
        return wire.getStartPort().getParentSystem();
    }
    
    /**
     * Sets spawn protection to prevent immediate destruction
     */
    public void setSpawnProtection(boolean protection) {
        this.spawnProtection = protection;
        if (protection) {
            this.spawnTime = java.lang.System.currentTimeMillis();
        }
    }
    
    /**
     * Checks if spawn protection is active
     */
    public boolean hasSpawnProtection() {
        if (!spawnProtection) return false;
        
        // Spawn protection lasts for 2 seconds
        long currentTime = java.lang.System.currentTimeMillis();
        if (currentTime - spawnTime > 2000) {
            spawnProtection = false; // Expire spawn protection
            return false;
        }
        return true;
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
                    acceleration = 0.0f; // No acceleration
                } else {
                    currentSpeed = 1.0f; // Normal speed from compatible ports
                    acceleration = 0.0f; // No acceleration
                }
                break;
                
            case GREEN_DIAMOND_LARGE:
                if (!isCompatible) {
                    currentSpeed = 1.0f; // Start at normal speed
                    acceleration = 0.2f; // Accelerate through incompatible ports
                } else {
                    currentSpeed = 1.0f; // Constant speed from compatible ports
                    acceleration = 0.0f; // No acceleration
                }
                break;
                
            case INFINITY_SYMBOL:
                if (!isCompatible) {
                    currentSpeed = 1.0f; // Start at normal speed
                    acceleration = -0.1f; // Deceleration through incompatible ports
                } else {
                    currentSpeed = 1.0f; // Start at normal speed
                    acceleration = 0.1f; // Constant acceleration from compatible ports
                }
                break;
                
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                // Confidential packets slow down near malicious systems
                if (portType == PortType.MALICIOUS || portType == PortType.SPY) {
                    currentSpeed = 0.5f; // Slow down to avoid detection
                    acceleration = 0.0f;
                } else {
                    currentSpeed = 1.0f; // Normal speed
                    acceleration = 0.0f;
                }
                break;
                
            case PADLOCK_ICON:
                // Protected packets maintain normal speed but with random variation
                currentSpeed = 1.0f;
                acceleration = 0.0f;
                break;
                
            default:
                // Other packets maintain normal speed
                currentSpeed = 1.0f;
                acceleration = 0.0f;
                break;
        }
        
        Logger.getInstance().debug("Applied port compatibility: " + packet.getType() + 
                                 " compatible=" + isCompatible + 
                                 " speed=" + currentSpeed + 
                                 " acceleration=" + acceleration);
    }
    
    /**
     * Handles collision behavior based on packet type
     */
    public void handleCollision() {
        switch (packet.getType()) {
            case INFINITY_SYMBOL:
                // Return to source system on collision
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
        // Requires complex path management
        Logger.getInstance().info("INFINITY_SYMBOL packet returning to source after collision");
    }
    
    /**
     * Updates the packet's speed based on acceleration and packet type
     */
    private void updateSpeed() {
        switch (packet.getType()) {
            case INFINITY_SYMBOL:
                // Infinity packets have constant acceleration/deceleration
                currentSpeed += acceleration;
                currentSpeed = Math.max(0.1f, Math.min(2.0f, currentSpeed)); // Clamp speed
                break;
                
            case GREEN_DIAMOND_LARGE:
                // Large diamond packets accelerate through incompatible ports
                if (acceleration > 0) {
                    currentSpeed += acceleration * 0.1f; // Gradual acceleration
                    currentSpeed = Math.min(2.5f, currentSpeed); // Cap at 2.5x speed
                }
                break;
                
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                // Confidential packets adjust speed based on nearby packets
                if (packet instanceof ConfidentialPacket) {
                    ConfidentialPacket confPacket = (ConfidentialPacket) packet;
                    currentSpeed = confPacket.getCurrentSpeed();
                }
                break;
                
            case PADLOCK_ICON:
                // Protected packets have random movement patterns
                if (packet instanceof ProtectedPacket) {
                    // Speed varies randomly for protected packets
                    currentSpeed = 0.8f + (float)(Math.random() * 0.4f); // 0.8 to 1.2
                }
                break;
                
            default:
                // Other packets maintain their base speed
                break;
        }
    }
    
    /**
     * Calculates the effective speed for movement based on packet type and conditions
     */
    private double calculateEffectiveSpeed() {
        double baseSpeed = currentSpeed * 2.0; // Convert to pixels per update
        
        // Apply packet-specific speed modifications
        if (packet instanceof BulkPacket) {
            BulkPacket bulkPacket = (BulkPacket) packet;
            baseSpeed *= bulkPacket.getCurrentSpeed();
        } else if (packet instanceof ConfidentialPacket) {
            ConfidentialPacket confPacket = (ConfidentialPacket) packet;
            baseSpeed *= confPacket.getCurrentSpeed();
        }
        
        return baseSpeed;
    }
    
    /**
     * Checks if the current segment is curved (has arc points)
     */
    private boolean isOnCurvedSegment() {
        // Check if the wire has arc points and we're between them
        List<Point> arcPoints = wire.getArcPoints().stream()
            .map(arc -> arc.getPosition())
            .collect(java.util.stream.Collectors.toList());
            
        if (arcPoints.isEmpty()) {
            return false; // No curves
        }
        
        // Check if current segment involves an arc point
        if (currentSegmentIndex < path.size() - 1) {
            Point currentPoint = path.get(currentSegmentIndex);
            Point nextPoint = path.get(currentSegmentIndex + 1);
            
            // If either point is an arc point, we're on a curve
            return arcPoints.contains(currentPoint) || arcPoints.contains(nextPoint);
        }
        
        return false;
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
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    private System sourceSystem;
    private System destinationSystem;
    private boolean isOnCurve = false;
    private int wirePassCount = 0;
    private long spawnProtectionEndTime = 0;
    
    /**
     * Constructor for save system
     */
    public MovingPacket(Packet packet, System sourceSystem, System destinationSystem, Wire wire) {
        this.packet = packet;
        this.wire = wire;
        this.sourceSystem = sourceSystem;
        this.destinationSystem = destinationSystem;
        this.path = wire.getAllPoints();
        this.currentSegmentIndex = 0;
        this.progressOnSegment = 0.0;
        
        // Initialize movement properties
        this.currentSpeed = calculateBaseSpeed();
        this.acceleration = 0.0f;
        this.spawnTime = java.lang.System.currentTimeMillis();
        
        if (!path.isEmpty()) {
            packet.setPosition(path.get(0).x, path.get(0).y);
        }
    }
    
    
    /**
     * Gets the progress on the wire
     */
    public double getProgress() {
        if (path.size() <= 1) return 0.0;
        return (currentSegmentIndex + progressOnSegment) / (path.size() - 1);
    }
    
    /**
     * Sets the progress on the wire
     */
    public void setProgress(double progress) {
        if (path.size() <= 1) return;
        double totalProgress = progress * (path.size() - 1);
        currentSegmentIndex = (int) totalProgress;
        progressOnSegment = totalProgress - currentSegmentIndex;
        
        if (currentSegmentIndex >= path.size() - 1) {
            currentSegmentIndex = path.size() - 2;
            progressOnSegment = 1.0;
        }
    }
    
    /**
     * Updates the packet's visual position based on current progress
     * This is used when restoring from snapshots
     */
    public void updatePositionFromProgress() {
        if (path.size() < 2) return;
        
        if (currentSegmentIndex >= path.size() - 1) {
            // Packet has arrived at destination
            Point end = path.get(path.size() - 1);
            packet.setPosition(end.x - packet.getWidth() / 2, end.y - packet.getHeight() / 2);
            hasArrived = true;
        } else {
            // Interpolate position on current segment
            Point start = path.get(currentSegmentIndex);
            Point end = path.get(currentSegmentIndex + 1);
            int currentX = (int) (start.x + (end.x - start.x) * progressOnSegment);
            int currentY = (int) (start.y + (end.y - start.y) * progressOnSegment);
            packet.setPosition(currentX - packet.getWidth() / 2, currentY - packet.getHeight() / 2);
        }
    }
    
    /**
     * Gets the spawn protection end time
     */
    public long getSpawnProtectionEndTime() {
        return spawnProtectionEndTime;
    }
    
    /**
     * Gets the current speed
     */
    public double getCurrentSpeed() {
        return currentSpeed;
    }
    
    /**
     * Sets the current speed
     */
    public void setCurrentSpeed(double currentSpeed) {
        this.currentSpeed = (float) currentSpeed;
    }
    
    /**
     * Gets the acceleration
     */
    public double getAcceleration() {
        return acceleration;
    }
    
    /**
     * Sets the acceleration
     */
    public void setAcceleration(double acceleration) {
        this.acceleration = (float) acceleration;
    }
    
    /**
     * Checks if on curve
     */
    public boolean isOnCurve() {
        return isOnCurve;
    }
    
    /**
     * Sets if on curve
     */
    public void setOnCurve(boolean isOnCurve) {
        this.isOnCurve = isOnCurve;
    }
    
    /**
     * Gets wire pass count
     */
    public int getWirePassCount() {
        return wirePassCount;
    }
    
    /**
     * Sets wire pass count
     */
    public void setWirePassCount(int wirePassCount) {
        this.wirePassCount = wirePassCount;
    }
    
    /**
     * Sets spawn protection with end time
     */
    public void setSpawnProtection(boolean hasSpawnProtection, long spawnProtectionEndTime) {
        this.spawnProtection = hasSpawnProtection;
        this.spawnProtectionEndTime = spawnProtectionEndTime;
    }
}
