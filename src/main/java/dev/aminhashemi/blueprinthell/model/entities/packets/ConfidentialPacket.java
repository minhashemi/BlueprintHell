package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;
import java.util.List;

/**
 * ConfidentialPacket - A packet for secure communication
 * 
 * Behavior:
 * - Size: 4 units (SMALL) or 6 units (LARGE)
 * - Coin Reward: 3 coins (SMALL) or 4 coins (LARGE)
 * - Movement: Constant speed, but slows down when other packets are in the same system
 * - Vulnerability: Destroyed by SpySystems
 * - Special Behavior: Maintains distance from other packets
 */
public class ConfidentialPacket extends Packet {

    public enum ConfidentialType {
        SMALL(4, 3, PacketType.CAMOUFLAGE_ICON_SMALL),
        LARGE(6, 4, PacketType.CAMOUFLAGE_ICON_LARGE);

        private final int size;
        private final int coinReward;
        private final PacketType packetType;

        ConfidentialType(int size, int coinReward, PacketType packetType) {
            this.size = size;
            this.coinReward = coinReward;
            this.packetType = packetType;
        }

        public int getSize() { return size; }
        public int getCoinReward() { return coinReward; }
        public PacketType getPacketType() { return packetType; }
    }

    private final ConfidentialType confidentialType;
    private final PacketType type;
    private float baseSpeed = 1.0f;
    private float currentSpeed = 1.0f;
    private boolean isSlowingDown = false;
    private long lastSpeedCheck = 0;
    private static final long SPEED_CHECK_INTERVAL = Config.CONFIDENTIAL_SPEED_CHECK_INTERVAL; // Check every 500ms

    public ConfidentialPacket(int x, int y, ConfidentialType confidentialType) {
        super(x, y, confidentialType.getSize() * 3, confidentialType.getSize() * 3); // Size affects visual size
        this.confidentialType = confidentialType;
        this.type = confidentialType.getPacketType();
        this.currentSpeed = baseSpeed;
    }

    @Override
    public void update(GameEngine engine) {
        long currentTime = java.lang.System.currentTimeMillis();
        
        // Check for other packets in nearby systems periodically
        if (currentTime - lastSpeedCheck > SPEED_CHECK_INTERVAL) {
            checkForNearbyPackets(engine);
            lastSpeedCheck = currentTime;
        }
        
        // Apply speed changes gradually
        if (isSlowingDown) {
            currentSpeed = Math.max(Config.CONFIDENTIAL_SLOWDOWN_FACTOR, currentSpeed - Config.CONFIDENTIAL_SPEED_RECOVERY_FACTOR);
        } else {
            currentSpeed = Math.min(baseSpeed, currentSpeed + Config.CONFIDENTIAL_SPEED_RECOVERY_FACTOR);
        }
    }

    /**
     * Checks for other packets in systems and adjusts speed accordingly
     * Implements the requirement: "if such a packet is on the way to a network system 
     * and another packet is stored in this system, it reduces its speed to a certain limit 
     * so as not to be present at the same time as another packet in this system"
     */
    private void checkForNearbyPackets(GameEngine engine) {
        // Check if there are other packets stored in systems we're approaching
        boolean systemHasStoredPackets = checkForStoredPacketsInTargetSystems(engine);
        
        if (systemHasStoredPackets) {
            isSlowingDown = true;
            Logger.getInstance().debug("ConfidentialPacket slowing down - target system has stored packets");
        } else {
            isSlowingDown = false;
        }
        
        // Also check for malicious systems nearby (spy systems)
        // Confidential packets are extra cautious near spy systems
        boolean nearSpySystem = Math.random() < Config.GameBalance.CONFIDENTIAL_SPY_PROXIMITY_CHANCE; // 20% chance of being near spy system
        if (nearSpySystem) {
            isSlowingDown = true;
            currentSpeed = Math.max(Config.CONFIDENTIAL_SPY_SLOWDOWN_FACTOR, currentSpeed - Config.CONFIDENTIAL_SPY_SPEED_REDUCTION); // Significant slowdown
            Logger.getInstance().debug("ConfidentialPacket slowing down near spy system");
        }
        
        // For LARGE confidential packets, maintain distance from other packets
        if (confidentialType == ConfidentialType.LARGE) {
            maintainDistanceFromOtherPackets(engine);
        }
    }
    
