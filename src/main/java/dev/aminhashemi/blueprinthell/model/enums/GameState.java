package dev.aminhashemi.blueprinthell.model.enums;

/**
 * Enum representing the different states of the game.
 * 
 * This enum follows the State Pattern and makes the game state management
 * more explicit and easier to extend with new states.
 */
public enum GameState {
    
    /**
     * Main menu state - player is in the main menu
     */
    MAIN_MENU("Main Menu", "Player is in the main menu"),
    
    /**
     * Playing state - game is actively running
     */
    PLAYING("Playing", "Game is actively running"),
    
    /**
     * Paused state - game is paused
     */
    PAUSED("Paused", "Game is paused"),
    
    /**
     * Wiring mode - player is creating wires
     */
    WIRING_MODE("Wiring Mode", "Player is creating wires"),
    
    /**
     * Shop open - shop interface is displayed
     */
    SHOP_OPEN("Shop Open", "Shop interface is displayed"),
    
    /**
     * Test running - packet test is in progress
     */
    TEST_RUNNING("Test Running", "Packet test is in progress"),
    
    /**
     * Test completed - packet test has finished
     */
    TEST_COMPLETED("Test Completed", "Packet test has finished"),
    
    /**
     * Level completed - current level has been completed
     */
    LEVEL_COMPLETED("Level Completed", "Current level has been completed"),
    
    /**
     * Game over - game has ended
     */
    GAME_OVER("Game Over", "Game has ended");
    
    private final String displayName;
    private final String description;
    
    /**
     * Constructs a GameState with the specified properties.
     * 
     * @param displayName Human-readable name for the state
     * @param description Brief description of the state
     */
    GameState(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the description of the state.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if the game is in an active playing state.
     * 
     * @return True if the game is actively running
     */
    public boolean isActive() {
        return this == PLAYING || this == WIRING_MODE || this == TEST_RUNNING;
    }
    
    /**
     * Checks if the game is in a menu state.
     * 
     * @return True if the game is in a menu
     */
    public boolean isMenu() {
        return this == MAIN_MENU || this == SHOP_OPEN;
    }
    
    /**
     * Checks if the game is in a test-related state.
     * 
     * @return True if the game is in a test state
     */
    public boolean isTest() {
        return this == TEST_RUNNING || this == TEST_COMPLETED;
    }
    
    /**
     * Checks if the game is in a completed state.
     * 
     * @return True if the game is in a completed state
     */
    public boolean isCompleted() {
        return this == LEVEL_COMPLETED || this == GAME_OVER;
    }
}
