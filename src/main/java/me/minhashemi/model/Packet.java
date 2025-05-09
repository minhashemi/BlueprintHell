package me.minhashemi.model;

import java.util.List;
import me.minhashemi.model.Config;

public class Packet {
    public int packet_id;
    public Position position;
    public List<PortType> inputs;
    public List<PortType> outputs;

    public List<PortType> getInputs() {
        return inputs;
    }

    public List<PortType> getOutputs() {
        return outputs;
    }

    public int getHeight() {
        int rows = Math.max(inputs.size(), outputs.size());
        return rows * Config.STANDARD_HEIGHT;
    }

    public int getX() {
        return position.x;
    }

    public int getY() {
        return position.y;
    }
}
