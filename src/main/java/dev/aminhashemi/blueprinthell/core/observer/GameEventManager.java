package dev.aminhashemi.blueprinthell.core.observer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages game events and notifies listeners.
 * Follows Observer pattern and Single Responsibility Principle.
 */
public class GameEventManager {
    
    private final List<GameEventListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * Adds an event listener
     * @param listener Listener to add
     */
    public void addListener(GameEventListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes an event listener
     * @param listener Listener to remove
     */
    public void removeListener(GameEventListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Fires a game event to all listeners
     * @param event Event to fire
     */
    public void fireEvent(GameEvent event) {
        for (GameEventListener listener : listeners) {
            try {
                listener.onGameEvent(event);
            } catch (Exception e) {
                // Log error but don't stop other listeners
                System.err.println("Error in event listener: " + e.getMessage());
            }
        }
    }
    
    /**
     * Fires a game event with type and data
     * @param type Event type
     * @param data Event data
     */
    public void fireEvent(GameEvent.EventType type, Object data) {
        fireEvent(new GameEvent(type, data));
    }
    
    /**
     * Clears all listeners
     */
    public void clearListeners() {
        listeners.clear();
    }
    
    /**
     * Gets the number of listeners
     * @return Number of listeners
     */
    public int getListenerCount() {
        return listeners.size();
    }
}
