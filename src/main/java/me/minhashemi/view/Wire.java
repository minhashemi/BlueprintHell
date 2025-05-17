package me.minhashemi.view;

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
        return calculateBezierLength(getStart(), getEnd());
    }

    public boolean hasPacket() {
        return hasPacket;
    }

    public void setHasPacket(boolean hasPacket) {
        this.hasPacket = hasPacket;
    }

    private double calculateBezierLength(Point start, Point end) {
        int segments = 20;
        double length = 0.0;

        int ctrlOffset = Math.abs(end.x - start.x) / 2;
        Point ctrl1 = new Point(start.x + ctrlOffset, start.y);
        Point ctrl2 = new Point(end.x - ctrlOffset, end.y);

        Point prev = start;
        for (int i = 1; i <= segments; i++) {
            double t = i / (double) segments;
            Point curr = evaluateBezier(start, ctrl1, ctrl2, end, t);
            length += prev.distance(curr);
            prev = curr;
        }

        return length;
    }

    private Point evaluateBezier(Point p0, Point c1, Point c2, Point p3, double t) {
        double x = Math.pow(1 - t, 3) * p0.x
                + 3 * Math.pow(1 - t, 2) * t * c1.x
                + 3 * (1 - t) * t * t * c2.x
                + t * t * t * p3.x;
        double y = Math.pow(1 - t, 3) * p0.y
                + 3 * Math.pow(1 - t, 2) * t * c1.y
                + 3 * (1 - t) * t * t * c2.y
                + t * t * t * p3.y;
        return new Point((int) x, (int) y);
    }

    public NetSysPort getFromPort() {
        return fromPort;
    }

    public NetSysPort getToPort() {
        return toPort;
    }
}