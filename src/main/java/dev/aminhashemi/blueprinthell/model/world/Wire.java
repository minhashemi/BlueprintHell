package dev.aminhashemi.blueprinthell.model.world;

import dev.aminhashemi.blueprinthell.model.entities.systems.Port;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Wire {

    private final Port startPort;
    private Port endPort;
    private final List<ArcPoint> arcPoints;
    private List<Point> flattenedPath; // To store the detailed path for packet movement

    public Wire(Port startPort) {
        this.startPort = startPort;
        this.arcPoints = new ArrayList<>();
        this.flattenedPath = new ArrayList<>();
        regeneratePath();
    }

    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.PINK);

        // This method builds the smooth visual curve
        Path2D.Float path = buildPath();
        if (path != null) {
            g.draw(path);
        }

        // Draw the draggable arc points on top
        for (ArcPoint arc : arcPoints) {
            arc.draw(g);
        }
    }

    /**
     * Regenerates the detailed, flattened path whenever the wire's shape changes.
     */
    public void regeneratePath() {
        this.flattenedPath.clear();
        Path2D.Float path = buildPath();
        if (path == null) return;

        // The PathIterator "walks" along the curve. The 0.5 flatness value determines
        // how many points are generated. A smaller number means a more detailed path.
        PathIterator pi = path.getPathIterator(null, 0.5);
        double[] coords = new double[6];
        while (!pi.isDone()) {
            pi.currentSegment(coords);
            flattenedPath.add(new Point((int) coords[0], (int) coords[1]));
            pi.next();
        }
    }

    /**
     * Builds the smooth, drawable Path2D object from the control points.
     * @return A drawable Path2D.Float object.
     */
    private Path2D.Float buildPath() {
        List<Point> allControlPoints = new ArrayList<>();
        allControlPoints.add(startPort.getCenter());
        allControlPoints.addAll(arcPoints.stream().map(ArcPoint::getPosition).collect(Collectors.toList()));
        if (endPort == null) return null; // Can't build a full path until the wire is complete
        allControlPoints.add(endPort.getCenter());

        if (allControlPoints.size() < 2) return null;

        Path2D.Float path = new Path2D.Float();
        path.moveTo(allControlPoints.get(0).x, allControlPoints.get(0).y);

        if (allControlPoints.size() == 2) {
            path.lineTo(allControlPoints.get(1).x, allControlPoints.get(1).y);
        } else {
            for (int i = 1; i < allControlPoints.size() - 1; i++) {
                Point p2 = allControlPoints.get(i);
                Point p3 = allControlPoints.get(i + 1);
                Point midPoint = new Point((p2.x + p3.x) / 2, (p2.y + p3.y) / 2);
                path.quadTo(p2.x, p2.y, midPoint.x, midPoint.y);
            }
            path.lineTo(allControlPoints.get(allControlPoints.size() - 1).x, allControlPoints.get(allControlPoints.size() - 1).y);
        }
        return path;
    }

    public double calculateLength() {
        double length = 0;
        List<Point> points = getAllPoints(); // This now correctly uses the flattened path
        for (int i = 0; i < points.size() - 1; i++) {
            length += points.get(i).distance(points.get(i + 1));
        }
        return length;
    }

    /**
     * Returns the detailed, flattened path for the MovingPacket to follow.
     * @return A list of points representing the curve.
     */
    public List<Point> getAllPoints() {
        return flattenedPath;
    }

    public void setEndPort(Port endPort) {
        this.endPort = endPort;
        regeneratePath(); // Update path when wire is completed
    }

    public void addArcPoint(Point point) {
        if (arcPoints.size() < 3) {
            arcPoints.add(new ArcPoint(point, this)); // Pass the parent wire
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
