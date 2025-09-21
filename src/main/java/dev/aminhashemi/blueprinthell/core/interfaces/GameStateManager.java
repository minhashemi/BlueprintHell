package dev.aminhashemi.blueprinthell.core.interfaces;

import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.MovingPacket;

import java.util.List;

/**
 * Interface for managing game state operations.
 * Follows Interface Segregation Principle by focusing only on state management.
 */
public interface GameStateManager {
    
    /**
     * Loads a level from level data
     * @param levelData The level data to load
     */
    void loadLevel(LevelData levelData);
    
    /**
     * Resets the current level to its initial state
     */
    void resetCurrentLevel();
    
    /**
     * Loads the next level
     */
    void loadNextLevel();
    
    /**
     * Gets the current level number
     * @return Current level number
     */
    int getCurrentLevelNumber();
    
    /**
     * Gets the current level name
     * @return Current level name
     */
    String getCurrentLevelName();
    
    /**
     * Gets all systems in the game
     * @return List of systems
     */
    List<System> getSystems();
    
    /**
     * Gets all wires in the game
     * @return List of wires
     */
    List<Wire> getWires();
    
    /**
     * Gets all moving packets
     * @return List of moving packets
     */
    List<MovingPacket> getMovingPackets();
    
    /**
     * Clears all game entities
     */
    void clearGameState();
}
