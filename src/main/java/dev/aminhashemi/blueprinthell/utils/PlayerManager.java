package dev.aminhashemi.blueprinthell.utils;

/**
 * Manages player information including username
 */
public class PlayerManager {
    private static PlayerManager instance;
    private String playerName;
    
    private PlayerManager() {
        this.playerName = "Player"; // Default name
    }
    
    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }
    
    /**
     * Sets the player's name
     */
    public void setPlayerName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.playerName = name.trim();
        } else {
            this.playerName = "Player";
        }
    }
    
    /**
     * Gets the current player's name
     */
    public String getPlayerName() {
        return playerName;
    }
    
    /**
     * Resets the player name to default
     */
    public void resetPlayerName() {
        this.playerName = "Player";
    }
}
