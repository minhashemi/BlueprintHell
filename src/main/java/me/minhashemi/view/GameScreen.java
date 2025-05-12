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
    private double lastTotalWireLength = 0;

    private Packet selectedPacket = null;
    private Point dragOffset = null;

    private int temporalProgress = 0;
    private int packetLoss = 0;
    private int coins = 0;

    private String calloutMessage = null;
    private long calloutTimestamp = 0;
    private static final int CALLOUT_DURATION = 3000; // ms

    private void recalculateTotalWireLength() {
        lastTotalWireLength = 0;
        for (Wire wire : wires) {
            lastTotalWireLength += wire.getLength();
        }
    }

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(null);

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
                        recalculateTotalWireLength();
                        Config.remainingWireLength += nearby.getLength();
                        updateHUD();
                        showCallout("🗑️ Wire removed. Refunded: " + (int) nearby.getLength());
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

                        if (endPort != null
                                && !wireStartPort.isConnected()
                                && !endPort.isConnected()
                                && wireStartPort.isInput() != endPort.isInput()
                                && wireStartPort.getType() == endPort.getType()) {

                            PacketPort from = wireStartPort.isInput() ? endPort : wireStartPort;
                            PacketPort to = wireStartPort.isInput() ? wireStartPort : endPort;

                            Wire wire = new Wire(from, to);
                            if (wire.getLength() <= Config.remainingWireLength) {
                                wires.add(wire);
                                recalculateTotalWireLength();
                                from.setConnected(true);
                                to.setConnected(true);
                                Config.remainingWireLength -= wire.getLength();
                                coins += 1;
                                updateHUD();
                                showCallout("✅ Wire connected. Remaining: " + (int) Config.remainingWireLength);
                            } else {
                                showCallout("❌ Not enough wire! Needed: " + (int) wire.getLength());
                            }
                        } else {
                            showCallout("❌ Ports must be opposite directions and same type.");
                        }
                    }

                    wireStart = null;
                    wireStartPort = null;
                    repaint();
                }

                if (selectedPacket != null) {
                    double newTotalLength = 0;
                    for (Wire wire : wires) {
                        newTotalLength += wire.getLength();
                    }

                    double delta = newTotalLength - lastTotalWireLength;
                    Config.remainingWireLength -= delta;
                    lastTotalWireLength = newTotalLength;

                    updateHUD();
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
                    selectedPacket.initializePorts();
                    repaint();
                }
            }
        });

        Timer calloutTimer = new Timer(100, e -> {
            if (calloutMessage != null && System.currentTimeMillis() - calloutTimestamp > CALLOUT_DURATION) {
                calloutMessage = null;
                repaint();
            }
        });
        calloutTimer.start();

        updateHUD();
    }

    private void updateHUD() {
        // Logic only. Drawing happens in paintComponent
    }

    private void showCallout(String message) {
        this.calloutMessage = message;
        this.calloutTimestamp = System.currentTimeMillis();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // HUD
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int hudY = 20;
        g.drawString("Remaining Wire Length: " + (int) Config.remainingWireLength, 10, hudY);
        g.drawString("Temporal Progress: " + temporalProgress, 250, hudY);
        g.drawString("Packet Loss: " + packetLoss, 450, hudY);
        g.drawString("Coins: " + coins, 600, hudY);

        if (calloutMessage != null && System.currentTimeMillis() - calloutTimestamp < CALLOUT_DURATION) {
            int boxWidth = g.getFontMetrics().stringWidth(calloutMessage) + 20;
            int boxHeight = 30;
            int x = (getWidth() - boxWidth) / 2;
            int y = 40;
            g.setColor(new Color(255, 220, 220));
            g.fillRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            g.setColor(Color.RED);
            g.drawRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            g.drawString(calloutMessage, x + 10, y + 20);
        }

        for (Packet packet : levelData.packets) {
            drawPacket(g, packet);
        }

        g.setColor(Color.RED);
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

    public static class Wire {
        public final PacketPort fromPort;
        public final PacketPort toPort;

        public Wire(PacketPort fromPort, PacketPort toPort) {
            this.fromPort = fromPort;
            this.toPort = toPort;
        }

        public Point getStart() {
            Point p = fromPort.getPosition();
            return new Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
        }

        public Point getEnd() {
            Point p = toPort.getPosition();
            return new Point(p.x + Config.PORT_SIZE / 2, p.y + Config.PORT_SIZE / 2);
        }

        public double getLength() {
            return calculateBezierLength(getStart(), getEnd());
        }

        private double calculateBezierLength(Point start, Point end) {
            int segments = 20;
            double length = 0.0;

            int ctrlOffset = Math.abs(end.x - start.x) / 2;
            Point ctrl1 = new Point(start.x + ctrlOffset, start.y);
            Point ctrl2 = new Point(end.x - ctrlOffset, end.y);

            Point prev = start;
            for (int i = 1; i <= segments; i++) {
                double t = i / (double) segments;
                Point curr = evaluateBezier(start, ctrl1, ctrl2, end, t);
                length += prev.distance(curr);
                prev = curr;
            }

            return length;
        }

        private Point evaluateBezier(Point p0, Point c1, Point c2, Point p3, double t) {
            double x = Math.pow(1 - t, 3) * p0.x
                    + 3 * Math.pow(1 - t, 2) * t * c1.x
                    + 3 * (1 - t) * t * t * c2.x
                    + t * t * t * p3.x;
            double y = Math.pow(1 - t, 3) * p0.y
                    + 3 * Math.pow(1 - t, 2) * t * c1.y
                    + 3 * (1 - t) * t * t * c2.y
                    + t * t * t * p3.y;
            return new Point((int) x, (int) y);
        }
    }
}
