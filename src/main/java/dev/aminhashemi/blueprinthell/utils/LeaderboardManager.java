package dev.aminhashemi.blueprinthell.utils;

import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.network.ClientNetworkManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.concurrent.CompletableFuture;

/**
 * Manages leaderboard data with server integration
 */
public class LeaderboardManager {
    private static final String LEADERBOARD_FILE = "leaderboard.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static ClientNetworkManager networkManager;
    private static boolean serverMode = false;
    
    /**
     * Initialize with server connection
     */
    public static void initializeWithServer(ClientNetworkManager networkManager) {
        LeaderboardManager.networkManager = networkManager;
        serverMode = true;
    }
    
    /**
     * Saves leaderboard data to server or local file
     */
    public static CompletableFuture<Boolean> saveLeaderboard(LeaderboardData leaderboardData) {
        if (serverMode && networkManager != null && networkManager.isConnected()) {
            // Server mode - data is managed by server
            return CompletableFuture.completedFuture(true);
        } else {
            // Offline mode - save to local file
            return CompletableFuture.supplyAsync(() -> {
                try (FileWriter writer = new FileWriter(LEADERBOARD_FILE)) {
                    gson.toJson(leaderboardData, writer);
                    Logger.getInstance().info("Leaderboard data saved to " + LEADERBOARD_FILE);
                    return true;
                } catch (IOException e) {
                    Logger.getInstance().error("Failed to save leaderboard data", e);
                    return false;
                }
            });
        }
    }
    
    /**
     * Loads leaderboard data from server or local file
     */
    public static CompletableFuture<LeaderboardData> loadLeaderboard() {
        if (serverMode && networkManager != null && networkManager.isConnected()) {
            return networkManager.getLeaderboard();
        } else {
            return CompletableFuture.supplyAsync(() -> {
                File file = new File(LEADERBOARD_FILE);
                if (!file.exists()) {
                    Logger.getInstance().info("No existing leaderboard file found, creating new one");
                    return new LeaderboardData();
                }
                
                try (FileReader reader = new FileReader(LEADERBOARD_FILE)) {
                    LeaderboardData data = gson.fromJson(reader, LeaderboardData.class);
                    if (data == null) {
                        Logger.getInstance().warning("Failed to parse leaderboard data, creating new one");
                        return new LeaderboardData();
                    }
                    Logger.getInstance().info("Leaderboard data loaded from " + LEADERBOARD_FILE);
                    return data;
                } catch (IOException e) {
                    Logger.getInstance().error("Failed to load leaderboard data", e);
                    return new LeaderboardData();
                }
            });
        }
    }
    
    /**
     * Saves a single record to the leaderboard
     */
    public static CompletableFuture<Boolean> saveRecord(String levelName, LeaderboardData.PlayerRecord record) {
        if (serverMode && networkManager != null && networkManager.isConnected()) {
            return networkManager.addGameRecord(levelName, record);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                LeaderboardData data = loadLeaderboard().join();
                data.addRecord(levelName, record);
                return saveLeaderboard(data).join();
            });
        }
    }
    
    /**
     * Updates player statistics
     */
    public static CompletableFuture<Boolean> updatePlayerStats(String playerName, int levelNumber, long completionTime, 
                                       int xpEarned, int coinsEarned) {
        if (serverMode && networkManager != null && networkManager.isConnected()) {
            // Server handles player stats
            return CompletableFuture.completedFuture(true);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                LeaderboardData data = loadLeaderboard().join();
                data.updatePlayerStats(playerName, levelNumber, completionTime, xpEarned, coinsEarned);
                return saveLeaderboard(data).join();
            });
        }
    }
    
    /**
     * Set server mode
     */
    public static void setServerMode(boolean serverMode) {
        LeaderboardManager.serverMode = serverMode;
    }
    
    /**
     * Check if in server mode
     */
    public static boolean isServerMode() {
        return serverMode;
    }
}
