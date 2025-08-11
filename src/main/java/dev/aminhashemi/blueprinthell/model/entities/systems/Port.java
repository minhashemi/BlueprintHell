package dev.aminhashemi.blueprinthell.model.entities.systems;

import java.awt.*;

public class Port {

    private final PortType type;
    private final System parentSystem;
    private final boolean isInput;
    private final int index;

    public static final int PORT_SIZE = 10;

    public Port(PortType type, System parentSystem, boolean isInput, int index) {
        this.type = type;
        this.parentSystem = parentSystem;
        this.isInput = isInput;
        this.index = index;
    }

    public void draw(Graphics2D g) {
        g.setColor(type.getColor());
        Point pos = getPosition();

        // Draw the correct shape based on the port's type
        switch (type) {
            case SQUARE:
                g.fillRect(pos.x, pos.y, PORT_SIZE, PORT_SIZE);
                break;
            case TRIANGLE:
                Polygon triangle = new Polygon();
                if (isInput) {
                    // Pointing right towards the system
                    triangle.addPoint(pos.x, pos.y);
                    triangle.addPoint(pos.x, pos.y + PORT_SIZE);
                    triangle.addPoint(pos.x + PORT_SIZE, pos.y + PORT_SIZE / 2);
                } else {
                    // Pointing left away from the system
                    triangle.addPoint(pos.x + PORT_SIZE, pos.y);
                    triangle.addPoint(pos.x + PORT_SIZE, pos.y + PORT_SIZE);
                    triangle.addPoint(pos.x, pos.y + PORT_SIZE / 2);
                }
                g.fillPolygon(triangle);
                break;
        }
    }

    public Point getPosition() {
        int parentX = parentSystem.getX();
        int parentY = parentSystem.getY();
        int parentHeight = parentSystem.getHeight();

        int portCount = isInput ? parentSystem.getInputPorts().size() : parentSystem.getOutputPorts().size();
        if (portCount == 0) portCount = 1; // Prevent division by zero

        int yOffset = (parentHeight / (portCount + 1)) * (index + 1);
        int yPos = parentY + yOffset - (PORT_SIZE / 2);

        int xPos;
        if (isInput) {
            xPos = parentX - PORT_SIZE;
        } else {
            xPos = parentX + parentSystem.getWidth();
        }

        return new Point(xPos, yPos);
    }

    public Point getCenter() {
        Point pos = getPosition();
        return new Point(pos.x + PORT_SIZE / 2, pos.y + PORT_SIZE / 2);
    }

    public boolean contains(Point p) {
        return new Rectangle(getPosition(), new Dimension(PORT_SIZE, PORT_SIZE)).contains(p);
    }

    public boolean isInput() {
        return isInput;
    }

    public System getParentSystem() {
        return parentSystem;
    }
}
