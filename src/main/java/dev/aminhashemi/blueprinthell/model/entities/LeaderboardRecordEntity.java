package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for LeaderboardRecord data
 */
@Entity
@Table(name = "leaderboard_records", indexes = {
    @Index(name = "idx_level_name", columnList = "level_name"),
    @Index(name = "idx_completion_time", columnList = "completion_time"),
    @Index(name = "idx_player_name", columnList = "player_name"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
public class LeaderboardRecordEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;
    
    @Column(name = "player_name", length = 50, nullable = false)
    private String playerName;
    
    @Column(name = "completion_time", nullable = false)
    private long completionTime; // in milliseconds
    
    @Column(name = "xp_earned", nullable = false)
    private int xpEarned;
    
    @Column(name = "level_number", nullable = false)
    private int levelNumber;
    
    @Column(name = "level_name", length = 100, nullable = false)
    private String levelName;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "packet_loss_percentage", nullable = false)
    private double packetLossPercentage;
    
    @Column(name = "coins_earned", nullable = false)
    private int coinsEarned;
    
    @Column(name = "user_mac_address", length = 17)
    private String userMacAddress;
    
    // Constructors
    public LeaderboardRecordEntity() {
        this.timestamp = LocalDateTime.now();
    }
    
    public LeaderboardRecordEntity(String playerName, long completionTime, int xpEarned, 
                                 int levelNumber, String levelName, double packetLossPercentage, 
                                 int coinsEarned, String userMacAddress) {
        this();
        this.playerName = playerName;
        this.completionTime = completionTime;
        this.xpEarned = xpEarned;
        this.levelNumber = levelNumber;
        this.levelName = levelName;
        this.packetLossPercentage = packetLossPercentage;
        this.coinsEarned = coinsEarned;
        this.userMacAddress = userMacAddress;
    }
    
    // Getters and setters
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    
    public long getCompletionTime() { return completionTime; }
    public void setCompletionTime(long completionTime) { this.completionTime = completionTime; }
    
    public int getXpEarned() { return xpEarned; }
    public void setXpEarned(int xpEarned) { this.xpEarned = xpEarned; }
    
    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public double getPacketLossPercentage() { return packetLossPercentage; }
    public void setPacketLossPercentage(double packetLossPercentage) { this.packetLossPercentage = packetLossPercentage; }
    
    public int getCoinsEarned() { return coinsEarned; }
    public void setCoinsEarned(int coinsEarned) { this.coinsEarned = coinsEarned; }
    
    public String getUserMacAddress() { return userMacAddress; }
    public void setUserMacAddress(String userMacAddress) { this.userMacAddress = userMacAddress; }
    
    // Utility methods
    public String getFormattedTime() {
        long seconds = completionTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
