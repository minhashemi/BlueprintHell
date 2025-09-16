package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import java.awt.*;

public class MessengerPacket extends Packet {

    private final PacketType type;

    public MessengerPacket(int x, int y, PacketType type) {
        super(x, y, 12, 12);
        this.type = type;
    }

    @Override
    public void update(GameEngine engine) {
        // Movement handled by MovingPacket
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(type.getColor());

        switch (type) {
            case SQUARE_MESSENGER:
                g.fillRect(x, y, width, height);
                break;
            case TRIANGLE_MESSENGER:
                Polygon triangle = new Polygon();
                triangle.addPoint(x + width / 2, y);
                triangle.addPoint(x, y + height);
                triangle.addPoint(x + width, y + height);
                g.fillPolygon(triangle);
                break;
            case GREEN_DIAMOND_SMALL:
            case GREEN_DIAMOND_LARGE:
                // Draw diamond
                Polygon diamond = new Polygon();
                int centerX = x + width / 2;
                int centerY = y + height / 2;
                diamond.addPoint(centerX, y);
                diamond.addPoint(x + width, centerY);
                diamond.addPoint(centerX, y + height);
                diamond.addPoint(x, centerY);
                g.fillPolygon(diamond);
                break;
            case INFINITY_SYMBOL:
                // Draw infinity symbol
                g.fillOval(x, y, width / 2, height);
                g.fillOval(x + width / 2, y, width / 2, height);
                break;
            case PADLOCK_ICON:
                // Draw padlock
                g.fillRect(x + 2, y + 4, width - 4, height - 4);
                g.fillRect(x + 3, y + 2, width - 6, 3);
                break;
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                // Draw camouflage pattern
                g.fillOval(x + 1, y + 1, width - 2, height - 2);
                g.setColor(Color.WHITE);
                g.fillOval(x + 3, y + 3, 6, 6);
                g.setColor(type.getColor());
                break;
            case TROJAN_PACKET:
                // Draw trojan packet as corrupted square
                g.fillRect(x, y, width, height);
                g.setColor(Color.RED);
                g.drawRect(x - 1, y - 1, width + 2, height + 2);
                break;
            default:
                // Fallback: draw as circle
                g.fillOval(x, y, width, height);
                break;
        }
    }

    @Override
    public PacketType getType() {
        return this.type;
    }
}
