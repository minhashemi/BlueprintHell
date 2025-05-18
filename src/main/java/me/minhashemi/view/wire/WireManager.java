package me.minhashemi.view.wire;

import me.minhashemi.model.*;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.level.LevelData;

import java.awt.*;
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
            drawStraightWire(g2, wire.getStart(), wire.getEnd());
        }

        if (draggingWire && wireStart != null && wireEnd != null) {
            drawStraightWire(g2, wireStart, wireEnd);
        }
    }

    private void drawStraightWire(Graphics2D g2, Point start, Point end) {
        g2.drawLine(start.x, start.y, end.x, end.y);
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
}
