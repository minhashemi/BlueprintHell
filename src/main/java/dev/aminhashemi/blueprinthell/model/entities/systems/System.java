package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.GameEntity;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.MovingPacket;

import java.util.ArrayList;
import java.util.List;

public abstract class System extends GameEntity {

    protected List<Port> inputPorts;
    protected List<Port> outputPorts;
    protected boolean isActive;
    protected long deactivationTime;
    protected long deactivationDuration;
    
    // Packet storage system - capacity of 5 packets as per documentation
    protected List<Packet> storedPackets;
    public static final int MAX_STORAGE_CAPACITY = 5;

    public System(int x, int y, int width, int height, LevelData.SystemData data) {
        super(x, y, width, height);
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();
        this.storedPackets = new ArrayList<>();
        this.isActive = true;
        this.deactivationTime = 0;
        this.deactivationDuration = 0;
        initializePorts(data);
        
        // Set system ID from level data
        if (data.id != null && !data.id.isEmpty()) {
            this.id = data.id;
        }
    }

    private void initializePorts(LevelData.SystemData data) {
        if (data.inputPorts != null) {
            for (int i = 0; i < data.inputPorts.size(); i++) {
                PortType type = PortType.valueOf(data.inputPorts.get(i).type);
                inputPorts.add(new Port(type, this, true, i));
            }
        }

        if (data.outputPorts != null) {
            for (int i = 0; i < data.outputPorts.size(); i++) {
                PortType type = PortType.valueOf(data.outputPorts.get(i).type);
                outputPorts.add(new Port(type, this, false, i));
            }
        }
    }

    public void receivePacket(Packet packet, GameEngine engine) {
        if (!isActive) {
            // If system is deactivated, return packet to source
            returnPacketToSource(packet, engine);
            return;
        }
        engine.routePacket(packet, this);
    }
    
    public void receiveMovingPacket(MovingPacket movingPacket, GameEngine engine) {
        if (!isActive) {
            // If system is deactivated, return packet to source
            returnMovingPacketToSource(movingPacket, engine);
            return;
        }
        // Route the packet as a MovingPacket to preserve the playerSpawned flag
        engine.routeMovingPacket(movingPacket, this);
    }

    // Abstract method for system-specific update logic
    @Override
    public abstract void update(GameEngine engine);

    public List<Port> getInputPorts() {
        return inputPorts;
    }

    public List<Port> getOutputPorts() {
        return outputPorts;
    }
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    private String id;
    
    /**
     * Gets the system ID
     */
    public String getId() {
        if (id == null) {
            id = this.getClass().getSimpleName() + "_" + x + "_" + y;
        }
        return id;
    }
    
    /**
     * Sets the system ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    // ==================== SYSTEM DEACTIVATION ====================
    
    /**
     * Checks if the system is currently active
     */
    public boolean isActive() {
        return isActive;
    }
    
    /**
     * Deactivates the system for a specified duration
     * @param duration Duration in milliseconds
     */
    public void deactivate(long duration) {
        this.isActive = false;
        this.deactivationTime = java.lang.System.currentTimeMillis();
        this.deactivationDuration = duration;
    }
    
    /**
     * Reactivates the system immediately
     */
    public void reactivate() {
        this.isActive = true;
        this.deactivationTime = 0;
        this.deactivationDuration = 0;
    }
    
    /**
     * Updates the system's deactivation status
     * Should be called in the update method
     */
    protected void updateDeactivationStatus() {
        if (!isActive && deactivationTime > 0) {
            long currentTime = java.lang.System.currentTimeMillis();
            if (currentTime - deactivationTime >= deactivationDuration) {
                reactivate();
            }
        }
    }
    
    /**
     * Returns a packet to its source system when this system is deactivated
     */
    protected void returnPacketToSource(Packet packet, GameEngine engine) {
        // Find the wire that brought this packet here and reverse direction
        // This is a simplified implementation - in practice, you'd need to track the source
        // For now, we'll just remove the packet from the game
        // In a full implementation, this would reverse the packet's direction
    }
    
    /**
     * Returns a moving packet to its source system when this system is deactivated
     */
    protected void returnMovingPacketToSource(MovingPacket movingPacket, GameEngine engine) {
        // Find the wire that brought this packet here and reverse direction
        // This is a simplified implementation - in practice, you'd need to track the source
        // For now, we'll just remove the packet from the game
        // In a full implementation, this would reverse the packet's direction
    }
    
    // ==================== PACKET STORAGE SYSTEM ====================
    
    /**
     * Stores a packet in this system's storage
     * @param packet The packet to store
     * @return true if packet was stored, false if storage is full
     */
    public boolean storePacket(Packet packet) {
        if (storedPackets.size() >= MAX_STORAGE_CAPACITY) {
            return false; // Storage full
        }
        storedPackets.add(packet);
        return true;
    }
    
    /**
     * Removes and returns a packet from storage
     * @return The first stored packet, or null if storage is empty
     */
    public Packet removeStoredPacket() {
        if (storedPackets.isEmpty()) {
            return null;
        }
        return storedPackets.remove(0); // FIFO - first in, first out
    }
    
    /**
     * Gets the number of packets currently stored
     * @return Number of stored packets
     */
    public int getStoredPacketCount() {
        return storedPackets.size();
    }
    
    /**
     * Checks if the system has storage capacity available
     * @return true if storage is not full
     */
    public boolean hasStorageCapacity() {
        return storedPackets.size() < MAX_STORAGE_CAPACITY;
    }
    
    /**
     * Gets all stored packets (for debugging/display purposes)
     * @return List of stored packets
     */
    public List<Packet> getStoredPackets() {
        return new ArrayList<>(storedPackets);
    }
    
    /**
     * Clears all stored packets (useful for system reset)
     */
    public void clearStoredPackets() {
        storedPackets.clear();
    }
}
