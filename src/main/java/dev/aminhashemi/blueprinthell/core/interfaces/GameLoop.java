package dev.aminhashemi.blueprinthell.core.interfaces;

/**
 * Interface for game loop management following Single Responsibility Principle.
 * Handles the main game loop execution and timing.
 */
public interface GameLoop {
    
    /**
     * Starts the game loop.
     */
    void start();
    
    /**
     * Stops the game loop.
     */
    void stop();
    
    /**
     * Checks if the game loop is running.
     * @return True if running
     */
    boolean isRunning();
    
    /**
     * Updates the game state.
     * Called once per update cycle.
     */
    void update();
    
    /**
     * Renders the game.
     * Called once per render cycle.
     */
    void render();
    
    /**
     * Gets the current FPS (Frames Per Second).
     * @return Current FPS
     */
    int getFPS();
    
    /**
     * Gets the current UPS (Updates Per Second).
     * @return Current UPS
     */
    int getUPS();
}