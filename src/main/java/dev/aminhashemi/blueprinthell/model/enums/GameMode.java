package dev.aminhashemi.blueprinthell.model.enums;

/**
 * Enum representing the different modes the game can run in.
 * 
 * This enum follows the Strategy Pattern and makes the game mode management
 * more explicit and easier to extend with new modes.
 */
public enum GameMode {
    
    /**
     * Offline mode - single player, no network connection
     */
    OFFLINE("Offline", "Single player, no network connection", false),
    
    /**
     * Client mode - connected to a server
     */
    CLIENT("Client", "Connected to a server", true),
    
    /**
     * Server mode - hosting a game
     */
    SERVER("Server", "Hosting a game", true),
    
    /**
     * Headless server mode - server without GUI
     */
    HEADLESS_SERVER("Headless Server", "Server without GUI", true);
    
    private final String displayName;
    private final String description;
    private final boolean requiresNetwork;
    
    /**
     * Constructs a GameMode with the specified properties.
     * 
     * @param displayName Human-readable name for the mode
     * @param description Brief description of the mode
     * @param requiresNetwork Whether this mode requires network connectivity
     */
    GameMode(String displayName, String description, boolean requiresNetwork) {
        this.displayName = displayName;
        this.description = description;
        this.requiresNetwork = requiresNetwork;
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
     * Gets the description of the mode.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Checks if this mode requires network connectivity.
     * 
     * @return True if network is required
     */
    public boolean requiresNetwork() {
        return requiresNetwork;
    }
    
    /**
     * Checks if this is a server mode.
     * 
     * @return True if this is a server mode
     */
    public boolean isServer() {
        return this == SERVER || this == HEADLESS_SERVER;
    }
    
    /**
     * Checks if this is a client mode.
     * 
     * @return True if this is a client mode
     */
    public boolean isClient() {
        return this == CLIENT;
    }
    
    /**
     * Checks if this is an offline mode.
     * 
     * @return True if this is an offline mode
     */
    public boolean isOffline() {
        return this == OFFLINE;
    }
    
    /**
     * Gets the game mode from a string identifier.
     * 
     * @param modeString The string identifier
     * @return The corresponding GameMode, or null if not found
     */
    public static GameMode fromString(String modeString) {
        if (modeString == null) {
            return null;
        }
        
        for (GameMode mode : values()) {
            if (mode.name().equalsIgnoreCase(modeString) || 
                mode.displayName.equalsIgnoreCase(modeString)) {
                return mode;
            }
        }
        return null;
    }
}
