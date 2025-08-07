package dev.aminhashemi.blueprinthell.model.world;

import java.awt.*;

public class ArcPoint {
    private Point position;
    private final Wire parentWire; // NEW: Reference to the parent wire
    public static final int SIZE = 8;

    public ArcPoint(Point position, Wire parentWire) {
        this.position = position;
        this.parentWire = parentWire;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.ORANGE);
        g.fillOval(position.x - SIZE / 2, position.y - SIZE / 2, SIZE, SIZE);
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
        // NEW: Tell the parent wire to update its path when this point moves
        parentWire.regeneratePath();
    }

    public boolean contains(Point p) {
        return position.distance(p) <= SIZE / 2.0;
    }
}
