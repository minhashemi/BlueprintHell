package dev.aminhashemi.blueprinthell.model.world;

import dev.aminhashemi.blueprinthell.model.entities.systems.Port;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Wire {

    public enum WireStyle {
        POLYLINE, // Straight lines connecting arc points
        CURVED    // Smooth Bezier curve using arc points as guides
    }

    private final Port startPort;
    private Port endPort;
    private final List<ArcPoint> arcPoints;
    private List<Point> cachedPath; // To store the detailed path for packet movement and length calculation
    private WireStyle style = WireStyle.CURVED; // Default to curved, can be changed

    public Wire(Port startPort) {
        this.startPort = startPort;
        this.arcPoints = new ArrayList<>();
        this.cachedPath = new ArrayList<>();
        regeneratePath();
    }

    public void setStyle(WireStyle style) {
        this.style = style;
        regeneratePath(); // The path needs to be recalculated when the style changes
    }

    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.PINK);

        // The drawing logic is now handled by the regenerated path
        if (!cachedPath.isEmpty()) {
            for (int i = 0; i < cachedPath.size() - 1; i++) {
                Point p1 = cachedPath.get(i);
                Point p2 = cachedPath.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        // Draw the arc points on top
        for (ArcPoint arc : arcPoints) {
            arc.draw(g);
        }
    }

    /**
     * Regenerates the cached path based on the current style.
     * This is the core of the fix.
     */
    public void regeneratePath() {
        this.cachedPath.clear();
        List<Point> controlPoints = getControlPoints();
        if (controlPoints.size() < 2) return;

        if (style == WireStyle.POLYLINE) {
            // For polylines, the path is simply the control points
            this.cachedPath.addAll(controlPoints);
        } else {
            // For curves, we build a detailed path by flattening a Bezier curve
            Path2D.Float path = buildCurvedPath(controlPoints);
            // The PathIterator "walks" along the curve. A smaller flatness value (e.g., 0.5)
            // creates a more detailed, smoother path for packets to follow.
            PathIterator pi = path.getPathIterator(null, 0.5);
            double[] coords = new double[6];
            while (!pi.isDone()) {
                pi.currentSegment(coords);
                cachedPath.add(new Point((int) coords[0], (int) coords[1]));
                pi.next();
            }
        }
    }

    private Path2D.Float buildCurvedPath(List<Point> controlPoints) {
        Path2D.Float path = new Path2D.Float();
        path.moveTo(controlPoints.get(0).x, controlPoints.get(0).y);

        if (controlPoints.size() == 2) {
            path.lineTo(controlPoints.get(1).x, controlPoints.get(1).y);
        } else {
            for (int i = 1; i < controlPoints.size() - 1; i++) {
                Point p2 = controlPoints.get(i);
                Point p3 = controlPoints.get(i + 1);
                Point midPoint = new Point((p2.x + p3.x) / 2, (p2.y + p3.y) / 2);
                path.quadTo(p2.x, p2.y, midPoint.x, midPoint.y);
            }
            path.lineTo(controlPoints.get(controlPoints.size() - 1).x, controlPoints.get(controlPoints.size() - 1).y);
        }
        return path;
    }

    public double calculateLength() {
        double length = 0;
        // The length is now always calculated from the consistent cached path
        for (int i = 0; i < cachedPath.size() - 1; i++) {
            length += cachedPath.get(i).distance(cachedPath.get(i + 1));
        }
        return length;
    }

    /**
     * Returns the detailed, cached path for the MovingPacket to follow.
     * This works for both straight and curved styles.
     */
    public List<Point> getAllPoints() {
        return cachedPath;
    }

    /**
     * Helper method to get the primary control points (start, arcs, end).
     */
    private List<Point> getControlPoints() {
        List<Point> points = new ArrayList<>();
        points.add(startPort.getCenter());
        points.addAll(arcPoints.stream().map(ArcPoint::getPosition).collect(Collectors.toList()));
        if (endPort != null) {
            points.add(endPort.getCenter());
        }
        return points;
    }

    public void setEndPort(Port endPort) {
        this.endPort = endPort;
        regeneratePath(); // Update path when wire is completed
    }

    public void addArcPoint(Point point) {
        if (arcPoints.size() < 3) {
            arcPoints.add(new ArcPoint(point, this));
            regeneratePath(); // Update path when an arc is added
        }
    }

    public List<ArcPoint> getArcPoints() {
        return arcPoints;
    }

    public Port getStartPort() {
        return startPort;
    }
}
