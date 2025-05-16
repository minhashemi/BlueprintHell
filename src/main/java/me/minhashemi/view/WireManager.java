package me.minhashemi.view;

import me.minhashemi.model.*;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;

public class WireManager {
    private final List<Wire> wires = new ArrayList<>();
    private Point wireStart = null;
    private Point wireEnd = null;
    private boolean draggingWire = false;
    private NetSysPort wireStartPort = null;
    private double lastTotalWireLength = 0;
    private final LevelData levelData;

    public WireManager(LevelData levelData) {
        this.levelData = levelData;
    }

    public void recalculateTotalWireLength() {
        lastTotalWireLength = 0;
        for (Wire wire : wires) {
            lastTotalWireLength += wire.getLength();
        }
    }

    public void addWire(Wire wire) {
        wires.add(wire);
        recalculateTotalWireLength();
    }

    public void removeWire(Wire wire) {
        wires.remove(wire);
        recalculateTotalWireLength();
    }

    public List<Wire> getWires() {
        return wires;
    }

    public void setWireStart(Point wireStart, NetSysPort wireStartPort) {
        this.wireStart = wireStart;
        this.wireStartPort = wireStartPort;
    }

    public void setWireEnd(Point wireEnd) {
        this.wireEnd = wireEnd;
    }

    public void setDraggingWire(boolean draggingWire) {
        this.draggingWire = draggingWire;
    }

    public Point getWireStart() {
        return wireStart;
    }

    public NetSysPort getWireStartPort() {
        return wireStartPort;
    }

    public Point getWireEnd() {
        return wireEnd;
    }

    public boolean isDraggingWire() {
        return draggingWire;
    }

    public double getLastTotalWireLength() {
        return lastTotalWireLength;
    }

    public void render(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.RED);
        g2.setStroke(new BasicStroke(2));

        for (Wire wire : wires) {
            drawBezierWire(g2, wire.getStart(), wire.getEnd());
        }

        if (draggingWire && wireStart != null && wireEnd != null) {
            drawBezierWire(g2, wireStart, wireEnd);
        }
    }

    public NetSysPort findNearbyOutputPort(Point mousePosition) {
        for (NetSys netsys : levelData.packets) {
            for (NetSysPort port : netsys.getOutputPorts()) {
                if (!port.isConnected() && isNearPort(mousePosition, port.getPosition())) {
                    return port;
                }
            }
        }
        return null;
    }

    public NetSysPort findNearbyInputPort(Point mousePosition) {
        for (NetSys netsys : levelData.packets) {
            for (NetSysPort port : netsys.getInputPorts()) {
                if (!port.isConnected() && isNearPort(mousePosition, port.getPosition())) {
                    return port;
                }
            }
        }
        return null;
    }

    public Wire findNearbyWire(Point p) {
        for (Wire wire : wires) {
            if (isPointNearLine(p, wire.getStart(), wire.getEnd(), Config.TOLERANCE)) {
                return wire;
            }
        }
        return null;
    }

    private boolean isNearPort(Point mousePosition, Point portPosition) {
        int distance = (int) mousePosition.distance(portPosition);
        return distance < Config.PORT_SIZE * 2;
    }

    private void drawBezierWire(Graphics2D g2, Point start, Point end) {
        int ctrlOffset = Math.abs(end.x - start.x) / 2;
        Point ctrl1 = new Point(start.x + ctrlOffset, start.y);
        Point ctrl2 = new Point(end.x - ctrlOffset, end.y);

        CubicCurve2D curve = new CubicCurve2D.Float(
                start.x, start.y,
                ctrl1.x, ctrl1.y,
                ctrl2.x, ctrl2.y,
                end.x, end.y
        );

        g2.draw(curve);
    }

    private boolean isPointNearLine(Point p, Point a, Point b, int tolerance) {
        double dist = ptLineDist(a.x, a.y, b.x, b.y, p.x, p.y);
        return dist < tolerance;
    }

    private double ptLineDist(double x1, double y1, double x2, double y2, double px, double py) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        if (dx == 0 && dy == 0) {
            dx = px - x1;
            dy = py - y1;
            return Math.sqrt(dx * dx + dy * dy);
        }

        double t = ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy);
        if (t < 0) {
            dx = px - x1;
            dy = py - y1;
        } else if (t > 1) {
            dx = px - x2;
            dy = py - y2;
        } else {
            double nearestX = x1 + t * dx;
            double nearestY = y1 + t * dy;
            dx = px - nearestX;
            dy = py - nearestY;
        }

        return Math.sqrt(dx * dx + dy * dy);
    }

    public static class Wire {
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

    public boolean isPortConnected(NetSysPort port) {
        for (Wire wire : wires) {
            if (wire.getFromPort() == port || wire.getToPort() == port) {
                return true;
            }
        }
        return false;
    }

}