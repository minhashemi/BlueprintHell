package dev.aminhashemi.blueprinthell.model.entities.packets;

import java.awt.Color;

public enum PacketType {
    // Phase 1 Packet Types
    SQUARE_MESSENGER(Color.decode("#FF4757")), // Bright red
    TRIANGLE_MESSENGER(Color.decode("#2ED573")), // Bright green

    // Phase 2 Packet Types
    GREEN_DIAMOND_SMALL(Color.decode("#00FFFF"), 2, 0, 2),      // Pure cyan - Size 2, 2 coins
    GREEN_DIAMOND_LARGE(Color.decode("#00FF00"), 3, 0, 3),      // Pure lime green - Size 3, 3 coins
    INFINITY_SYMBOL(Color.decode("#FFFF00"), 1, 0, 1),          // Pure yellow - Size 1, 1 coin
    
    PADLOCK_ICON(Color.decode("#FF69B4"), 2, 0, 5),             // Hot pink - Size 2, 5 coins (protected)
    
    CAMOUFLAGE_ICON_SMALL(Color.decode("#FFD700"), 4, 0, 3),    // Pure gold - Size 4, 3 coins (confidential)
    CAMOUFLAGE_ICON_LARGE(Color.decode("#FF00FF"), 6, 0, 4),    // Pure magenta - Size 6, 4 coins (confidential)
    
    // Bulk Packet Types
    BULK_PACKET_SMALL(Color.decode("#8B4513"), 8, 0, 8),        // Brown - Size 8, 8 coins
    BULK_PACKET_LARGE(Color.decode("#654321"), 10, 0, 10);      // Dark brown - Size 10, 10 coins

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
