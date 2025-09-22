package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.entities.systems.Port;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BulkPacket - A large packet that carries bulk data
 * 
 * SMALL Bulk Packet (Size 8, 8 coins):
 * - Movement: Constant speed on straight wires, constant acceleration on curves
 * - Wire Damage: Each wire can only handle 3 bulk packet passes before destruction
 * - System Impact: Destroys all packets in systems it enters
 * - Port Randomization: Randomly changes ports when entering systems
 * 
 * LARGE Bulk Packet (Size 10, 10 coins):
 * - Movement: Constant speed on all wires
 * - Center Deviation: Center deviates from wire by specific amount after traveling specific distance
 * - Wire Damage: Each wire can only handle 3 bulk packet passes before destruction
 * - System Impact: Destroys all packets in systems it enters
 * - Port Randomization: Randomly changes ports when entering systems
 */
public class BulkPacket extends Packet {

    public enum BulkType {
        SMALL(8, 8),
        LARGE(10, 10);

        private final int size;
        private final int coinReward;

        BulkType(int size, int coinReward) {
            this.size = size;
            this.coinReward = coinReward;
        }

        public int getSize() { return size; }
        public int getCoinReward() { return coinReward; }
    }

    private final BulkType bulkType;
    private final PacketType type;
    private float currentSpeed = 1.0f;
    private boolean isOnCurve = false;
    private int wirePassCount = 0;
    private static final int MAX_WIRE_PASSES = Config.MAX_BULK_PACKET_PASSES;
    
    // Center deviation tracking for LARGE bulk packets
    private float distanceTraveled = 0.0f;
    private float lastDeviationDistance = 0.0f;
    private static final float DEVIATION_INTERVAL = 50.0f; // Distance between deviations
    private static final float DEVIATION_AMOUNT = 2.0f; // Amount of deviation

    public BulkPacket(int x, int y, BulkType bulkType) {
        super(x, y, bulkType.getSize() * 4, bulkType.getSize() * 4); // Larger visual size
        this.bulkType = bulkType;
        this.type = bulkType == BulkType.SMALL ? PacketType.BULK_PACKET_SMALL : PacketType.BULK_PACKET_LARGE;
    }

    @Override
    public void update(GameEngine engine) {
        // Different movement behavior for SMALL vs LARGE bulk packets
        if (bulkType == BulkType.SMALL) {
            // SMALL bulk packets: Constant speed on straight wires, constant acceleration on curves
            if (isOnCurve) {
                // Constant acceleration on curves
                currentSpeed = Math.min(Config.BULK_MAX_SPEED, currentSpeed + Config.BULK_CURVE_ACCELERATION);
            } else {
                // Constant speed on straight wires
                currentSpeed = Config.BULK_MIN_SPEED; // Maintain constant speed
            }
        } else {
            // LARGE bulk packets: Constant speed on all wires
            currentSpeed = Config.BULK_MIN_SPEED; // Constant speed on all wires
            
            // Update distance traveled for center deviation
            distanceTraveled += currentSpeed;
            
            // Apply center deviation after traveling specific distance
            if (distanceTraveled - lastDeviationDistance >= DEVIATION_INTERVAL) {
                applyCenterDeviation();
                lastDeviationDistance = distanceTraveled;
            }
        }
        
        // Bulk packets also have slight random movement to simulate bulk data transfer
        if (Math.random() < Config.BULK_RANDOM_MOVEMENT_CHANCE) { // 10% chance to add slight random movement
            dx = (Math.random() - 0.5) * Config.BULK_RANDOM_MOVEMENT_AMOUNT;
            dy = (Math.random() - 0.5) * Config.BULK_RANDOM_MOVEMENT_AMOUNT;
        }
    }
    
    /**
     * Applies center deviation for LARGE bulk packets
     * The center deviates from the wire by a specific amount after traveling a certain distance
     */
    private void applyCenterDeviation() {
        // LARGE bulk packets: Center deviates by specific amount after traveling specific distance
        // This is similar to Impact effect on other packets
        Logger.getInstance().debug("LARGE BulkPacket applying center deviation after traveling " + distanceTraveled + " units");
        
        // Apply deviation in a random direction
        double angle = Math.random() * 2 * Math.PI; // Random angle
        float deviationX = (float)(Math.cos(angle) * DEVIATION_AMOUNT);
        float deviationY = (float)(Math.sin(angle) * DEVIATION_AMOUNT);
        
        // Apply the deviation
        dx += deviationX;
        dy += deviationY;
        
        // Clamp deviation to reasonable bounds
        dx = Math.max(-Config.BULK_DEVIATION_BOUNDS, Math.min(Config.BULK_DEVIATION_BOUNDS, dx));
        dy = Math.max(-Config.BULK_DEVIATION_BOUNDS, Math.min(Config.BULK_DEVIATION_BOUNDS, dy));
    }

