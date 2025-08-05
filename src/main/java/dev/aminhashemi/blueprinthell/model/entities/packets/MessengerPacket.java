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
        // Movement logic is handled by MovingPacket, so this is empty for now.
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
        }
    }

    @Override
    public PacketType getType() {
        return this.type;
    }
}