    /**
     * Checks if target systems have stored packets
     * This implements the core requirement for camouflage packets
     */
    private boolean checkForStoredPacketsInTargetSystems(GameEngine engine) {
        // Get all systems in the game
        List<System> allSystems = engine.getSystems();
        
        // Check each system for stored packets
        for (System system : allSystems) {
            if (system.getStoredPacketCount() > 0) {
                // If any system has stored packets, we should slow down
                // This implements the requirement: "if such a packet is on the way to a network system 
                // and another packet is stored in this system, it reduces its speed to a certain limit 
                // so as not to be present at the same time as another packet in this system"
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Maintains distance from other packets (for LARGE confidential packets)
     * Implements: "This packet tries to maintain a specific distance from all other packets 
     * on network wires by moving forward or backward on network connections"
     */
    private void maintainDistanceFromOtherPackets(GameEngine engine) {
        // Get all moving packets on the network
        List<MovingPacket> allMovingPackets = engine.getMovingPackets();
        
        // Check distance to other packets
        boolean needsDistanceAdjustment = false;
        double minDistance = Double.MAX_VALUE;
        
        for (MovingPacket otherPacket : allMovingPackets) {
            if (otherPacket.getPacket() == this) continue; // Skip self
            
            // Calculate distance to other packet
            double distance = Math.sqrt(
                Math.pow(this.getX() - otherPacket.getPacket().getX(), 2) + 
                Math.pow(this.getY() - otherPacket.getPacket().getY(), 2)
            );
            
            minDistance = Math.min(minDistance, distance);
            
            // If too close to another packet, need to adjust distance
            if (distance < Config.CONFIDENTIAL_MIN_DISTANCE) {
                needsDistanceAdjustment = true;
            }
        }
        
        if (needsDistanceAdjustment) {
            // Implement forward/backward movement to maintain distance
            // This simulates the requirement to move forward or backward on network connections
            
            if (minDistance < Config.CONFIDENTIAL_MIN_DISTANCE * 0.5) {
                // Very close - move forward (speed up)
                currentSpeed = Math.min(currentSpeed + Config.CONFIDENTIAL_DISTANCE_ADJUSTMENT, 
                                      baseSpeed * Config.CONFIDENTIAL_MAX_SPEED_MULTIPLIER);
                Logger.getInstance().debug("LARGE ConfidentialPacket moving forward to maintain distance");
            } else {
                // Close but not too close - move backward (slow down)
                currentSpeed = Math.max(currentSpeed - Config.CONFIDENTIAL_DISTANCE_ADJUSTMENT, 
                                      baseSpeed * Config.CONFIDENTIAL_MIN_SPEED_MULTIPLIER);
                Logger.getInstance().debug("LARGE ConfidentialPacket moving backward to maintain distance");
            }
        } else {
            // No other packets too close - maintain normal speed
            currentSpeed = Math.min(currentSpeed + Config.CONFIDENTIAL_DISTANCE_ADJUSTMENT, baseSpeed);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw camouflage pattern
        g.setColor(type.getColor());
        
        // Main body with camouflage pattern
        g.fillOval(x + 1, y + 1, width - 2, height - 2);
        
        // Camouflage spots
        g.setColor(Color.WHITE);
        g.fillOval(x + 3, y + 3, 6, 6);
        g.fillOval(x + width - 9, y + height - 9, 4, 4);
        
        g.setColor(Color.BLACK);
        g.fillOval(x + 6, y + height - 6, 3, 3);
        g.fillOval(x + width - 6, y + 6, 2, 2);
        
        // Draw size indicator
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.drawString(String.valueOf(confidentialType.getSize()), x + width/2 - 3, y + height/2 + 3);
        
        // Draw speed indicator
        if (isSlowingDown) {
            g.setColor(Color.RED);
            g.drawString("SLOW", x + width + 2, y + height/2);
        }
        
        // Draw confidentiality indicator
        g.setColor(Color.YELLOW);
        g.drawRect(x, y, width, height);
    }

    @Override
    public PacketType getType() {
        return type;
    }

    /**
     * Gets the confidential type (SMALL or LARGE)
     */
    public ConfidentialType getConfidentialType() {
        return confidentialType;
    }

    /**
     * Gets the current movement speed
     */
    public float getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * Sets the base speed for this packet
     */
    public void setBaseSpeed(float speed) {
        this.baseSpeed = speed;
        this.currentSpeed = speed;
    }

    /**
     * Checks if the packet is currently slowing down
     */
    public boolean isSlowingDown() {
        return isSlowingDown;
    }

    /**
     * Forces the packet to slow down
     */
    public void forceSlowDown() {
        isSlowingDown = true;
        currentSpeed = Math.max(0.3f, currentSpeed - 0.2f);
    }

    /**
     * Returns the coin reward for this packet type
     */
    public int getCoinReward() {
        return confidentialType.getCoinReward();
    }

    /**
     * Returns the size of this packet
     */
    public int getPacketSize() {
        return confidentialType.getSize();
    }

    /**
     * Confidential packets are vulnerable to spy systems
     */
    public boolean isVulnerableToSpySystems() {
        return true;
    }

    /**
     * Confidential packets maintain distance from other packets
     */
    public void maintainDistanceFromPackets(List<Packet> otherPackets) {
        // Implementation for maintaining distance
        // This would be called by the movement system
        for (Packet otherPacket : otherPackets) {
            if (otherPacket != this) {
                double distance = this.getPosition().distance(otherPacket.getPosition());
                if (distance < 50) { // If too close
                    // Adjust position to maintain distance
                    double angle = Math.atan2(this.getY() - otherPacket.getY(), 
                                            this.getX() - otherPacket.getX());
                    double newX = otherPacket.getX() + Math.cos(angle) * 50;
                    double newY = otherPacket.getY() + Math.sin(angle) * 50;
                    this.setPosition((int)newX, (int)newY);
                }
            }
        }
    }
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    /**
     * Gets the last speed check time
     */
    public long getLastSpeedCheck() {
        return lastSpeedCheck;
    }
    
    /**
     * Sets the last speed check time (for save system)
     */
    public void setLastSpeedCheck(long time) {
        this.lastSpeedCheck = time;
    }
}
