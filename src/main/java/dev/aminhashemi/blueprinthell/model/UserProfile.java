package dev.aminhashemi.blueprinthell.model;

import com.google.gson.annotations.Expose;
import java.util.ArrayList;
import java.util.List;

/**
 * User profile data stored on server
 */
public class UserProfile {
    @Expose
    private String macAddress;
    
    @Expose
    private String username;
    
    @Expose
    private int totalXP;
    
    @Expose
    private int totalCoins;
    
    @Expose
    private List<String> unlockedFeatures;
    
    @Expose
    private List<String> activeAbilities;
    
    @Expose
    private List<GameRecord> gameHistory;
    
    @Expose
    private long lastLoginTime;
    
    @Expose
    private long accountCreatedTime;
    
    public UserProfile() {
        this.unlockedFeatures = new ArrayList<>();
        this.activeAbilities = new ArrayList<>();
        this.gameHistory = new ArrayList<>();
        this.totalXP = 0;
        this.totalCoins = 0;
        this.lastLoginTime = System.currentTimeMillis();
        this.accountCreatedTime = System.currentTimeMillis();
    }
    
    public UserProfile(String macAddress, String username) {
        this();
        this.macAddress = macAddress;
        this.username = username;
    }
    
    // Getters and setters
    public String getMacAddress() { return macAddress; }
    public void setMacAddress(String macAddress) { this.macAddress = macAddress; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
    
    public int getTotalCoins() { return totalCoins; }
    public void setTotalCoins(int totalCoins) { this.totalCoins = totalCoins; }
    
    public List<String> getUnlockedFeatures() { return unlockedFeatures; }
    public void setUnlockedFeatures(List<String> unlockedFeatures) { this.unlockedFeatures = unlockedFeatures; }
    
    public List<String> getActiveAbilities() { return activeAbilities; }
    public void setActiveAbilities(List<String> activeAbilities) { this.activeAbilities = activeAbilities; }
    
    public List<GameRecord> getGameHistory() { return gameHistory; }
    public void setGameHistory(List<GameRecord> gameHistory) { this.gameHistory = gameHistory; }
    
    public long getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(long lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    
    public long getAccountCreatedTime() { return accountCreatedTime; }
    public void setAccountCreatedTime(long accountCreatedTime) { this.accountCreatedTime = accountCreatedTime; }
    
    /**
     * Add XP to user profile
     */
    public void addXP(int xp) {
        this.totalXP += xp;
    }
    
    /**
     * Add coins to user profile
     */
    public void addCoins(int coins) {
        this.totalCoins += coins;
    }
    
    /**
     * Add a game record to history
     */
    public void addGameRecord(GameRecord record) {
        this.gameHistory.add(record);
        // Keep only last 100 records
        if (this.gameHistory.size() > 100) {
            this.gameHistory = this.gameHistory.subList(this.gameHistory.size() - 100, this.gameHistory.size());
        }
    }
    
    /**
     * Unlock a feature
     */
    public void unlockFeature(String feature) {
        if (!unlockedFeatures.contains(feature)) {
            unlockedFeatures.add(feature);
        }
    }
    
    /**
     * Activate an ability
     */
    public void activateAbility(String ability) {
        if (!activeAbilities.contains(ability)) {
            activeAbilities.add(ability);
        }
    }
    
    /**
     * Deactivate an ability
     */
    public void deactivateAbility(String ability) {
        activeAbilities.remove(ability);
    }
    
    /**
     * Update last login time
     */
    public void updateLastLogin() {
        this.lastLoginTime = System.currentTimeMillis();
    }
    
    /**
     * Game record inner class
     */
    public static class GameRecord {
        @Expose
        private String levelName;
        
        @Expose
        private long completionTime;
        
        @Expose
        private int xpEarned;
        
        @Expose
        private int coinsEarned;
        
        @Expose
        private double packetLossPercentage;
        
        @Expose
        private long timestamp;
        
        public GameRecord() {}
        
        public GameRecord(String levelName, long completionTime, int xpEarned, int coinsEarned, double packetLossPercentage) {
            this.levelName = levelName;
            this.completionTime = completionTime;
            this.xpEarned = xpEarned;
            this.coinsEarned = coinsEarned;
            this.packetLossPercentage = packetLossPercentage;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getLevelName() { return levelName; }
        public void setLevelName(String levelName) { this.levelName = levelName; }
        
        public long getCompletionTime() { return completionTime; }
        public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
        
        public int getXpEarned() { return xpEarned; }
        public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
        
        public int getCoinsEarned() { return coinsEarned; }
        public void setCoinsEarned(int coinsEarned) { this.coinsEarned = coinsEarned; }
        
        public double getPacketLossPercentage() { return packetLossPercentage; }
        public void setPacketLossPercentage(double packetLossPercentage) { this.packetLossPercentage = packetLossPercentage; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}
