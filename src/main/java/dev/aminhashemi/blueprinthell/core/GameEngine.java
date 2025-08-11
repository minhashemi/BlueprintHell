package dev.aminhashemi.blueprinthell.core;

import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.systems.Port;
import dev.aminhashemi.blueprinthell.model.entities.systems.ReferenceSystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.ArcPoint;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.utils.LevelLoader;
import dev.aminhashemi.blueprinthell.view.GamePanel;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GameEngine implements Runnable {

    // --- All necessary fields are now declared here ---
    private final GamePanel gamePanel;
    private Thread gameThread;
    private volatile boolean running = false;

    private final int FPS_SET = 120;
    private final int UPS_SET = 200;

    private List<System> systems;
    private List<Wire> wires;
    private List<MovingPacket> movingPackets;

    // Fields for input handling
    private System draggedSystem = null;
    private ArcPoint draggedArcPoint = null;
    private Point dragOffset = null;
    private boolean inWiringMode = false;
    private Wire previewWire = null;
    private Point currentMousePos = new Point();

    public GameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        systems = new ArrayList<>();
        wires = new ArrayList<>();
        movingPackets = new ArrayList<>();
        init();
    }

    private void init() {
        LevelData levelData = LevelLoader.loadLevel(1);
        if (levelData == null || levelData.systems == null) {
            java.lang.System.err.println("Failed to load level data. Game cannot start.");
            return;
        }

        for (LevelData.SystemData sysData : levelData.systems) {
            if ("REFERENCE".equals(sysData.type)) {
                systems.add(new ReferenceSystem(sysData.position.x, sysData.position.y, sysData));
            } else {
                java.lang.System.err.println("Unknown system type in level file: " + sysData.type);
            }
        }
    }

    public void startGameLoop() {
        gameThread = new Thread(this);
        running = true;
        gameThread.start();
    }

    @Override
    public void run() {
        double timePerFrame = 1_000_000_000.0 / FPS_SET;
        double timePerUpdate = 1_000_000_000.0 / UPS_SET;
        long previousTime = java.lang.System.nanoTime();
        double deltaU = 0;
        double deltaF = 0;
        long lastCheck = java.lang.System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            long currentTime = java.lang.System.nanoTime();
            deltaU += (currentTime - previousTime) / timePerUpdate;
            deltaF += (currentTime - previousTime) / timePerFrame;
            previousTime = currentTime;

            if (deltaU >= 1) {
                update();
                updates++;
                deltaU--;
            }

            if (deltaF >= 1) {
                gamePanel.repaint();
                frames++;
                deltaF--;
            }

            if (java.lang.System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = java.lang.System.currentTimeMillis();
                java.lang.System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    private void update() {
        // Automatic spawning is removed. Systems now have a simple update.
        for (System system : systems) {
            system.update();
        }

        for (MovingPacket movingPacket : new ArrayList<>(movingPackets)) {
            movingPacket.update();
            if (movingPacket.hasArrived()) {
                movingPackets.remove(movingPacket);
            }
        }
    }

    public void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Wire wire : new ArrayList<>(wires)) {
            wire.draw(g);
        }

        for (MovingPacket movingPacket : new ArrayList<>(movingPackets)) {
            movingPacket.draw(g);
        }

        for (System system : new ArrayList<>(systems)) {
            system.draw(g);
        }

        if (previewWire != null) {
            drawPreviewWire(g);
        }

        if (inWiringMode) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("WIRING MODE ACTIVE", 10, 20);
        }
    }

    // --- METHOD RE-ADDED ---
    private void drawPreviewWire(Graphics2D g) {
        List<Point> points = new ArrayList<>();
        points.add(previewWire.getStartPort().getCenter());
        points.addAll(previewWire.getArcPoints().stream().map(ArcPoint::getPosition).collect(Collectors.toList()));
        points.add(currentMousePos);

        boolean isValid = true;
        for (int i = 0; i < points.size() - 1; i++) {
            Line2D.Float segment = new Line2D.Float(points.get(i), points.get(i + 1));
            for (System system : systems) {
                if (system != previewWire.getStartPort().getParentSystem() && segment.intersects(system.getBounds())) {
                    isValid = false;
                    break;
                }
            }
            if (!isValid) break;
        }

        g.setColor(isValid ? Color.GREEN : Color.RED);
        g.setStroke(new BasicStroke(2));

        for (int i = 0; i < points.size() - 1; i++) {
            g.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
        }
    }

    public void spawnPacket(Packet packet, ReferenceSystem spawner) {
        List<Wire> availableWires = wires.stream()
                .filter(wire -> wire.getStartPort().getParentSystem() == spawner)
                .collect(Collectors.toList());

        if (!availableWires.isEmpty()) {
            Wire targetWire = availableWires.get((int) (Math.random() * availableWires.size()));
            movingPackets.add(new MovingPacket(packet, targetWire));
        }
    }

    /**
     * Called by the InputHandler when the spacebar is pressed.
     * It tells all Reference Systems to spawn a packet.
     */
    public void handleManualPacketSpawn() {
        for (System system : systems) {
            if (system instanceof ReferenceSystem) {
                ((ReferenceSystem) system).spawnRandomPacket(this);
            }
        }
    }

    public void toggleWiringMode(boolean isEnabled) {
        this.inWiringMode = isEnabled;
        if (!isEnabled) {
            previewWire = null;
        }
    }

    public void handleMouseMove(Point point) {
        currentMousePos.setLocation(point);
    }

    public void handleMouseDrag(Point point) {
        currentMousePos.setLocation(point);
        if (draggedSystem != null) {
            draggedSystem.setPosition(point.x - dragOffset.x, point.y - dragOffset.y);
        } else if (draggedArcPoint != null) {
            draggedArcPoint.setPosition(point);
        }
    }

    public void handleLeftMousePress(Point point) {
        if (inWiringMode) {
            Port clickedPort = findPortAt(point);
            if (clickedPort != null && !clickedPort.isInput()) {
                previewWire = new Wire(clickedPort);
            }
        } else {
            draggedArcPoint = findArcPointAt(point);
            if (draggedArcPoint == null) {
                handleSystemDragPress(point);
            }
        }
    }

    public void handleLeftMouseRelease(Point point) {
        if (previewWire != null) {
            Port clickedPort = findPortAt(point);
            if (clickedPort != null && clickedPort.isInput() && clickedPort.getParentSystem() != previewWire.getStartPort().getParentSystem()) {
                previewWire.setEndPort(clickedPort);
                wires.add(previewWire);
                java.lang.System.out.println("Wire created with length: " + previewWire.calculateLength());
            }
            previewWire = null;
        } else if (draggedSystem != null) {
            draggedSystem = null;
            dragOffset = null;
        } else if (draggedArcPoint != null) {
            draggedArcPoint = null;
        }
    }

    public void handleRightMousePress(Point point) {
        if (previewWire != null) {
            previewWire.addArcPoint(point);
        } else if (!inWiringMode) {
            Wire clickedWire = findWireAt(point);
            if (clickedWire != null) {
                clickedWire.addArcPoint(point);
            }
        }
    }

    private void handleSystemDragPress(Point point) {
        List<System> reversedSystems = new ArrayList<>(systems);
        Collections.reverse(reversedSystems);
        for (System system : reversedSystems) {
            if (system.contains(point)) {
                draggedSystem = system;
                dragOffset = new Point(point.x - system.getX(), point.y - system.getY());
                return;
            }
        }
    }

    private Port findPortAt(Point point) {
        for (System system : systems) {
            for (Port port : system.getOutputPorts()) {
                if (port.contains(point)) return port;
            }
            for (Port port : system.getInputPorts()) {
                if (port.contains(point)) return port;
            }
        }
        return null;
    }

    private ArcPoint findArcPointAt(Point point) {
        for (Wire wire : wires) {
            for (ArcPoint arc : wire.getArcPoints()) {
                if (arc.contains(point)) {
                    return arc;
                }
            }
        }
        return null;
    }

    private Wire findWireAt(Point point) {
        final double CLICK_THRESHOLD = 5.0;
        Wire closestWire = null;
        double minDistance = Double.MAX_VALUE;

        for (Wire wire : wires) {
            List<Point> points = wire.getAllPoints();
            for (int i = 0; i < points.size() - 1; i++) {
                Line2D.Float segment = new Line2D.Float(points.get(i), points.get(i + 1));
                double distance = segment.ptSegDist(point);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestWire = wire;
                }
            }
        }

        if (minDistance < CLICK_THRESHOLD) {
            return closestWire;
        }
        return null;
    }
}
