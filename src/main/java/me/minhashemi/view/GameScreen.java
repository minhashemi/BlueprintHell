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

    private final List<Wire> wires = new ArrayList<>();

    private Packet selectedPacket = null;
    private Point dragOffset = null;

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(null); // manually position packets

        for (Packet packet : levelData.packets) {
            packet.initializePorts();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    Wire nearby = findNearbyWire(e.getPoint());
                    if (nearby != null) {
                        nearby.fromPort.setConnected(false);
                        nearby.toPort.setConnected(false);
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
                    repaint();
                    return;
                }

                for (Packet packet : levelData.packets) {
                    Rectangle bounds = new Rectangle(packet.position.x, packet.position.y, Config.PACKET_WIDTH, packet.getHeight());
                    if (bounds.contains(e.getPoint())) {
                        selectedPacket = packet;
                        dragOffset = new Point(e.getX() - packet.position.x, e.getY() - packet.position.y);
                        return;
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggingWire) {
                    wireEnd = e.getPoint();
                    draggingWire = false;

                    if (wireStart != null && wireStartPort != null) {
                        PacketPort endPort = findNearbyInputPort(e.getPoint());

                        if (endPort != null && !wireStartPort.isConnected() && !endPort.isConnected()) {
                            wireStartPort.setConnected(true);
                            endPort.setConnected(true);
                            wires.add(new Wire(wireStartPort, endPort));
                        }
                    }

                    wireStart = null;
                    wireStartPort = null;
                    repaint();
                }

                selectedPacket = null;
                dragOffset = null;
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingWire) {
                    wireEnd = e.getPoint();
                    repaint();
                } else if (selectedPacket != null && dragOffset != null) {
                    selectedPacket.setPosition(new Point(e.getX() - dragOffset.x, e.getY() - dragOffset.y));
                    selectedPacket.initializePorts(); // update port positions
                    repaint(); // ✅ Ensures wires repaint with updated port positions
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

        for (Wire wire : wires) {
            drawBezierWire(g2, wire.getStart(), wire.getEnd());
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

    private Wire findNearbyWire(Point p) {
        for (Wire wire : wires) {
            if (isPointNearLine(p, wire.getStart(), wire.getEnd(), Config.TOLERANCE)) {
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

    // ✅ Wire references ports and gets positions live
    static class Wire {
        PacketPort fromPort;
        PacketPort toPort;

        Wire(PacketPort fromPort, PacketPort toPort) {
            this.fromPort = fromPort;
            this.toPort = toPort;
        }

        Point getStart() {
            Point p = fromPort.getPosition();
            return new Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
        }

        Point getEnd() {
            Point p = toPort.getPosition();
            return new Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
        }
    }
}
