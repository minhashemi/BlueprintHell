package dev.aminhashemi.blueprinthell.model.world;

import java.awt.*;

public class ArcPoint {
    private Point position;
    public static final int SIZE = 8; // The visual size of the arc point

    public ArcPoint(Point position) {
        this.position = position;
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
    }

    public boolean contains(Point p) {
        // Check if the point p is within the arc point's circular area
        return position.distance(p) <= SIZE / 2.0;
    }
}
