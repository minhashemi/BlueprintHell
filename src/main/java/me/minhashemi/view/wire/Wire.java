package me.minhashemi.view.wire;

import me.minhashemi.model.Config;
import me.minhashemi.model.block.NetSysPort;

import java.awt.*;

public class Wire {
    public final NetSysPort fromPort;
    public final NetSysPort toPort;
    private boolean hasPacket = false;

    public Wire(NetSysPort fromPort, NetSysPort toPort) {
        this.fromPort = fromPort;
        this.toPort = toPort;
    }

    public Point getStart() {
        Point p = fromPort.getPosition();
        return new Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
    }

    public Point getEnd() {
        Point p = toPort.getPosition();
        return new Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
    }

    public double getLength() {
        return getStart().distance(getEnd());
    }

    public boolean hasPacket() {
        return hasPacket;
    }

    public void setHasPacket(boolean hasPacket) {
        this.hasPacket = hasPacket;
    }

    public NetSysPort getFromPort() {
        return fromPort;
    }

    public NetSysPort getToPort() {
        return toPort;
    }
}
