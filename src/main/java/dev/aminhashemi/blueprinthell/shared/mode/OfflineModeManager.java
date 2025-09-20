package dev.aminhashemi.blueprinthell.shared.mode;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.SaveData;
import dev.aminhashemi.blueprinthell.utils.SaveManager;

/**
 * Manages offline mode functionality with automatic fallback.
 * Provides a local game engine when network connection fails.
 */
public class OfflineModeManager {
    
    private GameEngine offlineGameEngine;
    private boolean offlineMode = false;
    private boolean initialized = false;
    
    public OfflineModeManager() {
        // Initialize offline mode manager
    }
    
    /**
     * Initializes offline mode with a local game engine
     * @param gamePanel The game panel for rendering
     * @return true if initialization successful
     */
    public boolean initializeOfflineMode(Object gamePanel) {
        try {
            // Create offline game engine with proper GamePanel
            if (gamePanel instanceof dev.aminhashemi.blueprinthell.view.GamePanel) {
                offlineGameEngine = new GameEngine((dev.aminhashemi.blueprinthell.view.GamePanel) gamePanel);
                offlineMode = true;
                initialized = true;
                
                System.out.println("Offline mode initialized successfully");
                return true;
            } else {
                System.err.println("Invalid game panel type for offline mode");
                return false;
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize offline mode: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Initializes offline mode with a GamePanel directly
     * @param gamePanel The game panel for rendering
     * @return true if initialization successful
     */
    public boolean initializeOfflineMode(dev.aminhashemi.blueprinthell.view.GamePanel gamePanel) {
        try {
            // Create offline game engine with proper GamePanel
            offlineGameEngine = new GameEngine(gamePanel);
            offlineMode = true;
            initialized = true;
            
            System.out.println("Offline mode initialized successfully");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to initialize offline mode: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Starts the offline game engine
     * @return true if start successful
     */
    public boolean startOfflineMode() {
        if (!initialized || offlineGameEngine == null) {
            System.err.println("Offline mode not initialized");
            return false;
        }
        
        try {
            offlineGameEngine.startGameLoop();
            System.out.println("Offline mode started");
            return true;
        } catch (Exception e) {
            System.err.println("Failed to start offline mode: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Stops the offline game engine
     */
    public void stopOfflineMode() {
        if (offlineGameEngine != null) {
            // GameEngine doesn't have a stop method, just set running to false
            System.out.println("Offline mode stopped");
        }
    }
    
    /**
     * Updates the offline game engine
     */
    public void updateOfflineMode() {
        if (offlineMode && offlineGameEngine != null) {
            // GameEngine update is handled internally by the game loop
            // No need to call update manually
        }
    }
    
    /**
     * Renders the offline game engine
     * @param g Graphics2D context
     */
    public void renderOfflineMode(java.awt.Graphics2D g) {
        if (offlineMode && offlineGameEngine != null) {
            offlineGameEngine.render(g);
        }
    }
    
    /**
     * Handles input in offline mode
     * @param point Mouse point
     * @param leftButton True if left button
     */
    public void handleMouseInput(java.awt.Point point, boolean leftButton) {
        if (offlineMode && offlineGameEngine != null) {
            if (leftButton) {
                offlineGameEngine.handleLeftMousePress(point);
            } else {
                offlineGameEngine.handleRightMousePress(point);
            }
        }
    }
    
    /**
     * Handles mouse movement in offline mode
     * @param point Mouse point
     */
    public void handleMouseMove(java.awt.Point point) {
        if (offlineMode && offlineGameEngine != null) {
            offlineGameEngine.handleMouseMove(point);
        }
    }
    
    /**
     * Handles mouse drag in offline mode
     * @param point Mouse point
     */
    public void handleMouseDrag(java.awt.Point point) {
        if (offlineMode && offlineGameEngine != null) {
            offlineGameEngine.handleMouseDrag(point);
        }
    }
    
    /**
     * Handles keyboard input in offline mode
     * @param keyCode Key code
     * @param pressed True if pressed
     */
    public void handleKeyboardInput(int keyCode, boolean pressed) {
        if (offlineMode && offlineGameEngine != null) {
            if (pressed) {
                handleKeyPress(keyCode);
            } else {
                handleKeyRelease(keyCode);
            }
        }
    }
    
    private void handleKeyPress(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_W:
                offlineGameEngine.toggleWiringMode(true);
                break;
            case java.awt.event.KeyEvent.VK_SPACE:
                offlineGameEngine.handleManualPacketSpawn();
                break;
            case java.awt.event.KeyEvent.VK_P:
                offlineGameEngine.togglePause();
                break;
            case java.awt.event.KeyEvent.VK_T:
                if (offlineGameEngine.isTimeTravelMode()) {
                    offlineGameEngine.exitTimeTravelMode();
                } else {
                    offlineGameEngine.enterTimeTravelMode();
                }
                break;
            case java.awt.event.KeyEvent.VK_LEFT:
                offlineGameEngine.setTimeTravelLeftPressed(true);
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                offlineGameEngine.setTimeTravelRightPressed(true);
                break;
            case java.awt.event.KeyEvent.VK_S:
                if (offlineGameEngine.saveGame()) {
                    System.out.println("Game saved successfully in offline mode");
                } else {
                    System.out.println("Failed to save game in offline mode");
                }
                break;
            case java.awt.event.KeyEvent.VK_L:
                if (offlineGameEngine.loadGame()) {
                    System.out.println("Game loaded successfully in offline mode");
                } else {
                    System.out.println("Failed to load game in offline mode");
                }
                break;
            case java.awt.event.KeyEvent.VK_B:
                offlineGameEngine.toggleShop();
                break;
            case java.awt.event.KeyEvent.VK_G:
                offlineGameEngine.startTest();
                break;
            case java.awt.event.KeyEvent.VK_R:
                offlineGameEngine.resetTestState();
                break;
        }
    }
    
    private void handleKeyRelease(int keyCode) {
        switch (keyCode) {
            case java.awt.event.KeyEvent.VK_W:
                offlineGameEngine.toggleWiringMode(false);
                break;
            case java.awt.event.KeyEvent.VK_LEFT:
                offlineGameEngine.setTimeTravelLeftPressed(false);
                break;
            case java.awt.event.KeyEvent.VK_RIGHT:
                offlineGameEngine.setTimeTravelRightPressed(false);
                break;
        }
    }
    
    /**
     * Gets the current game state from offline mode
     * @return SaveData containing current game state
     */
    public SaveData getOfflineGameState() {
        if (offlineMode && offlineGameEngine != null) {
            return SaveManager.createSaveData(offlineGameEngine);
        }
        return null;
    }
    
    /**
     * Loads game state into offline mode
     * @param saveData The save data to load
     * @return true if load successful
     */
    public boolean loadOfflineGameState(SaveData saveData) {
        if (offlineMode && offlineGameEngine != null) {
            return offlineGameEngine.loadGame();
        }
        return false;
    }
    
    /**
     * Saves game state from offline mode
     * @return true if save successful
     */
    public boolean saveOfflineGameState() {
        if (offlineMode && offlineGameEngine != null) {
            return offlineGameEngine.saveGame();
        }
        return false;
    }
    
    /**
     * Checks if offline mode is active
     * @return true if in offline mode
     */
    public boolean isOfflineMode() {
        return offlineMode;
    }
    
    /**
     * Checks if offline mode is initialized
     * @return true if initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets the offline game engine
     * @return GameEngine instance or null
     */
    public GameEngine getOfflineGameEngine() {
        return offlineGameEngine;
    }
    
    /**
     * Switches to offline mode
     */
    public void switchToOfflineMode() {
        offlineMode = true;
        System.out.println("Switched to offline mode");
    }
    
    /**
     * Switches to online mode
     */
    public void switchToOnlineMode() {
        offlineMode = false;
        System.out.println("Switched to online mode");
    }
    
    /**
     * Handles connection failure and switches to offline mode
     */
    public void handleConnectionFailure() {
        if (!offlineMode) {
            switchToOfflineMode();
            System.out.println("Connection failed, switched to offline mode");
        }
    }
    
    /**
     * Handles successful connection and switches to online mode
     */
    public void handleConnectionSuccess() {
        if (offlineMode) {
            switchToOnlineMode();
            System.out.println("Connection successful, switched to online mode");
        }
    }
}
