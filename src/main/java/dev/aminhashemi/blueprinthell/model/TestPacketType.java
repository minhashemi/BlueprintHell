package dev.aminhashemi.blueprinthell.model;

import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;

/**
 * Enum representing different packet types that can be used in testing
 * Each type has different characteristics and spawn probabilities
 */
public enum TestPacketType {
    MESSENGER("Messenger", 0.3),      // 30% chance
    BULK("Bulk", 0.25),               // 25% chance  
    SPY("Spy", 0.2),                  // 20% chance
    MALICIOUS("Malicious", 0.15),     // 15% chance
    VPN("VPN", 0.1);                  // 10% chance
    
    private final String displayName;
    private final double spawnProbability;
    
    TestPacketType(String displayName, double spawnProbability) {
        this.displayName = displayName;
        this.spawnProbability = spawnProbability;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getSpawnProbability() {
        return spawnProbability;
    }
    
    /**
     * Selects a random packet type based on their spawn probabilities
     */
    public static TestPacketType getRandomPacketType() {
        double random = Math.random();
        double cumulativeProbability = 0.0;
        
        for (TestPacketType type : values()) {
            cumulativeProbability += type.spawnProbability;
            if (random <= cumulativeProbability) {
                return type;
            }
        }
        
        // Fallback to MESSENGER if something goes wrong
        return MESSENGER;
    }
    
    /**
     * Converts to the actual PacketType used in the game
     */
    public PacketType toPacketType() {
        switch (this) {
            case MESSENGER:
                return PacketType.SQUARE_MESSENGER;
            case BULK:
                return PacketType.BULK_PACKET_SMALL;
            case SPY:
                return PacketType.CAMOUFLAGE_ICON_SMALL;
            case MALICIOUS:
                return PacketType.TROJAN_PACKET;
            case VPN:
                return PacketType.PADLOCK_ICON;
            default:
                return PacketType.SQUARE_MESSENGER;
        }
    }
}
