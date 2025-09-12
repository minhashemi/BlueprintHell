package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.awt.*;

public class VPNSystem extends System {

    public VPNSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, 100, 80, data);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.decode("#A55EEA")); // Bright purple
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE); // White border
        g.drawRect(x, y, width, height);
        g.setColor(Color.WHITE); // White text
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
        
        // Add coins for successful protection
        engine.addCoins(2);
        
        // Route the protected packet
        super.receivePacket(protectedPacket, engine);
    }

    @Override
    public void update(GameEngine engine) {
        // VPN system doesn't need special update logic
        // The protection behavior is handled in receivePacket
    }
}
