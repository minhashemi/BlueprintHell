package dev.aminhashemi.blueprinthell.core.strategy;

import dev.aminhashemi.blueprinthell.core.interfaces.GameStateManager;
import dev.aminhashemi.blueprinthell.core.interfaces.PacketManager;
import dev.aminhashemi.blueprinthell.core.interfaces.WireManager;

/**
 * Strategy interface for different game modes.
 * Follows Strategy pattern and Open/Closed Principle.
 */
public interface GameModeStrategy {
    
    /**
     * Initializes the game mode
     */
    void initialize();
    
    /**
     * Updates the game mode
     * @param deltaTime Time since last update
     */
    void update(double deltaTime);
    
    /**
     * Renders the game mode
     * @param g2d Graphics2D context
     */
    void render(java.awt.Graphics2D g2d);
    
    /**
     * Handles input for the game mode
     * @param inputType Type of input
     * @param data Input data
     */
    void handleInput(String inputType, Object data);
    
    /**
     * Gets the game state manager
     * @return Game state manager
     */
    GameStateManager getGameStateManager();
    
    /**
     * Gets the packet manager
     * @return Packet manager
     */
    PacketManager getPacketManager();
    
    /**
     * Gets the wire manager
     * @return Wire manager
     */
    WireManager getWireManager();
    
    /**
     * Cleans up resources
     */
    void cleanup();
}