    /**
     * Called when the packet enters a system
     * Destroys all other packets in the system
     */
    public void onSystemEntry(GameEngine engine, System system) {
        Logger.getInstance().info("BulkPacket entered system - destroying all stored packets");
        
        // Destroy all stored packets in the system
        if (system.getStoredPacketCount() > 0) {
            Logger.getInstance().info("Destroying " + system.getStoredPacketCount() + " stored packets in system");
            system.clearStoredPackets();
        }
    }

    /**
     * Called when the packet passes through a wire
     * Increments the wire's usage count
     */
    public void onWirePass(Wire wire, GameEngine engine) {
        wirePassCount++;
        Logger.getInstance().info("BulkPacket passed through wire. Pass count: " + wirePassCount);
        
        // Reset distance tracking for LARGE bulk packets when entering new wire
        if (bulkType == BulkType.LARGE) {
            distanceTraveled = 0.0f;
            lastDeviationDistance = 0.0f;
        }
        
        // Check if wire should be destroyed
        if (wirePassCount >= MAX_WIRE_PASSES) {
            Logger.getInstance().warning("Wire has reached maximum bulk packet passes and should be destroyed");
            // Mark wire for destruction
            wire.markForDestruction();
            // Notify GameEngine to remove the wire
            engine.removeWire(wire);
        }
    }

    /**
     * Randomizes the port when entering a system
     * Bulk packets transform the port they enter through to a random other port
     */
    public void randomizePort(System system) {
        Logger.getInstance().info("BulkPacket randomizing port in system");
        
        // Get all available ports in the system
        List<Port> allPorts = new ArrayList<>();
        allPorts.addAll(system.getInputPorts());
        allPorts.addAll(system.getOutputPorts());
        
        if (allPorts.size() > 1) {
            // Randomly select a different port to transform to
            Port randomPort = allPorts.get((int)(Math.random() * allPorts.size()));
            Logger.getInstance().info("BulkPacket transformed port to: " + randomPort.getType());
            // The actual port transformation would be handled by the system
        }
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw bulk packet with size indicator
        g.setColor(type.getColor());
        
        // Main body - larger rectangle
        g.fillRect(x + 2, y + 2, width - 4, height - 4);
        
        // Draw bulk data indicator
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString("BULK", x + width/2 - 12, y + height/2 - 5);
        g.drawString(String.valueOf(bulkType.getSize()), x + width/2 - 5, y + height/2 + 8);
        
        // Draw size indicator border
        g.setColor(Color.RED);
        g.drawRect(x, y, width, height);
        
        // Draw speed indicator
        if (isOnCurve) {
            g.setColor(Color.YELLOW);
            g.drawString("FAST", x + width + 2, y + height/2);
        }
        
        // Draw wire damage indicator
        if (wirePassCount > 0) {
            g.setColor(Color.ORANGE);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            g.drawString("DAMAGE: " + wirePassCount, x + width + 2, y + height/2 + 15);
        }
    }

    @Override
    public PacketType getType() {
        return type;
    }

    /**
     * Gets the bulk type (SMALL or LARGE)
     */
    public BulkType getBulkType() {
        return bulkType;
    }

    /**
     * Gets the current movement speed
     */
    public float getCurrentSpeed() {
        return currentSpeed;
    }

    /**
     * Sets whether the packet is currently on a curve
     */
    public void setOnCurve(boolean onCurve) {
        this.isOnCurve = onCurve;
    }

    /**
     * Gets the wire pass count
     */
    public int getWirePassCount() {
        return wirePassCount;
    }

    /**
     * Resets the wire pass count
     */
    public void resetWirePassCount() {
        this.wirePassCount = 0;
    }

    /**
     * Returns the coin reward for this packet type
     */
    public int getCoinReward() {
        return bulkType.getCoinReward();
    }

    /**
     * Returns the size of this packet
     */
    public int getPacketSize() {
        return bulkType.getSize();
    }

    /**
     * Checks if the wire should be destroyed after this packet passes
     */
    public boolean shouldDestroyWire() {
        return wirePassCount >= MAX_WIRE_PASSES;
    }

    /**
     * Bulk packets are not compatible with regular ports
     */
    public boolean requiresSpecialPorts() {
        return true;
    }

    /**
     * Bulk packets can be split into smaller packets
     */
    public List<Packet> splitIntoSmallerPackets() {
        // This would be implemented to split the bulk packet
        // into smaller messenger packets
        Logger.getInstance().info("BulkPacket splitting into smaller packets");
        return List.of(); // Placeholder
    }

    /**
     * Bulk packets can be merged from smaller packets
     */
    public static BulkPacket mergeFromPackets(List<Packet> packets) {
        // This would be implemented to merge smaller packets
        // into a bulk packet
        Logger.getInstance().info("Merging packets into BulkPacket");
        return new BulkPacket(0, 0, BulkType.SMALL); // Placeholder
    }
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    /**
     * Checks if the packet is on a curve
     */
    public boolean isOnCurve() {
        return isOnCurve;
    }
}
