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

        // Draw shape based on port type
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
            case DIAMOND:
                // Draw diamond
                Polygon diamond = new Polygon();
                int centerX = pos.x + PORT_SIZE / 2;
                int centerY = pos.y + PORT_SIZE / 2;
                diamond.addPoint(centerX, pos.y);
                diamond.addPoint(pos.x + PORT_SIZE, centerY);
                diamond.addPoint(centerX, pos.y + PORT_SIZE);
                diamond.addPoint(pos.x, centerY);
                g.fillPolygon(diamond);
                break;
            case INFINITY:
                // Draw infinity symbol
                g.fillOval(pos.x, pos.y, PORT_SIZE / 2, PORT_SIZE);
                g.fillOval(pos.x + PORT_SIZE / 2, pos.y, PORT_SIZE / 2, PORT_SIZE);
                break;
            case PADLOCK:
                // Draw padlock
                g.fillRect(pos.x + 2, pos.y + 4, PORT_SIZE - 4, PORT_SIZE - 4);
                g.fillRect(pos.x + 3, pos.y + 2, PORT_SIZE - 6, 3);
                break;
            case CAMOUFLAGE:
                // Draw camouflage pattern
                g.fillOval(pos.x + 1, pos.y + 1, PORT_SIZE - 2, PORT_SIZE - 2);
                g.setColor(Color.WHITE);
                g.fillOval(pos.x + 3, pos.y + 3, 4, 4);
                g.setColor(type.getColor());
                break;
            case VPN:
                // Draw VPN symbol
                Polygon shield = new Polygon();
                shield.addPoint(pos.x + PORT_SIZE / 2, pos.y);
                shield.addPoint(pos.x + PORT_SIZE, pos.y + PORT_SIZE / 3);
                shield.addPoint(pos.x + PORT_SIZE, pos.y + PORT_SIZE);
                shield.addPoint(pos.x, pos.y + PORT_SIZE);
                shield.addPoint(pos.x, pos.y + PORT_SIZE / 3);
                g.fillPolygon(shield);
                break;
            case MALICIOUS:
                // Draw malicious symbol
                Polygon warning = new Polygon();
                warning.addPoint(pos.x + PORT_SIZE / 2, pos.y);
                warning.addPoint(pos.x + PORT_SIZE, pos.y + PORT_SIZE);
                warning.addPoint(pos.x, pos.y + PORT_SIZE);
                g.fillPolygon(warning);
                break;
            case SPY:
                // Draw spy symbol
                g.fillOval(pos.x, pos.y, PORT_SIZE, PORT_SIZE);
                g.setColor(Color.WHITE);
                g.fillOval(pos.x + 2, pos.y + 2, PORT_SIZE - 4, PORT_SIZE - 4);
                g.setColor(Color.BLACK);
                g.fillOval(pos.x + 4, pos.y + 4, 2, 2);
                g.setColor(type.getColor());
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

    public PortType getType() {
        return type;
    }
    
    public int getIndex() {
        return index;
    }
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    /**
     * Gets the port ID
     */
    public String getId() {
        return parentSystem.getId() + "_" + (isInput ? "IN" : "OUT") + "_" + index;
    }
}
