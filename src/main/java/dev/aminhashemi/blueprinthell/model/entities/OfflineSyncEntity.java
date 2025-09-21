package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity for tracking offline data synchronization
 */
@Entity
@Table(name = "offline_sync", indexes = {
    @Index(name = "idx_user_mac", columnList = "userMacAddress"),
    @Index(name = "idx_sync_status", columnList = "syncStatus"),
    @Index(name = "idx_created_time", columnList = "createdTime")
})
public class OfflineSyncEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sync_id")
    private Long syncId;
    
    @Column(name = "user_mac_address", length = 17, nullable = false)
    private String userMacAddress;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false)
    private SyncStatus syncStatus;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false)
    private DataType dataType;
    
    @Column(name = "data_id", length = 100)
    private String dataId;
    
    @Lob
    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;
    
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;
    
    @Column(name = "last_attempt_time")
    private LocalDateTime lastAttemptTime;
    
    @Column(name = "attempt_count", nullable = false)
    private int attemptCount = 0;
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;
    
    @Column(name = "server_sync_time")
    private LocalDateTime serverSyncTime;
    
    // Constructors
    public OfflineSyncEntity() {
        this.createdTime = LocalDateTime.now();
        this.syncStatus = SyncStatus.PENDING;
    }
    
    public OfflineSyncEntity(String userMacAddress, DataType dataType, String dataId, String dataJson) {
        this();
        this.userMacAddress = userMacAddress;
        this.dataType = dataType;
        this.dataId = dataId;
        this.dataJson = dataJson;
    }
    
    // Getters and setters
    public Long getSyncId() { return syncId; }
    public void setSyncId(Long syncId) { this.syncId = syncId; }
    
    public String getUserMacAddress() { return userMacAddress; }
    public void setUserMacAddress(String userMacAddress) { this.userMacAddress = userMacAddress; }
    
    public SyncStatus getSyncStatus() { return syncStatus; }
    public void setSyncStatus(SyncStatus syncStatus) { this.syncStatus = syncStatus; }
    
    public DataType getDataType() { return dataType; }
    public void setDataType(DataType dataType) { this.dataType = dataType; }
    
    public String getDataId() { return dataId; }
    public void setDataId(String dataId) { this.dataId = dataId; }
    
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    
    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }
    
    public LocalDateTime getLastAttemptTime() { return lastAttemptTime; }
    public void setLastAttemptTime(LocalDateTime lastAttemptTime) { this.lastAttemptTime = lastAttemptTime; }
    
    public int getAttemptCount() { return attemptCount; }
    public void setAttemptCount(int attemptCount) { this.attemptCount = attemptCount; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public LocalDateTime getServerSyncTime() { return serverSyncTime; }
    public void setServerSyncTime(LocalDateTime serverSyncTime) { this.serverSyncTime = serverSyncTime; }
    
    // Business methods
    public void markAsAttempted() {
        this.lastAttemptTime = LocalDateTime.now();
        this.attemptCount++;
    }
    
    public void markAsSuccess() {
        this.syncStatus = SyncStatus.SYNCED;
        this.serverSyncTime = LocalDateTime.now();
        this.errorMessage = null;
    }
    
    public void markAsFailed(String errorMessage) {
        this.syncStatus = SyncStatus.FAILED;
        this.errorMessage = errorMessage;
    }
    
    public void markAsRetry() {
        this.syncStatus = SyncStatus.PENDING;
        this.errorMessage = null;
    }
    
    // Enums
    public enum SyncStatus {
        PENDING,    // Waiting to be synced
        SYNCING,    // Currently being synced
        SYNCED,     // Successfully synced
        FAILED,     // Failed to sync
        RETRY       // Ready for retry
    }
    
    public enum DataType {
        USER_PROFILE,
        GAME_RECORD,
        LEADERBOARD_RECORD,
        PLAYER_STATS,
        SAVE_DATA,
        SHOP_ITEM
    }
}
