package me.minhashemi.model;

import java.util.List;

public class Packet {
    public static final int STANDARD_PORT_HEIGHT = 30;

    private final List<PortType> inputs;
    private final List<PortType> outputs;

    public Packet(List<PortType> inputs, List<PortType> outputs) {
        this.inputs = inputs;
        this.outputs = outputs;
    }

    public List<PortType> getInputs() {
        return inputs;
    }

    public List<PortType> getOutputs() {
        return outputs;
    }

    public int getHeight() {
        return Math.max(inputs.size(), outputs.size()) * STANDARD_PORT_HEIGHT;
    }
}
