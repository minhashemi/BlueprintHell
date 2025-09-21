package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for WireData
 */
@Entity
@Table(name = "wire_data", indexes = {
    @Index(name = "idx_level_id", columnList = "levelId"),
    @Index(name = "idx_start_system", columnList = "startSystemId"),
    @Index(name = "idx_end_system", columnList = "endSystemId")
})
public class WireDataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wire_id")
    private Long wireId;
    
    @Column(name = "start_system_id", length = 50, nullable = false)
    private String startSystemId;
    
    @Column(name = "start_port_index", nullable = false)
    private int startPortIndex;
    
    @Column(name = "end_system_id", length = 50, nullable = false)
    private String endSystemId;
    
    @Column(name = "end_port_index", nullable = false)
    private int endPortIndex;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private LevelDataEntity levelData;
    
    // Constructors
    public WireDataEntity() {}
    
    public WireDataEntity(String startSystemId, int startPortIndex, String endSystemId, int endPortIndex) {
        this.startSystemId = startSystemId;
        this.startPortIndex = startPortIndex;
        this.endSystemId = endSystemId;
        this.endPortIndex = endPortIndex;
    }
    
    // Getters and setters
    public Long getWireId() { return wireId; }
    public void setWireId(Long wireId) { this.wireId = wireId; }
    
    public String getStartSystemId() { return startSystemId; }
    public void setStartSystemId(String startSystemId) { this.startSystemId = startSystemId; }
    
    public int getStartPortIndex() { return startPortIndex; }
    public void setStartPortIndex(int startPortIndex) { this.startPortIndex = startPortIndex; }
    
    public String getEndSystemId() { return endSystemId; }
    public void setEndSystemId(String endSystemId) { this.endSystemId = endSystemId; }
    
    public int getEndPortIndex() { return endPortIndex; }
    public void setEndPortIndex(int endPortIndex) { this.endPortIndex = endPortIndex; }
    
    public LevelDataEntity getLevelData() { return levelData; }
    public void setLevelData(LevelDataEntity levelData) { this.levelData = levelData; }
}
