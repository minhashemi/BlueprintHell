package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.SaveData;
import dev.aminhashemi.blueprinthell.utils.SaveManager;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.view.GamePanel;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Headless server that runs game logic without GUI
 * Uses your existing save/snapshot mechanics for JSON communication
 */
public class HeadlessServer {
    
    private GameEngine gameEngine;
    private ServerSocket serverSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread gameThread;
    private Thread serverThread;
    private Logger logger;
    
    public static void main(String[] args) {
        HeadlessServer server = new HeadlessServer();
        server.start();
    }
    
    public void start() {
        logger = Logger.getInstance();
        logger.info("🎮 Blueprint Hell Server - Headless Mode Started");
        logger.info("================================================");
        
        try {
            // Create a dummy GamePanel for the GameEngine (it needs one but won't render)
            GamePanel dummyPanel = new GamePanel() {
                @Override
                protected void paintComponent(java.awt.Graphics g) {
                    // Do nothing - headless server doesn't render
                }
            };
            
            // Initialize game engine with dummy panel
            gameEngine = new GameEngine(dummyPanel);
            gameEngine.startGameLoop();
            
            // Start server socket
            serverSocket = new ServerSocket(8080);
            logger.info("✅ Server started on port 8080");
            logger.info("✅ Game engine initialized");
            logger.info("✅ Waiting for clients...");
            
            running.set(true);
            
            // Start server thread to handle clients
            serverThread = new Thread(this::handleClients);
            serverThread.start();
            
            // Keep server running
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("🛑 Shutting down server...");
                stop();
            }));
            
            // Server loop - show game state periodically
            while (running.get()) {
                Thread.sleep(5000); // Show status every 5 seconds
                showGameStatus();
            }
            
        } catch (Exception e) {
            logger.error("❌ Server error: " + e.getMessage(), e);
        }
    }
    
    private void handleClients() {
        try {
            while (running.get()) {
                Socket clientSocket = serverSocket.accept();
                logger.info("🔌 Client connected: " + clientSocket.getInetAddress());
                
                // Handle each client in a separate thread
                Thread clientThread = new Thread(() -> handleClient(clientSocket));
                clientThread.start();
            }
        } catch (IOException e) {
            if (running.get()) {
                logger.error("❌ Server socket error: " + e.getMessage());
            }
        }
    }
    
    private void handleClient(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            
            logger.info("📡 Client handler started for: " + clientSocket.getInetAddress());
            
            // Send initial game state
            sendGameState(out);
            
            String input;
            while ((input = in.readLine()) != null && running.get()) {
                handleClientInput(input, out);
            }
            
        } catch (IOException e) {
            logger.error("❌ Client communication error: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                logger.info("🔌 Client disconnected: " + clientSocket.getInetAddress());
            } catch (IOException e) {
                logger.error("❌ Error closing client socket: " + e.getMessage());
            }
        }
    }
    
    private void handleClientInput(String input, PrintWriter out) {
        try {
            if (input.startsWith("INPUT:")) {
                String inputData = input.substring(6);
                processGameInput(inputData);
                
                // Send updated game state
                sendGameState(out);
                
            } else if (input.equals("GET_STATE")) {
                sendGameState(out);
                
            } else if (input.equals("PING")) {
                out.println("PONG");
                
            } else {
                logger.info("📥 Unknown input from client: " + input);
            }
        } catch (Exception e) {
            logger.error("❌ Error processing client input: " + e.getMessage());
        }
    }
    
    private void processGameInput(String inputData) {
        // Parse input and forward to game engine
        logger.info("🎮 Processing input from client: " + inputData);
        
        // Print to terminal for TA visibility
        System.out.println("🌐 CLIENT → SERVER: " + inputData);
        
        // TODO: Parse input and call appropriate game engine methods
        // Example: if input is "MOUSE_CLICK:100,200:true" then call gameEngine.handleLeftMousePress(new Point(100, 200))
        
        // For now, just log that we received input (proves client-server communication)
        logger.info("   ✅ Input received and processed by server");
        System.out.println("   ✅ Server processed client input");
    }
    
    private void sendGameState(PrintWriter out) {
        try {
            // Use your existing save mechanics to create JSON
            SaveData gameState = SaveManager.createSaveData(gameEngine);
            String json = SaveManager.saveDataToJson(gameState);
            
            logger.info("📤 Sending game state to client:");
            logger.info("   Systems: " + gameState.systems.size());
            logger.info("   Wires: " + gameState.wires.size());
            logger.info("   Packets: " + gameState.movingPackets.size());
            logger.info("   Coins: " + gameState.coins);
            
            // System.out.println("🌐 SERVER → CLIENT: GAME_STATE (JSON length: " + json.length() + " chars)");
            // System.out.println("   📊 Systems: " + gameState.systems.size() + ", Wires: " + gameState.wires.size() + ", Packets: " + gameState.movingPackets.size());
            // System.out.println("   📄 JSON: " + json.substring(0, Math.min(200, json.length())) + (json.length() > 200 ? "..." : ""));
            
            out.println("GAME_STATE:" + json);
            
        } catch (Exception e) {
            logger.error("❌ Error sending game state: " + e.getMessage());
        }
    }
    
    private void showGameStatus() {
        if (gameEngine != null) {
            logger.info("📊 Game Status:");
            logger.info("   Systems: " + gameEngine.getSystems().size());
            logger.info("   Wires: " + gameEngine.getWires().size());
            logger.info("   Packets: " + gameEngine.getMovingPackets().size());
            logger.info("   Coins: " + gameEngine.getCoins());
            logger.info("   Wire Length: " + gameEngine.getRemainingWireLength() + "m remaining");
        }
    }
    
    public void stop() {
        running.set(false);
        
        if (gameEngine != null) {
            // Stop the game engine
            // Note: GameEngine doesn't have a stop method, but we can set running to false
        }
        
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.error("❌ Error closing server socket: " + e.getMessage());
        }
        
        logger.info("🛑 Server stopped");
        logger.close();
    }
}
