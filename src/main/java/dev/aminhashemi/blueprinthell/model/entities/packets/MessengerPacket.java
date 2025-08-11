package dev.aminhashemi.blueprinthell.model.entities.packets;

import java.awt.*;

public class MessengerPacket extends Packet {

    private final PacketType type;

    public MessengerPacket(int x, int y, PacketType type) {
        super(x, y, 12, 12); // Packets are 12x12 pixels
        this.type = type;
    }

    @Override
    public void update() {
        // Packet movement logic will go here.
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(type.getColor());

        // Draw the correct shape based on the packet's type
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
        }
    }
}
