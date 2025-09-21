package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.network.NetworkManager;
import dev.aminhashemi.blueprinthell.view.GameFrame;
import dev.aminhashemi.blueprinthell.core.constants.GameConstants;
import dev.aminhashemi.blueprinthell.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Main client that tries to connect to server, falls back to offline if failed
 */
public class Main {
    
    private static final String DEFAULT_SERVER_IP = "localhost";
    private static final int DEFAULT_SERVER_PORT = GameConstants.DEFAULT_SERVER_PORT;
    
    private static GameFrame gameFrame;
    private static GameEngine gameEngine;
    private static NetworkManager networkManager;
    private static JLabel connectionStatusLabel;
    
    public static void main(String[] args) {
        System.out.println("🎮 BlueprintHell Client Starting...");
        System.out.println("=" + "=".repeat(40));
        
        try {
            // Initialize game frame
            gameFrame = new GameFrame();
            
            // Initialize game engine
            gameEngine = new GameEngine(gameFrame.getGamePanel());
            
            // Try to connect to server
            boolean connected = tryConnectToServer();
            
            // Add connection status indicator
            addConnectionStatusIndicator(connected);
            
            // Show game frame
            gameFrame.setVisible(true);
            
            if (connected) {
                System.out.println("✅ Connected to server! Game running in online mode.");
            } else {
                System.out.println("💻 Running in offline mode. All features available.");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize game: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Tries to connect to the server
     * @return True if connected successfully
     */
    private static boolean tryConnectToServer() {
        System.out.println("🔗 Attempting to connect to server...");
        System.out.println("📡 Server: " + DEFAULT_SERVER_IP + ":" + DEFAULT_SERVER_PORT);
        
        try {
            // Initialize network manager
            networkManager = new NetworkManager();
            
            // Attempt connection
            boolean connected = networkManager.connectToServer(DEFAULT_SERVER_IP, DEFAULT_SERVER_PORT);
            
            if (connected) {
                System.out.println("✅ Successfully connected to server!");
                return true;
            } else {
                System.out.println("❌ Failed to connect to server.");
                System.out.println("💻 Falling back to offline mode...");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("❌ Connection error: " + e.getMessage());
            System.out.println("💻 Falling back to offline mode...");
            return false;
        }
    }
    
    /**
     * Adds connection status indicator to the game frame
     */
    private static void addConnectionStatusIndicator(boolean connected) {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel.setOpaque(false);
        
        // Connection status indicator
        connectionStatusLabel = new JLabel();
        updateConnectionStatus(connected);
        
        // Mode indicator
        final JLabel modeLabel = new JLabel("Mode: " + (connected ? "ONLINE" : "OFFLINE"));
        modeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        modeLabel.setForeground(Color.WHITE);
        
        // Reconnect button (only if offline)
        final JButton reconnectButton;
        if (!connected) {
            reconnectButton = new JButton("🔄 Reconnect");
            reconnectButton.setFont(new Font("Arial", Font.PLAIN, 10));
            reconnectButton.setBackground(new Color(100, 150, 255));
            reconnectButton.setForeground(Color.WHITE);
            reconnectButton.setFocusPainted(false);
            reconnectButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            
            reconnectButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("🔄 Attempting to reconnect...");
                    boolean reconnected = tryConnectToServer();
                    updateConnectionStatus(reconnected);
                    
                    if (reconnected) {
                        modeLabel.setText("Mode: ONLINE");
                        reconnectButton.setVisible(false);
                        System.out.println("✅ Reconnected successfully!");
                    } else {
                        System.out.println("❌ Reconnection failed. Still in offline mode.");
                    }
                }
            });
        } else {
            reconnectButton = null;
        }
        
        statusPanel.add(modeLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(connectionStatusLabel);
        if (reconnectButton != null) {
            statusPanel.add(Box.createHorizontalStrut(5));
            statusPanel.add(reconnectButton);
        }
        
        // Add to game frame
        gameFrame.add(statusPanel, BorderLayout.NORTH);
    }
    
    /**
     * Updates connection status indicator
     */
    private static void updateConnectionStatus(boolean connected) {
        if (connectionStatusLabel == null) return;
        
        if (connected) {
            connectionStatusLabel.setText("🟢 Connected");
            connectionStatusLabel.setForeground(Color.GREEN);
        } else {
            connectionStatusLabel.setText("🔴 Offline");
            connectionStatusLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Gets the network manager instance
     * @return Network manager or null if not connected
     */
    public static NetworkManager getNetworkManager() {
        return networkManager;
    }
    
    /**
     * Gets the game engine instance
     * @return Game engine
     */
    public static GameEngine getGameEngine() {
        return gameEngine;
    }
    
    /**
     * Checks if currently connected to server
     * @return True if connected
     */
    public static boolean isConnected() {
        return networkManager != null && networkManager.isConnected();
    }
}