package dev.aminhashemi.blueprinthell.test;

import dev.aminhashemi.blueprinthell.database.DatabaseManager;
import dev.aminhashemi.blueprinthell.database.UserProfileDatabaseManager;
import dev.aminhashemi.blueprinthell.model.entities.UserProfileEntity;

/**
 * Simple database test to verify basic functionality
 */
public class SimpleDatabaseTest {
    public static void main(String[] args) {
        System.out.println("Starting simple database test...");
        
        try {
            // Test database connection
            DatabaseManager dbManager = DatabaseManager.getInstance();
            dbManager.initialize();
            
            System.out.println("✓ Database initialized successfully");
            
            // Test user profile operations
            UserProfileDatabaseManager userManager = new UserProfileDatabaseManager();
            String testMacAddress = "AA:BB:CC:DD:EE:FF";
            
            UserProfileEntity profile = userManager.getUserProfile(testMacAddress);
            System.out.println("✓ User profile created: " + profile.getUsername());
            
            profile.setUsername("TestPlayer");
            profile.addXP(100);
            userManager.updateUserProfile(profile);
            
            UserProfileEntity updatedProfile = userManager.getUserProfile(testMacAddress);
            if (updatedProfile.getTotalXP() == 100) {
                System.out.println("✓ User profile update successful");
            } else {
                System.out.println("✗ User profile update failed");
            }
            
            System.out.println("✓ Simple database test completed successfully!");
            
        } catch (Exception e) {
            System.err.println("✗ Database test failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup
            DatabaseManager.getInstance().shutdown();
        }
    }
}
