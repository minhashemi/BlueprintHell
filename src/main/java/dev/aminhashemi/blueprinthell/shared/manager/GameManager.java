package dev.aminhashemi.blueprinthell.shared.manager;

import dev.aminhashemi.blueprinthell.shared.network.*;
import dev.aminhashemi.blueprinthell.shared.connection.ConnectionManager;
import dev.aminhashemi.blueprinthell.shared.mode.OfflineModeManager;
import dev.aminhashemi.blueprinthell.client.core.SimpleClientGameEngine;
import dev.aminhashemi.blueprinthell.server.core.SimpleServerGameEngine;
import dev.aminhashemi.blueprinthell.view.GamePanel;
import dev.aminhashemi.blueprinthell.model.SaveData;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main game manager that coordinates client-server architecture with offline fallback.
 * Manages the overall game state and handles mode switching.
 */
public class GameManager implements NetworkMessageHandler, GameStateSync {
    
    public enum GameMode {
        SERVER,
        CLIENT,
        OFFLINE
    }
    
    private final GameMode mode;
    private final GamePanel gamePanel;
    
    // Core components
    private NetworkManager networkManager;
    private ConnectionManager connectionManager;
    private OfflineModeManager offlineModeManager;
    private SimpleClientGameEngine clientGameEngine;
    private SimpleServerGameEngine serverGameEngine;
    
    // State management
    private final AtomicBoolean running = new AtomicBoolean(false);
    private GameMode currentMode;
    private SaveData currentGameState;
    
    public GameManager(GameMode mode, GamePanel gamePanel) {
        this.mode = mode;
        this.gamePanel = gamePanel;
        this.currentMode = mode;
        this.currentGameState = new SaveData();
    }
    
