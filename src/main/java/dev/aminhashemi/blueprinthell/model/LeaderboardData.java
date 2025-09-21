package dev.aminhashemi.blueprinthell.model;

import java.util.*;

/**
 * Represents leaderboard data for tracking player records and statistics
 */
public class LeaderboardData {
    
    // Player records for each level
    public Map<String, List<PlayerRecord>> levelRecords;
    
    // Global records across all levels
    public List<PlayerRecord> globalRecords;
    
    // Current player's statistics
    public PlayerStats currentPlayerStats;
    
    public LeaderboardData() {
        this.levelRecords = new HashMap<>();
        this.globalRecords = new ArrayList<>();
        this.currentPlayerStats = new PlayerStats();
    }
    
    /**
     * Represents a single player record
     */
    public static class PlayerRecord implements Comparable<PlayerRecord> {
        public String playerName;
        public long completionTime; // in milliseconds
        public int xpEarned;
        public int levelNumber;
        public long timestamp; // when the record was set
        public double packetLossPercentage;
        public int coinsEarned;
        
        public PlayerRecord(String playerName, long completionTime, int xpEarned, int levelNumber, 
                          double packetLossPercentage, int coinsEarned) {
            this.playerName = playerName;
            this.completionTime = completionTime;
            this.xpEarned = xpEarned;
            this.levelNumber = levelNumber;
            this.packetLossPercentage = packetLossPercentage;
            this.coinsEarned = coinsEarned;
            this.timestamp = System.currentTimeMillis();
        }
        
        @Override
        public int compareTo(PlayerRecord other) {
            // Sort by completion time (ascending - fastest first)
            return Long.compare(this.completionTime, other.completionTime);
        }
        
        public String getFormattedTime() {
            long seconds = completionTime / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
    }
    
    /**
     * Represents current player's statistics
     */
    public static class PlayerStats {
        public String playerName;
        public int totalXP;
        public int totalCoins;
        public int levelsCompleted;
        public long bestTime; // best completion time across all levels
        public int bestXP; // highest XP earned in a single game
        public Map<Integer, Long> levelBestTimes; // best time for each level
        public Map<Integer, Integer> levelBestXP; // best XP for each level
        
        public PlayerStats() {
            this.playerName = "Player";
            this.totalXP = 0;
            this.totalCoins = 0;
            this.levelsCompleted = 0;
            this.bestTime = Long.MAX_VALUE;
            this.bestXP = 0;
            this.levelBestTimes = new HashMap<>();
            this.levelBestXP = new HashMap<>();
        }
    }
    
    /**
     * Add a new record for a specific level
     */
    public void addRecord(String levelName, PlayerRecord record) {
        levelRecords.computeIfAbsent(levelName, k -> new ArrayList<>()).add(record);
        
        // Sort records by completion time (fastest first)
        levelRecords.get(levelName).sort(PlayerRecord::compareTo);
        
        // Keep only top 10 records per level
        if (levelRecords.get(levelName).size() > 10) {
            List<PlayerRecord> topRecords = levelRecords.get(levelName).subList(0, 10);
            levelRecords.put(levelName, new ArrayList<>(topRecords));
        }
        
        // Add to global records
        globalRecords.add(record);
        globalRecords.sort(PlayerRecord::compareTo);
        
        // Keep only top 50 global records
        if (globalRecords.size() > 50) {
            globalRecords = globalRecords.subList(0, 50);
        }
    }
    
    /**
     * Add a record to level only (for loading from database)
     */
    public void addLevelRecord(String levelName, PlayerRecord record) {
        levelRecords.computeIfAbsent(levelName, k -> new ArrayList<>()).add(record);
        
        // Sort records by completion time (fastest first)
        levelRecords.get(levelName).sort(PlayerRecord::compareTo);
        
        // Keep only top 10 records per level
        if (levelRecords.get(levelName).size() > 10) {
            List<PlayerRecord> topRecords = levelRecords.get(levelName).subList(0, 10);
            levelRecords.put(levelName, new ArrayList<>(topRecords));
        }
    }
    
    /**
     * Rebuild global records from all level records
     */
    public void rebuildGlobalRecords() {
        globalRecords.clear();
        for (List<PlayerRecord> levelRecordList : levelRecords.values()) {
            globalRecords.addAll(levelRecordList);
        }
        globalRecords.sort(PlayerRecord::compareTo);
        
        // Keep only top 50 global records
        if (globalRecords.size() > 50) {
            globalRecords = globalRecords.subList(0, 50);
        }
    }
    
    /**
     * Update current player's statistics
     */
    public void updatePlayerStats(String playerName, int levelNumber, long completionTime, 
                                int xpEarned, int coinsEarned) {
        currentPlayerStats.playerName = playerName;
        currentPlayerStats.totalXP += xpEarned;
        currentPlayerStats.totalCoins += coinsEarned;
        currentPlayerStats.levelsCompleted++;
        
        // Update best time
        if (completionTime < currentPlayerStats.bestTime) {
            currentPlayerStats.bestTime = completionTime;
        }
        
        // Update best XP
        if (xpEarned > currentPlayerStats.bestXP) {
            currentPlayerStats.bestXP = xpEarned;
        }
        
        // Update level-specific records
        currentPlayerStats.levelBestTimes.merge(levelNumber, completionTime, 
            (existing, newTime) -> Math.min(existing, newTime));
        currentPlayerStats.levelBestXP.merge(levelNumber, xpEarned, 
            (existing, newXP) -> Math.max(existing, newXP));
    }
    
    /**
     * Get records for a specific level
     */
    public List<PlayerRecord> getLevelRecords(String levelName) {
        return levelRecords.getOrDefault(levelName, new ArrayList<>());
    }
    
    /**
     * Get global records
     */
    public List<PlayerRecord> getGlobalRecords() {
        return new ArrayList<>(globalRecords);
    }
    
    /**
     * Get current player's statistics
     */
    public PlayerStats getCurrentPlayerStats() {
        return currentPlayerStats;
    }
    
    /**
     * Get all level names that have records
     */
    public Set<String> getLevelNames() {
        return levelRecords.keySet();
    }
    
    /**
     * Calculate XP based on completion time and packet loss
     */
    public static int calculateXP(long completionTime, double packetLossPercentage, int coinsEarned) {
        // Base XP calculation
        int baseXP = 100;
        
        // Time bonus (faster completion = more XP)
        long timeBonus = Math.max(0, 30000 - completionTime) / 1000; // 30 second bonus
        
        // Packet loss penalty
        double lossPenalty = packetLossPercentage * 2; // 2 XP per 1% packet loss
        
        // Coins bonus
        int coinsBonus = coinsEarned;
        
        int totalXP = (int)(baseXP + timeBonus - lossPenalty + coinsBonus);
        return Math.max(0, totalXP); // Ensure non-negative XP
    }
}
