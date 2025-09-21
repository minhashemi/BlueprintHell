package dev.aminhashemi.blueprinthell.core.factory;

import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.TestPacketType;

import java.awt.Point;

/**
 * Factory interface for creating packets.
 * 
 * This interface follows the Factory Pattern and allows for easy extension
 * of new packet types without modifying existing code.
 * 
 * It also follows the Open/Closed Principle - open for extension, closed for modification.
 */
public interface IPacketFactory {
    
    /**
     * Creates a packet of the specified type at the given position.
     * 
     * @param packetType The type of packet to create
     * @param position The position to create the packet at
     * @param playerSpawned Whether the packet was spawned by the player
     * @return The created packet, or null if creation failed
     */
    Packet createPacket(PacketType packetType, Point position, boolean playerSpawned);
    
    /**
     * Creates a packet of the specified test type at the given position.
     * 
     * @param testPacketType The test packet type to create
     * @param position The position to create the packet at
     * @param playerSpawned Whether the packet was spawned by the player
     * @return The created packet, or null if creation failed
     */
    Packet createTestPacket(TestPacketType testPacketType, Point position, boolean playerSpawned);
    
    /**
     * Creates a random packet from the available types.
     * 
     * @param position The position to create the packet at
     * @param playerSpawned Whether the packet was spawned by the player
     * @return The created packet, or null if creation failed
     */
    Packet createRandomPacket(Point position, boolean playerSpawned);
    
    /**
     * Gets all available packet types that can be created.
     * 
     * @return Array of available packet types
     */
    PacketType[] getAvailablePacketTypes();
    
    /**
     * Gets all available test packet types that can be created.
     * 
     * @return Array of available test packet types
     */
    TestPacketType[] getAvailableTestPacketTypes();
    
    /**
     * Checks if a packet type is supported by this factory.
     * 
     * @param packetType The packet type to check
     * @return True if the packet type is supported
     */
    boolean supportsPacketType(PacketType packetType);
    
    /**
     * Checks if a test packet type is supported by this factory.
     * 
     * @param testPacketType The test packet type to check
     * @return True if the test packet type is supported
     */
    boolean supportsTestPacketType(TestPacketType testPacketType);
}
