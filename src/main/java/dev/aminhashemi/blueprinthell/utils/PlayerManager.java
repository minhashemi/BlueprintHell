package dev.aminhashemi.blueprinthell.utils;

import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.network.ClientNetworkManager;
import java.util.concurrent.CompletableFuture;

/**
 * Manages player information including username with server integration
 */
public class PlayerManager {
    private static PlayerManager instance;
    private String playerName;
    private UserProfile userProfile;
    private ClientNetworkManager networkManager;
    private boolean serverMode;
    
    private PlayerManager() {
        this.playerName = "Player"; // Default name
        this.networkManager = new ClientNetworkManager();
        this.serverMode = false;
    }
    
    public static PlayerManager getInstance() {
        if (instance == null) {
            instance = new PlayerManager();
        }
        return instance;
    }
    
    /**
     * Initialize with server connection
     */
    public CompletableFuture<Boolean> initializeWithServer() {
        return CompletableFuture.supplyAsync(() -> {
            if (networkManager.connect()) {
                serverMode = true;
                Logger.getInstance().info("PlayerManager initialized with server connection");
                return true;
            } else {
                serverMode = false;
                Logger.getInstance().warning("Failed to connect to server, using offline mode");
                return false;
            }
        });
    }
    
    /**
     * Authenticate with server
     */
    public CompletableFuture<Boolean> authenticate(String username) {
        if (!serverMode) {
            setPlayerName(username);
            return CompletableFuture.completedFuture(true);
        }
        
        return networkManager.authenticate(username).thenApply(profile -> {
            if (profile != null) {
                this.userProfile = profile;
                this.playerName = profile.getUsername();
                Logger.getInstance().info("Authenticated as: " + username);
                return true;
            } else {
                setPlayerName(username);
                return false;
            }
        });
    }
    
    /**
     * Get user profile from server
     */
    public CompletableFuture<UserProfile> getUserProfile() {
        if (!serverMode || userProfile != null) {
            return CompletableFuture.completedFuture(userProfile);
        }
        
        return networkManager.getUserProfile().thenApply(profile -> {
            this.userProfile = profile;
            if (profile != null) {
                this.playerName = profile.getUsername();
            }
            return profile;
        });
    }
    
    /**
     * Update user profile on server
     */
    public CompletableFuture<Boolean> updateUserProfile() {
        if (!serverMode || userProfile == null) {
            return CompletableFuture.completedFuture(true);
        }
        
        return networkManager.updateUserProfile(userProfile);
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
    
    public UserProfile getUserProfileSync() {
        return userProfile;
    }
    
    public boolean isServerMode() {
        return serverMode;
    }
    
    public ClientNetworkManager getNetworkManager() {
        return networkManager;
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        if (networkManager != null) {
            networkManager.disconnect();
        }
        serverMode = false;
    }
}
