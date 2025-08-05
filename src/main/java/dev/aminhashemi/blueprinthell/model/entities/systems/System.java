package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.Entity;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;

import java.util.ArrayList;
import java.util.List;

public abstract class System extends Entity {

    protected List<Port> inputPorts;
    protected List<Port> outputPorts;

    public System(int x, int y, int width, int height, LevelData.SystemData data) {
        super(x, y, width, height);
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();
        initializePorts(data);
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

    // This is now an abstract method that all subclasses must implement
    @Override
    public abstract void update(GameEngine engine);

    public List<Port> getInputPorts() {
        return inputPorts;
    }

    public List<Port> getOutputPorts() {
        return outputPorts;
    }
}
