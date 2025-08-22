package dev.aminhashemi.blueprinthell.model.entities.packets;

import java.awt.Color;

public enum PacketType {
    // Phase 1 Packet Types
    SQUARE_MESSENGER(Color.decode("#FF4757")), // Bright red
    TRIANGLE_MESSENGER(Color.decode("#2ED573")), // Bright green

    // Phase 2 Packet Types
    GREEN_DIAMOND_SMALL(Color.decode("#00FFFF")),      // Pure cyan
    GREEN_DIAMOND_LARGE(Color.decode("#00FF00")),      // Pure lime green
    INFINITY_SYMBOL(Color.decode("#FFFF00")),          // Pure yellow
    
    PADLOCK_ICON(Color.decode("#FF69B4")),             // Hot pink
    
    CAMOUFLAGE_ICON_SMALL(Color.decode("#FFD700")),    // Pure gold
    CAMOUFLAGE_ICON_LARGE(Color.decode("#FF00FF"));    // Pure magenta

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
