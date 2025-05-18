package me.minhashemi.model.block;

import me.minhashemi.model.Config;
import me.minhashemi.model.MovingPacket;
import me.minhashemi.controller.audio.*;
import me.minhashemi.view.wire.Wire;
import me.minhashemi.view.wire.WireManager;

import java.awt.Point;
import java.util.*;

public class NetSys {
    public int packetId;
    public Point position;
    private List<PortType> inputs;
    private List<PortType> outputs;

    private final List<NetSysPort> inputPorts = new ArrayList<>();
    private final List<NetSysPort> outputPorts = new ArrayList<>();

    private Queue<MovingPacket> buff = new LinkedList<>();
    private Queue<PortType> storedPackets = new LinkedList<>();
    private boolean hasReceivedPacket = false;

    private final int MAX_STORAGE = 5;

    public boolean enqueuePacket(MovingPacket packet) {
        if (buff.size() < Config.MAX_BUFFER_SIZE) {
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

    public void updateConnectionStatus() {
        boolean current = isFullyConnected();
        if (current != isFullyConnected()) {
            if (current) playGreenBeep();
            else playRedBeep();
        }
    }

    public boolean hasReceivedPacket() {
        return hasReceivedPacket;
    }

    private void playGreenBeep() {
        player.playEffect("connect");
    }

    private void playRedBeep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public void markReceived() {
        if (!hasReceivedPacket) {
            hasReceivedPacket = true;
            playGreenBeep(); // play sound when first received
        }
    }

    public void tryToForwardPacket(WireManager wireManager, List<MovingPacket> movingPackets, PortType type) {
        NetSysPort compatibleFreePort = null;
        List<NetSysPort> otherFreePorts = new ArrayList<>();

        for (NetSysPort out : getOutputPorts()) {
            if (out.isConnected()) {
                for (Wire wire : wireManager.getWires()) {
                    if (wire.getFromPort() == out && !wire.hasPacket()) {
                        if (out.getType() == type) {
                            compatibleFreePort = out;
                            break;
                        } else {
                            otherFreePorts.add(out);
                        }
                    }
                }
            }
        }

        NetSysPort selected = null;
        if (compatibleFreePort != null) {
            selected = compatibleFreePort;
        } else if (!otherFreePorts.isEmpty()) {
            selected = otherFreePorts.get(new Random().nextInt(otherFreePorts.size()));
        }

        if (selected != null) {
            for (Wire wire : wireManager.getWires()) {
                if (wire.getFromPort() == selected && !wire.hasPacket()) {
                    wire.setHasPacket(true);
                    movingPackets.add(new MovingPacket(wire, type));
                    return;
                }
            }
        } else {
            if (storedPackets.size() < MAX_STORAGE) {
                storedPackets.add(type);
            }
        }
    }

    public void update(WireManager wireManager, List<MovingPacket> movingPackets) {
        if (storedPackets.isEmpty()) return;

        PortType type = storedPackets.peek();

        NetSysPort compatibleFreePort = null;
        List<NetSysPort> otherFreePorts = new ArrayList<>();

        for (NetSysPort out : getOutputPorts()) {
            if (out.isConnected()) {
                for (Wire wire : wireManager.getWires()) {
                    if (wire.getFromPort() == out && !wire.hasPacket()) {
                        if (out.getType() == type) {
                            compatibleFreePort = out;
                            break;
                        } else {
                            otherFreePorts.add(out);
                        }
                    }
                }
            }
        }

        NetSysPort selected = null;
        if (compatibleFreePort != null) {
            selected = compatibleFreePort;
        } else if (!otherFreePorts.isEmpty()) {
            selected = otherFreePorts.get(new Random().nextInt(otherFreePorts.size()));
        }

        if (selected != null) {
            for (Wire wire : wireManager.getWires()) {
                if (wire.getFromPort() == selected && !wire.hasPacket()) {
                    wire.setHasPacket(true);
                    movingPackets.add(new MovingPacket(wire, type));
                    storedPackets.poll(); // remove from queue
                    break;
                }
            }
        }
    }

    public void spawnInitialPackets(WireManager wireManager, List<MovingPacket> movingPackets) {
        if (!getInputPorts().isEmpty()) return;

        for (NetSysPort output : getOutputPorts()) {
            if (output.isConnected()) {
                for (Wire wire : wireManager.getWires()) {
                    if (wire.getFromPort() == output && !wire.hasPacket()) {
                        PortType[] types = PortType.values();
                        PortType randomType = types[new Random().nextInt(types.length)];
                        movingPackets.add(new MovingPacket(wire, randomType));
                        wire.setHasPacket(true);
                        markReceived();
                        return;
                    }
                }
            }
        }
    }
}
