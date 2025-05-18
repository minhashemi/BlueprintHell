package me.minhashemi.view;

import me.minhashemi.controller.InputController;
import me.minhashemi.model.*;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.block.PortType;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.model.level.LevelLoader;
import me.minhashemi.model.shop.ShopItem;
import me.minhashemi.model.shop.ShopPanel;
import me.minhashemi.controller.audio.player;
import me.minhashemi.view.wire.Wire;
import me.minhashemi.view.wire.WireManager;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class GameScreen extends JPanel {
    private final LevelData levelData;
    private final HUD hud;
    private final WireManager wireManager;
    private final InputController inputController;
    private final List<MovingPacket> movingPackets = new ArrayList<>();
    private boolean victoryShown = false;

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(new BorderLayout());

        this.hud = new HUD();
        this.wireManager = new WireManager(levelData);
        this.inputController = new InputController(this, levelData, wireManager, hud);

        // Initialize ports for all NetSys
        for (NetSys netsys : levelData.packets) {
            netsys.initializePorts();
        }

        // Canvas panel to draw everything
        JPanel canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                hud.render(g, getWidth());

                // Draw each NetSys
                for (NetSys netsys : levelData.packets) {
                    drawNetSys(g, netsys);
                }

                // Draw all wires
                wireManager.render(g);

                // Update and draw moving packets
                Iterator<MovingPacket> iterator = movingPackets.iterator();
                List<MovingPacket> toAdd = new ArrayList<>();

                while (iterator.hasNext()) {
                    MovingPacket packet = iterator.next();
                    packet.update();

                    if (packet.isLost()) {
                        iterator.remove();
                    } else if (packet.isArrived()) {
                        iterator.remove();
                        packet.getWire().setHasPacket(false);

                        NetSysPort toPort = packet.getWire().getToPort();
                        NetSys targetSys = toPort.getParent();
                        targetSys.markReceived();

                        // Spawn one new packet from a random empty output port if available
                        List<NetSysPort> availableOutputs = new ArrayList<>();
                        for (NetSysPort out : targetSys.getOutputPorts()) {
                            if (out.isConnected()) {
                                for (Wire wire : wireManager.getWires()) {
                                    if (wire.getFromPort() == out && !wire.hasPacket()) {
                                        availableOutputs.add(out);
                                        break;
                                    }
                                }
                            }
                        }

                        if (!availableOutputs.isEmpty()) {
                            NetSysPort selectedOutput = availableOutputs.get(new Random().nextInt(availableOutputs.size()));
                            for (Wire wire : wireManager.getWires()) {
                                if (wire.getFromPort() == selectedOutput && !wire.hasPacket()) {
                                    // Use the same packet type as the incoming packet
                                    toAdd.add(new MovingPacket(wire, packet.getType()));
                                    wire.setHasPacket(true);
                                    break;
                                }
                            }
                        }
                    } else {
                        packet.draw(g2);
                    }
                }

                movingPackets.addAll(toAdd);

                // Victory check: no moving packets and all blocks green
                if (!victoryShown && movingPackets.isEmpty() && allBlocksGreen()) {
                    victoryShown = true;
                    player.playEffect("victory");
                    SwingUtilities.invokeLater(() -> showVictoryMessage("Victory! All packets delivered."));
                }
            }
        };

        canvasPanel.setOpaque(false);
        canvasPanel.setPreferredSize(new Dimension(800, 600));
        add(canvasPanel, BorderLayout.CENTER);

        // Controls panel at the bottom
        GameControlsPanel controlsPanel = new GameControlsPanel(new GameControlsPanel.GameControlListener() {
            @Override
            public void onShop() {
                openShop();
            }

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
                if (topFrame instanceof Window) {
                    ((Window) topFrame).quitToMenuFromGame();
                }
            }
        });
        add(controlsPanel, BorderLayout.SOUTH);

        setupKeyBinding(canvasPanel);

        // Timer for repainting at ~60 FPS
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
            if (netSys.getInputPorts().isEmpty()) {
                boolean spawned = false;
                for (NetSysPort output : netSys.getOutputPorts()) {
                    if (output.isConnected()) {
                        for (Wire wire : wireManager.getWires()) {
                            if (wire.getFromPort() == output && !wire.hasPacket()) {
                                PortType[] types = PortType.values();
                                PortType randomShape = types[new Random().nextInt(types.length)];
                                movingPackets.add(new MovingPacket(wire, randomShape));
                                wire.setHasPacket(true);
                                spawned = true;
                                break;
                            }
                        }
                    }
                    if (spawned) break;
                }
                if (spawned) {
                    netSys.markReceived();
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

    private boolean allBlocksGreen() {
        for (NetSys netsys : levelData.packets) {
            if (!netsys.hasReceivedPacket()) return false;
        }
        return true;
    }

    public void showVictoryMessage(String msg) {
        JPanel overlay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(0, 0, 0, 180)); // semi-transparent black
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        overlay.setLayout(new GridBagLayout());
        overlay.setOpaque(false);
        overlay.setBounds(0, 0, getWidth(), getHeight());
        overlay.setFocusable(false);

        JPanel victoryPanel = new JPanel();
        victoryPanel.setLayout(new BoxLayout(victoryPanel, BoxLayout.Y_AXIS));
        victoryPanel.setBackground(Color.DARK_GRAY);
        victoryPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel messageLabel = new JLabel(msg, SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Button to go to next level
        JButton nextLevelButton = new JButton("Next Level");
        nextLevelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextLevelButton.addActionListener(e -> {
            // Remove the overlay and trigger your "load next level" logic
            Container topFrame = SwingUtilities.getWindowAncestor(this);
            if (topFrame instanceof JFrame frame) {
                frame.getContentPane().removeAll();

                Config.lastPlayedStage = Config.lastPlayedStage + 1;
                LevelData nextLevel = LevelLoader.loadLevel(Config.lastPlayedStage);
                GameScreen nextGame = new GameScreen(nextLevel);
                frame.setContentPane(nextGame);
                frame.revalidate();
                frame.repaint();
            }
        });

        // Button to close the dialog
        JButton closeButton = new JButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> {
            GameScreen.this.remove(overlay);
            GameScreen.this.revalidate();
            GameScreen.this.repaint();
        });

        victoryPanel.add(Box.createVerticalGlue());
        victoryPanel.add(messageLabel);
        victoryPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        victoryPanel.add(nextLevelButton);
        victoryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        victoryPanel.add(closeButton);
        victoryPanel.add(Box.createVerticalGlue());

        overlay.add(victoryPanel, new GridBagConstraints());

        setLayout(null);
        overlay.setSize(getSize());
        add(overlay);
        setComponentZOrder(overlay, 0);

        revalidate();
        repaint();
    }

    // shop handler
    private JPanel shopOverlay;

    private void openShop() {
        if (shopOverlay != null) return; // already open

        shopOverlay = new JPanel(null); // absolute layout
        shopOverlay.setBounds(0, 0, getWidth(), getHeight());
        shopOverlay.setOpaque(false);

        ShopPanel shopPanel = new ShopPanel(new ShopPanel.ShopListener() {
            @Override
            public void onBuy(ShopItem item) {
                int newCoins = hud.getCoins() - item.getPrice();
                hud.updateHUD(hud.getTemporalProgress(), hud.getPacketLoss(), newCoins);
                closeShop();
            }

            @Override
            public void onCancel() {
                closeShop();
            }
        });


        shopPanel.setBounds(getWidth() / 2 - 250, getHeight() / 2 - 200, 500, 400);
        shopOverlay.add(shopPanel);
        add(shopOverlay);
        setComponentZOrder(shopOverlay, 0);
        revalidate();
        repaint();

        pauseGame();
    }

    private void closeShop() {
        if (shopOverlay != null) {
            remove(shopOverlay);
            shopOverlay = null;
            revalidate();
            repaint();
            resumeGame();
        }
    }

    // Dummy placeholders
    private void pauseGame() {
    }

    private void resumeGame() {
    }
}