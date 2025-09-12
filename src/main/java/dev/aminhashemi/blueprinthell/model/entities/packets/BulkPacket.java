package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.awt.*;
import java.util.List;

/**
 * BulkPacket - A large packet that carries bulk data
 * 
 * Behavior:
 * - Size: 8 units (SMALL) or 10 units (LARGE)
 * - Coin Reward: 8 coins (SMALL) or 10 coins (LARGE)
 * - Movement: Constant speed on straight wires, acceleration on curves
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
    private static final int MAX_WIRE_PASSES = 3;

    public BulkPacket(int x, int y, BulkType bulkType) {
        super(x, y, bulkType.getSize() * 4, bulkType.getSize() * 4); // Larger visual size
        this.bulkType = bulkType;
        this.type = PacketType.GREEN_DIAMOND_LARGE; // Use existing type for now
    }

    @Override
    public void update(GameEngine engine) {
        // Bulk packets have different movement behavior
        // Constant speed on straight wires, acceleration on curves
        if (isOnCurve) {
            currentSpeed = Math.min(2.0f, currentSpeed + 0.05f); // Accelerate on curves
        } else {
            currentSpeed = Math.max(1.0f, currentSpeed - 0.02f); // Decelerate on straight wires
        }
        
        // Bulk packets also have slight random movement to simulate bulk data transfer
        if (Math.random() < 0.1) { // 10% chance to add slight random movement
            dx = (Math.random() - 0.5) * 0.3;
            dy = (Math.random() - 0.5) * 0.3;
        }
    }

    /**
     * Called when the packet enters a system
     * Destroys all other packets in the system
     */
    public void onSystemEntry(GameEngine engine) {
        Logger.getInstance().info("BulkPacket entered system - destroying other packets");
        // This would be implemented to destroy other packets in the system
        // For now, we'll just log the action
    }

    /**
     * Called when the packet passes through a wire
     * Increments the wire's usage count
     */
    public void onWirePass(Wire wire) {
        wirePassCount++;
        Logger.getInstance().info("BulkPacket passed through wire. Pass count: " + wirePassCount);
        
        // Check if wire should be destroyed
        if (wirePassCount >= MAX_WIRE_PASSES) {
            Logger.getInstance().warning("Wire has reached maximum bulk packet passes and should be destroyed");
            // Wire destruction would be handled by the GameEngine
        }
    }

    /**
     * Randomizes the port when entering a system
     */
    public void randomizePort() {
        // This would be called by the system to randomize the port
        Logger.getInstance().info("BulkPacket randomizing port selection");
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
}
