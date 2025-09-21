package dev.aminhashemi.blueprinthell.server;

import dev.aminhashemi.blueprinthell.database.DatabaseManager;
import dev.aminhashemi.blueprinthell.database.UserProfileDatabaseManager;
import dev.aminhashemi.blueprinthell.database.LeaderboardDatabaseManager;
import dev.aminhashemi.blueprinthell.database.OfflineSyncManager;
import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.model.entities.UserProfileEntity;
import dev.aminhashemi.blueprinthell.model.entities.LeaderboardRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.GameRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.PlayerStatsEntity;
import dev.aminhashemi.blueprinthell.model.entities.OfflineSyncEntity;
import dev.aminhashemi.blueprinthell.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enhanced server-side data manager using database for persistence
 * Maintains JSON communication with clients while using database for storage
 */
public class DatabaseServerDataManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private final UserProfileDatabaseManager userProfileManager;
    private final LeaderboardDatabaseManager leaderboardManager;
    private final OfflineSyncManager offlineSyncManager;
    private final Logger logger;
    
    // Cache for frequently accessed data
    private final Map<String, UserProfile> userProfileCache;
    private final Map<String, LeaderboardData> leaderboardCache;
    private final Map<String, Object> globalGameData;
    
    public DatabaseServerDataManager() {
        this.userProfileManager = new UserProfileDatabaseManager();
        this.leaderboardManager = new LeaderboardDatabaseManager();
        this.offlineSyncManager = new OfflineSyncManager();
        this.logger = Logger.getInstance();
        
        this.userProfileCache = new ConcurrentHashMap<>();
        this.leaderboardCache = new ConcurrentHashMap<>();
        this.globalGameData = new HashMap<>();
        
        // Initialize database
        DatabaseManager.getInstance().initialize();
        
        // Start offline sync manager
        offlineSyncManager.start();
        
        logger.info("Database server data manager initialized");
    }
    
    /**
     * Get or create user profile by MAC address
     */
    public UserProfile getUserProfile(String macAddress) {
        // Check cache first
        UserProfile cachedProfile = userProfileCache.get(macAddress);
        if (cachedProfile != null) {
            return cachedProfile;
        }
        
        // Get from database
        UserProfileEntity entity = userProfileManager.getUserProfile(macAddress);
        UserProfile profile = userProfileManager.toUserProfile(entity);
        
        // Cache the profile
        userProfileCache.put(macAddress, profile);
        
        return profile;
    }
    
    /**
     * Update user profile
     */
    public void updateUserProfile(UserProfile profile) {
        try {
            // Convert to entity and save to database
            UserProfileEntity entity = userProfileManager.fromUserProfile(profile);
            userProfileManager.updateUserProfile(entity);
            
            // Update cache
            userProfileCache.put(profile.getMacAddress(), profile);
            
            // Queue for offline sync if needed
            String profileJson = gson.toJson(profile);
            offlineSyncManager.queueForSync(profile.getMacAddress(), 
                OfflineSyncEntity.DataType.USER_PROFILE, 
                profile.getMacAddress(), profileJson);
            
            logger.info("Updated user profile: " + profile.getUsername());
        } catch (Exception e) {
            logger.error("Failed to update user profile", e);
            throw e;
        }
    }
    
    /**
     * Add a game record
     */
    public void addGameRecord(String macAddress, UserProfile.GameRecord record) {
        try {
            // Create entity
            GameRecordEntity entity = new GameRecordEntity(
                macAddress,
                record.getLevelName(),
                record.getCompletionTime(),
                record.getXpEarned(),
                record.getCoinsEarned(),
                record.getPacketLossPercentage()
            );
            
            // Save to database
            userProfileManager.addGameRecord(macAddress, entity);
            
            // Create leaderboard record
            LeaderboardRecordEntity leaderboardRecord = new LeaderboardRecordEntity(
                getUserProfile(macAddress).getUsername(),
                record.getCompletionTime(),
                record.getXpEarned(),
                1, // level number - would need to be determined from level name
                record.getLevelName(),
                record.getPacketLossPercentage(),
                record.getCoinsEarned(),
                macAddress
            );
            
            leaderboardManager.addLeaderboardRecord(leaderboardRecord);
            
            // Update player stats
            leaderboardManager.updatePlayerStats(
                getUserProfile(macAddress).getUsername(),
                1, // level number
                record.getCompletionTime(),
                record.getXpEarned(),
                record.getCoinsEarned()
            );
            
            // Clear leaderboard cache to force refresh
            leaderboardCache.clear();
            
            // Queue for offline sync
            String recordJson = gson.toJson(record);
            offlineSyncManager.queueForSync(macAddress, 
                OfflineSyncEntity.DataType.GAME_RECORD, 
                record.getLevelName(), recordJson);
            
            logger.info("Added game record for user: " + macAddress);
        } catch (Exception e) {
            logger.error("Failed to add game record", e);
            throw e;
        }
    }
    
    /**
     * Get global leaderboard data
     */
    public LeaderboardData getGlobalLeaderboard() {
        // Check cache first
        LeaderboardData cachedLeaderboard = leaderboardCache.get("global");
        if (cachedLeaderboard != null) {
            return cachedLeaderboard;
        }
        
        // Get from database
        LeaderboardData leaderboard = leaderboardManager.getGlobalLeaderboardData();
        
        // Cache the result
        leaderboardCache.put("global", leaderboard);
        
        return leaderboard;
    }
    
    /**
     * Get level leaderboard data
     */
    public LeaderboardData getLevelLeaderboard(String levelName) {
        // Check cache first
        LeaderboardData cachedLeaderboard = leaderboardCache.get("level_" + levelName);
        if (cachedLeaderboard != null) {
            return cachedLeaderboard;
        }
        
        // Get from database
        LeaderboardData leaderboard = leaderboardManager.getLevelLeaderboardData(levelName);
        
        // Cache the result
        leaderboardCache.put("level_" + levelName, leaderboard);
        
        return leaderboard;
    }
    
    /**
     * Update global leaderboard
     */
    public void updateGlobalLeaderboard(LeaderboardData leaderboardData) {
        // Clear cache
        leaderboardCache.clear();
        
        // The leaderboard data is updated through individual record additions
        // This method is kept for compatibility
        logger.info("Global leaderboard updated");
    }
    
    /**
     * Add a game record to global leaderboard
     */
    public void addGameRecord(String levelName, LeaderboardData.PlayerRecord record) {
        try {
            // Create entity
            LeaderboardRecordEntity entity = new LeaderboardRecordEntity(
                record.playerName,
                record.completionTime,
                record.xpEarned,
                record.levelNumber,
                levelName,
                record.packetLossPercentage,
                record.coinsEarned,
                null // userMacAddress would need to be provided
            );
            
            leaderboardManager.addLeaderboardRecord(entity);
            
            // Clear cache
            leaderboardCache.clear();
            
            logger.info("Added leaderboard record for level: " + levelName);
        } catch (Exception e) {
            logger.error("Failed to add leaderboard record", e);
            throw e;
        }
    }
    
    /**
     * Get all user profiles
     */
    public Collection<UserProfile> getAllUserProfiles() {
        try {
            List<UserProfileEntity> entities = userProfileManager.getAllUserProfiles();
            List<UserProfile> profiles = new ArrayList<>();
            
            for (UserProfileEntity entity : entities) {
                UserProfile profile = userProfileManager.toUserProfile(entity);
                profiles.add(profile);
                
                // Update cache
                userProfileCache.put(profile.getMacAddress(), profile);
            }
            
            return profiles;
        } catch (Exception e) {
            logger.error("Failed to get all user profiles", e);
            throw e;
        }
    }
    
    /**
     * Get user count
     */
    public int getUserCount() {
        try {
            return userProfileManager.getAllUserProfiles().size();
        } catch (Exception e) {
            logger.error("Failed to get user count", e);
            return 0;
        }
    }
    
    /**
     * Get online users
     */
    public Collection<UserProfile> getOnlineUsers() {
        try {
            List<UserProfileEntity> entities = userProfileManager.getOnlineUsers();
            List<UserProfile> profiles = new ArrayList<>();
            
            for (UserProfileEntity entity : entities) {
                UserProfile profile = userProfileManager.toUserProfile(entity);
                profiles.add(profile);
            }
            
            return profiles;
        } catch (Exception e) {
            logger.error("Failed to get online users", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Set user online status
     */
    public void setUserOnlineStatus(String macAddress, boolean isOnline) {
        try {
            userProfileManager.setUserOnlineStatus(macAddress, isOnline);
            
            // Update cache if available
            UserProfile cachedProfile = userProfileCache.get(macAddress);
            if (cachedProfile != null) {
                cachedProfile.updateLastLogin();
            }
            
            logger.info("Set user online status: " + macAddress + " = " + isOnline);
        } catch (Exception e) {
            logger.error("Failed to set user online status", e);
            throw e;
        }
    }
    
    /**
     * Set global game data
     */
    public void setGlobalGameData(String key, Object value) {
        globalGameData.put(key, value);
        logger.info("Set global game data: " + key);
    }
    
    /**
     * Get global game data
     */
    public Object getGlobalGameData(String key) {
        return globalGameData.get(key);
    }
    
    /**
     * Get player statistics
     */
    public LeaderboardData.PlayerStats getPlayerStats(String playerName) {
        try {
            return leaderboardManager.getPlayerStats(playerName);
        } catch (Exception e) {
            logger.error("Failed to get player stats", e);
            return new LeaderboardData.PlayerStats();
        }
    }
    
    /**
     * Get pending sync items for a user
     */
    public List<OfflineSyncEntity> getPendingSyncItems(String userMacAddress) {
        try {
            return offlineSyncManager.getPendingSyncItems(userMacAddress);
        } catch (Exception e) {
            logger.error("Failed to get pending sync items", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Retry failed sync items for a user
     */
    public void retryFailedSyncItems(String userMacAddress) {
        try {
            offlineSyncManager.retryFailedSyncItems(userMacAddress);
            logger.info("Retried failed sync items for user: " + userMacAddress);
        } catch (Exception e) {
            logger.error("Failed to retry failed sync items", e);
            throw e;
        }
    }
    
    /**
     * Check if user has pending sync items
     */
    public boolean hasPendingSyncItems(String userMacAddress) {
        try {
            return offlineSyncManager.hasPendingSyncItems(userMacAddress);
        } catch (Exception e) {
            logger.error("Failed to check pending sync items", e);
            return false;
        }
    }
    
    /**
     * Cleanup old data
     */
    public void cleanupOldData() {
        try {
            leaderboardManager.cleanupOldRecords();
            logger.info("Cleaned up old data");
        } catch (Exception e) {
            logger.error("Failed to cleanup old data", e);
        }
    }
    
    /**
     * Shutdown the manager
     */
    public void shutdown() {
        try {
            offlineSyncManager.stop();
            DatabaseManager.getInstance().shutdown();
            logger.info("Database server data manager shutdown");
        } catch (Exception e) {
            logger.error("Failed to shutdown database server data manager", e);
        }
    }
}
