package dev.aminhashemi.blueprinthell.utils;

import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Manages leaderboard data persistence using JSON files
 */
public class LeaderboardManager {
    private static final String LEADERBOARD_FILE = "leaderboard.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Saves leaderboard data to JSON file
     */
    public static void saveLeaderboard(LeaderboardData leaderboardData) {
        try (FileWriter writer = new FileWriter(LEADERBOARD_FILE)) {
            gson.toJson(leaderboardData, writer);
            Logger.getInstance().info("Leaderboard data saved to " + LEADERBOARD_FILE);
        } catch (IOException e) {
            Logger.getInstance().error("Failed to save leaderboard data", e);
        }
    }
    
    /**
     * Loads leaderboard data from JSON file
     */
    public static LeaderboardData loadLeaderboard() {
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
    }
    
    /**
     * Saves a single record to the leaderboard
     */
    public static void saveRecord(String levelName, LeaderboardData.PlayerRecord record) {
        LeaderboardData data = loadLeaderboard();
        data.addRecord(levelName, record);
        saveLeaderboard(data);
    }
    
    /**
     * Updates player statistics
     */
    public static void updatePlayerStats(String playerName, int levelNumber, long completionTime, 
                                       int xpEarned, int coinsEarned) {
        LeaderboardData data = loadLeaderboard();
        data.updatePlayerStats(playerName, levelNumber, completionTime, xpEarned, coinsEarned);
        saveLeaderboard(data);
    }
}
