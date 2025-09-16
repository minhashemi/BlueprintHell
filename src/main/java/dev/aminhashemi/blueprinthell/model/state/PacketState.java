package dev.aminhashemi.blueprinthell.model.state;

import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.Wire;

/**
 * Manages packet state and lifecycle.
 * Single responsibility: tracking packet state and spawn protection.
 */
public class PacketState {
    
    private final Packet packet;
    private final Wire wire;
    
    private boolean spawnProtection = false;
    private long spawnTime;
    private long spawnProtectionEndTime;
    private boolean playerSpawned = false;
    
    public PacketState(Packet packet, Wire wire) {
        this.packet = packet;
        this.wire = wire;
        this.spawnTime = java.lang.System.currentTimeMillis();
    }
    
    /**
     * Sets spawn protection for the packet.
     */
    public void setSpawnProtection(boolean protection) {
        this.spawnProtection = protection;
        if (protection) {
            this.spawnProtectionEndTime = java.lang.System.currentTimeMillis() + 1000; // 1 second protection
        }
    }
    
    /**
     * Sets spawn protection with custom duration.
     */
    public void setSpawnProtection(boolean hasSpawnProtection, long spawnProtectionEndTime) {
        this.spawnProtection = hasSpawnProtection;
        this.spawnProtectionEndTime = spawnProtectionEndTime;
    }
    
    /**
     * Checks if the packet currently has spawn protection.
     */
    public boolean hasSpawnProtection() {
        if (!spawnProtection) return false;
        
        long currentTime = java.lang.System.currentTimeMillis();
        if (currentTime >= spawnProtectionEndTime) {
            spawnProtection = false;
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the destination system for this packet.
     */
    public System getDestinationSystem() {
        return wire.getEndPort().getParentSystem();
    }
    
    /**
     * Gets the source system for this packet.
     */
    public System getSourceSystem() {
        return wire.getStartPort().getParentSystem();
    }
    
    /**
     * Gets the spawn time of the packet.
     */
    public long getSpawnTime() {
        return spawnTime;
    }
    
    /**
     * Gets the spawn protection end time.
     */
    public long getSpawnProtectionEndTime() {
        return spawnProtectionEndTime;
    }
    
    /**
     * Checks if the packet was spawned by the player.
     */
    public boolean isPlayerSpawned() {
        return playerSpawned;
    }
    
    /**
     * Sets whether the packet was spawned by the player.
     */
    public void setPlayerSpawned(boolean playerSpawned) {
        this.playerSpawned = playerSpawned;
    }
    
    /**
     * Gets the wire this packet is traveling on.
     */
    public Wire getWire() {
        return wire;
    }
    
    /**
     * Gets the packet object.
     */
    public Packet getPacket() {
        return packet;
    }
}
