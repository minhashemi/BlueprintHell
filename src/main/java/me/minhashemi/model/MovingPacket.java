package me.minhashemi.model;

import me.minhashemi.view.WireManager;

import java.awt.*;
import java.awt.geom.Point2D;

public class MovingPacket {
    private final WireManager.Wire wire;
    private float t; // Bezier interpolation value [0..1]
    private float speed; // How fast to move along the wire
    private float noise;
    private boolean lost;
    private Point2D.Float position;

    public static final float NOISE_THRESHOLD = 100f;
    public static final float MAX_DISTANCE_FROM_WIRE = 20f;

    public MovingPacket(WireManager.Wire wire) {
        this.wire = wire;
        this.t = 0;
        this.speed = 0.01f; // You can scale this based on wire length
        this.noise = 0;
        this.lost = false;
        this.position = evaluateBezier(wire.getStart(), wire.getEnd(), t);
    }

    public void update() {
        if (lost || t >= 1f) return;

        t += speed;
        if (t > 1f) t = 1f;

        position = evaluateBezier(wire.getStart(), wire.getEnd(), t);

        // Noise logic (can be expanded with external forces)
        if (noise >= NOISE_THRESHOLD) {
            lost = true;
        }
    }

    public void draw(Graphics2D g) {
        if (lost) return;
        g.setColor(Color.CYAN);
        g.fillOval((int) position.x - 5, (int) position.y - 5, 10, 10);
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
}
