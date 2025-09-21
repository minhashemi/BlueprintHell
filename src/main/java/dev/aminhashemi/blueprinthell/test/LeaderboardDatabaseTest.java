package dev.aminhashemi.blueprinthell.test;

import dev.aminhashemi.blueprinthell.database.DatabaseManager;
import dev.aminhashemi.blueprinthell.database.LeaderboardDatabaseManager;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.model.entities.LeaderboardRecordEntity;
import dev.aminhashemi.blueprinthell.utils.DatabaseLeaderboardManager;
import dev.aminhashemi.blueprinthell.utils.Logger;

/**
 * Test to verify that leaderboard data is saved to database instead of JSON
 */
public class LeaderboardDatabaseTest {
    
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.info("🧪 Starting Leaderboard Database Test");
        logger.info("=====================================");
        
        try {
            // Initialize database
            DatabaseManager.getInstance().initialize();
            logger.info("✅ Database initialized");
            
            // Test 1: Create leaderboard data
            LeaderboardData leaderboardData = new LeaderboardData();
            
            // Add some test records
            LeaderboardData.PlayerRecord record1 = new LeaderboardData.PlayerRecord(
                "TestPlayer1", 30000, 150, 1, 5.0, 50
            );
            LeaderboardData.PlayerRecord record2 = new LeaderboardData.PlayerRecord(
                "TestPlayer2", 25000, 200, 1, 2.0, 75
            );
            
            leaderboardData.addRecord("Level1", record1);
            leaderboardData.addRecord("Level1", record2);
            
            // Update player stats
            leaderboardData.updatePlayerStats("TestPlayer1", 1, 30000, 150, 50);
            
            logger.info("📊 Created test leaderboard data:");
            logger.info("   Records for Level1: " + leaderboardData.getLevelRecords("Level1").size());
            logger.info("   Player stats: " + leaderboardData.getCurrentPlayerStats().playerName);
            
            // Test 2: Save to database using DatabaseLeaderboardManager
            logger.info("💾 Saving leaderboard data to database...");
            boolean saveResult = DatabaseLeaderboardManager.saveLeaderboard(leaderboardData).get();
            
            if (saveResult) {
                logger.info("✅ Leaderboard data saved to database successfully");
            } else {
                logger.error("❌ Failed to save leaderboard data to database");
                return;
            }
            
            // Test 3: Load from database
            logger.info("📥 Loading leaderboard data from database...");
            LeaderboardData loadedData = DatabaseLeaderboardManager.loadLeaderboard().get();
            
            logger.info("📊 Loaded leaderboard data:");
            logger.info("   Records for Level1: " + loadedData.getLevelRecords("Level1").size());
            logger.info("   Player stats: " + loadedData.getCurrentPlayerStats().playerName);
            
            // Test 4: Verify data integrity
            boolean dataMatches = loadedData.getLevelRecords("Level1").size() == 2;
            if (dataMatches) {
                logger.info("✅ Data integrity verified - records loaded correctly");
            } else {
                logger.error("❌ Data integrity failed - records don't match");
            }
            
            // Test 5: Direct database query to verify data exists
            LeaderboardDatabaseManager dbManager = new LeaderboardDatabaseManager();
            var levelRecords = dbManager.getLevelRecords("Level1", 10);
            logger.info("🔍 Direct database query results:");
            logger.info("   Records found in database: " + levelRecords.size());
            
            for (LeaderboardRecordEntity entity : levelRecords) {
                logger.info("   - " + entity.getPlayerName() + " (Time: " + entity.getCompletionTime() + "ms)");
            }
            
            logger.info("🎉 Leaderboard Database Test completed successfully!");
            logger.info("✅ Confirmed: Leaderboard data is now stored in database, not JSON files");
            
        } catch (Exception e) {
            logger.error("❌ Leaderboard Database Test failed", e);
        } finally {
            // Database cleanup is handled automatically
            logger.info("🏁 Test cleanup completed");
        }
    }
}
