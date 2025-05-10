package me.minhashemi.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Packet {
    public int packetId;
    public Position position;
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


    // ✅ Initialize ports only once
    public void initializePorts() {
        inputPorts.clear();
        outputPorts.clear();

        for (int i = 0; i < inputs.size(); i++) {
            int y = position.y + i * Config.STANDARD_HEIGHT + (Config.STANDARD_HEIGHT - Config.PORT_SIZE) / 2;
            int x = position.x - Config.PORT_SIZE;
            inputPorts.add(new PacketPort(new Point(x, y), inputs.get(i)));
        }

        for (int i = 0; i < outputs.size(); i++) {
            int y = position.y + i * Config.STANDARD_HEIGHT + (Config.STANDARD_HEIGHT - Config.PORT_SIZE) / 2;
            int x = position.x + Config.PACKET_WIDTH;
            outputPorts.add(new PacketPort(new Point(x, y), outputs.get(i)));
        }
    }
}
