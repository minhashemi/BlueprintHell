package me.minhashemi.view;

import me.minhashemi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.List;

public class GameScreen extends JPanel {
    private final LevelData levelData;
    private Point wireStart = null;
    private Point wireEnd = null;
    private boolean draggingWire = false;
    private PacketPort wireStartPort = null;

    private final List<Line> wires = new ArrayList<>();

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(null); // manually position packets

        // Initialize ports once at setup time
        for (Packet packet : levelData.packets) {
            packet.initializePorts();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Line nearby = findNearbyWire(e.getPoint());
                    if (nearby != null) {
                        for (Packet packet : levelData.packets) {
                            for (PacketPort port : packet.getInputPorts()) {
                                if (isNearPort(nearby.end, port.getPosition())) {
                                    port.setConnected(false);
                                }
                            }
                            for (PacketPort port : packet.getOutputPorts()) {
                                if (isNearPort(nearby.start, port.getPosition())) {
                                    port.setConnected(false);
                                }
                            }
                        }

                        wires.remove(nearby);
                        repaint();
                        return;
                    }
                }

                wireStartPort = findNearbyOutputPort(e.getPoint());
                if (wireStartPort != null && !wireStartPort.isConnected()) {
                    Point startPos = wireStartPort.getPosition();
                    wireStart = new Point(startPos.x + Config.PORT_SIZE / 2, startPos.y + Config.PORT_SIZE / 2);
                    wireEnd = wireStart;
                    draggingWire = true;
                }

                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                wireEnd = e.getPoint();
                draggingWire = false;

                if (wireStart != null && wireStartPort != null) {
                    PacketPort endPort = findNearbyInputPort(e.getPoint());

                    if (endPort != null && !wireStartPort.isConnected() && !endPort.isConnected()) {
                        Point endPos = endPort.getPosition();
                        wireEnd = new Point(endPos.x + Config.PORT_SIZE / 2, endPos.y + Config.PORT_SIZE / 2);
                        wires.add(new Line(wireStart, wireEnd));
                        wireStartPort.setConnected(true);
                        endPort.setConnected(true);
                    }
                }

                wireStart = null;
                wireStartPort = null;
                repaint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingWire) {
                    wireEnd = e.getPoint();
                    repaint();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Packet packet : levelData.packets) {
            drawPacket(g, packet);
        }

        g.setColor(Color.RED);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        for (Line wire : wires) {
            drawBezierWire(g2, wire.start, wire.end);
        }

        if (draggingWire && wireStart != null && wireEnd != null) {
            drawBezierWire(g2, wireStart, wireEnd);
        }
    }

    private void drawPacket(Graphics g, Packet packet) {
        int x = packet.position.x;
        int y = packet.position.y;
        int height = packet.getHeight();

        g.setColor(Color.GRAY);
        g.fillRect(x, y, Config.PACKET_WIDTH, height);

        for (PacketPort input : packet.getInputPorts()) {
            drawPort(g, input);
        }

        for (PacketPort output : packet.getOutputPorts()) {
            drawPort(g, output);
        }
    }

    private void drawPort(Graphics g, PacketPort port) {
        g.setColor(Color.BLACK);
        Point pos = port.getPosition();

        if (port.getType() == PortType.SQUARE) {
            g.fillRect(pos.x, pos.y, Config.PORT_SIZE, Config.PORT_SIZE);
        } else {
            int[] xs = {pos.x, pos.x + Config.PORT_SIZE / 2, pos.x + Config.PORT_SIZE};
            int[] ys = {pos.y + Config.PORT_SIZE, pos.y, pos.y + Config.PORT_SIZE};
            g.fillPolygon(xs, ys, 3);
        }
    }

    private PacketPort findNearbyOutputPort(Point mousePosition) {
        for (Packet packet : levelData.packets) {
            for (PacketPort port : packet.getOutputPorts()) {
                if (!port.isConnected() && isNearPort(mousePosition, port.getPosition())) {
                    return port;
                }
            }
        }
        return null;
    }

    private PacketPort findNearbyInputPort(Point mousePosition) {
        for (Packet packet : levelData.packets) {
            for (PacketPort port : packet.getInputPorts()) {
                if (!port.isConnected() && isNearPort(mousePosition, port.getPosition())) {
                    return port;
                }
            }
        }
        return null;
    }

    private boolean isNearPort(Point mousePosition, Point portPosition) {
        int distance = (int) mousePosition.distance(portPosition);
        return distance < Config.PORT_SIZE * 2;
    }

    // NEW: draw a smooth wire using a cubic Bézier curve
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

    static class Line {
        Point start;
        Point end;

        Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }
    }

    private Line findNearbyWire(Point p) {
        for (Line wire : wires) {
            if (isPointNearLine(p, wire.start, wire.end, Config.TOLERANCE)) {
                return wire;
            }
        }
        return null;
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
