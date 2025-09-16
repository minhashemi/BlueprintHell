package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;

public class VPNSystem extends System {

    public VPNSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, Config.SYSTEM_WIDTH, Config.SYSTEM_HEIGHT, data);
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
        
        // Check if packet is already protected
        if (packet instanceof ProtectedPacket) {
            Logger.getInstance().info("Packet is already protected - routing normally");
            super.receiveMovingPacket(movingPacket, engine);
            return;
        }
        
        // Convert packet to protected packet
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
