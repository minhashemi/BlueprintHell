package dev.aminhashemi.blueprinthell.core.observer;

/**
 * Interface for game event listeners.
 * Follows Observer pattern for loose coupling.
 */
public interface GameEventListener {
    
    /**
     * Handles a game event
     * @param event The game event
     */
    void onGameEvent(GameEvent event);
}
