package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.awt.*;

/**
 * TrojanPacket - A harmful packet created by malicious systems
 * 
 * Behavior:
 * - Size: 2 units
 * - Coin Reward: 0 coins (harmful to the player)
 * - Movement: Same as original packet but with added noise
 * - Effect: Consumes resources without providing benefits
 * - Immunity: Cannot be converted back to normal packets
 */
public class TrojanPacket extends Packet {

    private final PacketType originalType;
    private final PacketType trojanType;
    private float corruptionLevel = 0.0f; // How corrupted this packet is

    public TrojanPacket(int x, int y, PacketType originalType) {
        super(x, y, 12, 12); // Size 2 units
        this.originalType = originalType;
        this.trojanType = PacketType.TROJAN_PACKET;
        this.corruptionLevel = 0.5f; // Start with some corruption
    }

    /**
     * Creates a trojan packet from an existing packet
     */
    public static TrojanPacket fromPacket(Packet originalPacket) {
        TrojanPacket trojanPacket = new TrojanPacket(
            originalPacket.getX(), 
            originalPacket.getY(), 
            originalPacket.getType()
        );
        
        // Copy movement properties but add noise
        trojanPacket.speed = originalPacket.speed * 0.8f; // Slightly slower
        trojanPacket.dx = originalPacket.dx;
        trojanPacket.dy = originalPacket.dy;
        trojanPacket.noise = originalPacket.noise + 0.3f; // Add corruption noise
        
        Logger.getInstance().info("Created TrojanPacket from " + originalPacket.getType());
        return trojanPacket;
    }

    @Override
    public void update(GameEngine engine) {
        // Trojan packets move erratically due to corruption
        if (Math.random() < 0.1) { // 10% chance per frame
            // Random direction change due to corruption
            double angle = Math.random() * 2 * Math.PI;
            dx = Math.cos(angle);
            dy = Math.sin(angle);
        }
        
        // Increase corruption over time
        corruptionLevel = Math.min(corruptionLevel + 0.001f, 1.0f);
        noise = Math.min(noise + 0.001f, 1.0f);
        
        // Move the packet
        x += dx * speed;
        y += dy * speed;
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw as a corrupted version of the original packet
        g.setColor(trojanType.getColor());
        
        // Draw a corrupted square with jagged edges
        int[] xPoints = {
            x, x + width - 2, x + width, x + width - 1, 
            x + 1, x, x + 2, x + width - 1
        };
        int[] yPoints = {
            y, y, y + 2, y + height - 1, 
            y + height, y + height - 2, y + height, y + 1
        };
        
        g.fillPolygon(xPoints, yPoints, xPoints.length);
        
        // Add corruption effect
        g.setColor(new Color(255, 0, 0, (int)(corruptionLevel * 100)));
        g.drawRect(x - 1, y - 1, width + 2, height + 2);
    }

    @Override
    public PacketType getType() {
        return trojanType;
    }

    /**
     * Gets the original packet type before trojan conversion
     */
    public PacketType getOriginalType() {
        return originalType;
    }

    /**
     * Gets the corruption level (0.0 to 1.0)
     */
    public float getCorruptionLevel() {
        return corruptionLevel;
    }

    /**
     * Trojan packets are harmful and give no coins
     */
    public int getCoinReward() {
        return 0; // Trojan packets give no coins
    }

    /**
     * Returns the size of this packet
     */
    public int getPacketSize() {
        return 2; // Size 2 units
    }

    /**
     * Trojan packets cannot be converted back to normal packets
     */
    public boolean canBeConverted() {
        return false;
    }
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    /**
     * Gets the original packet type
     */
    public PacketType getOriginalPacketType() {
        return originalType;
    }
    
    /**
     * Gets the corruption level
     */
    public float getCorruption() {
        return corruptionLevel;
    }
    
    /**
     * Sets the corruption level
     */
    public void setCorruption(float corruption) {
        this.corruptionLevel = Math.max(0.0f, Math.min(1.0f, corruption));
    }
}
