package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * SpySystem - A system that can teleport packets to other SpySystems
 * 
 * Behavior:
 * - When a packet enters a SpySystem, it can exit from any other SpySystem in the network
 * - Confidential packets are destroyed when entering SpySystems
 * - Protected packets are unaffected by SpySystems
 * - Regular packets are teleported to a random SpySystem
 */
public class SpySystem extends System {

    private final Random random = new Random();
    private List<SpySystem> spyNetwork; // Network of all SpySystems
    private boolean isActive = true; // Can be disabled by other systems

    public SpySystem(int x, int y, LevelData.SystemData data) {
        super(x, y, 100, 80, data);
        this.spyNetwork = new ArrayList<>();
    }

    /**
     * Sets the spy network - all SpySystems in the game
     */
    public void setSpyNetwork(List<SpySystem> spyNetwork) {
        this.spyNetwork = spyNetwork;
    }

    /**
     * Override receivePacket to implement spy system behavior
     */
    @Override
    public void receivePacket(Packet packet, GameEngine engine) {
        Logger.getInstance().info("Packet " + packet.getType() + " entered SpySystem at (" + x + ", " + y + ")");
        
        // Check if system is active
        if (!isActive) {
            Logger.getInstance().info("SpySystem is inactive - routing packet normally");
            super.receivePacket(packet, engine);
            return;
        }

        // Handle different packet types
        switch (packet.getType()) {
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                // Confidential packets are destroyed
                Logger.getInstance().info("Confidential packet destroyed by SpySystem");
                engine.addCoins(3); // Reward for destroying confidential packet
                return;
                
            case PADLOCK_ICON:
                // Protected packets are unaffected
                Logger.getInstance().info("Protected packet unaffected by SpySystem - routing normally");
                super.receivePacket(packet, engine);
                return;
                
            default:
                // Regular packets are teleported to another SpySystem
                teleportPacket(packet, engine);
                break;
        }
    }

    /**
     * Teleports a packet to another SpySystem in the network
     */
    private void teleportPacket(Packet packet, GameEngine engine) {
        if (spyNetwork.size() <= 1) {
            // No other SpySystems to teleport to
            Logger.getInstance().info("No other SpySystems available - routing packet normally");
            super.receivePacket(packet, engine);
            return;
        }

        // Find other active SpySystems
        List<SpySystem> availableSpySystems = new ArrayList<>();
        for (SpySystem spySystem : spyNetwork) {
            if (spySystem != this && spySystem.isActive && !spySystem.getOutputPorts().isEmpty()) {
                availableSpySystems.add(spySystem);
            }
        }

        if (availableSpySystems.isEmpty()) {
            Logger.getInstance().info("No active SpySystems with output ports available");
            super.receivePacket(packet, engine);
            return;
        }

        // Select a random SpySystem to teleport to
        SpySystem targetSpySystem = availableSpySystems.get(random.nextInt(availableSpySystems.size()));
        
        // Move packet to target SpySystem position
        packet.setPosition(targetSpySystem.getX() + targetSpySystem.getWidth() / 2, 
                          targetSpySystem.getY() + targetSpySystem.getHeight() / 2);
        
        Logger.getInstance().info("Packet teleported from SpySystem (" + x + ", " + y + ") to (" + 
                                targetSpySystem.getX() + ", " + targetSpySystem.getY() + ")");
        
        // Route packet from the target SpySystem
        targetSpySystem.routePacketFromSpySystem(packet, engine);
    }

    /**
     * Routes a packet that was teleported to this SpySystem
     */
    public void routePacketFromSpySystem(Packet packet, GameEngine engine) {
        // Add coins for successful spy network usage
        engine.addCoins(2);
        
        // Route the packet normally from this system
        super.receivePacket(packet, engine);
    }

    /**
     * Sets the active state of the SpySystem
     */
    public void setActive(boolean active) {
        this.isActive = active;
        Logger.getInstance().info("SpySystem at (" + x + ", " + y + ") is now " + (active ? "active" : "inactive"));
    }

    /**
     * Checks if the SpySystem is active
     */
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw system body with spy-themed colors
        if (isActive) {
            g.setColor(Color.decode("#FF9F43")); // Bright orange-red when active
        } else {
            g.setColor(Color.decode("#95A5A6")); // Gray when inactive
        }
        g.fillRect(x, y, width, height);
        
        // Draw border
        g.setColor(Color.WHITE);
        g.drawRect(x, y, width, height);
        
        // Draw spy symbol
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("SPY", x + 10, y + 20);
        
        // Draw status indicator
        if (isActive) {
            g.setColor(Color.GREEN);
            g.fillOval(x + width - 15, y + 5, 10, 10);
        } else {
            g.setColor(Color.RED);
            g.fillOval(x + width - 15, y + 5, 10, 10);
        }

        // Draw ports
        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update(GameEngine engine) {
        // SpySystem doesn't need special update logic
        // The spy network behavior is handled in receivePacket
    }
}
