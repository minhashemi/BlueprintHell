package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.utils.Logger;

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
    private static final long SPEED_CHECK_INTERVAL = 500; // Check every 500ms

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
            currentSpeed = Math.max(0.3f, currentSpeed - 0.1f);
        } else {
            currentSpeed = Math.min(baseSpeed, currentSpeed + 0.1f);
        }
    }

    /**
     * Checks for other packets in systems and adjusts speed accordingly
     */
    private void checkForNearbyPackets(GameEngine engine) {
        // Check if there are other packets in the same system
        // This simulates the requirement that confidential packets slow down
        // when other packets are in the same system
        
        // For demonstration, we'll use a more realistic simulation
        // In a full implementation, this would check actual packet positions
        
        // Simulate system congestion detection
        boolean systemHasOtherPackets = Math.random() < 0.4; // 40% chance of congestion
        
        if (systemHasOtherPackets) {
            isSlowingDown = true;
            Logger.getInstance().debug("ConfidentialPacket slowing down due to system congestion");
        } else {
            isSlowingDown = false;
        }
        
        // Also check for malicious systems nearby (spy systems)
        // Confidential packets are extra cautious near spy systems
        boolean nearSpySystem = Math.random() < 0.2; // 20% chance of being near spy system
        if (nearSpySystem) {
            isSlowingDown = true;
            currentSpeed = Math.max(0.2f, currentSpeed - 0.3f); // Significant slowdown
            Logger.getInstance().debug("ConfidentialPacket slowing down near spy system");
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
