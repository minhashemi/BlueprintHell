package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;

import java.awt.*;

public class MaliciousSystem extends System {

    public MaliciousSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, 100, 80, data);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Color.decode("#FF3838")); // Bright crimson for malicious
        g.fillRect(x, y, width, height);
        g.setColor(Color.WHITE); // White border for contrast
        g.drawRect(x, y, width, height);
        g.setColor(Color.WHITE); // White text for readability
        g.drawString("MAL", x + 10, y + 20);

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update(GameEngine engine) {
        // Malicious systems can interfere with packets passing through them
        // This will be implemented in the packet routing logic
    }
}
