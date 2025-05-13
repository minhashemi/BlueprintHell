package me.minhashemi.model;

import java.awt.Point;

public class NetSysPort {
    private final PortType type;
    private boolean connected = false;
    private final boolean isInput;  // Track whether the port is input or output
    private final NetSys netsys;    // reference to netsys
    private final int index;        // port index within netsys

    // Constructor: Accepting netsys, connection status, PortType, and isInput
    public NetSysPort(NetSys netsys, boolean connected, PortType type, boolean isInput, int index) {
        this.netsys = netsys;
        this.connected = connected;
        this.type = type;
        this.isInput = isInput;
        this.index = index;
    }

    public Point getPosition() {
        int x = netsys.position.x;
        int y = netsys.position.y;

        // Calculate the Y position considering margins and spacing
        int portY = y + Config.PORT_MARGIN + index * (Config.PORT_SIZE + Config.PORT_SPACING);
        // Calculate the X position depending on whether it's an input or output port
        int portX = isInput ? x - Config.PORT_SIZE : x + Config.NETSYS_WIDTH;

        return new Point(portX, portY);
    }

    public PortType getType() {
        return type;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    // New method to help in determining if the port is an input or output
    public boolean isInput() {
        return isInput;
    }
}
