package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.controller.InputHandler;
import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;

/**
 * Main game rendering panel that displays the game world and HUD.
 * Handles input events and renders all game entities including systems, wires, and packets.
 */
public class GamePanel extends JPanel {

    // ==================== CORE COMPONENTS ====================
    private GameEngine gameEngine;              // Reference to the game engine
    
    // ==================== HUD STATE ====================
    private boolean showHUD = true;             // Whether HUD is currently visible
    private long hudToggleTime = 0;             // When HUD was last toggled
    private static final long HUD_DISPLAY_DURATION = Config.HUD_DISPLAY_DURATION; // 3 seconds
    
    // HUD data variables
    private int remainingWireLength = Config.TOTAL_WIRE_LENGTH;  // Available wire length
    private int temporalProgress = 0;           // Game progress percentage
    private int packetLoss = 0;                 // Number of lost packets
    private int coins = Config.LevelDefaults.INITIAL_COINS; // Current coin count

    /**
     * Constructs the game panel and initializes basic settings.
     */
    public GamePanel() {
        initPanel();
    }

    /**
     * Sets the game engine and configures input handlers.
     * @param gameEngine The game engine instance to connect to
     */
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
                    case KeyEvent.VK_B:
                        if (gameEngine != null) {
                            gameEngine.toggleShop();
                        }
                        break;
                    case KeyEvent.VK_1:
                        if (gameEngine != null && gameEngine.isShopOpen()) {
                            gameEngine.handleShopPurchase(1);
                        }
                        break;
                    case KeyEvent.VK_2:
                        if (gameEngine != null && gameEngine.isShopOpen()) {
                            gameEngine.handleShopPurchase(2);
                        }
                        break;
                    case KeyEvent.VK_3:
                        if (gameEngine != null && gameEngine.isShopOpen()) {
                            gameEngine.handleShopPurchase(3);
                        }
                        break;
                    case KeyEvent.VK_4:
                        if (gameEngine != null && gameEngine.isShopOpen()) {
                            gameEngine.handleShopPurchase(4);
                        }
                        break;
                    case KeyEvent.VK_5:
                        if (gameEngine != null && gameEngine.isShopOpen()) {
                            gameEngine.handleShopPurchase(5);
                        }
                        break;
                    case KeyEvent.VK_6:
                        if (gameEngine != null && gameEngine.isShopOpen()) {
                            gameEngine.handleShopPurchase(6);
                        }
                        break;
                    case KeyEvent.VK_G:
                        if (gameEngine != null && !gameEngine.isShopOpen()) {
                            gameEngine.startTest();
                        }
                        break;
                    case KeyEvent.VK_R:
                        if (gameEngine != null && !gameEngine.isShopOpen()) {
                            gameEngine.resetTestState();
                            Logger.getInstance().info("Test state reset! You can now use normal game functions.");
                        }
                        break;
                }
            }
        });
    }

    private void initPanel() {
        setName(GameFrame.GAME_PANEL);
        setPreferredSize(new Dimension(Config.WINDOW_WIDTH, Config.WINDOW_HEIGHT));
        setBackground(Config.BACKGROUND_COLOR); // Dark blue-gray background
        setFocusable(true); // Required for key events
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
        
        // Draw win/lose overlay
        drawWinLoseOverlay(g2d);
        
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
        int hudWidth = Config.HUD_WIDTH;
        int hudHeight = Config.HUD_HEIGHT;
        int hudX = getWidth() - hudWidth - Config.HUD_MARGIN;
        int hudY = Config.HUD_MARGIN;
        
        // Semi-transparent background
        g.setColor(new Color(0, 0, 0, Config.HUD_BACKGROUND_ALPHA));
        g.fillRoundRect(hudX, hudY, hudWidth, hudHeight, 15, 15);
        
        // HUD border
        g.setColor(new Color(255, 255, 255, Config.HUD_BORDER_ALPHA));
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(hudX, hudY, hudWidth, hudHeight, 15, 15);
        
        // HUD title
        g.setColor(Color.WHITE);
        g.setFont(new Font(Config.Fonts.DEFAULT_FONT_FAMILY, Font.BOLD, Config.Fonts.HUD_TITLE_SIZE));
        g.drawString("Game Status", hudX + 15, hudY + 25);
        
        // HUD content - Only required information from documentation
        g.setFont(new Font(Config.Fonts.DEFAULT_FONT_FAMILY, Font.PLAIN, Config.Fonts.HUD_TEXT_SIZE));
        int textY = hudY + 45;
        int lineHeight = Config.HUD_LINE_HEIGHT;
        
        // Required: Wire Length
        g.setColor(Config.getWireLengthColor(remainingWireLength));
        g.drawString("Wire Length: " + remainingWireLength + "m", hudX + 15, textY);
        textY += lineHeight;
        
        // Required: Packet Loss
        g.setColor(Config.HUD_PACKET_LOSS_COLOR);
        g.drawString("Packet Loss: " + packetLoss, hudX + 15, textY);
        textY += lineHeight;
        
        // Required: Coins
        g.setColor(Config.HUD_COINS_COLOR); // Gold color
        g.drawString("Coins: " + coins, hudX + 15, textY);
        textY += lineHeight + 5;
        
        // Required: Active Network Capabilities
        g.setColor(Color.WHITE);
        g.setFont(new Font(Config.Fonts.DEFAULT_FONT_FAMILY, Font.BOLD, Config.Fonts.HUD_SMALL_TEXT_SIZE));
        g.drawString("Network Status:", hudX + 15, textY);
        textY += Config.HUD_TEXT_SPACING;
        
        g.setFont(new Font(Config.Fonts.DEFAULT_FONT_FAMILY, Font.PLAIN, Config.Fonts.HUD_SMALL_TEXT_SIZE));
        g.setColor(Config.HUD_ACTIVE_SYSTEMS_COLOR);
        g.drawString("Active Systems: " + getActiveSystemCount(), hudX + 15, textY);
        textY += lineHeight;
        
        g.setColor(Config.HUD_WIRE_CONNECTIONS_COLOR);
        g.drawString("Wire Connections: " + getWireConnectionCount(), hudX + 15, textY);
        
        // HUD toggle indicator
        g.setColor(new Color(255, 255, 255, 150));
        g.setFont(new Font(Config.Fonts.DEFAULT_FONT_FAMILY, Font.ITALIC, Config.Fonts.HUD_TINY_TEXT_SIZE));
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
    
    /**
     * Draws win/lose overlay when test is completed
     */
    private void drawWinLoseOverlay(Graphics2D g2d) {
        if (gameEngine == null) return;
        
        // Check if test is completed
        if (gameEngine.isTestCompleted()) {
            // Semi-transparent overlay
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            // Win/Lose message
            String message;
            Color messageColor;
            
            if (gameEngine.isGameWon()) {
                message = "🎉 LEVEL COMPLETED! 🎉";
                messageColor = Color.GREEN;
            } else if (gameEngine.isGameLost()) {
                message = "💀 LEVEL FAILED! 💀";
                messageColor = Color.RED;
            } else {
                return; // Test not completed yet
            }
            
            // Draw message
            g2d.setColor(messageColor);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g2d.getFontMetrics();
            int messageWidth = fm.stringWidth(message);
            int messageHeight = fm.getHeight();
            int x = (getWidth() - messageWidth) / 2;
            int y = (getHeight() - messageHeight) / 2;
            
            g2d.drawString(message, x, y);
            
            // Draw packet loss info
            String lossInfo = "Packet Loss: " + String.format("%.1f", gameEngine.getPacketLossPercentage()) + "%";
            g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            fm = g2d.getFontMetrics();
            int infoWidth = fm.stringWidth(lossInfo);
            int infoX = (getWidth() - infoWidth) / 2;
            int infoY = y + messageHeight + 30;
            g2d.drawString(lossInfo, infoX, infoY);
            
            // Draw instructions
            String instructions = "Press R to restart or M for menu";
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            fm = g2d.getFontMetrics();
            int instWidth = fm.stringWidth(instructions);
            int instX = (getWidth() - instWidth) / 2;
            int instY = infoY + 40;
            g2d.setColor(Color.WHITE);
            g2d.drawString(instructions, instX, instY);
        }
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
