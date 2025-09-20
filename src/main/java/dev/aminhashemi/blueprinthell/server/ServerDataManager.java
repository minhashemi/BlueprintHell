package dev.aminhashemi.blueprinthell.server;

import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side data manager for storing and managing all player data
 */
public class ServerDataManager {
    private static final String USERS_DIR = "server_data/users/";
    private static final String GLOBAL_DATA_FILE = "server_data/global_data.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private Map<String, UserProfile> userProfiles;
    private LeaderboardData globalLeaderboard;
    private Map<String, Object> globalGameData;
    
    public ServerDataManager() {
        this.userProfiles = new ConcurrentHashMap<>();
        this.globalLeaderboard = new LeaderboardData();
        this.globalGameData = new HashMap<>();
        
        // Create directories if they don't exist
        createDirectories();
        
        // Load existing data
        loadGlobalData();
        loadUserProfiles();
    }
    
    /**
     * Create necessary directories
     */
    private void createDirectories() {
        File usersDir = new File(USERS_DIR);
        if (!usersDir.exists()) {
            usersDir.mkdirs();
        }
        
        File serverDataDir = new File("server_data");
        if (!serverDataDir.exists()) {
            serverDataDir.mkdirs();
        }
    }
    
    /**
     * Get or create user profile by MAC address
     */
    public UserProfile getUserProfile(String macAddress) {
        return userProfiles.computeIfAbsent(macAddress, mac -> {
            UserProfile profile = new UserProfile(mac, "Player_" + mac.substring(0, 8));
            saveUserProfile(profile);
            return profile;
        });
    }
    
    /**
     * Update user profile
     */
    public void updateUserProfile(UserProfile profile) {
        userProfiles.put(profile.getMacAddress(), profile);
        saveUserProfile(profile);
    }
    
    /**
     * Save user profile to file
     */
    private void saveUserProfile(UserProfile profile) {
        try {
            String filename = USERS_DIR + profile.getMacAddress().replace(":", "_") + ".json";
            try (FileWriter writer = new FileWriter(filename)) {
                gson.toJson(profile, writer);
            }
            Logger.getInstance().info("User profile saved: " + profile.getUsername());
        } catch (IOException e) {
            Logger.getInstance().error("Failed to save user profile", e);
        }
    }
    
    /**
     * Load user profiles from files
     */
    private void loadUserProfiles() {
        File usersDir = new File(USERS_DIR);
        File[] files = usersDir.listFiles((dir, name) -> name.endsWith(".json"));
        
        if (files != null) {
            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    UserProfile profile = gson.fromJson(reader, UserProfile.class);
                    if (profile != null) {
                        userProfiles.put(profile.getMacAddress(), profile);
                    }
                } catch (IOException e) {
                    Logger.getInstance().error("Failed to load user profile: " + file.getName(), e);
                }
            }
        }
        
        Logger.getInstance().info("Loaded " + userProfiles.size() + " user profiles");
    }
    
    /**
     * Get global leaderboard data
     */
    public LeaderboardData getGlobalLeaderboard() {
        return globalLeaderboard;
    }
    
    /**
     * Update global leaderboard
     */
    public void updateGlobalLeaderboard(LeaderboardData leaderboardData) {
        this.globalLeaderboard = leaderboardData;
        saveGlobalData();
    }
    
    /**
     * Add a game record to global leaderboard
     */
    public void addGameRecord(String levelName, LeaderboardData.PlayerRecord record) {
        globalLeaderboard.addRecord(levelName, record);
        saveGlobalData();
    }
    
    /**
     * Save global data to file
     */
    private void saveGlobalData() {
        try (FileWriter writer = new FileWriter(GLOBAL_DATA_FILE)) {
            gson.toJson(globalLeaderboard, writer);
        } catch (IOException e) {
            Logger.getInstance().error("Failed to save global data", e);
        }
    }
    
    /**
     * Load global data from file
     */
    private void loadGlobalData() {
        File file = new File(GLOBAL_DATA_FILE);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                globalLeaderboard = gson.fromJson(reader, LeaderboardData.class);
                if (globalLeaderboard == null) {
                    globalLeaderboard = new LeaderboardData();
                }
            } catch (IOException e) {
                Logger.getInstance().error("Failed to load global data", e);
                globalLeaderboard = new LeaderboardData();
            }
        }
    }
    
    /**
     * Get all user profiles
     */
    public Collection<UserProfile> getAllUserProfiles() {
        return userProfiles.values();
    }
    
    /**
     * Get user count
     */
    public int getUserCount() {
        return userProfiles.size();
    }
    
    /**
     * Set global game data
     */
    public void setGlobalGameData(String key, Object value) {
        globalGameData.put(key, value);
        saveGlobalData();
    }
    
    /**
     * Get global game data
     */
    public Object getGlobalGameData(String key) {
        return globalGameData.get(key);
    }
}
