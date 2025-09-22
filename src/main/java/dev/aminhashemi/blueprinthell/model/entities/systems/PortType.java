package dev.aminhashemi.blueprinthell.model.entities.systems;

import java.awt.Color;
import dev.aminhashemi.blueprinthell.utils.Config;

public enum PortType {
    // Phase 1 Port Types
    SQUARE(Config.PortColors.SQUARE_COLOR),        // Bright red
    TRIANGLE(Config.PortColors.TRIANGLE_COLOR),      // Bright green
    
    // Phase 2 Port Types
    DIAMOND(Config.PortColors.DIAMOND_COLOR),       // Pure cyan
    INFINITY(Config.PortColors.INFINITY_COLOR),      // Pure yellow
    PADLOCK(Config.PortColors.PADLOCK_COLOR),       // Hot pink
    VPN(Config.PortColors.VPN_COLOR),           // Pure magenta
    MALICIOUS(Config.PortColors.MALICIOUS_COLOR),     // Bright crimson
    SPY(Config.PortColors.SPY_COLOR);           // Bright orange-red

    private final Color color;

    PortType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
