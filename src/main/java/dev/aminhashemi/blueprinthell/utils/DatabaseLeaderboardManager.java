package dev.aminhashemi.blueprinthell.utils;

import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.database.LeaderboardDatabaseManager;
import dev.aminhashemi.blueprinthell.database.UserProfileDatabaseManager;
import dev.aminhashemi.blueprinthell.model.entities.LeaderboardRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.PlayerStatsEntity;
import dev.aminhashemi.blueprinthell.network.ClientNetworkManager;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Database-based leaderboard manager that replaces JSON file storage
 */
public class DatabaseLeaderboardManager {
    private static final LeaderboardDatabaseManager leaderboardManager = new LeaderboardDatabaseManager();
    private static final UserProfileDatabaseManager userProfileManager = new UserProfileDatabaseManager();
    private static ClientNetworkManager networkManager;
    private static boolean serverMode = false;
    
    /**
     * Initialize with server connection
     */
    public static void initializeWithServer(ClientNetworkManager networkManager) {
        DatabaseLeaderboardManager.networkManager = networkManager;
        serverMode = true;
    }
    
    /**
     * Saves leaderboard data to database
     */
    public static CompletableFuture<Boolean> saveLeaderboard(LeaderboardData leaderboardData) {
        if (serverMode && networkManager != null && networkManager.isConnected()) {
            // Server mode - data is managed by server
            return CompletableFuture.completedFuture(true);
        } else {
            // Offline mode - save to database
            return CompletableFuture.supplyAsync(() -> {
                try {
                    // Save all records for each level
                    for (String levelName : leaderboardData.getLevelNames()) {
                        List<LeaderboardData.PlayerRecord> records = leaderboardData.getLevelRecords(levelName);
                        for (LeaderboardData.PlayerRecord record : records) {
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
                        }
                    }
                    
                    // Save current player stats
                    LeaderboardData.PlayerStats stats = leaderboardData.getCurrentPlayerStats();
                    PlayerStatsEntity entity = new PlayerStatsEntity();
                    entity.setPlayerName(stats.playerName);
                    entity.setTotalXP(stats.totalXP);
                    entity.setTotalCoins(stats.totalCoins);
                    entity.setLevelsCompleted(stats.levelsCompleted);
                    entity.setBestTime(stats.bestTime);
                    entity.setBestXP(stats.bestXP);
                    entity.setLevelBestTimes(stats.levelBestTimes);
                    entity.setLevelBestXP(stats.levelBestXP);
                    leaderboardManager.addPlayerStats(entity);
                    
                    Logger.getInstance().info("Leaderboard data saved to database");
                    return true;
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to save leaderboard data to database", e);
                    return false;
                }
            });
        }
    }
    
    /**
     * Loads leaderboard data from database
     */
    public static CompletableFuture<LeaderboardData> loadLeaderboard() {
        if (serverMode && networkManager != null && networkManager.isConnected()) {
            return networkManager.getLeaderboard();
        } else {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    LeaderboardData data = new LeaderboardData();
                    
                    // Load all leaderboard records by level
                    List<String> levelNames = leaderboardManager.getLevelNames();
                    for (String levelName : levelNames) {
                        List<LeaderboardRecordEntity> records = leaderboardManager.getLevelRecords(levelName, 10);
                        for (LeaderboardRecordEntity entity : records) {
                            LeaderboardData.PlayerRecord record = new LeaderboardData.PlayerRecord(
                                entity.getPlayerName(),
                                entity.getCompletionTime(),
                                entity.getXpEarned(),
                                entity.getLevelNumber(),
                                entity.getPacketLossPercentage(),
                                entity.getCoinsEarned()
                            );
                            data.addRecord(entity.getLevelName(), record);
                        }
                    }
                    
                    // Load current player stats (we'll use the first one found or create default)
                    // Note: This is a simplified approach - in a real app you'd track the current player
                    LeaderboardData.PlayerStats currentStats = data.getCurrentPlayerStats();
                    // Keep the default stats for now
                    
                    Logger.getInstance().info("Leaderboard data loaded from database");
                    return data;
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to load leaderboard data from database", e);
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
                    Logger.getInstance().info("Leaderboard record saved to database for level: " + levelName);
                    return true;
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to save leaderboard record to database", e);
                    return false;
                }
            });
        }
    }
    
    /**
     * Updates player statistics
     */
    public static CompletableFuture<Boolean> updatePlayerStats(String playerName, LeaderboardData.PlayerStats stats) {
        // For now, always save to database since ClientNetworkManager doesn't have updatePlayerStats
        return CompletableFuture.supplyAsync(() -> {
            try {
                PlayerStatsEntity entity = new PlayerStatsEntity();
                entity.setPlayerName(stats.playerName);
                entity.setTotalXP(stats.totalXP);
                entity.setTotalCoins(stats.totalCoins);
                entity.setLevelsCompleted(stats.levelsCompleted);
                entity.setBestTime(stats.bestTime);
                entity.setBestXP(stats.bestXP);
                entity.setLevelBestTimes(stats.levelBestTimes);
                entity.setLevelBestXP(stats.levelBestXP);
                
                leaderboardManager.addPlayerStats(entity);
                Logger.getInstance().info("Player stats updated in database for: " + playerName);
                return true;
            } catch (Exception e) {
                Logger.getInstance().error("Failed to update player stats in database", e);
                return false;
            }
        });
    }
    
    /**
     * Gets leaderboard for a specific level
     */
    public static CompletableFuture<List<LeaderboardData.PlayerRecord>> getLevelLeaderboard(String levelName) {
        // For now, always get from database since ClientNetworkManager doesn't have getLevelLeaderboard
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<LeaderboardRecordEntity> entities = leaderboardManager.getLevelRecords(levelName, 10);
                return entities.stream()
                        .map(entity -> new LeaderboardData.PlayerRecord(
                            entity.getPlayerName(),
                            entity.getCompletionTime(),
                            entity.getXpEarned(),
                            entity.getLevelNumber(),
                            entity.getPacketLossPercentage(),
                            entity.getCoinsEarned()
                        ))
                        .collect(Collectors.toList());
            } catch (Exception e) {
                Logger.getInstance().error("Failed to get level leaderboard from database", e);
                return List.of();
            }
        });
    }
}
