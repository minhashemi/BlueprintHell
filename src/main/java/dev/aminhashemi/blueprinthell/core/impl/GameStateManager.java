package dev.aminhashemi.blueprinthell.core.impl;

import dev.aminhashemi.blueprinthell.core.interfaces.IGameStateManager;
import dev.aminhashemi.blueprinthell.core.interfaces.IGameStateChangeListener;
import dev.aminhashemi.blueprinthell.model.enums.GameState;
import dev.aminhashemi.blueprinthell.model.enums.GameMode;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Concrete implementation of IGameStateManager.
 * 
 * This class follows the State Pattern and manages game state transitions
 * in a thread-safe manner. It also follows the Observer Pattern for
 * notifying listeners of state changes.
 * 
 * It implements the Single Responsibility Principle by focusing solely
 * on state management, and the Open/Closed Principle by being easily
 * extensible for new states and transitions.
 */
public class GameStateManager implements IGameStateManager {
    
    private static final Logger logger = Logger.getInstance();
    
    private GameState currentState;
    private GameState previousState;
    private GameMode currentMode;
    private final List<IGameStateChangeListener> listeners;
    
    /**
     * Constructs a new GameStateManager with default state and mode.
     */
    public GameStateManager() {
        this.currentState = GameState.MAIN_MENU;
        this.previousState = null;
        this.currentMode = GameMode.OFFLINE;
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    /**
     * Constructs a new GameStateManager with specified initial state and mode.
     * 
     * @param initialState The initial game state
     * @param initialMode The initial game mode
     */
    public GameStateManager(GameState initialState, GameMode initialMode) {
        this.currentState = initialState;
        this.previousState = null;
        this.currentMode = initialMode;
        this.listeners = new CopyOnWriteArrayList<>();
    }
    
    // ==================== STATE QUERIES ====================
    
    @Override
    public GameState getCurrentState() {
        return currentState;
    }
    
    @Override
    public GameMode getCurrentMode() {
        return currentMode;
    }
    
    @Override
    public boolean isInState(GameState state) {
        return currentState == state;
    }
    
    @Override
    public boolean isInMode(GameMode mode) {
        return currentMode == mode;
    }
    
    @Override
    public boolean isActive() {
        return currentState.isActive();
    }
    
    @Override
    public boolean isPaused() {
        return currentState == GameState.PAUSED;
    }
    
    // ==================== STATE TRANSITIONS ====================
    
    @Override
    public boolean transitionTo(GameState newState) {
        if (newState == null) {
            logger.warning("Cannot transition to null state");
            return false;
        }
        
        if (newState == currentState) {
            logger.debug("Already in state: " + newState);
            return true;
        }
        
        if (!isValidTransition(currentState, newState)) {
            logger.warning("Invalid transition from " + currentState + " to " + newState);
            return false;
        }
        
        GameState oldState = currentState;
        previousState = oldState;
        currentState = newState;
        
        logger.info("State transition: " + oldState + " -> " + newState);
        notifyStateChange(oldState, newState);
        
        return true;
    }
    
    @Override
    public boolean transitionTo(GameMode newMode) {
        if (newMode == null) {
            logger.warning("Cannot transition to null mode");
            return false;
        }
        
        if (newMode == currentMode) {
            logger.debug("Already in mode: " + newMode);
            return true;
        }
        
        if (!isValidTransition(currentMode, newMode)) {
            logger.warning("Invalid transition from " + currentMode + " to " + newMode);
            return false;
        }
        
        GameMode oldMode = currentMode;
        currentMode = newMode;
        
        logger.info("Mode transition: " + oldMode + " -> " + newMode);
        notifyModeChange(oldMode, newMode);
        
        return true;
    }
    
    @Override
    public boolean pause() {
        if (currentState != GameState.PLAYING) {
            logger.warning("Cannot pause game in state: " + currentState);
            return false;
        }
        
        return transitionTo(GameState.PAUSED);
    }
    
    @Override
    public boolean resume() {
        if (currentState != GameState.PAUSED) {
            logger.warning("Cannot resume game in state: " + currentState);
            return false;
        }
        
        return transitionTo(GameState.PLAYING);
    }
    
    @Override
    public boolean start() {
        if (currentState != GameState.MAIN_MENU) {
            logger.warning("Cannot start game from state: " + currentState);
            return false;
        }
        
        return transitionTo(GameState.PLAYING);
    }
    
    @Override
    public boolean stop() {
        if (currentState == GameState.GAME_OVER) {
            logger.debug("Game already stopped");
            return true;
        }
        
        return transitionTo(GameState.GAME_OVER);
    }
    
    // ==================== STATE HISTORY ====================
    
    @Override
    public GameState getPreviousState() {
        return previousState;
    }
    
    @Override
    public boolean isValidTransition(GameState fromState, GameState toState) {
        if (fromState == null || toState == null) {
            return false;
        }
        
        // Define valid transitions
        return switch (fromState) {
            case MAIN_MENU -> toState == GameState.PLAYING || toState == GameState.SHOP_OPEN;
            case PLAYING -> toState == GameState.PAUSED || toState == GameState.WIRING_MODE || 
                           toState == GameState.TEST_RUNNING || toState == GameState.LEVEL_COMPLETED ||
                           toState == GameState.GAME_OVER;
            case PAUSED -> toState == GameState.PLAYING || toState == GameState.MAIN_MENU;
            case WIRING_MODE -> toState == GameState.PLAYING;
            case SHOP_OPEN -> toState == GameState.MAIN_MENU || toState == GameState.PLAYING;
            case TEST_RUNNING -> toState == GameState.TEST_COMPLETED || toState == GameState.PLAYING;
            case TEST_COMPLETED -> toState == GameState.PLAYING || toState == GameState.LEVEL_COMPLETED;
            case LEVEL_COMPLETED -> toState == GameState.PLAYING || toState == GameState.MAIN_MENU;
            case GAME_OVER -> toState == GameState.MAIN_MENU;
        };
    }
    
    @Override
    public boolean isValidTransition(GameMode fromMode, GameMode toMode) {
        if (fromMode == null || toMode == null) {
            return false;
        }
        
        // All mode transitions are valid, but log them for debugging
        logger.debug("Mode transition from " + fromMode + " to " + toMode);
        return true;
    }
    
    // ==================== STATE LISTENERS ====================
    
    @Override
    public void addStateChangeListener(IGameStateChangeListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
            logger.debug("Added state change listener: " + listener.getClass().getSimpleName());
        }
    }
    
    @Override
    public void removeStateChangeListener(IGameStateChangeListener listener) {
        if (listener != null && listeners.remove(listener)) {
            logger.debug("Removed state change listener: " + listener.getClass().getSimpleName());
        }
    }
    
    @Override
    public void notifyStateChange(GameState oldState, GameState newState) {
        for (IGameStateChangeListener listener : listeners) {
            try {
                listener.onStateChanged(oldState, newState);
            } catch (Exception e) {
                logger.error("Error notifying state change listener", e);
            }
        }
    }
    
    /**
     * Notifies all listeners of a mode change.
     * 
     * @param oldMode The previous mode
     * @param newMode The new mode
     */
    private void notifyModeChange(GameMode oldMode, GameMode newMode) {
        for (IGameStateChangeListener listener : listeners) {
            try {
                listener.onModeChanged(oldMode, newMode);
            } catch (Exception e) {
                logger.error("Error notifying mode change listener", e);
            }
        }
    }
    
    /**
     * Gets the number of registered listeners.
     * 
     * @return The number of listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
    
    /**
     * Clears all listeners.
     */
    public void clearListeners() {
        listeners.clear();
        logger.debug("Cleared all state change listeners");
    }
}
