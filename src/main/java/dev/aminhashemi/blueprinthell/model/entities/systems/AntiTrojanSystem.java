package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.entities.packets.TrojanPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;
import java.util.List;

/**
 * AntiTrojanSystem - A system that detects and neutralizes trojan packets
 * 
 * Behavior according to documentation:
 * - Checks packet health within a specific radius
 * - Converts trojan packets to messenger packets when in range
 * - Becomes inactive for a specific duration after successful operation
 */
public class AntiTrojanSystem extends System {

    private boolean isActive = true;
    private long lastActivationTime = 0;
    private static final long COOLDOWN_DURATION = Config.ANTITROJAN_COOLDOWN_DURATION; // 5 seconds cooldown
    private static final float DETECTION_RADIUS = Config.ANTITROJAN_DETECTION_RADIUS; // Detection radius

    public AntiTrojanSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, Config.SYSTEM_WIDTH, Config.SYSTEM_HEIGHT, data);
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw system with different color based on active state
        if (isActive) {
            g.setColor(Config.SystemColors.ANTITROJAN_COLOR); // Bright green when active
        } else {
            g.setColor(Config.SystemColors.ANTITROJAN_INACTIVE_COLOR); // Dark green when inactive
        }
        g.fillRect(x, y, width, height);
        g.setColor(Config.SYSTEM_BORDER_COLOR); // White border
        g.drawRect(x, y, width, height);
        g.setColor(Config.SYSTEM_TEXT_COLOR); // White text
        g.drawString("ANTI", x + 5, y + 15);
        g.drawString("TROJ", x + 5, y + 30);

        // Draw detection radius when active
        if (isActive) {
            g.setColor(new Color(0, 255, 0, 50)); // Semi-transparent green
            g.fillOval(x - (int)DETECTION_RADIUS + width/2, y - (int)DETECTION_RADIUS + height/2, 
                      (int)DETECTION_RADIUS * 2, (int)DETECTION_RADIUS * 2);
        }

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update(GameEngine engine) {
        // Check if cooldown period has ended
        long currentTime = java.lang.System.currentTimeMillis();
        if (!isActive && (currentTime - lastActivationTime) >= COOLDOWN_DURATION) {
            isActive = true;
            Logger.getInstance().info("AntiTrojanSystem at (" + x + ", " + y + ") is now active again");
        }
        
        // Only scan for trojan packets when active
        if (isActive) {
            scanForTrojanPackets(engine);
        }
    }

    @Override
    public void receiveMovingPacket(MovingPacket movingPacket, GameEngine engine) {
        Packet packet = movingPacket.getPacket();
        Logger.getInstance().info("Packet " + packet.getType() + " entered AntiTrojanSystem at (" + x + ", " + y + ")");
        
        // AntiTrojan systems behave like normal systems for packet routing
        super.receiveMovingPacket(movingPacket, engine);
    }

    /**
     * Scans for trojan packets within detection radius
     */
    private void scanForTrojanPackets(GameEngine engine) {
        List<MovingPacket> allMovingPackets = engine.getMovingPackets();
        
        for (MovingPacket movingPacket : allMovingPackets) {
            Packet packet = movingPacket.getPacket();
            
            // Only process trojan packets
            if (packet instanceof TrojanPacket) {
                // Calculate distance to packet
                float packetX = packet.getX() + packet.getWidth() / 2;
                float packetY = packet.getY() + packet.getHeight() / 2;
                float systemX = this.x + this.width / 2;
                float systemY = this.y + this.height / 2;
                
                double distance = Math.sqrt(
                    Math.pow(packetX - systemX, 2) + 
                    Math.pow(packetY - systemY, 2)
                );
                
                // If trojan packet is within detection radius
                if (distance <= DETECTION_RADIUS) {
                    neutralizeTrojanPacket(movingPacket, engine);
                    break; // Only neutralize one packet per scan
                }
            }
        }
    }

    /**
     * Neutralizes a trojan packet by converting it to a messenger packet
     */
    private void neutralizeTrojanPacket(MovingPacket trojanMovingPacket, GameEngine engine) {
        Packet trojanPacket = trojanMovingPacket.getPacket();
        
        if (trojanPacket instanceof TrojanPacket) {
            TrojanPacket trojan = (TrojanPacket) trojanPacket;
            PacketType originalType = trojan.getOriginalType();
            
            // Convert trojan packet back to original messenger packet
            MessengerPacket messengerPacket = new MessengerPacket(
                trojanPacket.getX(), 
                trojanPacket.getY(), 
                originalType
            );
            
            // Copy properties
            messengerPacket.setNoise(trojanPacket.getNoise());
            messengerPacket.setCurrentSpeed(trojanPacket.getCurrentSpeed());
            
            // Create new moving packet
            MovingPacket messengerMovingPacket = new MovingPacket(messengerPacket, trojanMovingPacket.getWire());
            messengerMovingPacket.setPlayerSpawned(trojanMovingPacket.isPlayerSpawned());
            
            // Replace the trojan packet with messenger packet
            engine.replaceMovingPacket(trojanMovingPacket, messengerMovingPacket);
            
            Logger.getInstance().info("AntiTrojanSystem neutralized trojan packet, converted to " + originalType);
            
            // Deactivate system for cooldown period
            isActive = false;
            lastActivationTime = java.lang.System.currentTimeMillis();
            Logger.getInstance().info("AntiTrojanSystem deactivated for " + (COOLDOWN_DURATION / 1000) + " seconds");
        }
    }

    /**
     * Checks if the AntiTrojan system is currently active
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Gets the remaining cooldown time in milliseconds
     */
    public long getRemainingCooldown() {
        if (isActive) {
            return 0;
        }
        long elapsed = java.lang.System.currentTimeMillis() - lastActivationTime;
        return Math.max(0, COOLDOWN_DURATION - elapsed);
    }
}
