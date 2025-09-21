package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for UserProfile data
 */
@Entity
@Table(name = "user_profiles", indexes = {
    @Index(name = "idx_mac_address", columnList = "macAddress"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_last_login", columnList = "lastLoginTime")
})
public class UserProfileEntity {
    
    @Id
    @Column(name = "mac_address", length = 17, unique = true, nullable = false)
    private String macAddress;
    
    @Column(name = "username", length = 50, nullable = false)
    private String username;
    
    @Column(name = "total_xp", nullable = false)
    private int totalXP = 0;
    
    @Column(name = "total_coins", nullable = false)
    private int totalCoins = 0;
    
    @ElementCollection
    @CollectionTable(name = "user_unlocked_features", joinColumns = @JoinColumn(name = "mac_address"))
    @Column(name = "feature_name", length = 100)
    private List<String> unlockedFeatures = new ArrayList<>();
    
    @ElementCollection
    @CollectionTable(name = "user_active_abilities", joinColumns = @JoinColumn(name = "mac_address"))
    @Column(name = "ability_name", length = 100)
    private List<String> activeAbilities = new ArrayList<>();
    
    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<GameRecordEntity> gameHistory = new ArrayList<>();
    
    @Column(name = "last_login_time", nullable = false)
    private LocalDateTime lastLoginTime;
    
    @Column(name = "account_created_time", nullable = false)
    private LocalDateTime accountCreatedTime;
    
    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;
    
    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;
    
    // Constructors
    public UserProfileEntity() {
        this.lastLoginTime = LocalDateTime.now();
        this.accountCreatedTime = LocalDateTime.now();
    }
    
    public UserProfileEntity(String macAddress, String username) {
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
    
    public List<GameRecordEntity> getGameHistory() { return gameHistory; }
    public void setGameHistory(List<GameRecordEntity> gameHistory) { this.gameHistory = gameHistory; }
    
    public LocalDateTime getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(LocalDateTime lastLoginTime) { this.lastLoginTime = lastLoginTime; }
    
    public LocalDateTime getAccountCreatedTime() { return accountCreatedTime; }
    public void setAccountCreatedTime(LocalDateTime accountCreatedTime) { this.accountCreatedTime = accountCreatedTime; }
    
    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
    
    public LocalDateTime getLastSyncTime() { return lastSyncTime; }
    public void setLastSyncTime(LocalDateTime lastSyncTime) { this.lastSyncTime = lastSyncTime; }
    
    // Business methods
    public void addXP(int xp) {
        this.totalXP += xp;
    }
    
    public void addCoins(int coins) {
        this.totalCoins += coins;
    }
    
    public void addGameRecord(GameRecordEntity record) {
        this.gameHistory.add(record);
        record.setUserProfile(this);
        
        // Keep only last 100 records
        if (this.gameHistory.size() > 100) {
            this.gameHistory = this.gameHistory.subList(this.gameHistory.size() - 100, this.gameHistory.size());
        }
    }
    
    public void unlockFeature(String feature) {
        if (!unlockedFeatures.contains(feature)) {
            unlockedFeatures.add(feature);
        }
    }
    
    public void activateAbility(String ability) {
        if (!activeAbilities.contains(ability)) {
            activeAbilities.add(ability);
        }
    }
    
    public void deactivateAbility(String ability) {
        activeAbilities.remove(ability);
    }
    
    public void updateLastLogin() {
        this.lastLoginTime = LocalDateTime.now();
    }
    
    public void updateSyncTime() {
        this.lastSyncTime = LocalDateTime.now();
    }
}
