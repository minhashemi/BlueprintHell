package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.entities.packets.ConfidentialPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.BulkPacket;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReferenceSystem extends System {

    private final Random random = new Random();

    public ReferenceSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, 100, 80, data);
    }

    public void spawnRandomPacket(GameEngine engine) {
        if (outputPorts.isEmpty()) {
            return;
        }

        List<PacketType> possibleTypes = new ArrayList<>();
        for (Port port : outputPorts) {
            switch (port.getType()) {
                case SQUARE:
                    possibleTypes.add(PacketType.SQUARE_MESSENGER);
                    break;
                case TRIANGLE:
                    possibleTypes.add(PacketType.TRIANGLE_MESSENGER);
                    break;
                case DIAMOND:
                    possibleTypes.add(PacketType.GREEN_DIAMOND_SMALL);
                    possibleTypes.add(PacketType.GREEN_DIAMOND_LARGE);
                    break;
                case INFINITY:
                    possibleTypes.add(PacketType.INFINITY_SYMBOL);
                    break;
                case PADLOCK:
                    possibleTypes.add(PacketType.PADLOCK_ICON);
                    break;
                case CAMOUFLAGE:
                    possibleTypes.add(PacketType.CAMOUFLAGE_ICON_SMALL);
                    possibleTypes.add(PacketType.CAMOUFLAGE_ICON_LARGE);
                    break;
                case VPN:
                    possibleTypes.add(PacketType.PADLOCK_ICON);
                    break;
            }
        }

        if (possibleTypes.isEmpty()) {
            return;
        }

        PacketType randomType = possibleTypes.get(random.nextInt(possibleTypes.size()));
        Packet newPacket = createPacketByType(this.x, this.y, randomType);

        engine.spawnPacket(newPacket, this);
    }

    /**
     * Creates the appropriate packet type based on the packet type
     */
    private Packet createPacketByType(int x, int y, PacketType packetType) {
        switch (packetType) {
            case CAMOUFLAGE_ICON_SMALL:
                return new ConfidentialPacket(x, y, ConfidentialPacket.ConfidentialType.SMALL);
            case CAMOUFLAGE_ICON_LARGE:
                return new ConfidentialPacket(x, y, ConfidentialPacket.ConfidentialType.LARGE);
            case BULK_PACKET_SMALL:
                return new BulkPacket(x, y, BulkPacket.BulkType.SMALL);
            case BULK_PACKET_LARGE:
                return new BulkPacket(x, y, BulkPacket.BulkType.LARGE);
            default:
                // All other packet types use MessengerPacket
                return new MessengerPacket(x, y, packetType);
        }
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.decode("#00D2FF")); // Bright cyan
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE); // White border
        g.drawRect(x, y, width, height);
        g.setColor(Color.BLACK); // Black text
        g.drawString("REF", x + 10, y + 20);

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update(GameEngine engine) {
        // Manual spawning only for now
    }
}
