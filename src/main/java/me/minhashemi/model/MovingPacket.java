package me.minhashemi.model;

import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.block.PortType;
import me.minhashemi.view.Wire;
import me.minhashemi.view.WireManager;

import java.awt.*;
import java.awt.geom.Point2D;

public class MovingPacket {
    private final Wire wire;
    private final NetSys destinationNetSys;
    private final PortType shape;

    private float t; // Bezier interpolation value [0..1]
    private float speed; // How fast to move along the wire
    private float noise;
    private boolean lost;
    private Point2D.Float position;
    private boolean delivered = false;

    public static final float NOISE_THRESHOLD = 100f;
    public static final float MAX_DISTANCE_FROM_WIRE = 20f;

    public MovingPacket(Wire wire, PortType shape) {
        this.wire = wire;
        this.shape = shape;
        this.t = 0;
        this.speed = 0.01f;
        this.noise = 0;
        this.lost = false;
        this.position = evaluateBezier(wire.getStart(), wire.getEnd(), t);

        NetSysPort endPort = wire.getToPort();
        this.destinationNetSys = endPort != null ? endPort.getParent() : null;
    }


    public void update() {
        if (lost || t >= 1f) return;

        // Adjust speed based on shape
        NetSysPort fromPort = wire.getFromPort();
        if (fromPort != null) {
            PortType type = fromPort.getType();
            if (type == PortType.SQUARE) {
                speed = 0.02f;
            } else if (type == PortType.TRIANGLE) {
                speed = 0.03f;
            }
        }

        t += speed;
        if (t > 1f) t = 1f;

        position = evaluateBezier(wire.getStart(), wire.getEnd(), t);

        if (t >= 1f && !lost && !delivered && destinationNetSys != null) {
            delivered = true;
            wire.setHasPacket(false);
            destinationNetSys.markReceived();
        }

        if (noise >= NOISE_THRESHOLD && !lost) {
            lost = true;
            wire.setHasPacket(false);
        }
    }

    public void draw(Graphics2D g) {
        if (lost) return;

        g.setColor(Color.CYAN);
        int x = (int) position.x;
        int y = (int) position.y;
        int size = 10;

        switch (shape) {
            case SQUARE:
                g.fillRect(x - size / 2, y - size / 2, size, size);
                break;
            case TRIANGLE:
                Polygon triangle = new Polygon();
                triangle.addPoint(x, y - size / 2); // top
                triangle.addPoint(x - size / 2, y + size / 2); // bottom left
                triangle.addPoint(x + size / 2, y + size / 2); // bottom right
                g.fillPolygon(triangle);
                break;
            default:
                // Fallback to circle
                g.fillOval(x - size / 2, y - size / 2, size, size);
                break;
        }
    }

    public void applyImpact(Point2D.Float forceVector, float distanceFromImpact) {
        float attenuation = 1.0f - Math.min(1.0f, distanceFromImpact / 100f);
        noise += 10f * attenuation;
    }

    public boolean isArrived() {
        return t >= 1f;
    }

    public boolean isLost() {
        return lost;
    }

    public Point2D.Float getPosition() {
        return position;
    }

    private Point2D.Float evaluateBezier(Point start, Point end, float t) {
        int ctrlOffset = Math.abs(end.x - start.x) / 2;
        Point ctrl1 = new Point(start.x + ctrlOffset, start.y);
        Point ctrl2 = new Point(end.x - ctrlOffset, end.y);

        float x = (float) (Math.pow(1 - t, 3) * start.x +
                3 * Math.pow(1 - t, 2) * t * ctrl1.x +
                3 * (1 - t) * t * t * ctrl2.x +
                t * t * t * end.x);

        float y = (float) (Math.pow(1 - t, 3) * start.y +
                3 * Math.pow(1 - t, 2) * t * ctrl1.y +
                3 * (1 - t) * t * t * ctrl2.y +
                t * t * t * end.y);

        return new Point2D.Float(x, y);
    }

    public Wire getWire() {
        return wire;
    }
}
