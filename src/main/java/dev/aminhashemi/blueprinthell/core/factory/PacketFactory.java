package dev.aminhashemi.blueprinthell.core.factory;

import dev.aminhashemi.blueprinthell.model.entities.packets.*;
import dev.aminhashemi.blueprinthell.model.TestPacketType;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.awt.Point;
import java.util.Random;

/**
 * Concrete implementation of IPacketFactory.
 * 
 * This class follows the Factory Pattern and provides a centralized way
 * to create packets of different types. It's easily extensible for new
 * packet types without modifying existing code.
 * 
 * It also follows the Single Responsibility Principle by focusing solely
 * on packet creation.
 */
public class PacketFactory implements IPacketFactory {
    
    private static final Logger logger = Logger.getInstance();
    private static final Random random = new Random();
    
    /**
     * Creates a packet of the specified type at the given position.
     * 
     * @param packetType The type of packet to create
     * @param position The position to create the packet at
     * @param playerSpawned Whether the packet was spawned by the player
     * @return The created packet, or null if creation failed
     */
    @Override
    public Packet createPacket(PacketType packetType, Point position, boolean playerSpawned) {
        if (packetType == null || position == null) {
            logger.warning("Cannot create packet: packetType or position is null");
            return null;
        }
        
        try {
            return switch (packetType) {
                case SQUARE_MESSENGER, TRIANGLE_MESSENGER -> 
                    new MessengerPacket(position.x, position.y, packetType);
                    
                case GREEN_DIAMOND_SMALL, GREEN_DIAMOND_LARGE, INFINITY_SYMBOL, PADLOCK_ICON -> 
                    new ProtectedPacket(position.x, position.y, packetType);
                    
                case CAMOUFLAGE_ICON_SMALL, CAMOUFLAGE_ICON_LARGE -> 
                    new ConfidentialPacket(position.x, position.y, 
                        packetType == PacketType.CAMOUFLAGE_ICON_SMALL ? 
                            ConfidentialPacket.ConfidentialType.SMALL : 
                            ConfidentialPacket.ConfidentialType.LARGE);
                    
                case BULK_PACKET_SMALL, BULK_PACKET_LARGE -> 
                    new BulkPacket(position.x, position.y, 
                        packetType == PacketType.BULK_PACKET_SMALL ? 
                            BulkPacket.BulkType.SMALL : 
                            BulkPacket.BulkType.LARGE);
                    
                case TROJAN_PACKET -> 
                    new TrojanPacket(position.x, position.y, packetType);
                    
                default -> {
                    logger.warning("Unknown packet type: " + packetType);
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Failed to create packet of type " + packetType, e);
            return null;
        }
    }
    
    /**
     * Creates a packet of the specified test type at the given position.
     * 
     * @param testPacketType The test packet type to create
     * @param position The position to create the packet at
     * @param playerSpawned Whether the packet was spawned by the player
     * @return The created packet, or null if creation failed
     */
    @Override
    public Packet createTestPacket(TestPacketType testPacketType, Point position, boolean playerSpawned) {
        if (testPacketType == null || position == null) {
            logger.warning("Cannot create test packet: testPacketType or position is null");
            return null;
        }
        
        try {
            PacketType packetType = testPacketType.toPacketType();
            return createPacket(packetType, position, playerSpawned);
        } catch (Exception e) {
            logger.error("Failed to create test packet of type " + testPacketType, e);
            return null;
        }
    }
    
    /**
     * Creates a random packet from the available types.
     * 
     * @param position The position to create the packet at
     * @param playerSpawned Whether the packet was spawned by the player
     * @return The created packet, or null if creation failed
     */
    @Override
    public Packet createRandomPacket(Point position, boolean playerSpawned) {
        PacketType[] availableTypes = getAvailablePacketTypes();
        if (availableTypes.length == 0) {
            logger.warning("No available packet types for random creation");
            return null;
        }
        
        PacketType randomType = availableTypes[random.nextInt(availableTypes.length)];
        return createPacket(randomType, position, playerSpawned);
    }
    
    /**
     * Gets all available packet types that can be created.
     * 
     * @return Array of available packet types
     */
    @Override
    public PacketType[] getAvailablePacketTypes() {
        return PacketType.values();
    }
    
    /**
     * Gets all available test packet types that can be created.
     * 
     * @return Array of available test packet types
     */
    @Override
    public TestPacketType[] getAvailableTestPacketTypes() {
        return TestPacketType.values();
    }
    
    /**
     * Checks if a packet type is supported by this factory.
     * 
     * @param packetType The packet type to check
     * @return True if the packet type is supported
     */
    @Override
    public boolean supportsPacketType(PacketType packetType) {
        return packetType != null;
    }
    
    /**
     * Checks if a test packet type is supported by this factory.
     * 
     * @param testPacketType The test packet type to check
     * @return True if the test packet type is supported
     */
    @Override
    public boolean supportsTestPacketType(TestPacketType testPacketType) {
        return testPacketType != null;
    }
    
    /**
     * Creates a packet with random properties for testing.
     * 
     * @param position The position to create the packet at
     * @param playerSpawned Whether the packet was spawned by the player
     * @return The created packet, or null if creation failed
     */
    public Packet createRandomTestPacket(Point position, boolean playerSpawned) {
        TestPacketType[] availableTypes = getAvailableTestPacketTypes();
        if (availableTypes.length == 0) {
            logger.warning("No available test packet types for random creation");
            return null;
        }
        
        TestPacketType randomType = availableTypes[random.nextInt(availableTypes.length)];
        return createTestPacket(randomType, position, playerSpawned);
    }
}
