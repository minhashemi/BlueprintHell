package dev.aminhashemi.blueprinthell.model.entities.packets;

import java.awt.Color;
import dev.aminhashemi.blueprinthell.utils.Config;

public enum PacketType {
    // Phase 1 Packet Types
    SQUARE_MESSENGER(Config.Phase1Packets.SQUARE_MESSENGER_COLOR), // Bright red
    TRIANGLE_MESSENGER(Config.Phase1Packets.TRIANGLE_MESSENGER_COLOR), // Bright green

    // Phase 2 Packet Types
    GREEN_DIAMOND_SMALL(Config.Phase2Packets.GREEN_DIAMOND_SMALL_COLOR, Config.Phase2Packets.GREEN_DIAMOND_SMALL_SIZE, 0, Config.Phase2Packets.GREEN_DIAMOND_SMALL_COINS),      // Pure cyan - Size 2, 2 coins
    GREEN_DIAMOND_LARGE(Config.Phase2Packets.GREEN_DIAMOND_LARGE_COLOR, Config.Phase2Packets.GREEN_DIAMOND_LARGE_SIZE, 0, Config.Phase2Packets.GREEN_DIAMOND_LARGE_COINS),      // Pure lime green - Size 3, 3 coins
    INFINITY_SYMBOL(Config.Phase2Packets.INFINITY_SYMBOL_COLOR, Config.Phase2Packets.INFINITY_SYMBOL_SIZE, 0, Config.Phase2Packets.INFINITY_SYMBOL_COINS),          // Pure yellow - Size 1, 1 coin
    
    PADLOCK_ICON(Config.Phase2Packets.PADLOCK_ICON_COLOR, Config.Phase2Packets.PADLOCK_ICON_SIZE, 0, Config.Phase2Packets.PADLOCK_ICON_COINS),             // Hot pink - Size 2, 5 coins (protected)
    
    CAMOUFLAGE_ICON_SMALL(Config.Phase2Packets.CAMOUFLAGE_SMALL_COLOR, Config.Phase2Packets.CAMOUFLAGE_SMALL_SIZE, 0, Config.Phase2Packets.CAMOUFLAGE_SMALL_COINS),    // Pure gold - Size 4, 3 coins (confidential)
    CAMOUFLAGE_ICON_LARGE(Config.Phase2Packets.CAMOUFLAGE_LARGE_COLOR, Config.Phase2Packets.CAMOUFLAGE_LARGE_SIZE, 0, Config.Phase2Packets.CAMOUFLAGE_LARGE_COINS),    // Pure magenta - Size 6, 4 coins (confidential)
    
    // Bulk Packet Types
    BULK_PACKET_SMALL(Config.Phase2Packets.BULK_SMALL_COLOR, Config.Phase2Packets.BULK_SMALL_SIZE, 0, Config.Phase2Packets.BULK_SMALL_COINS),        // Brown - Size 8, 8 coins
    BULK_PACKET_LARGE(Config.Phase2Packets.BULK_LARGE_COLOR, Config.Phase2Packets.BULK_LARGE_SIZE, 0, Config.Phase2Packets.BULK_LARGE_COINS);      // Dark brown - Size 10, 10 coins

    private final Color color;
    private final int size;
    private final int baseSpeed;
    private final int coinReward;

    // Constructor for Phase 1 packets
    PacketType(Color color) {
        this(color, 1, 0, 0);
    }

    // Constructor for Phase 2 packets
    PacketType(Color color, int size, int baseSpeed, int coinReward) {
        this.color = color;
        this.size = size;
        this.baseSpeed = baseSpeed;
        this.coinReward = coinReward;
    }

    public Color getColor() {
        return color;
    }

    public int getSize() {
        return size;
    }

    public int getBaseSpeed() {
        return baseSpeed;
    }

    public int getCoinReward() {
        return coinReward;
    }
}
