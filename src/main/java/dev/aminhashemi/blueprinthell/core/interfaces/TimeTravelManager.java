package dev.aminhashemi.blueprinthell.core.interfaces;

/**
 * Interface for time travel management following Single Responsibility Principle.
 * Handles time travel functionality including snapshots and navigation.
 */
public interface TimeTravelManager {
    
    /**
     * Enters time travel mode.
     */
    void enterTimeTravelMode();
    
    /**
     * Exits time travel mode.
     */
    void exitTimeTravelMode();
    
    /**
     * Checks if currently in time travel mode.
     * @return True if in time travel mode
     */
    boolean isTimeTravelMode();
    
    /**
     * Creates a snapshot of the current game state.
     */
    void createSnapshot();
    
    /**
     * Restores a snapshot at the given index.
     * @param index Index of the snapshot to restore
     */
    void restoreSnapshot(int index);
    
    /**
     * Gets the current snapshot index.
     * @return Current snapshot index
     */
    int getCurrentSnapshotIndex();
    
    /**
     * Gets the total number of snapshots.
     * @return Total number of snapshots
     */
    int getSnapshotCount();
    
    /**
     * Moves to the previous snapshot.
     */
    void moveToPreviousSnapshot();
    
    /**
     * Moves to the next snapshot.
     */
    void moveToNextSnapshot();
    
    /**
     * Sets the time travel left pressed state.
     * @param pressed True if left arrow is pressed
     */
    void setTimeTravelLeftPressed(boolean pressed);
    
    /**
     * Sets the time travel right pressed state.
     * @param pressed True if right arrow is pressed
     */
    void setTimeTravelRightPressed(boolean pressed);
    
    /**
     * Checks if time travel left is pressed.
     * @return True if left arrow is pressed
     */
    boolean isTimeTravelLeftPressed();
    
    /**
     * Checks if time travel right is pressed.
     * @return True if right arrow is pressed
     */
    boolean isTimeTravelRightPressed();
}