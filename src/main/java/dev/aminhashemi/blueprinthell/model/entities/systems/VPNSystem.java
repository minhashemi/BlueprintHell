package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ConfidentialPacket;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;

public class VPNSystem extends System {

    private boolean isActive = true; // VPN can fail and become inactive

    public VPNSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, Config.SYSTEM_WIDTH, Config.SYSTEM_HEIGHT, data);
    }
    
    /**
     * Sets the VPN system as failed/inactive
     * When VPN fails, all protected packets created by this VPN should revert to original type
     */
    public void setFailed(boolean failed) {
        this.isActive = !failed;
        if (failed) {
            Logger.getInstance().warning("VPNSystem at (" + x + ", " + y + ") has failed!");
        } else {
            Logger.getInstance().info("VPNSystem at (" + x + ", " + y + ") is now active");
        }
    }
    
    /**
     * Checks if the VPN system is active
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Config.SystemColors.VPN_COLOR); // Bright purple
        g.fillRect(x, y, width, height);
        g.setColor(Config.SYSTEM_BORDER_COLOR); // White border
        g.drawRect(x, y, width, height);
        g.setColor(Config.SYSTEM_TEXT_COLOR); // White text
        g.drawString("VPN", x + 10, y + 20);

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    /**
     * Override receivePacket to implement VPN system behavior
     * Converts regular packets to protected packets
     */
    @Override
    public void receivePacket(Packet packet, GameEngine engine) {
        Logger.getInstance().info("Packet " + packet.getType() + " entered VPNSystem at (" + x + ", " + y + ")");
        
        // Check if packet is already protected
        if (packet instanceof ProtectedPacket) {
            Logger.getInstance().info("Packet is already protected - routing normally");
            super.receivePacket(packet, engine);
            return;
        }
        
        // Convert packet to protected packet
        ProtectedPacket protectedPacket = ProtectedPacket.fromPacket(packet);
        Logger.getInstance().info("Packet converted to ProtectedPacket by VPNSystem");
        
        // Route the protected packet
        super.receivePacket(protectedPacket, engine);
    }
    
    @Override
    public void receiveMovingPacket(MovingPacket movingPacket, GameEngine engine) {
        Packet packet = movingPacket.getPacket();
        Logger.getInstance().info("Packet " + packet.getType() + " entered VPNSystem at (" + x + ", " + y + ")");
        
        // Check if VPN system is active
        if (!isActive) {
            Logger.getInstance().info("VPNSystem is inactive - routing packet normally");
            super.receiveMovingPacket(movingPacket, engine);
            return;
        }
        
        // Check if packet is already protected
        if (packet instanceof ProtectedPacket) {
            Logger.getInstance().info("Packet is already protected - routing normally");
            super.receiveMovingPacket(movingPacket, engine);
            return;
        }
        
        // Special handling for confidential packets
        if (packet instanceof ConfidentialPacket) {
            ConfidentialPacket confidentialPacket = (ConfidentialPacket) packet;
            
            // Convert SMALL confidential to LARGE confidential
            if (confidentialPacket.getConfidentialType() == ConfidentialPacket.ConfidentialType.SMALL) {
                Logger.getInstance().info("Converting SMALL confidential packet to LARGE confidential packet");
                
                // Create LARGE confidential packet
                ConfidentialPacket largeConfidentialPacket = new ConfidentialPacket(
                    packet.getX(), 
                    packet.getY(), 
                    ConfidentialPacket.ConfidentialType.LARGE
                );
                
                // Copy properties
                largeConfidentialPacket.setBaseSpeed(confidentialPacket.getCurrentSpeed());
                largeConfidentialPacket.setNoise(confidentialPacket.getNoise());
                
                // Create new MovingPacket with LARGE confidential packet
                MovingPacket largeConfidentialMovingPacket = new MovingPacket(largeConfidentialPacket, movingPacket.getWire());
                largeConfidentialMovingPacket.setPlayerSpawned(movingPacket.isPlayerSpawned());
                
                super.receiveMovingPacket(largeConfidentialMovingPacket, engine);
                return;
            } else {
                // LARGE confidential packets pass through normally
                Logger.getInstance().info("LARGE confidential packet - routing normally");
                super.receiveMovingPacket(movingPacket, engine);
                return;
            }
        }
        
        // Convert other packets to protected packet
        ProtectedPacket protectedPacket = ProtectedPacket.fromPacket(packet);
        Logger.getInstance().info("Packet converted to ProtectedPacket by VPNSystem");
        
        // VPNSystem no longer adds coins - coins are only added when packets reach the final reference system
        Logger.getInstance().info("Packet " + packet.getType() + " protected by VPNSystem - no coins added here");
        
        // Route the protected packet as a MovingPacket to preserve playerSpawned flag
        // Create a new MovingPacket with the protected packet
        MovingPacket protectedMovingPacket = new MovingPacket(protectedPacket, movingPacket.getWire());
        protectedMovingPacket.setPlayerSpawned(movingPacket.isPlayerSpawned());
        super.receiveMovingPacket(protectedMovingPacket, engine);
    }

    @Override
    public void update(GameEngine engine) {
        // VPN system doesn't need special update logic
        // The protection behavior is handled in receivePacket
    }
}
