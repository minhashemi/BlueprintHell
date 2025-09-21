package dev.aminhashemi.blueprinthell.core.interfaces;

import dev.aminhashemi.blueprinthell.model.enums.GameState;
import dev.aminhashemi.blueprinthell.model.enums.GameMode;

/**
 * Interface for managing game state.
 * 
 * This interface follows the Single Responsibility Principle by focusing
 * solely on game state management. It also follows the Interface Segregation
 * Principle by providing only the methods needed for state management.
 * 
 * It allows for easy extension of new state management behaviors without
 * modifying existing code (Open/Closed Principle).
 */
public interface IGameStateManager {
    
    // ==================== STATE QUERIES ====================
    
    /**
     * Gets the current game state.
     * 
     * @return The current game state
     */
    GameState getCurrentState();
    
    /**
     * Gets the current game mode.
     * 
     * @return The current game mode
     */
    GameMode getCurrentMode();
    
    /**
     * Checks if the game is in a specific state.
     * 
     * @param state The state to check
     * @return True if the game is in the specified state
     */
    boolean isInState(GameState state);
    
    /**
     * Checks if the game is in a specific mode.
     * 
     * @param mode The mode to check
     * @return True if the game is in the specified mode
     */
    boolean isInMode(GameMode mode);
    
    /**
     * Checks if the game is currently active (playing, wiring, testing).
     * 
     * @return True if the game is active
     */
    boolean isActive();
    
    /**
     * Checks if the game is currently paused.
     * 
     * @return True if the game is paused
     */
    boolean isPaused();
    
    // ==================== STATE TRANSITIONS ====================
    
    /**
     * Transitions to a new game state.
     * 
     * @param newState The new state to transition to
     * @return True if the transition was successful
     */
    boolean transitionTo(GameState newState);
    
    /**
     * Transitions to a new game mode.
     * 
     * @param newMode The new mode to transition to
     * @return True if the transition was successful
     */
    boolean transitionTo(GameMode newMode);
    
    /**
     * Pauses the game.
     * 
     * @return True if the game was paused successfully
     */
    boolean pause();
    
    /**
     * Resumes the game.
     * 
     * @return True if the game was resumed successfully
     */
    boolean resume();
    
    /**
     * Starts the game.
     * 
     * @return True if the game was started successfully
     */
    boolean start();
    
    /**
     * Stops the game.
     * 
     * @return True if the game was stopped successfully
     */
    boolean stop();
    
    // ==================== STATE HISTORY ====================
    
    /**
     * Gets the previous game state.
     * 
     * @return The previous game state, or null if none
     */
    GameState getPreviousState();
    
    /**
     * Checks if a state transition is valid.
     * 
     * @param fromState The current state
     * @param toState The target state
     * @return True if the transition is valid
     */
    boolean isValidTransition(GameState fromState, GameState toState);
    
    /**
     * Checks if a mode transition is valid.
     * 
     * @param fromMode The current mode
     * @param toMode The target mode
     * @return True if the transition is valid
     */
    boolean isValidTransition(GameMode fromMode, GameMode toMode);
    
    // ==================== STATE LISTENERS ====================
    
    /**
     * Adds a state change listener.
     * 
     * @param listener The listener to add
     */
    void addStateChangeListener(IGameStateChangeListener listener);
    
    /**
     * Removes a state change listener.
     * 
     * @param listener The listener to remove
     */
    void removeStateChangeListener(IGameStateChangeListener listener);
    
    /**
     * Notifies all listeners of a state change.
     * 
     * @param oldState The previous state
     * @param newState The new state
     */
    void notifyStateChange(GameState oldState, GameState newState);
}
