package dev.aminhashemi.blueprinthell.client;

import dev.aminhashemi.blueprinthell.database.DatabaseManager;
import dev.aminhashemi.blueprinthell.database.UserProfileDatabaseManager;
import dev.aminhashemi.blueprinthell.database.LeaderboardDatabaseManager;
import dev.aminhashemi.blueprinthell.database.OfflineSyncManager;
import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.model.SaveData;
import dev.aminhashemi.blueprinthell.model.entities.UserProfileEntity;
import dev.aminhashemi.blueprinthell.model.entities.GameRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.LeaderboardRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.SaveDataEntity;
import dev.aminhashemi.blueprinthell.model.entities.OfflineSyncEntity;
import dev.aminhashemi.blueprinthell.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

/**
 * Client-side database manager for offline data storage and synchronization
 */
public class ClientDatabaseManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    private final SessionFactory sessionFactory;
    private final UserProfileDatabaseManager userProfileManager;
    private final LeaderboardDatabaseManager leaderboardManager;
    private final OfflineSyncManager offlineSyncManager;
    private final Logger logger;
    
    public ClientDatabaseManager() {
        this.sessionFactory = DatabaseManager.getInstance().getSessionFactory();
        this.userProfileManager = new UserProfileDatabaseManager();
        this.leaderboardManager = new LeaderboardDatabaseManager();
        this.offlineSyncManager = new OfflineSyncManager();
        this.logger = Logger.getInstance();
        
        // Initialize database
        DatabaseManager.getInstance().initialize();
        
        // Start offline sync manager
        offlineSyncManager.start();
        
        logger.info("Client database manager initialized");
    }
    
    /**
     * Save user profile locally
     */
    public void saveUserProfile(UserProfile profile) {
        try {
            UserProfileEntity entity = userProfileManager.fromUserProfile(profile);
            userProfileManager.updateUserProfile(entity);
            
            // Queue for server sync
            String profileJson = gson.toJson(profile);
            offlineSyncManager.queueForSync(profile.getMacAddress(), 
                OfflineSyncEntity.DataType.USER_PROFILE, 
                profile.getMacAddress(), profileJson);
            
            logger.info("Saved user profile locally: " + profile.getUsername());
        } catch (Exception e) {
            logger.error("Failed to save user profile locally", e);
            throw e;
        }
    }
    
    /**
     * Load user profile from local database
     */
    public UserProfile loadUserProfile(String macAddress) {
        try {
            UserProfileEntity entity = userProfileManager.getUserProfile(macAddress);
            return userProfileManager.toUserProfile(entity);
        } catch (Exception e) {
            logger.error("Failed to load user profile locally", e);
            throw e;
        }
    }
    
    /**
     * Save game record locally
     */
    public void saveGameRecord(String macAddress, UserProfile.GameRecord record) {
        try {
            GameRecordEntity entity = new GameRecordEntity(
                macAddress,
                record.getLevelName(),
                record.getCompletionTime(),
                record.getXpEarned(),
                record.getCoinsEarned(),
                record.getPacketLossPercentage()
            );
            
            userProfileManager.addGameRecord(macAddress, entity);
            
            // Queue for server sync
            String recordJson = gson.toJson(record);
            offlineSyncManager.queueForSync(macAddress, 
                OfflineSyncEntity.DataType.GAME_RECORD, 
                record.getLevelName(), recordJson);
            
            logger.info("Saved game record locally for user: " + macAddress);
        } catch (Exception e) {
            logger.error("Failed to save game record locally", e);
            throw e;
        }
    }
    
    /**
     * Save leaderboard record locally
     */
    public void saveLeaderboardRecord(LeaderboardData.PlayerRecord record, String levelName) {
        try {
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
            
            // Queue for server sync
            String recordJson = gson.toJson(record);
            offlineSyncManager.queueForSync(null, 
                OfflineSyncEntity.DataType.LEADERBOARD_RECORD, 
                levelName, recordJson);
            
            logger.info("Saved leaderboard record locally for level: " + levelName);
        } catch (Exception e) {
            logger.error("Failed to save leaderboard record locally", e);
            throw e;
        }
    }
    
    /**
     * Save game data locally
     */
    public void saveGameData(String macAddress, String levelName, SaveData saveData) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                SaveDataEntity entity = new SaveDataEntity(macAddress, levelName);
                entity.setCoins(saveData.coins);
                entity.setUsedWireLength(saveData.usedWireLength);
                entity.setTotalWireLength(saveData.totalWireLength);
                entity.setWiringMode(saveData.isWiringMode);
                entity.setImpactWavesDisabled(saveData.impactWavesDisabled);
                entity.setPacketCollisionsDisabled(saveData.packetCollisionsDisabled);
                entity.setPacketNoiseZeroed(saveData.packetNoiseZeroed);
                entity.setGameStartTime(saveData.gameStartTime);
                entity.setCurrentGameTime(saveData.currentGameTime);
                entity.setTestRunning(saveData.isTestRunning);
                entity.setTestPacketsReleased(saveData.testPacketsReleased);
                entity.setTestPacketsReturned(saveData.testPacketsReturned);
                entity.setTestStartTime(saveData.testStartTime);
                entity.setLastPacketReleaseTime(saveData.lastPacketReleaseTime);
                entity.setTestCompleted(saveData.testCompleted);
                entity.setGameWon(saveData.gameWon);
                entity.setGameLost(saveData.gameLost);
                entity.setTimeTravelMode(saveData.isTimeTravelMode);
                entity.setPaused(saveData.isPaused);
                entity.setCurrentSnapshotIndex(saveData.currentSnapshotIndex);
                entity.setTimeTravelWindowSeconds(saveData.timeTravelWindowSeconds);
                entity.setWireLengths(saveData.wireLengths);
                
                // Store the complete save data as JSON for complex objects
                String saveDataJson = gson.toJson(saveData);
                entity.setSaveDataJson(saveDataJson);
                
                session.merge(entity);
                tx.commit();
                
                // Queue for server sync
                offlineSyncManager.queueForSync(macAddress, 
                    OfflineSyncEntity.DataType.SAVE_DATA, 
                    levelName, saveDataJson);
                
                logger.info("Saved game data locally for user: " + macAddress);
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to save game data locally", e);
                throw e;
            }
        }
    }
    
    /**
     * Load game data from local database
     */
    public SaveData loadGameData(String macAddress, String levelName) {
        try (Session session = sessionFactory.openSession()) {
            Query<SaveDataEntity> query = session.createQuery(
                "FROM SaveDataEntity WHERE userMacAddress = :macAddress AND levelName = :levelName ORDER BY saveTime DESC", 
                SaveDataEntity.class
            );
            query.setParameter("macAddress", macAddress);
            query.setParameter("levelName", levelName);
            query.setMaxResults(1);
            
            SaveDataEntity entity = query.uniqueResult();
            if (entity == null) {
                return null;
            }
            
            // If we have JSON data, use it
            if (entity.getSaveDataJson() != null && !entity.getSaveDataJson().isEmpty()) {
                return gson.fromJson(entity.getSaveDataJson(), SaveData.class);
            }
            
            // Otherwise, reconstruct from individual fields
            SaveData saveData = new SaveData();
            saveData.coins = entity.getCoins();
            saveData.usedWireLength = entity.getUsedWireLength();
            saveData.totalWireLength = entity.getTotalWireLength();
            saveData.isWiringMode = entity.isWiringMode();
            saveData.impactWavesDisabled = entity.isImpactWavesDisabled();
            saveData.packetCollisionsDisabled = entity.isPacketCollisionsDisabled();
            saveData.packetNoiseZeroed = entity.isPacketNoiseZeroed();
            saveData.gameStartTime = entity.getGameStartTime();
            saveData.currentGameTime = entity.getCurrentGameTime();
            saveData.isTestRunning = entity.isTestRunning();
            saveData.testPacketsReleased = entity.getTestPacketsReleased();
            saveData.testPacketsReturned = entity.getTestPacketsReturned();
            saveData.testStartTime = entity.getTestStartTime();
            saveData.lastPacketReleaseTime = entity.getLastPacketReleaseTime();
            saveData.testCompleted = entity.isTestCompleted();
            saveData.gameWon = entity.isGameWon();
            saveData.gameLost = entity.isGameLost();
            saveData.isTimeTravelMode = entity.isTimeTravelMode();
            saveData.isPaused = entity.isPaused();
            saveData.currentSnapshotIndex = entity.getCurrentSnapshotIndex();
            saveData.timeTravelWindowSeconds = entity.getTimeTravelWindowSeconds();
            saveData.wireLengths = entity.getWireLengths();
            
            return saveData;
        } catch (Exception e) {
            logger.error("Failed to load game data locally", e);
            throw e;
        }
    }
    
    /**
     * Get local leaderboard data
     */
    public LeaderboardData getLocalLeaderboard(String levelName) {
        try {
            return leaderboardManager.getLevelLeaderboardData(levelName);
        } catch (Exception e) {
            logger.error("Failed to get local leaderboard data", e);
            return new LeaderboardData();
        }
    }
    
    /**
     * Get local global leaderboard data
     */
    public LeaderboardData getLocalGlobalLeaderboard() {
        try {
            return leaderboardManager.getGlobalLeaderboardData();
        } catch (Exception e) {
            logger.error("Failed to get local global leaderboard data", e);
            return new LeaderboardData();
        }
    }
    
    /**
     * Get pending sync items
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
     * Get failed sync items
     */
    public List<OfflineSyncEntity> getFailedSyncItems(String userMacAddress) {
        try {
            return offlineSyncManager.getFailedSyncItems(userMacAddress);
        } catch (Exception e) {
            logger.error("Failed to get failed sync items", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Retry failed sync items
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
     * Check if there are pending sync items
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
     * Force sync all pending items
     */
    public void forceSyncAll(String userMacAddress) {
        try {
            List<OfflineSyncEntity> pendingItems = getPendingSyncItems(userMacAddress);
            for (OfflineSyncEntity item : pendingItems) {
                // This would typically involve sending data to server
                // For now, we'll just mark it as synced
                item.markAsSuccess();
            }
            logger.info("Force synced " + pendingItems.size() + " items for user: " + userMacAddress);
        } catch (Exception e) {
            logger.error("Failed to force sync all items", e);
            throw e;
        }
    }
    
    /**
     * Clear all local data (for testing or reset)
     */
    public void clearAllLocalData() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Clear all tables
                session.createMutationQuery("DELETE FROM OfflineSyncEntity").executeUpdate();
                session.createMutationQuery("DELETE FROM SaveDataEntity").executeUpdate();
                session.createMutationQuery("DELETE FROM GameRecordEntity").executeUpdate();
                session.createMutationQuery("DELETE FROM LeaderboardRecordEntity").executeUpdate();
                session.createMutationQuery("DELETE FROM PlayerStatsEntity").executeUpdate();
                session.createMutationQuery("DELETE FROM UserProfileEntity").executeUpdate();
                
                tx.commit();
                logger.info("Cleared all local data");
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to clear all local data", e);
                throw e;
            }
        }
    }
    
    /**
     * Shutdown the client database manager
     */
    public void shutdown() {
        try {
            offlineSyncManager.stop();
            DatabaseManager.getInstance().shutdown();
            logger.info("Client database manager shutdown");
        } catch (Exception e) {
            logger.error("Failed to shutdown client database manager", e);
        }
    }
}
