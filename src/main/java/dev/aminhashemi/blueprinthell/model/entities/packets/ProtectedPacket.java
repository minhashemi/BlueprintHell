package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;

/**
 * ProtectedPacket - A packet that has been protected by a VPN system
 * 
 * Behavior:
 * - Size: Twice the size of the original packet
 * - Coin Reward: 5 coins when entering systems
 * - Movement: Random movement pattern (unknown to network)
 * - Immunity: Unaffected by SpySystems and MaliciousSystems
 * - Visibility: Hidden from network detection
 */
public class ProtectedPacket extends Packet {

    private final PacketType originalType;
    private final PacketType protectedType;
    private boolean isVisible = false; // Hidden by default
    private long lastVisibilityToggle = 0;
    private static final long VISIBILITY_TOGGLE_INTERVAL = Config.VISIBILITY_TOGGLE_INTERVAL; // 2 seconds

    public ProtectedPacket(int x, int y, PacketType originalType) {
        // Size is 2x the original packet size
        super(x, y, originalType.getSize() * 2, originalType.getSize() * 2);
        this.originalType = originalType;
        this.protectedType = PacketType.PADLOCK_ICON; // Always appears as protected
    }

    /**
     * Creates a protected packet from an existing packet
     */
    public static ProtectedPacket fromPacket(Packet originalPacket) {
        ProtectedPacket protectedPacket = new ProtectedPacket(
            originalPacket.getX(), 
            originalPacket.getY(), 
            originalPacket.getType()
        );
        
        // Copy movement properties
        protectedPacket.speed = originalPacket.speed;
        protectedPacket.dx = originalPacket.dx;
        protectedPacket.dy = originalPacket.dy;
        protectedPacket.noise = originalPacket.noise;
        
        Logger.getInstance().info("Created ProtectedPacket from " + originalPacket.getType());
        return protectedPacket;
    }

    @Override
    public void update(GameEngine engine) {
        // Toggle visibility periodically to simulate network detection attempts
        long currentTime = java.lang.System.currentTimeMillis();
        if (currentTime - lastVisibilityToggle > VISIBILITY_TOGGLE_INTERVAL) {
            isVisible = !isVisible;
            lastVisibilityToggle = currentTime;
            
            if (isVisible) {
                Logger.getInstance().debug("ProtectedPacket became visible briefly");
            }
        }
        
        // Protected packets have random movement patterns
        // This simulates the "unknown movement" behavior
        if (Math.random() < 0.1) { // 10% chance to change direction
            dx = (Math.random() - 0.5) * 0.5;
            dy = (Math.random() - 0.5) * 0.5;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        if (!isVisible) {
            // Draw as a subtle shadow when hidden
            g.setColor(new Color(0, 0, 0, 50));
            g.fillOval(x + 2, y + 2, width, height);
            return;
        }

        // Draw protected packet with padlock icon
        g.setColor(protectedType.getColor());
        
        // Draw main body
        g.fillRect(x + 4, y + 8, width - 8, height - 8);
        
        // Draw padlock top
        g.fillRect(x + 6, y + 4, width - 12, 6);
        
        // Draw padlock body
        g.fillRect(x + 8, y + 10, width - 16, height - 12);
        
        // Draw protection indicator
        g.setColor(Config.SYSTEM_TEXT_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, Config.SMALL_FONT_SIZE));
        g.drawString("P", x + width/2 - 3, y + height/2 + 3);
        
        // Draw size indicator (showing it's larger)
        g.setColor(Config.WIRING_MODE_COLOR);
        g.drawRect(x, y, width, height);
        
        // DEBUG: Yellow rectangle around protected packets for visual identification
        
        g.setColor(Color.YELLOW);
        g.setStroke(new BasicStroke(2.0f)); // 2-pixel thick border
        g.drawRect(x - 2, y - 2, width + 4, height + 4); // Slightly larger rectangle
        g.setStroke(new BasicStroke(1.0f)); // Reset stroke
        
    }

    @Override
    public PacketType getType() {
        return protectedType;
    }

    /**
     * Gets the original packet type before protection
     */
    public PacketType getOriginalType() {
        return originalType;
    }

    /**
     * Checks if the packet is currently visible
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Forces the packet to become visible
     */
    public void makeVisible() {
        isVisible = true;
        lastVisibilityToggle = java.lang.System.currentTimeMillis();
    }

    /**
     * Forces the packet to become hidden
     */
    public void makeHidden() {
        isVisible = false;
        lastVisibilityToggle = java.lang.System.currentTimeMillis();
    }

    /**
     * Protected packets are immune to spy system effects
     */
    public boolean isImmuneToSpySystems() {
        return true;
    }

    /**
     * Protected packets are immune to malicious system effects
     */
    public boolean isImmuneToMaliciousSystems() {
        return true;
    }

    /**
     * Returns the coin reward for this packet type
     */
    public int getCoinReward() {
        return Config.Phase1Packets.PROTECTED_PACKET_COINS; // Protected packets give 5 coins
    }

    /**
     * Returns the size of this packet
     */
    public int getPacketSize() {
        return Config.Phase1Packets.PROTECTED_PACKET_SIZE; // Twice the size of original packet
    }
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    /**
     * Gets the original packet type
     */
    public PacketType getOriginalPacketType() {
        return originalType;
    }
    
    /**
     * Gets the last visibility toggle time
     */
    public long getLastVisibilityToggle() {
        return lastVisibilityToggle;
    }
    
    /**
     * Toggles visibility (for save system)
     */
    public void toggleVisibility() {
        isVisible = !isVisible;
        lastVisibilityToggle = System.currentTimeMillis();
    }
}
