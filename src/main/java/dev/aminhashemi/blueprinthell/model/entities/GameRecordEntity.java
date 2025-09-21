package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for GameRecord data
 */
@Entity
@Table(name = "game_records", indexes = {
    @Index(name = "idx_user_mac", columnList = "userMacAddress"),
    @Index(name = "idx_level_name", columnList = "levelName"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_completion_time", columnList = "completionTime")
})
public class GameRecordEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;
    
    @Column(name = "user_mac_address", length = 17, nullable = false)
    private String userMacAddress;
    
    @Column(name = "level_name", length = 100, nullable = false)
    private String levelName;
    
    @Column(name = "completion_time", nullable = false)
    private long completionTime; // in milliseconds
    
    @Column(name = "xp_earned", nullable = false)
    private int xpEarned;
    
    @Column(name = "coins_earned", nullable = false)
    private int coinsEarned;
    
    @Column(name = "packet_loss_percentage", nullable = false)
    private double packetLossPercentage;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_mac_address", referencedColumnName = "mac_address", insertable = false, updatable = false)
    private UserProfileEntity userProfile;
    
    // Constructors
    public GameRecordEntity() {
        this.timestamp = LocalDateTime.now();
    }
    
    public GameRecordEntity(String userMacAddress, String levelName, long completionTime, 
                           int xpEarned, int coinsEarned, double packetLossPercentage) {
        this();
        this.userMacAddress = userMacAddress;
        this.levelName = levelName;
        this.completionTime = completionTime;
        this.xpEarned = xpEarned;
        this.coinsEarned = coinsEarned;
        this.packetLossPercentage = packetLossPercentage;
    }
    
    // Getters and setters
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    
    public String getUserMacAddress() { return userMacAddress; }
    public void setUserMacAddress(String userMacAddress) { this.userMacAddress = userMacAddress; }
    
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
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public UserProfileEntity getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfileEntity userProfile) { this.userProfile = userProfile; }
    
    // Utility methods
    public String getFormattedTime() {
        long seconds = completionTime / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
