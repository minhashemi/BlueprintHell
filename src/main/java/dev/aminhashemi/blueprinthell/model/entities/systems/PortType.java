package dev.aminhashemi.blueprinthell.model.entities.systems;

import java.awt.Color;

public enum PortType {
    SQUARE(Color.RED),
    TRIANGLE(Color.GREEN);
    // In the future, you can add more types here
    // CIRCLE(Color.BLUE), etc.

    private final Color color;

    PortType(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
