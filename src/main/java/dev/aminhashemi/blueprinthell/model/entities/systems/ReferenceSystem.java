package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ReferenceSystem extends System {

    private final Random random = new Random();

    public ReferenceSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, 100, 80, data);
    }

    /**
     * Creates a new packet with a random type corresponding to one of its output ports
     * and tells the GameEngine to spawn it.
     * @param engine The GameEngine instance.
     */
    public void spawnRandomPacket(GameEngine engine) {
        if (outputPorts.isEmpty()) {
            return; // Can't spawn without output ports
        }

        // Create a list of possible packet types based on the system's output ports
        List<PacketType> possibleTypes = new ArrayList<>();
        for (Port port : outputPorts) {
            if (port.getType() == PortType.SQUARE) {
                possibleTypes.add(PacketType.SQUARE_MESSENGER);
            } else if (port.getType() == PortType.TRIANGLE) {
                possibleTypes.add(PacketType.TRIANGLE_MESSENGER);
            }
        }

        if (possibleTypes.isEmpty()) {
            return;
        }

        // Pick a random type from the list
        PacketType randomType = possibleTypes.get(random.nextInt(possibleTypes.size()));
        Packet newPacket = new MessengerPacket(this.x, this.y, randomType);

        // Pass the new packet to the engine to be placed on a wire
        engine.spawnPacket(newPacket, this);
    }


    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.CYAN);
        g.fillRect(x, y, width, height);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.drawString("REF", x + 10, y + 20);

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update() {
        // This is now empty as spawning is no longer automatic.
    }
}
