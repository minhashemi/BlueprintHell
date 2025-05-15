package me.minhashemi.view;

import me.minhashemi.controller.InputController;
import me.minhashemi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GameScreen extends JPanel {
    private final LevelData levelData;
    private final HUD hud;
    private final WireManager wireManager;
    private final InputController inputController;
    private final List<MovingPacket> movingPackets = new ArrayList<>();

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(new BorderLayout());

        this.hud = new HUD();
        this.wireManager = new WireManager(levelData);
        this.inputController = new InputController(this, levelData, wireManager, hud);

        for (NetSys netsys : levelData.packets) {
            netsys.initializePorts();
        }

        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Render HUD
                hud.render(g, getWidth());

                // Render NetSys devices
                for (NetSys netsys : levelData.packets) {
                    drawNetSys(g, netsys);
                }

                // Render Wires
                wireManager.render(g);

                // Render and update moving packets
                Iterator<MovingPacket> iterator = movingPackets.iterator();
                while (iterator.hasNext()) {
                    MovingPacket packet = iterator.next();
                    packet.update();

                    if (packet.isLost() || packet.isArrived()) {
                        iterator.remove(); // Remove finished packets
                    } else {
                        packet.draw(g2);
                    }
                }
            }
        };
        canvasPanel.setOpaque(false);
        canvasPanel.setPreferredSize(new Dimension(800, 600));

        // Add canvas and controls
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
                topFrame.dispose();
                topFrame.setUndecorated(false);
                topFrame.setExtendedState(JFrame.NORMAL);
                topFrame.setContentPane(new me.minhashemi.view.Window());
                topFrame.pack();
                topFrame.setLocationRelativeTo(null);
                topFrame.setVisible(true);
            }
        });
        add(controlsPanel, BorderLayout.SOUTH);

        // Set up key binding for spawning packets
        setupKeyBinding(canvasPanel);

        // Timer for animation (repaint every 16ms)
        Timer timer = new Timer(16, e -> canvasPanel.repaint());
        timer.start();
    }

    private void setupKeyBinding(JComponent component) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "spawnPacket");
        component.getActionMap().put("spawnPacket", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spawnPackets();
            }
        });
    }

    private void spawnPackets() {
        for (NetSys netSys : levelData.packets) {
            for (NetSysPort output : netSys.getOutputPorts()) {
                if (output.isConnected()) {
                    for (WireManager.Wire wire : wireManager.getWires()) {
                        if (wire.getFromPort() == output) {
                            MovingPacket packet = new MovingPacket(wire);
                            movingPackets.add(packet);
                            break;
                        }
                    }
                }
            }
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

        g.setColor(packet.isFullyConnected() ? Color.GREEN : Color.RED);
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
