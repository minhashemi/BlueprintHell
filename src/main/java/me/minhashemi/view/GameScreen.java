package me.minhashemi.view;

import me.minhashemi.controller.InputController;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.model.MovingPacket;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.view.overlay.OverlayManager;
import me.minhashemi.view.wire.WireManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class GameScreen extends JPanel {
    private final LevelData levelData;
    private final HUD hud;
    private final WireManager wireManager;
    private final InputController inputController;
    private final PacketManager packetManager;
    private final OverlayManager overlayManager;
    private final ShopManager shopManager;
    private final List<MovingPacket> movingPackets;
    private Timer gameTimer;
    private final int totalPackets;

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        this.totalPackets = calculateTotalPackets();
        this.movingPackets = new ArrayList<>();
        this.hud = new HUD(totalPackets);
        this.wireManager = new WireManager(levelData);
        this.packetManager = new PacketManager(levelData, wireManager, hud, movingPackets, totalPackets);
        this.inputController = new InputController(this, levelData, wireManager, hud);
        this.overlayManager = new OverlayManager(this);
        this.shopManager = new ShopManager(this, hud);

        for (NetSys netsys : levelData.packets) {
            netsys.initializePorts();
        }

        setLayout(new BorderLayout());

        CanvasPanel canvasPanel = new CanvasPanel(levelData, hud, wireManager, movingPackets, totalPackets);
        add(canvasPanel, BorderLayout.CENTER);
        add(createControlsPanel(), BorderLayout.SOUTH);

        setupKeyBinding(canvasPanel);

        gameTimer = new Timer(16, e -> {
            packetManager.updateAndRenderPackets((Graphics2D) canvasPanel.getGraphics());
            overlayManager.checkAndShowOverlays(packetManager.getLostPackets(), totalPackets);
            canvasPanel.repaint();
        });
        gameTimer.start();
    }

    private int calculateTotalPackets() {
        int count = 0;
        for (NetSys netsys : levelData.packets) {
            count += 5; // Placeholder: 5 packets per NetSys
        }
        return count;
    }

    private GameControlsPanel createControlsPanel() {
        return new GameControlsPanel(new GameControlsPanel.GameControlListener() {
            @Override
            public void onShop() {
                shopManager.openShop();
            }

            @Override
            public void onTimeForward() {
                hud.updateHUD(hud.getTemporalProgress() + 1, packetManager.getLostPackets(), hud.getCoins());
                repaint();
            }

            @Override
            public void onTimeBackward() {
                hud.updateHUD(Math.max(0, hud.getTemporalProgress() - 1), packetManager.getLostPackets(), hud.getCoins());
                repaint();
            }

            @Override
            public void onQuitToMenu() {
                JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(GameScreen.this);
                if (topFrame instanceof Window window) {
                    window.quitToMenuFromGame();
                }
            }
        });
    }

    private void setupKeyBinding(JComponent component) {
        // Use KeyBindingManager to get current key bindings
        me.minhashemi.utils.KeyBindingManager keyManager = me.minhashemi.utils.KeyBindingManager.getInstance();
        
        // Spawn packet key binding
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(keyManager.getKeyStroke(me.minhashemi.model.Config.SPAWN_PACKET_ACTION), "spawnPacket");
        component.getActionMap().put("spawnPacket", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for (NetSys netSys : levelData.packets) {
                    netSys.spawnInitialPackets(wireManager, movingPackets);
                }
            }
        });
        
        // Pause key binding
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(keyManager.getKeyStroke(me.minhashemi.model.Config.PAUSE_ACTION), "pauseGame");
        component.getActionMap().put("pauseGame", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseGame();
            }
        });
    }

    public void pauseGame() {
        if (gameTimer != null) gameTimer.stop();
    }

    public void resumeGame() {
        if (gameTimer != null) gameTimer.start();
    }

    public void stopGameTimer() {
        if (gameTimer != null) gameTimer.stop();
    }

    public List<MovingPacket> getMovingPackets() {
        return movingPackets;
    }

    public HUD getHUD() {
        return hud;
    }

    public PacketManager getPacketManager() {
        return packetManager;
    }
}