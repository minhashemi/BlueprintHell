package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity for SystemData
 */
@Entity
@Table(name = "system_data", indexes = {
    @Index(name = "idx_level_id", columnList = "level_id"),
    @Index(name = "idx_system_id", columnList = "system_id"),
    @Index(name = "idx_system_type", columnList = "type")
})
public class SystemDataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "system_id", length = 50, nullable = false)
    private String systemId;
    
    @Column(name = "type", length = 50, nullable = false)
    private String type;
    
    @Embedded
    private PositionData position;
    
    @OneToMany(mappedBy = "systemData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PortDataEntity> inputPorts = new ArrayList<>();
    
    @OneToMany(mappedBy = "systemData", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PortDataEntity> outputPorts = new ArrayList<>();
    
    @Embedded
    private PacketGenerationData packetGeneration;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id")
    private LevelDataEntity levelData;
    
    // Constructors
    public SystemDataEntity() {}
    
    public SystemDataEntity(String systemId, String type) {
        this.systemId = systemId;
        this.type = type;
    }
    
    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSystemId() { return systemId; }
    public void setSystemId(String systemId) { this.systemId = systemId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public PositionData getPosition() { return position; }
    public void setPosition(PositionData position) { this.position = position; }
    
    public List<PortDataEntity> getInputPorts() { return inputPorts; }
    public void setInputPorts(List<PortDataEntity> inputPorts) { this.inputPorts = inputPorts; }
    
    public List<PortDataEntity> getOutputPorts() { return outputPorts; }
    public void setOutputPorts(List<PortDataEntity> outputPorts) { this.outputPorts = outputPorts; }
    
    public PacketGenerationData getPacketGeneration() { return packetGeneration; }
    public void setPacketGeneration(PacketGenerationData packetGeneration) { this.packetGeneration = packetGeneration; }
    
    public LevelDataEntity getLevelData() { return levelData; }
    public void setLevelData(LevelDataEntity levelData) { this.levelData = levelData; }
    
    /**
     * Embedded class for Position data
     */
    @Embeddable
    public static class PositionData {
        @Column(name = "x")
        private int x;
        
        @Column(name = "y")
        private int y;
        
        public PositionData() {}
        
        public PositionData(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
    }
    
    /**
     * Embedded class for PacketGeneration data
     */
    @Embeddable
    public static class PacketGenerationData {
        @Column(name = "packet_type", length = 50)
        private String packetType;
        
        @Column(name = "count")
        private Integer count;
        
        @Column(name = "frequency")
        private Double frequency;
        
        public PacketGenerationData() {}
        
        public PacketGenerationData(String packetType, Integer count, Double frequency) {
            this.packetType = packetType;
            this.count = count;
            this.frequency = frequency;
        }
        
        public String getPacketType() { return packetType; }
        public void setPacketType(String packetType) { this.packetType = packetType; }
        
        public Integer getCount() { return count; }
        public void setCount(Integer count) { this.count = count; }
        
        public Double getFrequency() { return frequency; }
        public void setFrequency(Double frequency) { this.frequency = frequency; }
    }
}
