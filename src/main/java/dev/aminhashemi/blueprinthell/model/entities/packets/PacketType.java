package dev.aminhashemi.blueprinthell.model.entities.packets;

import java.awt.Color;

public enum PacketType {
    // Phase 1 Packet Types
    SQUARE_MESSENGER(Color.decode("#FA8072")), // A salmon color
    TRIANGLE_MESSENGER(Color.decode("#98FB98")); // A pale green

    // In the future, you can add all other types here:
    // CONFIDENTIAL, VOLUMINOUS, etc.

    private final Color color;

    PacketType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
