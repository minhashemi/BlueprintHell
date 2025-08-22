package dev.aminhashemi.blueprinthell.model.entities.systems;

import java.awt.Color;

public enum PortType {
    // Phase 1 Port Types
    SQUARE(Color.decode("#FF4757")),        // Bright red
    TRIANGLE(Color.decode("#2ED573")),      // Bright green
    
    // Phase 2 Port Types
    DIAMOND(Color.decode("#00FFFF")),       // Pure cyan
    INFINITY(Color.decode("#FFFF00")),      // Pure yellow
    PADLOCK(Color.decode("#FF69B4")),       // Hot pink
    CAMOUFLAGE(Color.decode("#FFD700")),    // Pure gold
    VPN(Color.decode("#FF00FF")),           // Pure magenta
    MALICIOUS(Color.decode("#FF3838")),     // Bright crimson
    SPY(Color.decode("#FF9F43"));           // Bright orange-red

    private final Color color;

    PortType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
