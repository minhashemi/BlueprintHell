package dev.aminhashemi.blueprinthell.model.world;

import dev.aminhashemi.blueprinthell.model.entities.systems.Port;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Wire {

    private final Port startPort;
    private Port endPort;
    private final List<ArcPoint> arcPoints;

    public Wire(Port startPort) {
        this.startPort = startPort;
        this.arcPoints = new ArrayList<>();
    }

    public void draw(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2));
        g.setColor(Color.PINK);

        List<Point> allPoints = getAllPoints();

        // --- FIX: Draw a continuous path of quadratic Bezier curves ---
        if (allPoints.size() > 1) {
            Path2D.Float path = new Path2D.Float();
            path.moveTo(allPoints.get(0).x, allPoints.get(0).y);

            if (allPoints.size() == 2) {
                // If it's just a start and end point, draw a straight line
                path.lineTo(allPoints.get(1).x, allPoints.get(1).y);
            } else {
                // For 3 or more points, create a smooth curve through them
                for (int i = 1; i < allPoints.size() - 1; i++) {
                    Point p1 = allPoints.get(i - 1);
                    Point p2 = allPoints.get(i);
                    Point p3 = allPoints.get(i + 1);

                    // Use the midpoint as the control point for a quadratic curve
                    // This creates a smooth transition between segments
                    Point midPoint1 = new Point((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
                    Point midPoint2 = new Point((p2.x + p3.x) / 2, (p2.y + p3.y) / 2);

                    path.quadTo(p2.x, p2.y, midPoint2.x, midPoint2.y);
                }
                // Finally, draw a line to the very last point
                path.lineTo(allPoints.get(allPoints.size() - 1).x, allPoints.get(allPoints.size() - 1).y);
            }
            g.draw(path);
        }


        // Draw the arc points on top of the wire
        for (ArcPoint arc : arcPoints) {
            arc.draw(g);
        }
    }

    public double calculateLength() {
        double length = 0;
        List<Point> allPoints = getAllPoints();
        for (int i = 0; i < allPoints.size() - 1; i++) {
            length += allPoints.get(i).distance(allPoints.get(i + 1));
        }
        return length;
    }

    public List<Point> getAllPoints() {
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
    }

    public void addArcPoint(Point point) {
        if (arcPoints.size() < 3) {
            arcPoints.add(new ArcPoint(point));
        }
    }

    public List<ArcPoint> getArcPoints() {
        return arcPoints;
    }

    public Port getStartPort() {
        return startPort;
    }
}
