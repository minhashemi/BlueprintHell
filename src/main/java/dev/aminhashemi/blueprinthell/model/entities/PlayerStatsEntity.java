package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Entity for PlayerStats data
 */
@Entity
@Table(name = "player_stats", indexes = {
    @Index(name = "idx_player_name", columnList = "player_name"),
    @Index(name = "idx_total_xp", columnList = "total_xp"),
    @Index(name = "idx_levels_completed", columnList = "levels_completed")
})
public class PlayerStatsEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long statsId;
    
    @Column(name = "player_name", length = 50, nullable = false)
    private String playerName;
    
    @Column(name = "user_mac_address", length = 17, unique = true)
    private String userMacAddress;
    
    @Column(name = "total_xp", nullable = false)
    private int totalXP = 0;
    
    @Column(name = "total_coins", nullable = false)
    private int totalCoins = 0;
    
    @Column(name = "levels_completed", nullable = false)
    private int levelsCompleted = 0;
    
    @Column(name = "best_time")
    private Long bestTime; // best completion time across all levels
    
    @Column(name = "best_xp", nullable = false)
    private int bestXP = 0; // highest XP earned in a single game
    
    @ElementCollection
    @CollectionTable(name = "player_level_best_times", joinColumns = @JoinColumn(name = "stats_id"))
    @MapKeyColumn(name = "level_number")
    @Column(name = "best_time")
    private Map<Integer, Long> levelBestTimes = new HashMap<>();
    
    @ElementCollection
    @CollectionTable(name = "player_level_best_xp", joinColumns = @JoinColumn(name = "stats_id"))
    @MapKeyColumn(name = "level_number")
    @Column(name = "best_xp")
    private Map<Integer, Integer> levelBestXP = new HashMap<>();
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    // Constructors
    public PlayerStatsEntity() {
        this.lastUpdated = LocalDateTime.now();
        this.bestTime = Long.MAX_VALUE;
    }
    
    public PlayerStatsEntity(String playerName, String userMacAddress) {
        this();
        this.playerName = playerName;
        this.userMacAddress = userMacAddress;
    }
    
    // Getters and setters
    public Long getStatsId() { return statsId; }
    public void setStatsId(Long statsId) { this.statsId = statsId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public String getUserMacAddress() { return userMacAddress; }
    public void setUserMacAddress(String userMacAddress) { this.userMacAddress = userMacAddress; }
    
    public int getTotalXP() { return totalXP; }
    public void setTotalXP(int totalXP) { this.totalXP = totalXP; }
    
    public int getTotalCoins() { return totalCoins; }
    public void setTotalCoins(int totalCoins) { this.totalCoins = totalCoins; }
    
    public int getLevelsCompleted() { return levelsCompleted; }
    public void setLevelsCompleted(int levelsCompleted) { this.levelsCompleted = levelsCompleted; }
    
    public Long getBestTime() { return bestTime; }
    public void setBestTime(Long bestTime) { this.bestTime = bestTime; }
    
    public int getBestXP() { return bestXP; }
    public void setBestXP(int bestXP) { this.bestXP = bestXP; }
    
    public Map<Integer, Long> getLevelBestTimes() { return levelBestTimes; }
    public void setLevelBestTimes(Map<Integer, Long> levelBestTimes) { this.levelBestTimes = levelBestTimes; }
    
    public Map<Integer, Integer> getLevelBestXP() { return levelBestXP; }
    public void setLevelBestXP(Map<Integer, Integer> levelBestXP) { this.levelBestXP = levelBestXP; }
    
    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }
    
    // Business methods
    public void updateStats(String playerName, int levelNumber, long completionTime, 
                          int xpEarned, int coinsEarned) {
        this.playerName = playerName;
        this.totalXP += xpEarned;
        this.totalCoins += coinsEarned;
        this.levelsCompleted++;
        
        // Update best time
        if (completionTime < this.bestTime) {
            this.bestTime = completionTime;
        }
        
        // Update best XP
        if (xpEarned > this.bestXP) {
            this.bestXP = xpEarned;
        }
        
        // Update level-specific records
        this.levelBestTimes.merge(levelNumber, completionTime, 
            (existing, newTime) -> Math.min(existing, newTime));
        this.levelBestXP.merge(levelNumber, xpEarned, 
            (existing, newXP) -> Math.max(existing, newXP));
        
        this.lastUpdated = LocalDateTime.now();
    }
}
