package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for LevelData
 */
@Entity
@Table(name = "level_data", indexes = {
    @Index(name = "idx_level_number", columnList = "level_number"),
    @Index(name = "idx_level_name", columnList = "level_name")
})
public class LevelDataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "level_id")
    private Long levelId;
    
    @Column(name = "level_number", nullable = false, unique = true)
    private int levelNumber;
    
    @Column(name = "level_name", length = 100, nullable = false)
    private String levelName;
    
    @Embedded
    private PlayerStartData playerStart;
    
    @OneToMany(mappedBy = "levelData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SystemDataEntity> systems = new ArrayList<>();
    
    @OneToMany(mappedBy = "levelData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WireDataEntity> wires = new ArrayList<>();
    
    // Constructors
    public LevelDataEntity() {}
    
    public LevelDataEntity(int levelNumber, String levelName) {
        this.levelNumber = levelNumber;
        this.levelName = levelName;
    }
    
    // Getters and setters
    public Long getLevelId() { return levelId; }
    public void setLevelId(Long levelId) { this.levelId = levelId; }
    
    public int getLevelNumber() { return levelNumber; }
    public void setLevelNumber(int levelNumber) { this.levelNumber = levelNumber; }
    
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    
    public PlayerStartData getPlayerStart() { return playerStart; }
    public void setPlayerStart(PlayerStartData playerStart) { this.playerStart = playerStart; }
    
    public List<SystemDataEntity> getSystems() { return systems; }
    public void setSystems(List<SystemDataEntity> systems) { this.systems = systems; }
    
    public List<WireDataEntity> getWires() { return wires; }
    public void setWires(List<WireDataEntity> wires) { this.wires = wires; }
    
    /**
     * Embedded class for PlayerStart data
     */
    @Embeddable
    public static class PlayerStartData {
        @Column(name = "initial_coins")
        private int initialCoins;
        
        @Column(name = "initial_wire_length")
        private int initialWireLength;
        
        public PlayerStartData() {}
        
        public PlayerStartData(int initialCoins, int initialWireLength) {
            this.initialCoins = initialCoins;
            this.initialWireLength = initialWireLength;
        }
        
        public int getInitialCoins() { return initialCoins; }
        public void setInitialCoins(int initialCoins) { this.initialCoins = initialCoins; }
        
        public int getInitialWireLength() { return initialWireLength; }
        public void setInitialWireLength(int initialWireLength) { this.initialWireLength = initialWireLength; }
    }
}
