package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.model.LevelData;

import java.awt.*;

public class ReferenceSystem extends System {

    public ReferenceSystem(int x, int y, LevelData.SystemData data) {
        // Pass the data up to the parent System constructor to create the ports
        super(x, y, 100, 80, data);
    }

    @Override
    public void update() {
        // Logic for updating the system state will go here
    }

    @Override
    public void draw(Graphics2D g) {
        // Draw the main body
        g.setColor(Color.CYAN);
        g.fillRect(x, y, width, height);

        g.setColor(Color.BLACK);
        g.drawRect(x, y, width, height);
        g.drawString("REF", x + 10, y + 20);

        // --- NEW: Draw all the ports ---
        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }
}
