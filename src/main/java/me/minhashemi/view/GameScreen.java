package me.minhashemi.view;

import me.minhashemi.model.*;
import me.minhashemi.model.Config;

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

    // List to store the wires drawn between ports
    private final List<Line> wires = new ArrayList<>();

    public GameScreen(LevelData levelData) {
        this.levelData = levelData;
        setLayout(null); // manually position packets

        // Add mouse listeners for drawing wires
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Check if mouse is near any port (input or output)
                wireStartPort = findNearbyPort(e.getPoint());
                if (wireStartPort != null) {
                    Point pos = wireStartPort.getPosition();
                    wireStart = new Point(pos.x + Config.PORT_SIZE / 2, pos.y + Config.PORT_SIZE / 2);
                    wireEnd = wireStart;
                    draggingWire = true;
                }
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                wireEnd = e.getPoint();
                draggingWire = false;

                // If a wire was drawn, add it to the list of wires
                if (wireStart != null && wireStartPort != null) {
                    PacketPort endPort = findNearbyPort(e.getPoint());
                    if (endPort != null) {
                        Point endPos = endPort.getPosition();
                        wireEnd = new Point(endPos.x + Config.PORT_SIZE / 2, endPos.y + Config.PORT_SIZE / 2);
                        wires.add(new Line(wireStart, wireEnd));
                    }
                }

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

        // Draw packets
        for (Packet packet : levelData.packets) {
            drawPacket(g, packet);
        }

        // Draw all wires from the list
        g.setColor(Color.RED);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));

        for (Line wire : wires) {
            g2.drawLine(wire.start.x, wire.start.y, wire.end.x, wire.end.y);
        }

        // Draw temporary wire if dragging
        if (draggingWire && wireStart != null && wireEnd != null) {
            g2.drawLine(wireStart.x, wireStart.y, wireEnd.x, wireEnd.y);
        }
    }

    private void drawPacket(Graphics g, Packet packet) {
        int maxPorts = Math.max(packet.getInputs().size(), packet.getOutputs().size());
        int height = maxPorts * Config.STANDARD_HEIGHT;

        int x = packet.position.x;
        int y = packet.position.y;

        g.setColor(Color.GRAY);
        g.fillRect(x, y, Config.PACKET_WIDTH, height);

        // Draw ports (inputs on the left, outputs on the right)
        for (int i = 0; i < packet.getInputs().size(); i++) {
            drawPort(g, packet.getInputs().get(i), x - Config.PORT_SIZE, y + i * Config.STANDARD_HEIGHT + (Config.STANDARD_HEIGHT - Config.PORT_SIZE) / 2, packet, true);
        }

        for (int i = 0; i < packet.getOutputs().size(); i++) {
            drawPort(g, packet.getOutputs().get(i), x + Config.PACKET_WIDTH, y + i * Config.STANDARD_HEIGHT + (Config.STANDARD_HEIGHT - Config.PORT_SIZE) / 2, packet, false);
        }
    }

    private void drawPort(Graphics g, PortType type, int x, int y, Packet packet, boolean isInput) {
        g.setColor(Color.BLACK);
        if (type == PortType.SQUARE) {
            g.fillRect(x, y, Config.PORT_SIZE, Config.PORT_SIZE);
        } else {
            int[] xs = {x, x + Config.PORT_SIZE / 2, x + Config.PORT_SIZE};
            int[] ys = {y + Config.PORT_SIZE, y, y + Config.PORT_SIZE};
            g.fillPolygon(xs, ys, 3);
        }

        // Attach the port position to the packet's ports
        if (isInput) {
            packet.addInputPort(new PacketPort(new Point(x, y), type)); // Add input port for this packet
        } else {
            packet.addOutputPort(new PacketPort(new Point(x, y), type)); // Add output port for this packet
        }
    }

    private PacketPort findNearbyPort(Point mousePosition) {
        for (Packet packet : levelData.packets) {
            // Check if mouse is near any input port
            for (PacketPort port : packet.getInputPorts()) {
                if (isNearPort(mousePosition, port.getPosition())) {
                    return port;
                }
            }

            // Check if mouse is near any output port
            for (PacketPort port : packet.getOutputPorts()) {
                if (isNearPort(mousePosition, port.getPosition())) {
                    return port;
                }
            }

        }
        return null; // No nearby port found
    }

    private boolean isNearPort(Point mousePosition, Point portPosition) {
        int distance = (int) mousePosition.distance(portPosition);
        return distance < Config.PORT_SIZE * 2; // Define a "near" threshold (2x port size)
    }

    // Helper class to represent a wire (line) between two points
    static class Line {
        Point start;
        Point end;

        Line(Point start, Point end) {
            this.start = start;
            this.end = end;
        }
    }
}
