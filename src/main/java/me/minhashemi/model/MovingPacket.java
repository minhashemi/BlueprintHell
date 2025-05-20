package me.minhashemi.model;

import me.minhashemi.controller.audio.player;
import me.minhashemi.model.block.NetSys;
import me.minhashemi.model.block.NetSysPort;
import me.minhashemi.model.block.PortType;
import me.minhashemi.view.wire.Wire;

import java.awt.*;
import java.awt.geom.Point2D;

public class MovingPacket {
    private final Wire wire;
    private final NetSys destinationNetSys;
    private final PortType shape;

    private float t; // Interpolation value [0..1]
    private float speed; // How fast to move along the wire
    private float noise;
    private boolean lost;
    private Point2D.Float position;
    private Point2D.Float forceVector; // Force from impact wave
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
        this.position = evaluateLinear(wire.getStart(), wire.getEnd(), t);
        this.forceVector = new Point2D.Float(0, 0);

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

        // Calculate base position along wire
        Point2D.Float basePosition = evaluateLinear(wire.getStart(), wire.getEnd(), t);

        // Apply force vector to position (smoothly)
        float forceDecay = 0.9f; // Decay force over time for smooth effect
        position.x = basePosition.x + forceVector.x;
        position.y = basePosition.y + forceVector.y;
        forceVector.x *= forceDecay;
        forceVector.y *= forceDecay;

        // Check deviation from wire
        float distance = (float) pointToLineDistance(position, wire.getStart(), wire.getEnd());
        if (distance > MAX_DISTANCE_FROM_WIRE && !lost) {
            lost = true;
            wire.setHasPacket(false);
        }

        // Check noise threshold
        if (noise >= NOISE_THRESHOLD * 0.5f && !lost) {
            lost = true;
            wire.setHasPacket(false);
        }

        if (t >= 1f && !lost && !delivered && destinationNetSys != null) {
            delivered = true;
            wire.setHasPacket(false);
            destinationNetSys.markReceived();
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

    public void applyImpact(Point impactPoint) {
        // Calculate distance from packet's position to the impact point
        float distance = (float) position.distance(impactPoint.x, impactPoint.y);
        // Apply noise based on distance (attenuates with distance)
        float attenuation = 1.0f - Math.min(1.0f, distance / 100f);
        increaseNoise(20f * attenuation); // Increased noise for stronger effect

        // Calculate force vector (direction away from impact point)
        if (distance > 0) {
            float forceMagnitude = 10f * attenuation; // Stronger force closer to impact
            float dx = position.x - impactPoint.x;
            float dy = position.y - impactPoint.y;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            forceVector.x += (dx / length) * forceMagnitude;
            forceVector.y += (dy / length) * forceMagnitude;
        }
    }

    public void increaseNoise(float amount) {
        this.noise += amount;
        if (this.noise < 0) this.noise = 0; // Prevent negative noise
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

    public Shape getPath() {
        int size = 10;
        switch (shape) {
            case SQUARE:
                return new Rectangle((int) position.x - size / 2, (int) position.y - size / 2, size, size);
            case TRIANGLE:
                Polygon triangle = new Polygon();
                triangle.addPoint((int) position.x, (int) position.y - size / 2); // top
                triangle.addPoint((int) position.x - size / 2, (int) position.y + size / 2); // bottom left
                triangle.addPoint((int) position.x + size / 2, (int) position.y + size / 2); // bottom right
                return triangle;
            default:
                return new Rectangle((int) position.x - size / 2, (int) position.y - size / 2, size, size);
        }
    }

    private Point2D.Float evaluateLinear(Point start, Point end, float t) {
        float x = (1 - t) * start.x + t * end.x;
        float y = (1 - t) * start.y + t * end.y;
        return new Point2D.Float(x, y);
    }

    private float pointToLineDistance(Point2D.Float p, Point lineStart, Point lineEnd) {
        float px = p.x - lineStart.x;
        float py = p.y - lineStart.y;
        float vx = lineEnd.x - lineStart.x;
        float vy = lineEnd.y - lineStart.y;

        float lengthSquared = vx * vx + vy * vy;
        if (lengthSquared == 0) return (float) p.distance(lineStart.x, lineStart.y);

        float t = Math.max(0, Math.min(1, (px * vx + py * vy) / lengthSquared));
        float projX = lineStart.x + t * vx;
        float projY = lineStart.y + t * vy;

        return (float) p.distance(projX, projY);
    }

    public Wire getWire() {
        return wire;
    }

    public PortType getType() {
        return shape;
    }

    public void setNoise(int n) {
        this.noise = n;
    }

    public float getNoise() {
        return noise;
    }
}