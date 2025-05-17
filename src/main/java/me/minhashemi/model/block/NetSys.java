package me.minhashemi.model.block;

import me.minhashemi.model.Config;
import me.minhashemi.model.MovingPacket;

import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class NetSys {
    public int packetId;
    public Point position;
    private List<PortType> inputs;
    private List<PortType> outputs;

    private final List<NetSysPort> inputPorts = new ArrayList<>();
    private final List<NetSysPort> outputPorts = new ArrayList<>();

    private Queue<MovingPacket> buff = new LinkedList<>(); // buffer packets when wire is busy.
    private boolean hasReceivedPacket = false;

    public boolean enqueuePacket(MovingPacket packet){
        if (buff.size() < Config.MAX_BUFFER_SIZE){
            buff.add(packet);
            return true;
        }
        return false;
    }
    public List<PortType> getInputs() {
        return inputs;
    }

    public List<PortType> getOutputs() {
        return outputs;
    }

    public List<NetSysPort> getInputPorts() {
        return inputPorts;
    }

    public List<NetSysPort> getOutputPorts() {
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

    public void initializePorts() {
        while (inputPorts.size() < inputs.size()) {
            int i = inputPorts.size();
            inputPorts.add(new NetSysPort(this, false, inputs.get(i), true, i));
        }
        while (inputPorts.size() > inputs.size()) {
            inputPorts.remove(inputPorts.size() - 1);
        }
        for (int i = 0; i < inputs.size(); i++) {
            NetSysPort port = inputPorts.get(i);
            port.update(this, inputs.get(i), true, i);
        }

        while (outputPorts.size() < outputs.size()) {
            int i = outputPorts.size();
            outputPorts.add(new NetSysPort(this, false, outputs.get(i), false, i));
        }
        while (outputPorts.size() > outputs.size()) {
            outputPorts.remove(outputPorts.size() - 1);
        }
        for (int i = 0; i < outputs.size(); i++) {
            NetSysPort port = outputPorts.get(i);
            port.update(this, outputs.get(i), false, i);
        }
    }

    public void setPosition(Point newPosition) {
        this.position = newPosition;
    }

    public boolean isFullyConnected() {
        for (NetSysPort port : inputPorts) {
            if (!port.isConnected()) return false;
        }
        for (NetSysPort port : outputPorts) {
            if (!port.isConnected()) return false;
        }
        return true;
    }

    // This is no longer used for green status
    public void updateConnectionStatus() {
        // Only optional now, useful if you still want the beep system
        boolean current = isFullyConnected();
        if (current != isFullyConnected()) {
            if (current) playGreenBeep();
            else playRedBeep();
        }
    }

    public void markReceivedPacket() {
        if (!hasReceivedPacket) {
            hasReceivedPacket = true;
            playGreenBeep(); // play sound when first received
        }
    }

    public boolean hasReceivedPacket() {
        return hasReceivedPacket;
    }

    private void playGreenBeep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    private void playRedBeep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public void markReceived() {
        hasReceivedPacket = true;
    }
}
