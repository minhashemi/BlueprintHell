package me.minhashemi.view;

import me.minhashemi.controller.InputController;
import me.minhashemi.controller.audio.player;
import me.minhashemi.model.*;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.block.PortType;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.model.shop.ShopItem;
import me.minhashemi.model.shop.ShopPanel;
import me.minhashemi.view.overlay.VictoryOverlay;
import me.minhashemi.view.wire.WireManager;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class GameScreen extends JPanel {
    private final LevelData levelData;
    private final HUD hud;
    private final WireManager wireManager;
    private final InputController inputController;
    private final List<MovingPacket> movingPackets = new ArrayList<>();
    private boolean victoryShown = false;
    private JPanel shopOverlay;
    private Timer gameTimer;

    public List<MovingPacket> getMovingPackets() {
        return movingPackets;
    }

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(new BorderLayout());

        this.hud = new HUD();
        this.wireManager = new WireManager(levelData);
        this.inputController = new InputController(this, levelData, wireManager, hud);

        for (NetSys netsys : levelData.packets) {
            netsys.initializePorts();
        }

        JPanel canvasPanel = createCanvasPanel();
        canvasPanel.setOpaque(false);
        canvasPanel.setPreferredSize(new Dimension(800, 600));
        add(canvasPanel, BorderLayout.CENTER);

        add(createControlsPanel(), BorderLayout.SOUTH);
        setupKeyBinding(canvasPanel);

        gameTimer = new Timer(16, e -> canvasPanel.repaint());
        gameTimer.start();
    }

    private JPanel createCanvasPanel() {
        return new JPanel() {
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
                updateAndRenderPackets(g2);

                if (!victoryShown && movingPackets.isEmpty() && allBlocksGreen()) {
                    victoryShown = true;
                    player.playEffect("victory");
                    SwingUtilities.invokeLater(() -> showVictoryMessage("Victory! All packets delivered."));
                }
            }
        };
    }

    private GameControlsPanel createControlsPanel() {
        return new GameControlsPanel(new GameControlsPanel.GameControlListener() {
            @Override public void onShop() { openShop(); }
            @Override public void onTimeForward() {
                hud.updateHUD(hud.getTemporalProgress() + 1, hud.getPacketLoss(), hud.getCoins());
                repaint();
            }
            @Override public void onTimeBackward() {
                hud.updateHUD(Math.max(0, hud.getTemporalProgress() - 1), hud.getPacketLoss(), hud.getCoins());
                repaint();
            }
            @Override public void onQuitToMenu() {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GameScreen.this);
                if (topFrame instanceof Window window) {
                    window.quitToMenuFromGame();
                }
            }
        });
    }

    private void updateAndRenderPackets(Graphics2D g2) {
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
                targetSys.tryToForwardPacket(wireManager, toAdd, packet.getType());
            } else {
                packet.draw(g2);
            }
        }

        movingPackets.addAll(toAdd);
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

        for (NetSysPort input : packet.getInputPorts()) drawPort(g, input);
        for (NetSysPort output : packet.getOutputPorts()) drawPort(g, output);
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

    private void setupKeyBinding(JComponent component) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke("SPACE"), "spawnPacket");
        component.getActionMap().put("spawnPacket", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (NetSys netSys : levelData.packets) {
                    netSys.spawnInitialPackets(wireManager, movingPackets);
                }
            }
        });
    }

    public void showVictoryMessage(String msg) {
        VictoryOverlay overlay = new VictoryOverlay(this, msg);
        setLayout(null);
        overlay.setSize(getSize());
        add(overlay);
        setComponentZOrder(overlay, 0);

        revalidate();
        repaint();
    }

    private void openShop() {
        if (shopOverlay != null) return;

        shopOverlay = new JPanel(null);
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

    private void pauseGame() {
        if (gameTimer != null) gameTimer.stop();
    }

    private void resumeGame() {
        if (gameTimer != null) gameTimer.start();
    }
}
