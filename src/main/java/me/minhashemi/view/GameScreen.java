package me.minhashemi.view;

import me.minhashemi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

        // ✅ Initialize ports once at setup time
        for (Packet packet : levelData.packets) {
            packet.initializePorts();
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
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
            g2.drawLine(wire.start.x, wire.start.y, wire.end.x, wire.end.y);
        }

        if (draggingWire && wireStart != null && wireEnd != null) {
            g2.drawLine(wireStart.x, wireStart.y, wireEnd.x, wireEnd.y);
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

    static class Line {
        Point start;
        Point end;

        Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }
    }
}
