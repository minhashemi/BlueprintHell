package me.minhashemi.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Packet {
    public int packetId;
    public Point position;
    private List<PortType> inputs;
    private List<PortType> outputs;

    private final List<PacketPort> inputPorts = new ArrayList<>();
    private final List<PacketPort> outputPorts = new ArrayList<>();

    public List<PortType> getInputs() {
        return inputs;
    }

    public List<PortType> getOutputs() {
        return outputs;
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

    public void clearPorts() {
        inputPorts.clear();
        outputPorts.clear();
    }

    // Initialize ports only once
    public void initializePorts() {
        inputPorts.clear();
        outputPorts.clear();

        // Place input ports on the left side of the packet
        // Initialize input ports on the left side of the packet
        for (int i = 0; i < inputs.size(); i++) {
            inputPorts.add(new PacketPort(this, false, inputs.get(i), true, i));  // true for input, passing index
        }

        // Initialize output ports on the right side of the packet
        for (int i = 0; i < outputs.size(); i++) {
            outputPorts.add(new PacketPort(this, false, outputs.get(i), false, i));  // false for output, passing index
        }

    }

    public void setPosition(Point newPosition) {
        this.position = newPosition;
    }
}
