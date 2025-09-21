package dev.aminhashemi.blueprinthell.core.interfaces;

import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.TestConfiguration;

import java.awt.Point;
import java.util.List;

/**
 * Interface for managing packet operations.
 * Follows Interface Segregation Principle by focusing only on packet management.
 */
public interface PacketManager {
    
    /**
     * Spawns a packet at the specified location
     * @param x X coordinate
     * @param y Y coordinate
     * @param packetType Type of packet to spawn
     */
    void spawnPacket(int x, int y, String packetType);
    
    /**
     * Spawns a test packet using configuration
     * @param x X coordinate
     * @param y Y coordinate
     */
    void spawnTestPacket(int x, int y);
    
    /**
     * Handles packet arrival at a system
     * @param packet The packet that arrived
     * @param system The system it arrived at
     */
    void handlePacketArrival(Packet packet, System system);
    
    /**
     * Updates all moving packets
     * @param deltaTime Time since last update
     */
    void updatePackets(double deltaTime);
    
    /**
     * Gets all moving packets
     * @return List of moving packets
     */
    List<MovingPacket> getMovingPackets();
    
    /**
     * Clears all packets
     */
    void clearPackets();
    
    /**
     * Sets the test configuration
     * @param config Test configuration
     */
    void setTestConfiguration(TestConfiguration config);
    
    /**
     * Gets the test configuration
     * @return Test configuration
     */
    TestConfiguration getTestConfiguration();
}
