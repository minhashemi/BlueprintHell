package dev.aminhashemi.blueprinthell.test;

import dev.aminhashemi.blueprinthell.model.TestPacketType;
import dev.aminhashemi.blueprinthell.model.TestConfiguration;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.utils.Logger;

/**
 * Test class to verify the new test configuration functionality
 */
public class TestConfigurationTest {
    
    public static void main(String[] args) {
        Logger logger = Logger.getInstance();
        logger.info("Starting Test Configuration Test...");
        
        try {
            // Test 1: Default configuration with all packet types
            logger.info("=== Test 1: Default Configuration ===");
            TestConfiguration defaultConfig = new TestConfiguration();
            logger.info("Enabled packet types: " + defaultConfig.getEnabledTypeCount());
            
            // Test random packet selection
            for (int i = 0; i < 10; i++) {
                TestPacketType selected = defaultConfig.getRandomPacketType();
                logger.info("Random packet " + (i+1) + ": " + selected.getDisplayName() + 
                           " (probability: " + selected.getSpawnProbability() + ")");
            }
            
            // Test 2: Custom configuration with only specific types
            logger.info("\n=== Test 2: Custom Configuration (Messenger + Bulk only) ===");
            TestConfiguration customConfig = new TestConfiguration(
                TestPacketType.MESSENGER, 
                TestPacketType.BULK
            );
            logger.info("Enabled packet types: " + customConfig.getEnabledTypeCount());
            
            for (int i = 0; i < 10; i++) {
                TestPacketType selected = customConfig.getRandomPacketType();
                logger.info("Random packet " + (i+1) + ": " + selected.getDisplayName());
            }
            
            // Test 3: Equal probability configuration
            logger.info("\n=== Test 3: Equal Probability Configuration ===");
            TestConfiguration equalConfig = new TestConfiguration(
                true, // use equal probability
                TestPacketType.MESSENGER, 
                TestPacketType.BULK,
                TestPacketType.SPY
            );
            logger.info("Enabled packet types: " + equalConfig.getEnabledTypeCount());
            logger.info("Uses equal probability: " + equalConfig.isUseEqualProbability());
            
            for (int i = 0; i < 10; i++) {
                TestPacketType selected = equalConfig.getRandomPacketType();
                logger.info("Random packet " + (i+1) + ": " + selected.getDisplayName());
            }
            
            // Test 4: Packet type conversion
            logger.info("\n=== Test 4: Packet Type Conversion ===");
            for (TestPacketType testType : TestPacketType.values()) {
                logger.info(testType.getDisplayName() + " -> " + testType.toPacketType());
            }
            
            logger.info("\n✅ Test Configuration Test completed successfully!");
            
        } catch (Exception e) {
            logger.error("❌ Test Configuration Test failed", e);
        }
    }
}
