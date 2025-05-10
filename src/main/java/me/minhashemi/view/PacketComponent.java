package me.minhashemi.view;

import me.minhashemi.model.Packet;
import me.minhashemi.model.PortType;
import me.minhashemi.model.Config;

import javax.swing.*;
import java.awt.*;

public class PacketComponent extends JComponent {
    private final Packet packet;
    private static final int WIDTH = 100;
    private static final int PORT_SIZE = 12;

    public PacketComponent(Packet packet) {
        this.packet = packet;
        setPreferredSize(new Dimension(WIDTH, packet.getHeight()));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int height = packet.getHeight();
        Graphics2D g2d = (Graphics2D) g;

        // Draw stacked square body
        g2d.setColor(Color.LIGHT_GRAY);
        int rows = Math.max(packet.getInputs().size(), packet.getOutputs().size());
        for (int i = 0; i < rows; i++) {
            g2d.fillRect(20, i * Config.STANDARD_HEIGHT, WIDTH - 40, Config.STANDARD_HEIGHT - 2);
        }

        g2d.setColor(Color.BLACK);
        g2d.drawRect(20, 0, WIDTH - 40, height);

        // Draw input ports (left side)
        for (int i = 0; i < packet.getInputs().size(); i++) {
            int y = i * Config.STANDARD_HEIGHT + Config.STANDARD_HEIGHT / 2;
            drawPort(g2d, 5, y, packet.getInputs().get(i)); // ← PortType directly
        }
        // Draw output ports (right side)
        for (int i = 0; i < packet.getOutputs().size(); i++) {
            int y = i * Config.STANDARD_HEIGHT + Config.STANDARD_HEIGHT / 2;
            drawPort(g2d, WIDTH - 5 - PORT_SIZE, y, packet.getOutputs().get(i)); // ← PortType directly
        }

    }

    private void drawPort(Graphics2D g2d, int x, int yCenter, PortType type) {
        if (type == PortType.SQUARE) {
            g2d.setColor(Color.BLUE);
            g2d.fillRect(x, yCenter - PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
            g2d.setColor(Color.BLACK);
            g2d.drawRect(x, yCenter - PORT_SIZE / 2, PORT_SIZE, PORT_SIZE);
        } else if (type == PortType.TRIANGLE) {
            g2d.setColor(Color.GREEN);
            int[] xPoints = {x, x + PORT_SIZE, x + PORT_SIZE / 2};
            int[] yPoints = {yCenter - PORT_SIZE / 2, yCenter - PORT_SIZE / 2, yCenter + PORT_SIZE / 2};
            g2d.fillPolygon(xPoints, yPoints, 3);
            g2d.setColor(Color.BLACK);
            g2d.drawPolygon(xPoints, yPoints, 3);
        }
    }
}
