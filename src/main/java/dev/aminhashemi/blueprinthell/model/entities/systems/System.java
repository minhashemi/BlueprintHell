package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.GameEntity;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.MovingPacket;

import java.util.ArrayList;
import java.util.List;

public abstract class System extends GameEntity {

    protected List<Port> inputPorts;
    protected List<Port> outputPorts;

    public System(int x, int y, int width, int height, LevelData.SystemData data) {
        super(x, y, width, height);
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();
        initializePorts(data);
        
        // Set system ID from level data
        if (data.id != null && !data.id.isEmpty()) {
            this.id = data.id;
        }
    }

    private void initializePorts(LevelData.SystemData data) {
        if (data.inputPorts != null) {
            for (int i = 0; i < data.inputPorts.size(); i++) {
                PortType type = PortType.valueOf(data.inputPorts.get(i).type);
                inputPorts.add(new Port(type, this, true, i));
            }
        }

        if (data.outputPorts != null) {
            for (int i = 0; i < data.outputPorts.size(); i++) {
                PortType type = PortType.valueOf(data.outputPorts.get(i).type);
                outputPorts.add(new Port(type, this, false, i));
            }
        }
    }

    public void receivePacket(Packet packet, GameEngine engine) {
        engine.routePacket(packet, this);
    }
    
    public void receiveMovingPacket(MovingPacket movingPacket, GameEngine engine) {
        // Route the packet as a MovingPacket to preserve the playerSpawned flag
        engine.routeMovingPacket(movingPacket, this);
    }

    // Abstract method for system-specific update logic
    @Override
    public abstract void update(GameEngine engine);

    public List<Port> getInputPorts() {
        return inputPorts;
    }

    public List<Port> getOutputPorts() {
        return outputPorts;
    }
    
    // ==================== SAVE SYSTEM SUPPORT ====================
    
    private String id;
    
    /**
     * Gets the system ID
     */
    public String getId() {
        if (id == null) {
            id = this.getClass().getSimpleName() + "_" + x + "_" + y;
        }
        return id;
    }
    
    /**
     * Sets the system ID
     */
    public void setId(String id) {
        this.id = id;
    }
}
