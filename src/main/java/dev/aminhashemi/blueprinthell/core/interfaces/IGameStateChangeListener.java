package dev.aminhashemi.blueprinthell.core.interfaces;

import dev.aminhashemi.blueprinthell.model.enums.GameState;
import dev.aminhashemi.blueprinthell.model.enums.GameMode;

/**
 * Interface for listening to game state changes.
 * 
 * This interface follows the Observer Pattern and allows components
 * to be notified when the game state changes. It follows the Interface
 * Segregation Principle by providing only the methods needed for
 * state change notifications.
 */
public interface IGameStateChangeListener {
    
    /**
     * Called when the game state changes.
     * 
     * @param oldState The previous state
     * @param newState The new state
     */
    void onStateChanged(GameState oldState, GameState newState);
    
    /**
     * Called when the game mode changes.
     * 
     * @param oldMode The previous mode
     * @param newMode The new mode
     */
    void onModeChanged(GameMode oldMode, GameMode newMode);
    
    /**
     * Called when the game is paused.
     */
    void onGamePaused();
    
    /**
     * Called when the game is resumed.
     */
    void onGameResumed();
    
    /**
     * Called when the game is started.
     */
    void onGameStarted();
    
    /**
     * Called when the game is stopped.
     */
    void onGameStopped();
}
