package me.minhashemi.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class NetSys {
    public int packetId;
    public Point position;
    private List<PortType> inputs;
    private List<PortType> outputs;

    private final List<NetSysPort> inputPorts = new ArrayList<>();
    private final List<NetSysPort> outputPorts = new ArrayList<>();

    private boolean lastConnectionStatus = false;

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

    // Initialize ports only once
    public void initializePorts() {
        inputPorts.clear();
        outputPorts.clear();

        // Initialize input ports on the left side of the network system
        for (int i = 0; i < inputs.size(); i++) {
            inputPorts.add(new NetSysPort(this, false, inputs.get(i), true, i));  // true for input, passing index
        }

        // Initialize output ports on the right side of the network system
        for (int i = 0; i < outputs.size(); i++) {
            outputPorts.add(new NetSysPort(this, false, outputs.get(i), false, i));  // false for output, passing index
        }

    }

    public void setPosition(Point newPosition) {
        this.position = newPosition;
    }

    public boolean isFullyConnected() {
        for (NetSysPort port : inputPorts) {
            if (!port.isConnected()) {
                return false;
            }
        }
        for (NetSysPort port : outputPorts) {
            if (!port.isConnected()) {
                return false;
            }
        }
        return true;
    }
    public boolean getLastConnectionStatus() {
        return lastConnectionStatus;
    }

    public void updateConnectionStatus() {
        boolean current = isFullyConnected();
        if (current != lastConnectionStatus) {
            lastConnectionStatus = current;

            // Play different beeps for red/green
            if (current) {
                playGreenBeep();
            } else {
                playRedBeep();
            }
        }
    }

    private void playGreenBeep() {
        // Different beep (could be a higher tone or just a placeholder)
        java.awt.Toolkit.getDefaultToolkit().beep();
//        System.out.println("Beep: GREEN (connected)");
    }

    private void playRedBeep() {
        // Different beep (could be a lower tone or same if just using default beep)
        java.awt.Toolkit.getDefaultToolkit().beep();
//        System.out.println("Beep: RED (disconnected)");
    }


}
