package me.minhashemi.view;

import me.minhashemi.model.block.PortType;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.Config;
import me.minhashemi.model.MovingPacket;
import me.minhashemi.view.wire.WireManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class CanvasPanel extends JPanel {
    private final LevelData levelData;
    private final HUD hud;
    private final WireManager wireManager;
    private final List<MovingPacket> movingPackets;
    private final int totalPackets;

    public CanvasPanel(LevelData levelData, HUD hud, WireManager wireManager, List<MovingPacket> movingPackets, int totalPackets) {
        this.levelData = levelData;
        this.hud = hud;
        this.wireManager = wireManager;
        this.movingPackets = movingPackets;
        this.totalPackets = totalPackets;
        setOpaque(false);
        setPreferredSize(new Dimension(800, 600));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        hud.render(g, getWidth());

        for (NetSys netsys : levelData.packets) {
            drawNetSys(g, netsys);
        }

        wireManager.render(g);

        for (MovingPacket packet : movingPackets) {
            packet.draw(g2);
        }
    }

    private void drawNetSys(Graphics g, NetSys packet) {
        int x = packet.position.x;
        int y = packet.position.y;
        int width = Config.NETSYS_WIDTH;
        int height = packet.getHeight();

        packet.updateConnectionStatus();

        g.setColor(Color.GRAY);
        g.fillRect(x, y, width, height);

        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, width, height);

        g.setColor(packet.hasReceivedPacket() ? Color.GREEN : Color.RED);
        g.fillOval(x + width - 10, y + 4, 8, 8);

        for (NetSysPort input : packet.getInputPorts()) {
            drawPort(g, input);
        }
        for (NetSysPort output : packet.getOutputPorts()) {
            drawPort(g, output);
        }
    }

    private void drawPort(Graphics g, NetSysPort port) {
        g.setColor(Color.BLACK);
        Point pos = port.getPosition();

        if (port.getType() == PortType.SQUARE) {
            g.fillRect(pos.x, pos.y, Config.PORT_SIZE, Config.PORT_SIZE);
        } else {
            int[] xs = {pos.x, pos.x + Config.PORT_SIZE / 2, pos.x + Config.PORT_SIZE};
            int[] ys = {pos.y + Config.PORT_SIZE, pos.y, pos.y + Config.PORT_SIZE};
            g.fillPolygon(xs, ys, 3);
        }
    }
}