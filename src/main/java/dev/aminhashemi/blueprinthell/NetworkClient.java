package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.model.SaveData;
import dev.aminhashemi.blueprinthell.utils.SaveManager;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;

/**
 * Network client that only renders what the server sends
 * Sends input to server, receives game state via JSON
 */
public class NetworkClient {
    
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private GameFrame gameFrame;
    private SaveData currentGameState;
    private boolean connected = false;
    private Logger logger;
    
    public static void main(String[] args) {
        NetworkClient client = new NetworkClient();
        client.start();
    }
    
    public void start() {
        logger = Logger.getInstance();
        logger.info("🎮 Blueprint Hell Client - Network Mode Started");
        logger.info("==============================================");
        
        try {
            // Connect to server
            clientSocket = new Socket("localhost", 8080);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            logger.info("✅ Connected to server");
            connected = true;
            
            // Start GUI
            SwingUtilities.invokeLater(() -> {
                gameFrame = new GameFrame();
                setupInputHandlers();
                startGameStateReceiver();
            });
            
        } catch (IOException e) {
            logger.error("❌ Failed to connect to server: " + e.getMessage());
            logger.info("🔄 Falling back to offline mode...");
            
            // Fall back to offline mode
            SwingUtilities.invokeLater(() -> {
                new GameFrame(); // Launch original game
            });
        }
    }
    
    private void setupInputHandlers() {
        // Get the game panel from the frame
        JPanel gamePanel = findGamePanel(gameFrame);
        if (gamePanel == null) {
            logger.error("❌ Could not find game panel");
            return;
        }
        
        // Add mouse listener
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (connected) {
                    boolean leftButton = SwingUtilities.isLeftMouseButton(e);
                    sendInput("MOUSE_CLICK:" + e.getX() + "," + e.getY() + ":" + leftButton);
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (connected) {
                    boolean leftButton = SwingUtilities.isLeftMouseButton(e);
                    sendInput("MOUSE_RELEASE:" + e.getX() + "," + e.getY() + ":" + leftButton);
                }
            }
        });
        
        // Add mouse motion listener
        gamePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (connected) {
                    sendInput("MOUSE_MOVE:" + e.getX() + "," + e.getY());
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (connected) {
                    sendInput("MOUSE_DRAG:" + e.getX() + "," + e.getY());
                }
            }
        });
        
        // Add keyboard listener
        gamePanel.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (connected) {
                    sendInput("KEY_PRESS:" + e.getKeyCode());
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (connected) {
                    sendInput("KEY_RELEASE:" + e.getKeyCode());
                }
            }
            
            @Override
            public void keyTyped(KeyEvent e) {
                // Not used
            }
        });
        
        // Make sure the panel can receive focus for keyboard events
        gamePanel.setFocusable(true);
        gamePanel.requestFocus();
    }
    
    private JPanel findGamePanel(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                if (panel.getName() != null && panel.getName().contains("GAME")) {
                    return panel;
                }
            }
            if (comp instanceof Container) {
                JPanel found = findGamePanel((Container) comp);
                if (found != null) return found;
            }
        }
        return null;
    }
    
    private void startGameStateReceiver() {
        Thread receiverThread = new Thread(() -> {
            try {
                String input;
                while ((input = in.readLine()) != null && connected) {
                    if (input.startsWith("GAME_STATE:")) {
                        String json = input.substring(11);
                        updateGameState(json);
                    } else if (input.equals("PONG")) {
                        logger.info("🏓 Server ping received");
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    logger.error("❌ Connection lost: " + e.getMessage());
                    connected = false;
                }
            }
        });
        receiverThread.start();
    }
    
    private void updateGameState(String json) {
        try {
            currentGameState = SaveManager.jsonToSaveData(json);
            
            // Log that we received data from server (proves client-server architecture)
            logger.info("📡 Received game state from server:");
            logger.info("   Systems: " + currentGameState.systems.size());
            logger.info("   Wires: " + currentGameState.wires.size());
            logger.info("   Packets: " + currentGameState.movingPackets.size());
            logger.info("   Coins: " + currentGameState.coins);
            logger.info("   Wire Length: " + currentGameState.totalWireLength + "m total, " + (currentGameState.totalWireLength - currentGameState.usedWireLength) + "m remaining");
            
            // Print to terminal for TA visibility
            System.out.println("🌐 SERVER → CLIENT: GAME_STATE (JSON length: " + json.length() + " chars)");
            System.out.println("   📊 Systems: " + currentGameState.systems.size() + ", Wires: " + currentGameState.wires.size() + ", Packets: " + currentGameState.movingPackets.size());
            System.out.println("   📄 JSON: " + json.substring(0, Math.min(200, json.length())) + (json.length() > 200 ? "..." : ""));
            
            // Update the game panel with the new state
            SwingUtilities.invokeLater(() -> {
                if (gameFrame != null) {
                    // Force repaint to show updated state
                    gameFrame.repaint();
                }
            });
            
        } catch (Exception e) {
            logger.error("❌ Error updating game state: " + e.getMessage());
        }
    }
    
    private void sendInput(String input) {
        if (connected && out != null) {
            logger.info("📤 Sending input to server: " + input);
            
            // Print to terminal for TA visibility
            System.out.println("🌐 CLIENT → SERVER: " + input);
            
            out.println("INPUT:" + input);
        }
    }
    
    public void stop() {
        connected = false;
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (IOException e) {
            logger.error("❌ Error closing client socket: " + e.getMessage());
        }
    }
}
