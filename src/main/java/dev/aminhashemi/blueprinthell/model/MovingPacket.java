package dev.aminhashemi.blueprinthell.model;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.systems.PortType;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ConfidentialPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.BulkPacket;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a packet that is currently moving along a wire.
 * Handles movement, collision effects, and packet-specific behaviors.
 */
public class MovingPacket {

    // ==================== CORE PROPERTIES ====================
    private final Packet packet;              // The packet being moved
    private final Wire wire;                  // The wire the packet is traveling on
    private final List<Point> path;           // Complete path from start to end
    private int currentSegmentIndex;          // Current segment being traversed
    private double progressOnSegment;         // Progress within current segment (0.0 to 1.0)
    private boolean hasArrived = false;       // Whether packet has reached its destination
    
    // ==================== IMPACT SYSTEM ====================
    private float noiseLevel = 0.0f;          // Current noise level (affects packet health)
    private boolean isLost = false;           // Whether packet is lost due to high noise

    // ==================== MOVEMENT BEHAVIOR ====================
    private float currentSpeed;               // Current movement speed
    private float acceleration;               // Current acceleration/deceleration
    private boolean spawnProtection = false;  // Prevents immediate destruction after spawning
    private long spawnTime;                   // Timestamp when packet was created
    private boolean playerSpawned = false;    // Whether packet was spawned by player (space key)
    private boolean testPacketReturned = false; // Whether this packet has been counted as returned in test
    
    /**
     * Constructs a moving packet with the specified packet and wire.
     * @param packet The packet to move
     * @param wire The wire to move along
     */
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
        double effectiveSpeed = calculateEffectiveSpeed(engine);
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
            case SQUARE_MESSENGER:
                // Size 2: Half speed from compatible ports, 2x speed from incompatible ports
                if (isCompatible) {
                    currentSpeed = 0.5f; // Half speed from compatible ports
                    acceleration = 0.0f; // No acceleration
                } else {
                    currentSpeed = 2.0f; // 2x speed from incompatible ports
                    acceleration = 0.0f; // No acceleration
                }
                break;
                
            case TRIANGLE_MESSENGER:
                // Size 3: Constant speed from compatible ports, 2x speed + acceleration through incompatible ports
                if (isCompatible) {
                    currentSpeed = 1.0f; // Constant speed from compatible ports
                    acceleration = 0.0f; // No acceleration
                } else {
                    currentSpeed = 2.0f; // 2x speed from incompatible ports
                    acceleration = 0.2f; // Accelerate through incompatible ports
                }
                break;
                
            case GREEN_DIAMOND_SMALL:
                // Size 2: Half speed from compatible ports, normal speed from incompatible ports
                if (isCompatible) {
                    currentSpeed = 0.5f; // Half speed from compatible ports
                    acceleration = 0.0f; // No acceleration
                } else {
                    currentSpeed = 1.0f; // Normal speed from incompatible ports
                    acceleration = 0.0f; // No acceleration
                }
                break;
                
            case GREEN_DIAMOND_LARGE:
                // Size 3: Constant speed from compatible ports, accelerate through incompatible ports
                if (isCompatible) {
                    currentSpeed = 1.0f; // Constant speed from compatible ports
                    acceleration = 0.0f; // No acceleration
                } else {
                    currentSpeed = 1.0f; // Start at normal speed
                    acceleration = 0.2f; // Accelerate through incompatible ports
                }
                break;
                
            case INFINITY_SYMBOL:
                // Size 1: Constant acceleration from compatible ports, deceleration from incompatible ports
                // Special: 2x speed when entering from incompatible port
                if (isCompatible) {
                    currentSpeed = 1.0f; // Start at normal speed
                    acceleration = 0.1f; // Constant acceleration from compatible ports
                } else {
                    currentSpeed = 2.0f; // 2x speed from incompatible ports
                    acceleration = -0.1f; // Deceleration through incompatible ports
                }
                break;
                
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                // Confidential packets: no port compatibility effects - they don't have corresponding ports
                // Speed is managed by the ConfidentialPacket's own update logic
                // Do not modify currentSpeed or acceleration here
                break;
                
