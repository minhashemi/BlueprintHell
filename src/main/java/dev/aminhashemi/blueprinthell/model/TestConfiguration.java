package dev.aminhashemi.blueprinthell.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration class for test packet spawning
 * Allows customization of which packet types to spawn during testing
 */
public class TestConfiguration {
    private List<TestPacketType> enabledPacketTypes;
    private boolean useEqualProbability;
    
    public TestConfiguration() {
        // Default: enable all packet types with their defined probabilities
        this.enabledPacketTypes = new ArrayList<>();
        for (TestPacketType type : TestPacketType.values()) {
            enabledPacketTypes.add(type);
        }
        this.useEqualProbability = false;
    }
    
    /**
     * Creates a configuration with only specific packet types enabled
     */
    public TestConfiguration(TestPacketType... types) {
        this.enabledPacketTypes = new ArrayList<>();
        for (TestPacketType type : types) {
            enabledPacketTypes.add(type);
        }
        this.useEqualProbability = false;
    }
    
    /**
     * Creates a configuration with equal probability for all enabled types
     */
    public TestConfiguration(boolean useEqualProbability, TestPacketType... types) {
        this.enabledPacketTypes = new ArrayList<>();
        for (TestPacketType type : types) {
            enabledPacketTypes.add(type);
        }
        this.useEqualProbability = useEqualProbability;
    }
    
    public List<TestPacketType> getEnabledPacketTypes() {
        return new ArrayList<>(enabledPacketTypes);
    }
    
    public boolean isUseEqualProbability() {
        return useEqualProbability;
    }
    
    public void setEnabledPacketTypes(List<TestPacketType> types) {
        this.enabledPacketTypes = new ArrayList<>(types);
    }
    
    public void setUseEqualProbability(boolean useEqualProbability) {
        this.useEqualProbability = useEqualProbability;
    }
    
    /**
     * Selects a random packet type from the enabled types
     */
    public TestPacketType getRandomPacketType() {
        if (enabledPacketTypes.isEmpty()) {
            return TestPacketType.MESSENGER; // Fallback
        }
        
        if (useEqualProbability) {
            // Equal probability for all enabled types
            int randomIndex = (int) (Math.random() * enabledPacketTypes.size());
            return enabledPacketTypes.get(randomIndex);
        } else {
            // Use the defined probabilities, but only for enabled types
            double random = Math.random();
            double cumulativeProbability = 0.0;
            double totalProbability = 0.0;
            
            // Calculate total probability of enabled types
            for (TestPacketType type : enabledPacketTypes) {
                totalProbability += type.getSpawnProbability();
            }
            
            // Select based on normalized probabilities
            for (TestPacketType type : enabledPacketTypes) {
                double normalizedProbability = type.getSpawnProbability() / totalProbability;
                cumulativeProbability += normalizedProbability;
                if (random <= cumulativeProbability) {
                    return type;
                }
            }
            
            // Fallback to first enabled type
            return enabledPacketTypes.get(0);
        }
    }
    
    /**
     * Checks if a specific packet type is enabled
     */
    public boolean isPacketTypeEnabled(TestPacketType type) {
        return enabledPacketTypes.contains(type);
    }
    
    /**
     * Gets the number of enabled packet types
     */
    public int getEnabledTypeCount() {
        return enabledPacketTypes.size();
    }
}
