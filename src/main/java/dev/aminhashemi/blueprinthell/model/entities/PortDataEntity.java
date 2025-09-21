package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;

/**
 * JPA Entity for PortData
 */
@Entity
@Table(name = "port_data", indexes = {
    @Index(name = "idx_system_id", columnList = "system_id"),
    @Index(name = "idx_port_type", columnList = "type")
})
public class PortDataEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "port_id")
    private Long portId;
    
    @Column(name = "type", length = 50, nullable = false)
    private String type;
    
    @Column(name = "port_index", nullable = false)
    private int portIndex;
    
    @Column(name = "is_input", nullable = false)
    private boolean isInput;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "system_id")
    private SystemDataEntity systemData;
    
    // Constructors
    public PortDataEntity() {}
    
    public PortDataEntity(String type, int portIndex, boolean isInput) {
        this.type = type;
        this.portIndex = portIndex;
        this.isInput = isInput;
    }
    
    // Getters and setters
    public Long getPortId() { return portId; }
    public void setPortId(Long portId) { this.portId = portId; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public int getPortIndex() { return portIndex; }
    public void setPortIndex(int portIndex) { this.portIndex = portIndex; }
    
    public boolean isInput() { return isInput; }
    public void setInput(boolean input) { isInput = input; }
    
    public SystemDataEntity getSystemData() { return systemData; }
    public void setSystemData(SystemDataEntity systemData) { this.systemData = systemData; }
}
