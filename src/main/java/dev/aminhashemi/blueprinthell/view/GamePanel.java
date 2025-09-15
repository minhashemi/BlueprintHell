package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.controller.InputHandler;
import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.view.ui.TimeTravelPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

public class GamePanel extends JPanel {

    private GameEngine gameEngine;
    private boolean showHUD = true;
    private long hudToggleTime = 0;
    private static final long HUD_DISPLAY_DURATION = 3000; // 3 seconds
    private TimeTravelPanel timeTravelPanel;
    
    // HUD state variables
    private int remainingWireLength = 8000;
    private int temporalProgress = 0;
    private int packetLoss = 0;
    private int coins = 20;

    public GamePanel() {
        initPanel();
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;

        // Set up input handlers
        InputHandler mouseHandler = new InputHandler(gameEngine);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);

        InputHandler.KeyInput keyHandler = new InputHandler.KeyInput(gameEngine);
        this.addKeyListener(keyHandler);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE:
                        if (gameEngine != null) {
                            gameEngine.handleManualPacketSpawn();
                        }
                        break;
                    case KeyEvent.VK_W:
                        if (gameEngine != null) {
                            gameEngine.toggleWiringMode(true);
                        }
                        break;
                    case KeyEvent.VK_H:
                        // Toggle HUD visibility
                        showHUD = !showHUD;
                        if (showHUD) {
                            hudToggleTime = System.currentTimeMillis();
                        } else {
                            hudToggleTime = 0;
                        }
                        repaint();
                        break;
                }
            }
        });
        
        // Create and add time travel panel
        timeTravelPanel = new TimeTravelPanel(gameEngine);
        timeTravelPanel.setBounds(20, 20, 350, 200); // Position in top-left corner
        timeTravelPanel.setVisible(false); // Initially hidden
        add(timeTravelPanel);
    }

    private void initPanel() {
        setName(GameFrame.GAME_PANEL);
        setPreferredSize(new Dimension(1280, 720));
        setBackground(new Color(20, 25, 40)); // Dark blue-gray background
        setFocusable(true); // Required for key events
        setLayout(null); // Use absolute positioning for time travel panel
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        
        // Render game and HUD
        if (gameEngine != null) {
            gameEngine.render(g2d);
        }
        
        drawHUD(g2d);
        
        // Update time travel panel visibility
        if (timeTravelPanel != null && gameEngine != null) {
            boolean isTimeTravelMode = gameEngine.getTimeTravelManager().isTimeTravelMode();
            timeTravelPanel.setVisible(isTimeTravelMode);
        }
        
        // Auto-hide HUD after timeout
        if (System.currentTimeMillis() - hudToggleTime > HUD_DISPLAY_DURATION && hudToggleTime > 0) {
            showHUD = false;
            hudToggleTime = 0;
        }
    }

    /** Draws the game HUD */
    private void drawHUD(Graphics2D g) {
        if (!showHUD) return;
        
        // Set up HUD styling
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // HUD background (semi-transparent dark panel) - Fixed size for required content only
        int hudWidth = 280;
        int hudHeight = 160;
        int hudX = getWidth() - hudWidth - 20;
        int hudY = 20;
        
        // Semi-transparent background
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(hudX, hudY, hudWidth, hudHeight, 15, 15);
        
        // HUD border
        g.setColor(new Color(255, 255, 255, 100));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(hudX, hudY, hudWidth, hudHeight, 15, 15);
        
        // HUD title
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        g.drawString("Game Status", hudX + 15, hudY + 25);
        
        // HUD content - Only required information from documentation
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        int textY = hudY + 45;
        int lineHeight = 20;
        
        // Required: Wire Length
        if (remainingWireLength > 2000) {
            g.setColor(Color.CYAN);
        } else if (remainingWireLength > 500) {
            g.setColor(Color.ORANGE);
        } else {
            g.setColor(Color.RED);
        }
        g.drawString("Wire Length: " + remainingWireLength + "m", hudX + 15, textY);
        textY += lineHeight;
        
        // Required: Packet Loss
        g.setColor(Color.RED);
        g.drawString("Packet Loss: " + packetLoss, hudX + 15, textY);
        textY += lineHeight;
        
        // Required: Coins
        g.setColor(Color.decode("#FFD700")); // Gold color
        g.drawString("Coins: " + coins, hudX + 15, textY);
        textY += lineHeight + 5;
        
        // Required: Active Network Capabilities
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Network Status:", hudX + 15, textY);
        textY += 15;
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        g.setColor(Color.GREEN);
        g.drawString("Active Systems: " + getActiveSystemCount(), hudX + 15, textY);
        textY += lineHeight;
        
        g.setColor(Color.BLUE);
        g.drawString("Wire Connections: " + getWireConnectionCount(), hudX + 15, textY);
        
        // HUD toggle indicator
        g.setColor(new Color(255, 255, 255, 150));
        g.setFont(new Font("Arial", Font.ITALIC, 10));
        g.drawString("Press 'H' to toggle HUD", hudX + 15, hudY + hudHeight - 5);
    }

    /**
     * Updates HUD data from the game engine
     */
    public void updateHUDData(int wireLength, int progress, int lostPackets, int currentCoins) {
        this.remainingWireLength = wireLength;
        this.temporalProgress = progress;
        this.packetLoss = lostPackets;
        this.coins = currentCoins;
        repaint();
    }
    
    /**
     * Shows HUD temporarily (3 seconds)
     */
    public void showHUDTemporarily() {
        showHUD = true;
        hudToggleTime = System.currentTimeMillis();
        repaint();
    }
    
    // Active Network Capabilities Methods (Phase 2 Requirement)
    
    /**
     * Returns the count of active systems in the network
     */
    private int getActiveSystemCount() {
        if (gameEngine != null) {
            return gameEngine.getActiveSystemCount();
        }
        return 0;
    }
    
    /**
     * Returns the current network status
     */
    private String getNetworkStatus() {
        if (gameEngine != null) {
            return gameEngine.getNetworkStatus();
        }
        return "Unknown";
    }
    
    /**
     * Returns the count of active ports in the network
     */
    private int getActivePortCount() {
        if (gameEngine != null) {
            return gameEngine.getActivePortCount();
        }
        return 0;
    }
    
    /**
     * Returns the count of wire connections in the network
     */
    private int getWireConnectionCount() {
        if (gameEngine != null) {
            return gameEngine.getWireConnectionCount();
        }
        return 0;
    }
}
