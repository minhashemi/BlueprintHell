package me.minhashemi.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Packet {
    public int packetId;
    public Position position;
    private List<PortType> inputs;   // Loaded from JSON
    private List<PortType> outputs;  // Loaded from JSON

    private final List<PacketPort> inputPorts = new ArrayList<>();  // Used during rendering
    private final List<PacketPort> outputPorts = new ArrayList<>();

    public List<PortType> getInputs() {
        return inputs;
    }

    public List<PortType> getOutputs() {
        return outputs;
    }

    public void addInputPort(PacketPort port) {
        inputPorts.add(port);
    }

    public void addOutputPort(PacketPort port) {
        outputPorts.add(port);
    }

    public List<PacketPort> getInputPorts() {
        return inputPorts;
    }

    public List<PacketPort> getOutputPorts() {
        return outputPorts;
    }

    public int getHeight() {
        int portCount = Math.max(getInputs().size(), getOutputs().size());
        return portCount * Config.STANDARD_HEIGHT;
    }

}
