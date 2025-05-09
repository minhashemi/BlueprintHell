package me.minhashemi.view;

import me.minhashemi.model.*;
import me.minhashemi.model.Config;

import javax.swing.*;
import java.awt.*;

public class GameScreen extends JPanel {
    private final LevelData levelData;


    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(null); // we'll manually position everything
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Packet packet : levelData.packets) {
            drawPacket(g, packet);
        }
    }

    private void drawPacket(Graphics g, Packet packet) {
        int maxPorts = Math.max(packet.inputs.size(), packet.outputs.size());
        int height = maxPorts * Config.STANDARD_HEIGHT;

        int x = packet.position.x;
        int y = packet.position.y;

        g.setColor(Color.GRAY);
        g.fillRect(x, y, Config.PACKET_WIDTH, height);

        for (int i = 0; i < packet.inputs.size(); i++) {
            drawPort(g, packet.inputs.get(i), x - Config.PORT_SIZE, y + i * Config.STANDARD_HEIGHT + (Config.STANDARD_HEIGHT - Config.PORT_SIZE) / 2);
        }

        for (int i = 0; i < packet.outputs.size(); i++) {
            drawPort(g, packet.outputs.get(i), x + Config.PACKET_WIDTH, y + i * Config.STANDARD_HEIGHT + (Config.STANDARD_HEIGHT - Config.PORT_SIZE) / 2);
        }
    }

    private void drawPort(Graphics g, PortType type, int x, int y) {
        g.setColor(Color.BLACK);
        if (type == PortType.SQUARE) {
            g.fillRect(x, y, Config.PORT_SIZE, Config.PORT_SIZE);
        } else {
            int[] xs = {x, x + Config.PORT_SIZE / 2, x + Config.PORT_SIZE};
            int[] ys = {y + Config.PORT_SIZE, y, y + Config.PORT_SIZE};
            g.fillPolygon(xs, ys, 3);
        }
    }
}
