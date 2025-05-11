package me.minhashemi.model;

public class Wire {
    public final PacketPort fromPort;
    public final PacketPort toPort;

    public Wire(PacketPort fromPort, PacketPort toPort) {
        this.fromPort = fromPort;
        this.toPort = toPort;
    }

    public java.awt.Point getStart() {
        java.awt.Point p = fromPort.getPosition();
        return new java.awt.Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
    }

    public java.awt.Point getEnd() {
        java.awt.Point p = toPort.getPosition();
        return new java.awt.Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
    }
}