    /**
     * Initializes the game manager based on the specified mode
     * @return true if initialization successful
     */
    public boolean initialize() {
        try {
            // Initialize offline mode manager first (always available)
            offlineModeManager = new OfflineModeManager();
            if (!offlineModeManager.initializeOfflineMode(gamePanel)) {
                System.err.println("Failed to initialize offline mode manager");
                return false;
            }
            
            // Initialize based on mode
            switch (mode) {
                case SERVER:
                    return initializeServerMode();
                case CLIENT:
                    return initializeClientMode();
                case OFFLINE:
                    return initializeOfflineMode();
                default:
                    System.err.println("Unknown game mode: " + mode);
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize game manager: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    private boolean initializeServerMode() {
        try {
            // Initialize server components
            networkManager = new NetworkManager(true);
            serverGameEngine = new SimpleServerGameEngine();
            
            // Set up message handling
            networkManager.setMessageHandler(this);
            
            System.out.println("Server mode initialized");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to initialize server mode: " + e.getMessage());
            return false;
        }
    }
    
    private boolean initializeClientMode() {
        try {
            // Initialize client components
            networkManager = new NetworkManager(false);
            clientGameEngine = new SimpleClientGameEngine(gamePanel);
            
            // Initialize connection manager
            connectionManager = new ConnectionManager(networkManager, offlineModeManager);
            
            // Set up message handling
            networkManager.setMessageHandler(this);
            
            System.out.println("Client mode initialized");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to initialize client mode: " + e.getMessage());
            return false;
        }
    }
    
    private boolean initializeOfflineMode() {
        try {
            // Offline mode is already initialized in the constructor
            currentMode = GameMode.OFFLINE;
            System.out.println("Offline mode initialized");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to initialize offline mode: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Starts the game manager
     * @return true if start successful
     */
    public boolean start() {
        try {
            running.set(true);
            
            switch (currentMode) {
                case SERVER:
                    return startServerMode();
                case CLIENT:
                    return startClientMode();
                case OFFLINE:
                    return startOfflineMode();
                default:
                    return false;
            }
        } catch (Exception e) {
            System.err.println("Failed to start game manager: " + e.getMessage());
            return false;
        }
    }
    
    private boolean startServerMode() {
        if (serverGameEngine != null && serverGameEngine.start()) {
            System.out.println("Server started successfully");
            System.out.println("Server is running headless - no GUI will be shown");
            return true;
        } else {
            System.err.println("Failed to start server");
            return false;
        }
    }
    
    private boolean startClientMode() {
        // Try to connect to server first
        if (connectionManager.connect()) {
            if (clientGameEngine != null && clientGameEngine.start()) {
                System.out.println("Client started successfully");
                return true;
            } else {
                System.err.println("Failed to start client engine");
                return false;
            }
        } else {
            // Fall back to offline mode
            System.out.println("Failed to connect to server, switching to offline mode");
            return switchToOfflineMode();
        }
    }
    
    private boolean startOfflineMode() {
        if (offlineModeManager != null && offlineModeManager.startOfflineMode()) {
            System.out.println("Offline mode started successfully");
            return true;
        } else {
            System.err.println("Failed to start offline mode");
            return false;
        }
    }
    
    /**
     * Switches to offline mode
     * @return true if switch successful
     */
    public boolean switchToOfflineMode() {
        try {
            // Stop current mode
            stopCurrentMode();
            
            // Switch to offline mode
            currentMode = GameMode.OFFLINE;
            
            // Start offline mode
            if (offlineModeManager != null && offlineModeManager.startOfflineMode()) {
                System.out.println("Switched to offline mode");
                return true;
            } else {
                System.err.println("Failed to switch to offline mode");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error switching to offline mode: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Switches to client mode
     * @return true if switch successful
     */
    public boolean switchToClientMode() {
        try {
            // Stop current mode
            stopCurrentMode();
            
            // Switch to client mode
            currentMode = GameMode.CLIENT;
            
            // Initialize client mode if not already done
            if (clientGameEngine == null) {
                if (!initializeClientMode()) {
                    return false;
                }
            }
            
            // Start client mode
            if (connectionManager.connect() && clientGameEngine.start()) {
                System.out.println("Switched to client mode");
                return true;
            } else {
                System.out.println("Failed to connect to server, staying in offline mode");
                return switchToOfflineMode();
            }
        } catch (Exception e) {
            System.err.println("Error switching to client mode: " + e.getMessage());
            return switchToOfflineMode();
        }
    }
    
    private void stopCurrentMode() {
        switch (currentMode) {
            case SERVER:
                if (serverGameEngine != null) {
                    serverGameEngine.stop();
                }
                break;
            case CLIENT:
                if (clientGameEngine != null) {
                    clientGameEngine.stop();
                }
                if (connectionManager != null) {
                    connectionManager.disconnect();
                }
                break;
            case OFFLINE:
                if (offlineModeManager != null) {
                    offlineModeManager.stopOfflineMode();
                }
                break;
        }
    }
    
    /**
     * Updates the game based on current mode
     */
    public void update() {
        if (!running.get()) return;
        
        switch (currentMode) {
            case SERVER:
                if (serverGameEngine != null) {
                    serverGameEngine.update();
                }
                break;
            case CLIENT:
                if (clientGameEngine != null) {
                    // ClientGameEngine update is handled internally
                }
                break;
            case OFFLINE:
                if (offlineModeManager != null) {
                    offlineModeManager.updateOfflineMode();
                }
                break;
        }
    }
    
    /**
     * Renders the game based on current mode
     * @param g Graphics2D context
     */
    public void render(Graphics2D g) {
        if (!running.get()) return;
        
        switch (currentMode) {
            case SERVER:
                // Server doesn't render - it's headless
                break;
            case CLIENT:
                if (clientGameEngine != null) {
                    // Client rendering is handled by GamePanel
                }
                break;
            case OFFLINE:
                if (offlineModeManager != null) {
                    offlineModeManager.renderOfflineMode(g);
                }
                break;
        }
    }
    
    /**
     * Handles mouse input
     * @param point Mouse point
     * @param leftButton True if left button
     */
    public void handleMouseInput(Point point, boolean leftButton) {
        switch (currentMode) {
            case CLIENT:
                if (clientGameEngine != null) {
                    clientGameEngine.handleMouseClick(point, leftButton);
                }
                break;
            case OFFLINE:
                if (offlineModeManager != null) {
                    offlineModeManager.handleMouseInput(point, leftButton);
                }
                break;
            case SERVER:
                // Server doesn't handle direct input
                break;
        }
    }
    
    /**
     * Handles mouse movement
     * @param point Mouse point
     */
    public void handleMouseMove(Point point) {
        switch (currentMode) {
            case CLIENT:
                if (clientGameEngine != null) {
                    clientGameEngine.handleMouseMove(point);
                }
                break;
            case OFFLINE:
                if (offlineModeManager != null) {
                    offlineModeManager.handleMouseMove(point);
                }
                break;
            case SERVER:
                // Server doesn't handle direct input
                break;
        }
    }
    
    /**
     * Handles mouse drag
     * @param point Mouse point
     */
    public void handleMouseDrag(Point point) {
        switch (currentMode) {
            case CLIENT:
                if (clientGameEngine != null) {
                    clientGameEngine.handleMouseDrag(point);
                }
                break;
            case OFFLINE:
                if (offlineModeManager != null) {
                    offlineModeManager.handleMouseDrag(point);
                }
                break;
            case SERVER:
                // Server doesn't handle direct input
                break;
        }
    }
    
    /**
     * Handles keyboard input
     * @param keyCode Key code
     * @param pressed True if pressed
     */
    public void handleKeyboardInput(int keyCode, boolean pressed) {
        switch (currentMode) {
            case CLIENT:
                if (clientGameEngine != null) {
                    if (pressed) {
                        clientGameEngine.handleKeyPress(keyCode);
                    } else {
                        clientGameEngine.handleKeyRelease(keyCode);
                    }
                }
                break;
            case OFFLINE:
                if (offlineModeManager != null) {
                    offlineModeManager.handleKeyboardInput(keyCode, pressed);
                }
                break;
            case SERVER:
                // Server doesn't handle direct input
                break;
        }
    }
    
    /**
     * Stops the game manager
     */
    public void stop() {
        running.set(false);
        stopCurrentMode();
        
        if (connectionManager != null) {
            connectionManager.shutdown();
        }
        
        System.out.println("Game manager stopped");
    }
    
    // ==================== NETWORK MESSAGE HANDLING ====================
    
    @Override
    public void handleMessage(NetworkMessage message, String senderId) {
        switch (currentMode) {
            case SERVER:
                if (serverGameEngine != null) {
                    serverGameEngine.handleMessage(message, senderId);
                }
                break;
            case CLIENT:
                if (clientGameEngine != null) {
                    clientGameEngine.handleMessage(message, senderId);
                }
                break;
            case OFFLINE:
                // Offline mode doesn't handle network messages
                break;
        }
    }
    
    @Override
    public void handleConnectionEvent(String clientId, boolean connected) {
        switch (currentMode) {
            case SERVER:
                if (serverGameEngine != null) {
                    serverGameEngine.handleConnectionEvent(clientId, connected);
                }
                break;
            case CLIENT:
                if (clientGameEngine != null) {
                    clientGameEngine.handleConnectionEvent(clientId, connected);
                }
                break;
            case OFFLINE:
                // Offline mode doesn't handle connection events
                break;
        }
    }
    
    @Override
    public void handleNetworkError(String error, String clientId) {
        switch (currentMode) {
            case CLIENT:
                System.err.println("Network error in client mode: " + error);
                // Try to switch to offline mode
                switchToOfflineMode();
                break;
            case SERVER:
                if (serverGameEngine != null) {
                    serverGameEngine.handleNetworkError(error, clientId);
                }
                break;
            case OFFLINE:
                // Offline mode doesn't handle network errors
                break;
        }
    }
    
    // ==================== GAME STATE SYNC IMPLEMENTATION ====================
    
    @Override
    public void syncGameState(SaveData gameState) {
        currentGameState = gameState;
        
        switch (currentMode) {
            case CLIENT:
                if (clientGameEngine != null) {
                    clientGameEngine.syncGameState(gameState);
                }
                break;
            case OFFLINE:
                // Offline mode manages its own state
                break;
            case SERVER:
                // Server manages the authoritative state
                break;
        }
    }
    
    @Override
    public void syncDeltaState(SaveData deltaState) {
        // Handle delta state synchronization
    }
    
    @Override
    public void syncSnapshot(SaveData snapshot, int snapshotIndex) {
        // Handle snapshot synchronization
    }
    
    @Override
    public void syncPacketMovement(String packetData) {
        // Handle packet movement synchronization
    }
    
    @Override
    public void syncSystemUpdate(String systemData) {
        // Handle system update synchronization
    }
    
    @Override
    public void syncWireUpdate(String wireData) {
        // Handle wire update synchronization
    }
    
    @Override
    public void syncImpactEffect(String impactData) {
        // Handle impact effect synchronization
    }
    
    // ==================== GETTERS ====================
    
    public GameMode getCurrentMode() {
        return currentMode;
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public SaveData getCurrentGameState() {
        return currentGameState;
    }
    
    public String getStatus() {
        switch (currentMode) {
            case SERVER:
                return "Server Mode - " + (serverGameEngine != null ? "Running" : "Stopped");
            case CLIENT:
                return "Client Mode - " + (connectionManager != null ? connectionManager.getConnectionStatus() : "Not Connected");
            case OFFLINE:
                return "Offline Mode - " + (offlineModeManager != null && offlineModeManager.isOfflineMode() ? "Running" : "Stopped");
            default:
                return "Unknown Mode";
        }
    }
}
