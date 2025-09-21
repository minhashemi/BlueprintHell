package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA Entity for SaveData
 */
@Entity
@Table(name = "save_data", indexes = {
    @Index(name = "idx_user_mac", columnList = "user_mac_address"),
    @Index(name = "idx_level_name", columnList = "level_name"),
    @Index(name = "idx_save_time", columnList = "save_time")
})
public class SaveDataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "save_id")
    private Long saveId;
    
    @Column(name = "user_mac_address", length = 17, nullable = false)
    private String userMacAddress;
    
    @Column(name = "level_name", length = 100, nullable = false)
    private String levelName;
    
    @Column(name = "save_time", nullable = false)
    private LocalDateTime saveTime;
    
    // Game state
    @Column(name = "coins", nullable = false)
    private int coins;
    
    @Column(name = "used_wire_length", nullable = false)
    private int usedWireLength;
    
    @Column(name = "total_wire_length", nullable = false)
    private int totalWireLength;
    
    @Column(name = "is_wiring_mode", nullable = false)
    private boolean isWiringMode;
    
    // Shop and effects state
    @Column(name = "impact_waves_disabled", nullable = false)
    private boolean impactWavesDisabled;
    
    @Column(name = "packet_collisions_disabled", nullable = false)
    private boolean packetCollisionsDisabled;
    
    @Column(name = "packet_noise_zeroed", nullable = false)
    private boolean packetNoiseZeroed;
    
    @Column(name = "game_start_time", nullable = false)
    private long gameStartTime;
    
    @Column(name = "current_game_time", nullable = false)
    private long currentGameTime;
    
    // Test system state
    @Column(name = "is_test_running", nullable = false)
    private boolean isTestRunning;
    
    @Column(name = "test_packets_released", nullable = false)
    private int testPacketsReleased;
    
    @Column(name = "test_packets_returned", nullable = false)
    private int testPacketsReturned;
    
    @Column(name = "test_start_time", nullable = false)
    private long testStartTime;
    
    @Column(name = "last_packet_release_time", nullable = false)
    private long lastPacketReleaseTime;
    
    @Column(name = "test_completed", nullable = false)
    private boolean testCompleted;
    
    @Column(name = "game_won", nullable = false)
    private boolean gameWon;
    
    @Column(name = "game_lost", nullable = false)
    private boolean gameLost;
    
    // Time travel state
    @Column(name = "is_time_travel_mode", nullable = false)
    private boolean isTimeTravelMode;
    
    @Column(name = "is_paused", nullable = false)
    private boolean isPaused;
    
    @Column(name = "current_snapshot_index", nullable = false)
    private int currentSnapshotIndex;
    
    @Column(name = "time_travel_window_seconds", nullable = false)
    private int timeTravelWindowSeconds;
    
    @Lob
    @Column(name = "save_data_json", columnDefinition = "TEXT")
    private String saveDataJson;
    
    @ElementCollection
    @CollectionTable(name = "save_wire_lengths", joinColumns = @JoinColumn(name = "save_id"))
    @MapKeyColumn(name = "wire_id")
    @Column(name = "length")
    private Map<String, Integer> wireLengths = new HashMap<>();
    
    // Constructors
    public SaveDataEntity() {
        this.saveTime = LocalDateTime.now();
    }
    
    public SaveDataEntity(String userMacAddress, String levelName) {
        this();
        this.userMacAddress = userMacAddress;
        this.levelName = levelName;
    }
    
    // Getters and setters
    public Long getSaveId() { return saveId; }
    public void setSaveId(Long saveId) { this.saveId = saveId; }
    
    public String getUserMacAddress() { return userMacAddress; }
    public void setUserMacAddress(String userMacAddress) { this.userMacAddress = userMacAddress; }
    
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    
    public LocalDateTime getSaveTime() { return saveTime; }
    public void setSaveTime(LocalDateTime saveTime) { this.saveTime = saveTime; }
    
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    
    public int getUsedWireLength() { return usedWireLength; }
    public void setUsedWireLength(int usedWireLength) { this.usedWireLength = usedWireLength; }
    
    public int getTotalWireLength() { return totalWireLength; }
    public void setTotalWireLength(int totalWireLength) { this.totalWireLength = totalWireLength; }
    
    public boolean isWiringMode() { return isWiringMode; }
    public void setWiringMode(boolean wiringMode) { isWiringMode = wiringMode; }
    
    public boolean isImpactWavesDisabled() { return impactWavesDisabled; }
    public void setImpactWavesDisabled(boolean impactWavesDisabled) { this.impactWavesDisabled = impactWavesDisabled; }
    
    public boolean isPacketCollisionsDisabled() { return packetCollisionsDisabled; }
    public void setPacketCollisionsDisabled(boolean packetCollisionsDisabled) { this.packetCollisionsDisabled = packetCollisionsDisabled; }
    
    public boolean isPacketNoiseZeroed() { return packetNoiseZeroed; }
    public void setPacketNoiseZeroed(boolean packetNoiseZeroed) { this.packetNoiseZeroed = packetNoiseZeroed; }
    
    public long getGameStartTime() { return gameStartTime; }
    public void setGameStartTime(long gameStartTime) { this.gameStartTime = gameStartTime; }
    
    public long getCurrentGameTime() { return currentGameTime; }
    public void setCurrentGameTime(long currentGameTime) { this.currentGameTime = currentGameTime; }
    
    public boolean isTestRunning() { return isTestRunning; }
    public void setTestRunning(boolean testRunning) { isTestRunning = testRunning; }
    
    public int getTestPacketsReleased() { return testPacketsReleased; }
    public void setTestPacketsReleased(int testPacketsReleased) { this.testPacketsReleased = testPacketsReleased; }
    
    public int getTestPacketsReturned() { return testPacketsReturned; }
    public void setTestPacketsReturned(int testPacketsReturned) { this.testPacketsReturned = testPacketsReturned; }
    
    public long getTestStartTime() { return testStartTime; }
    public void setTestStartTime(long testStartTime) { this.testStartTime = testStartTime; }
    
    public long getLastPacketReleaseTime() { return lastPacketReleaseTime; }
    public void setLastPacketReleaseTime(long lastPacketReleaseTime) { this.lastPacketReleaseTime = lastPacketReleaseTime; }
    
    public boolean isTestCompleted() { return testCompleted; }
    public void setTestCompleted(boolean testCompleted) { this.testCompleted = testCompleted; }
    
    public boolean isGameWon() { return gameWon; }
    public void setGameWon(boolean gameWon) { this.gameWon = gameWon; }
    
    public boolean isGameLost() { return gameLost; }
    public void setGameLost(boolean gameLost) { this.gameLost = gameLost; }
    
    public boolean isTimeTravelMode() { return isTimeTravelMode; }
    public void setTimeTravelMode(boolean timeTravelMode) { isTimeTravelMode = timeTravelMode; }
    
    public boolean isPaused() { return isPaused; }
    public void setPaused(boolean paused) { isPaused = paused; }
    
    public int getCurrentSnapshotIndex() { return currentSnapshotIndex; }
    public void setCurrentSnapshotIndex(int currentSnapshotIndex) { this.currentSnapshotIndex = currentSnapshotIndex; }
    
    public int getTimeTravelWindowSeconds() { return timeTravelWindowSeconds; }
    public void setTimeTravelWindowSeconds(int timeTravelWindowSeconds) { this.timeTravelWindowSeconds = timeTravelWindowSeconds; }
    
    public String getSaveDataJson() { return saveDataJson; }
    public void setSaveDataJson(String saveDataJson) { this.saveDataJson = saveDataJson; }
    
    public Map<String, Integer> getWireLengths() { return wireLengths; }
    public void setWireLengths(Map<String, Integer> wireLengths) { this.wireLengths = wireLengths; }
}
