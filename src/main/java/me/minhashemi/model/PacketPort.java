package me.minhashemi.model;

import java.awt.Point;

public class PacketPort {
    private Point position;
    private PortType type;

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
}
