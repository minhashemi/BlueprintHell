package dev.aminhashemi.blueprinthell.test;

import dev.aminhashemi.blueprinthell.database.DatabaseManager;
import dev.aminhashemi.blueprinthell.database.UserProfileDatabaseManager;
import dev.aminhashemi.blueprinthell.database.LeaderboardDatabaseManager;
import dev.aminhashemi.blueprinthell.database.OfflineSyncManager;
import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.model.entities.UserProfileEntity;
import dev.aminhashemi.blueprinthell.model.entities.GameRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.LeaderboardRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.OfflineSyncEntity;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Test class for database integration
 */
public class DatabaseIntegrationTest {
    private static final Logger logger = Logger.getInstance();
    
    public static void main(String[] args) {
        try {
            logger.info("Starting database integration test...");
            
            // Test database connection
            testDatabaseConnection();
            
            // Test user profile operations
            testUserProfileOperations();
            
            // Test game record operations
            testGameRecordOperations();
            
            // Test leaderboard operations
            testLeaderboardOperations();
            
            // Test offline sync operations
            testOfflineSyncOperations();
            
            logger.info("Database integration test completed successfully!");
            
        } catch (Exception e) {
            logger.error("Database integration test failed", e);
        } finally {
            // Cleanup
            DatabaseManager.getInstance().shutdown();
        }
    }
    
    private static void testDatabaseConnection() {
        logger.info("Testing database connection...");
        
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.initialize();
        
        boolean isConnected = dbManager.testConnection();
        if (isConnected) {
            logger.info("✓ Database connection successful");
        } else {
            throw new RuntimeException("Database connection failed");
        }
    }
    
    private static void testUserProfileOperations() {
        logger.info("Testing user profile operations...");
        
        UserProfileDatabaseManager userManager = new UserProfileDatabaseManager();
        
        // Test creating a new user profile
        String testMacAddress = "AA:BB:CC:DD:EE:FF";
        UserProfileEntity profile = userManager.getUserProfile(testMacAddress);
        
        if (profile != null && profile.getMacAddress().equals(testMacAddress)) {
            logger.info("✓ User profile creation successful");
        } else {
            throw new RuntimeException("User profile creation failed");
        }
        
        // Test updating user profile
        profile.setUsername("TestPlayer");
        profile.addXP(100);
        profile.addCoins(50);
        profile.unlockFeature("test_feature");
        
        userManager.updateUserProfile(profile);
        
        // Verify update
        UserProfileEntity updatedProfile = userManager.getUserProfile(testMacAddress);
        if (updatedProfile.getTotalXP() == 100 && updatedProfile.getTotalCoins() == 50) {
            logger.info("✓ User profile update successful");
        } else {
            throw new RuntimeException("User profile update failed");
        }
    }
    
    private static void testGameRecordOperations() {
        logger.info("Testing game record operations...");
        
        UserProfileDatabaseManager userManager = new UserProfileDatabaseManager();
        String testMacAddress = "AA:BB:CC:DD:EE:FF";
        
        // Test adding a game record
        GameRecordEntity record = new GameRecordEntity(
            testMacAddress,
            "Test Level",
            30000, // 30 seconds
            150,
            25,
            0.05 // 5% packet loss
        );
        
        userManager.addGameRecord(testMacAddress, record);
        
        // Verify record was added
        List<GameRecordEntity> history = userManager.getUserGameHistory(testMacAddress, 10);
        if (!history.isEmpty() && history.get(0).getLevelName().equals("Test Level")) {
            logger.info("✓ Game record operations successful");
        } else {
            throw new RuntimeException("Game record operations failed");
        }
    }
    
    private static void testLeaderboardOperations() {
        logger.info("Testing leaderboard operations...");
        
        LeaderboardDatabaseManager leaderboardManager = new LeaderboardDatabaseManager();
        
        // Test adding leaderboard records
        LeaderboardRecordEntity record1 = new LeaderboardRecordEntity(
            "Player1",
            25000, // 25 seconds
            200,
            1,
            "Test Level",
            0.02, // 2% packet loss
            30,
            "AA:BB:CC:DD:EE:01"
        );
        
        LeaderboardRecordEntity record2 = new LeaderboardRecordEntity(
            "Player2",
            30000, // 30 seconds
            180,
            1,
            "Test Level",
            0.03, // 3% packet loss
            25,
            "AA:BB:CC:DD:EE:02"
        );
        
        leaderboardManager.addLeaderboardRecord(record1);
        leaderboardManager.addLeaderboardRecord(record2);
        
        // Test getting level records
        List<LeaderboardRecordEntity> levelRecords = leaderboardManager.getLevelRecords("Test Level", 10);
        if (levelRecords.size() == 2) {
            logger.info("✓ Leaderboard record addition successful");
        } else {
            throw new RuntimeException("Leaderboard record addition failed");
        }
        
        // Test getting global records
        List<LeaderboardRecordEntity> globalRecords = leaderboardManager.getGlobalRecords(10);
        if (globalRecords.size() == 2) {
            logger.info("✓ Global leaderboard operations successful");
        } else {
            throw new RuntimeException("Global leaderboard operations failed");
        }
        
        // Test getting leaderboard data
        LeaderboardData leaderboardData = leaderboardManager.getLevelLeaderboardData("Test Level");
        if (leaderboardData.getLevelRecords("Test Level").size() == 2) {
            logger.info("✓ Leaderboard data retrieval successful");
        } else {
            throw new RuntimeException("Leaderboard data retrieval failed");
        }
    }
    
    private static void testOfflineSyncOperations() {
        logger.info("Testing offline sync operations...");
        
        OfflineSyncManager syncManager = new OfflineSyncManager();
        String testMacAddress = "AA:BB:CC:DD:EE:FF";
        
        // Test queueing data for sync
        String testData = "{\"test\": \"data\"}";
        syncManager.queueForSync(testMacAddress, OfflineSyncEntity.DataType.USER_PROFILE, "test_id", testData);
        
        // Test getting pending sync items
        List<OfflineSyncEntity> pendingItems = syncManager.getPendingSyncItems(testMacAddress);
        if (!pendingItems.isEmpty() && pendingItems.get(0).getDataJson().equals(testData)) {
            logger.info("✓ Offline sync operations successful");
        } else {
            throw new RuntimeException("Offline sync operations failed");
        }
        
        // Test checking for pending items
        boolean hasPending = syncManager.hasPendingSyncItems(testMacAddress);
        if (hasPending) {
            logger.info("✓ Pending sync items check successful");
        } else {
            throw new RuntimeException("Pending sync items check failed");
        }
    }
}
