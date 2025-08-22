package dev.aminhashemi.blueprinthell.model.entities.systems;

import java.awt.Color;

public enum PortType {
    // Phase 1 Port Types
    SQUARE(Color.decode("#FF4757")),        // Bright red (matches packet)
    TRIANGLE(Color.decode("#2ED573")),      // Bright green (matches packet)
    
    // Phase 2 Port Types
    DIAMOND(Color.decode("#00FFFF")),       // Pure cyan (matches packet - maximum visibility!)
    INFINITY(Color.decode("#FFFF00")),      // Pure yellow (matches packet - maximum visibility!)
    PADLOCK(Color.decode("#FF69B4")),       // Hot pink (matches packet - maximum visibility!)
    CAMOUFLAGE(Color.decode("#FFD700")),    // Pure gold (matches packet - maximum visibility!)
    VPN(Color.decode("#FF00FF")),           // Pure magenta (matches packet - maximum visibility!)
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
