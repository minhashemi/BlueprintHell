package me.minhashemi.model;

import java.awt.Point;

public class PacketPort {
    private final Point position;
    private final PortType type;
    private boolean connected = false;

    public PacketPort(Point position, PortType type) {
        this.position = position;
        this.type = type;
    }

    public Point getPosition() {
        return position;
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
}
