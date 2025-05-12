package me.minhashemi.view;

import me.minhashemi.controller.InputController;
import me.minhashemi.model.*;

import javax.swing.*;
import java.awt.*;

public class GameScreen extends JPanel {
    private final LevelData levelData;
    private final HUD hud;
    private final WireManager wireManager;
    private final InputController inputController;

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(new BorderLayout());

        this.hud = new HUD();
        this.wireManager = new WireManager(levelData);
        this.inputController = new InputController(this, levelData, wireManager, hud);

        for (Packet packet : levelData.packets) {
            packet.initializePorts();
        }

        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Render HUD
                hud.render(g, getWidth());

                // Render packets
                for (Packet packet : levelData.packets) {
                    drawPacket(g, packet);
                }

                // Render wires
                wireManager.render(g);
            }
        };
        canvasPanel.setOpaque(false);
        canvasPanel.setPreferredSize(new Dimension(800, 600)); // Or your desired size

        add(canvasPanel, BorderLayout.CENTER);

        GameControlsPanel controlsPanel = new GameControlsPanel(new GameControlsPanel.GameControlListener() {
            @Override
            public void onTimeForward() {
                hud.updateHUD(hud.getTemporalProgress() + 1, hud.getPacketLoss(), hud.getCoins());
                repaint();
            }

            @Override
            public void onTimeBackward() {
                hud.updateHUD(Math.max(0, hud.getTemporalProgress() - 1), hud.getPacketLoss(), hud.getCoins());
                repaint();
            }

            @Override
            public void onQuitToMenu() {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GameScreen.this);

                // Exit fullscreen
                topFrame.dispose();
                topFrame.setUndecorated(false);
                topFrame.setExtendedState(JFrame.NORMAL);

                // Now set the main menu as the content pane
                topFrame.setContentPane(new me.minhashemi.view.Window());
                topFrame.pack();         // pack to recalculate layout
                topFrame.setLocationRelativeTo(null); // center on screen
                topFrame.setVisible(true); // show again
            }


        });

        add(controlsPanel, BorderLayout.SOUTH);
    }

    private void drawPacket(Graphics g, Packet packet) {
        int x = packet.position.x;
        int y = packet.position.y;
        int height = packet.getHeight();

        g.setColor(Color.GRAY);
        g.fillRect(x, y, Config.PACKET_WIDTH, height);

        for (PacketPort input : packet.getInputPorts()) {
            drawPort(g, input);
        }

        for (PacketPort output : packet.getOutputPorts()) {
            drawPort(g, output);
        }
    }

    private void drawPort(Graphics g, PacketPort port) {
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