            case PADLOCK_ICON:
                // Protected packets: random movement behavior from messenger packet types
                if (packet instanceof ProtectedPacket) {
                    // Randomly choose movement behavior from one of the messenger packet types
                    PacketType[] messengerTypes = {PacketType.SQUARE_MESSENGER, PacketType.TRIANGLE_MESSENGER, PacketType.INFINITY_SYMBOL};
                    PacketType randomMessengerType = messengerTypes[(int)(Math.random() * messengerTypes.length)];
                    
                    // Apply the random messenger packet behavior
                    switch (randomMessengerType) {
                        case SQUARE_MESSENGER:
                            // Half speed from compatible ports, 2x speed from incompatible ports
                            if (isCompatible) {
                                currentSpeed = 0.5f;
                                acceleration = 0.0f;
                            } else {
                                currentSpeed = 2.0f;
                                acceleration = 0.0f;
                            }
                            break;
                        case TRIANGLE_MESSENGER:
                            // Constant speed from compatible ports, 2x speed + acceleration from incompatible ports
                            if (isCompatible) {
                                currentSpeed = 1.0f;
                                acceleration = 0.0f;
                            } else {
                                currentSpeed = 2.0f;
                                acceleration = 0.2f;
                            }
                            break;
                        case INFINITY_SYMBOL:
                            // Constant acceleration from compatible ports, 2x speed + deceleration from incompatible ports
                            if (isCompatible) {
                                currentSpeed = 1.0f;
                                acceleration = 0.1f;
                            } else {
                                currentSpeed = 2.0f;
                                acceleration = -0.1f;
                            }
                            break;
                        default:
                            // Fallback to normal speed
                            currentSpeed = 1.0f;
                            acceleration = 0.0f;
                            break;
                    }
                } else {
                    // Fallback for non-ProtectedPacket PADLOCK_ICON packets
                    currentSpeed = 0.8f + (float)(Math.random() * 0.4f);
                    acceleration = 0.0f;
                }
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
                // Return to source system on collision and try again
                Logger.getInstance().info("INFINITY_SYMBOL packet collided - returning to source system");
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
        // Reverse the path by swapping start and end points
        if (path.size() >= 2) {
            // Create a new reversed path
            List<Point> reversedPath = new ArrayList<>();
            for (int i = path.size() - 1; i >= 0; i--) {
                reversedPath.add(path.get(i));
            }
            
            // Update the path and reset position
            path.clear();
            path.addAll(reversedPath);
            currentSegmentIndex = 0;
            progressOnSegment = 0.0;
            
            // Set position to the new start (original end)
            if (!path.isEmpty()) {
                packet.setPosition(path.get(0).x, path.get(0).y);
            }
            
            Logger.getInstance().info("INFINITY_SYMBOL packet path reversed - returning to source system");
        }
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
    private double calculateEffectiveSpeed(GameEngine engine) {
        double baseSpeed = currentSpeed * 2.0; // Convert to pixels per update
        
        // Apply global speed multiplier from shop upgrades
        baseSpeed *= engine.getPacketSpeedMultiplier();
        
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
            
            // Apply deviation effect based on impact intensity
            // Strong impacts can push packets off their wire path
            if (waveIntensity > 5.0f) { // Threshold for deviation
                applyDeviationEffect(impactPoint, waveIntensity);
            }
        }
    }
    
    /**
     * Applies deviation effect to push packet away from wire path
     * This implements the impact wave deviation from the documentation
     */
    private void applyDeviationEffect(Point impactPoint, float intensity) {
        // Calculate direction away from impact point
        Point packetPos = packet.getPosition();
        double dx = packetPos.x - impactPoint.x;
        double dy = packetPos.y - impactPoint.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        if (distance > 0) {
            // Normalize direction vector
            dx /= distance;
            dy /= distance;
            
            // Apply deviation based on intensity
            float deviationAmount = Math.min(intensity * 0.5f, 10.0f); // Cap at 10 pixels
            
            // Add deviation to packet's position
            packet.setPosition(packet.getX() + (int)(dx * deviationAmount), 
                             packet.getY() + (int)(dy * deviationAmount));
            
            // Add some random deviation to make it more realistic
            double randomAngle = Math.random() * Math.PI * 2;
            double randomDeviation = Math.random() * deviationAmount * 0.3;
            packet.setPosition(packet.getX() + (int)(Math.cos(randomAngle) * randomDeviation),
                             packet.getY() + (int)(Math.sin(randomAngle) * randomDeviation));
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
    
    /**
     * Checks if this packet was spawned by the player (space key)
     */
    public boolean isPlayerSpawned() {
        return playerSpawned;
    }
    
    /**
     * Sets whether this packet was spawned by the player (space key)
     */
    public void setPlayerSpawned(boolean playerSpawned) {
        this.playerSpawned = playerSpawned;
    }
    
    /**
     * Gets whether this packet has been counted as returned in test
     */
    public boolean isTestPacketReturned() {
        return testPacketReturned;
    }
    
    /**
     * Sets whether this packet has been counted as returned in test
     */
    public void setTestPacketReturned(boolean testPacketReturned) {
        this.testPacketReturned = testPacketReturned;
    }
}
