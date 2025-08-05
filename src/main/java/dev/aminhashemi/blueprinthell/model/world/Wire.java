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
        POLYLINE,
        CURVED
    }

    private final Port startPort;
    private Port endPort;
    private final List<ArcPoint> arcPoints;
    private List<Point> cachedPath;
    private WireStyle style = WireStyle.CURVED;

    public Wire(Port startPort) {
        this.startPort = startPort;
        this.arcPoints = new ArrayList<>();
        this.cachedPath = new ArrayList<>();
        regeneratePath();
    }

    public void setStyle(WireStyle style) {
        this.style = style;
        regeneratePath();
    }

    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.PINK);

        if (!cachedPath.isEmpty()) {
            for (int i = 0; i < cachedPath.size() - 1; i++) {
                Point p1 = cachedPath.get(i);
                Point p2 = cachedPath.get(i + 1);
                g.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

        for (ArcPoint arc : arcPoints) {
            arc.draw(g);
        }
    }

    public void regeneratePath() {
        this.cachedPath.clear();
        List<Point> controlPoints = getControlPoints();
        if (controlPoints.size() < 2) return;

        if (style == WireStyle.POLYLINE) {
            this.cachedPath.addAll(controlPoints);
        } else {
            Path2D.Float path = buildCurvedPath(controlPoints);
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
        for (int i = 0; i < cachedPath.size() - 1; i++) {
            length += cachedPath.get(i).distance(cachedPath.get(i + 1));
        }
        return length;
    }

    public List<Point> getAllPoints() {
        return cachedPath;
    }

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
        regeneratePath();
    }

    // This is the method that was missing from the context of the error
    public Port getEndPort() {
        return endPort;
    }

    public void addArcPoint(Point point) {
        if (arcPoints.size() < 3) {
            arcPoints.add(new ArcPoint(point, this));
            regeneratePath();
        }
    }

    public List<ArcPoint> getArcPoints() {
        return arcPoints;
    }

    public Port getStartPort() {
        return startPort;
    }
}
