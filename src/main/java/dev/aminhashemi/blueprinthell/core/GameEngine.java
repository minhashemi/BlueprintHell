package dev.aminhashemi.blueprinthell.core;

import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.entities.systems.PortType;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.entities.systems.ReferenceSystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.VPNSystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.MaliciousSystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.Port;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.model.world.ArcPoint;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.world.Impact;
import dev.aminhashemi.blueprinthell.utils.LevelLoader;
import dev.aminhashemi.blueprinthell.utils.AudioManager;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.view.GamePanel;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

public class GameEngine implements Runnable {

    private final GamePanel gamePanel;
    private Thread gameThread;
    private volatile boolean running = false;
    private final int FPS_SET = 120;
    private final int UPS_SET = 200;

    private List<System> systems;
    private List<Wire> wires;
    private List<MovingPacket> movingPackets;
    private ImpactManager impactManager;

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
        impactManager = new ImpactManager();
        init();
    }

    private void init() {
        LevelData levelData = LevelLoader.loadLevel(1);
        if (levelData == null || levelData.systems == null) {
            Logger.getInstance().error("Failed to load level data. Game cannot start.");
            return;
        }

        for (LevelData.SystemData sysData : levelData.systems) {
            switch (sysData.type) {
                case "REFERENCE":
                    systems.add(new ReferenceSystem(sysData.position.x, sysData.position.y, sysData));
                    break;
                case "VPN":
                    systems.add(new VPNSystem(sysData.position.x, sysData.position.y, sysData));
                    break;
                case "MALICIOUS":
                    systems.add(new MaliciousSystem(sysData.position.x, sysData.position.y, sysData));
                    break;
                default:
                    Logger.getInstance().error("Unknown system type in level file: " + sysData.type);
                    break;
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
                Logger.getInstance().info("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    private void update() {
        for (System system : systems) {
            // This check allows ReferenceSystem to have a unique update method for spawning
            if (system instanceof ReferenceSystem) {
                ((ReferenceSystem) system).update(this);
            } else {
                system.update(this);
            }
        }

        // Iterate over a copy to safely remove items
        for (MovingPacket movingPacket : new ArrayList<>(movingPackets)) {
            // Check if packet is lost before updating
            if (movingPacket.isLost()) {
                continue; // Skip updating lost packets
            }
            movingPacket.update(this);
        }
        
        // NEW: Impact detection and processing
        impactManager.detectCollisions(movingPackets);
        List<MovingPacket> destroyedPackets = impactManager.processImpacts(movingPackets);
        
        // Immediately remove destroyed packets
        movingPackets.removeAll(destroyedPackets);
        
        // Clean up any remaining lost packets
        cleanupLostPackets();
    }

    /**
     * Cleans up packets that are lost due to high noise levels.
     */
    private void cleanupLostPackets() {
        Iterator<MovingPacket> iterator = movingPackets.iterator();
        
        while (iterator.hasNext()) {
            MovingPacket packet = iterator.next();
            
            if (packet.isLost()) {
                iterator.remove();
                // Play lose sound for destroyed packet
                AudioManager.getInstance().playSound("boom.wav");
                // TODO: Update HUD or game state to reflect lost packet
                Logger.getInstance().info("Packet lost due to high noise level!");
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
        
        // NEW: Display impact system information
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Active Impacts: " + getActiveImpactCount(), 10, 40);
        
        // Draw active impacts on screen
        for (Impact impact : impactManager.getActiveImpacts()) {
            Point collisionPoint = impact.getCollisionPoint();
            g.setColor(Color.RED);
            g.fillOval(collisionPoint.x - 5, collisionPoint.y - 5, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString("IMPACT", collisionPoint.x + 10, collisionPoint.y + 5);
        }
        
        // Display packet noise levels and status
        // Create a copy to avoid ConcurrentModificationException
        List<MovingPacket> packetsCopy = new ArrayList<>(movingPackets);
        for (MovingPacket packet : packetsCopy) {
            Point pos = packet.getPacket().getPosition();
            if (packet.isLost()) {
                g.setColor(Color.RED);
                g.setFont(new Font("Arial", Font.BOLD, 10));
                g.drawString("DESTROYED", pos.x + 20, pos.y - 10);
            } else if (packet.getNoiseLevel() > 0) {
                g.setColor(Color.ORANGE);
                g.setFont(new Font("Arial", Font.PLAIN, 10));
                String noiseText = String.format("Noise: %.1f", packet.getNoiseLevel());
                g.drawString(noiseText, pos.x + 20, pos.y - 10);
            }
            
            // Phase 2: Display packet type and behavior info
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 8));
            String packetInfo = String.format("%s", packet.getPacket().getType());
            g.drawString(packetInfo, pos.x + 20, pos.y + 20);
        }
    }

    private void drawPreviewWire(Graphics2D g) {
        List<Point> points = new ArrayList<>();
        points.add(previewWire.getStartPort().getCenter());
        previewWire.getArcPoints().forEach(arcPoint -> points.add(arcPoint.getPosition()));
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

    public void handlePacketArrival(MovingPacket arrivedPacket) {
        movingPackets.remove(arrivedPacket);
        System destinationSystem = arrivedPacket.getDestinationSystem();
        destinationSystem.receivePacket(arrivedPacket.getPacket(), this);
    }

    public void routePacket(Packet packet, System currentSystem) {
        if (currentSystem instanceof ReferenceSystem) {
            Logger.getInstance().info("Packet " + packet.getType() + " returned home.");
            return;
        }

        List<Wire> outgoingWires = wires.stream()
                .filter(wire -> wire.getStartPort().getParentSystem() == currentSystem)
                .collect(Collectors.toList());

        if (outgoingWires.isEmpty()) {
            Logger.getInstance().info("Packet " + packet.getType() + " is stuck. No outgoing wires.");
            return;
        }

        PortType packetPortType = getPortTypeForPacket(packet.getType());
        List<Wire> compatibleWires = outgoingWires.stream()
                .filter(wire -> isPortCompatible(wire.getStartPort().getType(), packet.getType()))
                .collect(Collectors.toList());

        Wire targetWire;
        if (!compatibleWires.isEmpty()) {
            targetWire = compatibleWires.get((int) (Math.random() * compatibleWires.size()));
        } else {
            targetWire = outgoingWires.get((int) (Math.random() * outgoingWires.size()));
        }

        MovingPacket movingPacket = new MovingPacket(packet, targetWire);
        
        // Apply port compatibility effects
        boolean isCompatible = isPortCompatible(targetWire.getStartPort().getType(), packet.getType());
        movingPacket.applyPortCompatibilityEffect(targetWire.getStartPort().getType(), isCompatible);
        
        movingPackets.add(movingPacket);
    }

    private PortType getPortTypeForPacket(PacketType packetType) {
        // Phase 1 packet types (backward compatibility)
        if (packetType == PacketType.SQUARE_MESSENGER) return PortType.SQUARE;
        if (packetType == PacketType.TRIANGLE_MESSENGER) return PortType.TRIANGLE;
        
        // Phase 2 packet types
        switch (packetType) {
            case GREEN_DIAMOND_SMALL:
            case GREEN_DIAMOND_LARGE:
                return PortType.DIAMOND;
            case INFINITY_SYMBOL:
                return PortType.INFINITY;
            case PADLOCK_ICON:
                return PortType.PADLOCK;
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                return PortType.CAMOUFLAGE;
            default:
                return PortType.SQUARE; // Fallback
        }
    }
    
    /**
     * Checks if a port is compatible with a packet type
     */
    private boolean isPortCompatible(PortType portType, PacketType packetType) {
        // Phase 1 compatibility (backward compatibility)
        if (packetType == PacketType.SQUARE_MESSENGER && portType == PortType.SQUARE) return true;
        if (packetType == PacketType.TRIANGLE_MESSENGER && portType == PortType.TRIANGLE) return true;
        
        // Phase 2 compatibility rules
        switch (packetType) {
            case GREEN_DIAMOND_SMALL:
            case GREEN_DIAMOND_LARGE:
                return portType == PortType.DIAMOND;
            case INFINITY_SYMBOL:
                return portType == PortType.INFINITY;
            case PADLOCK_ICON:
                return portType == PortType.PADLOCK || portType == PortType.VPN;
            case CAMOUFLAGE_ICON_SMALL:
            case CAMOUFLAGE_ICON_LARGE:
                return portType == PortType.CAMOUFLAGE;
            default:
                return false;
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

    public void handleManualPacketSpawn() {
        for (System system : systems) {
            if (system instanceof ReferenceSystem) {
                // Spawn only ONE random packet per SPACE press
                ReferenceSystem refSystem = (ReferenceSystem) system;
                
                if (refSystem.getOutputPorts().isEmpty()) {
                    continue;
                }
                
                // Collect all possible packet types from available ports
                List<PacketType> possibleTypes = new ArrayList<>();
                for (Port port : refSystem.getOutputPorts()) {
                    switch (port.getType()) {
                        case SQUARE:
                            possibleTypes.add(PacketType.SQUARE_MESSENGER);
                            break;
                        case TRIANGLE:
                            possibleTypes.add(PacketType.TRIANGLE_MESSENGER);
                            break;
                        case DIAMOND:
                            // Randomly choose one diamond type, not both
                            if (Math.random() < 0.5) {
                                possibleTypes.add(PacketType.GREEN_DIAMOND_SMALL);
                            } else {
                                possibleTypes.add(PacketType.GREEN_DIAMOND_LARGE);
                            }
                            break;
                        case INFINITY:
                            possibleTypes.add(PacketType.INFINITY_SYMBOL);
                            break;
                        case PADLOCK:
                            possibleTypes.add(PacketType.PADLOCK_ICON);
                            break;
                        case CAMOUFLAGE:
                            // Randomly choose one camouflage type, not both
                            if (Math.random() < 0.5) {
                                possibleTypes.add(PacketType.CAMOUFLAGE_ICON_SMALL);
                            } else {
                                possibleTypes.add(PacketType.CAMOUFLAGE_ICON_LARGE);
                            }
                            break;
                    }
                }
                
                if (!possibleTypes.isEmpty()) {
                    // Spawn only ONE random packet
                    PacketType randomType = possibleTypes.get((int) (Math.random() * possibleTypes.size()));
                    Point pos = refSystem.getPosition();
                    spawnPacket(new MessengerPacket(pos.x, pos.y, randomType), refSystem);
                    
                    // Only spawn from the first reference system found
                    break;
                }
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
            regenerateWirePaths(); // Regenerate wire paths when system moves
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

    /**
     * Clears all active impacts.
     */
    public void clearImpacts() {
        impactManager.clearImpacts();
    }

    /**
     * Disables impact detection for a specified number of seconds.
     */
    public void disableImpactForSeconds(int seconds) {
        impactManager.setImpactDetectionEnabled(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                impactManager.setImpactDetectionEnabled(true);
            }
        }, seconds * 1000);
    }

    /**
     * Disables wave effects for a specified number of seconds.
     */
    public void disableWaveForSeconds(int seconds) {
        impactManager.setWaveEffectsEnabled(false);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                impactManager.setWaveEffectsEnabled(true);
            }
        }, seconds * 1000);
    }

    /**
     * Gets the number of active impacts.
     */
    public int getActiveImpactCount() {
        return impactManager.getActiveImpactCount();
    }
    
    /**
     * Resets noise levels for all packets.
     */
    public void resetAllNoise() {
        for (MovingPacket packet : movingPackets) {
            packet.setNoiseLevel(0.0f);
        }
    }

    /**
     * Demonstrates the new Phase 2 packet behaviors
     */
    public void demonstratePacketBehaviors() {
        Logger.getInstance().info("Demonstrating Phase 2 packet behaviors...");
        
        // Find a reference system to spawn from
        ReferenceSystem refSystem = null;
        for (System system : systems) {
            if (system instanceof ReferenceSystem) {
                refSystem = (ReferenceSystem) system;
                break;
            }
        }
        
        if (refSystem == null) {
            Logger.getInstance().warning("No reference system found for packet behavior demonstration");
            return;
        }
        
        Point pos = refSystem.getPosition();
        
        // Spawn different packet types to demonstrate behaviors
        spawnPacket(new MessengerPacket(pos.x, pos.y, PacketType.GREEN_DIAMOND_SMALL), refSystem);
        Logger.getInstance().info("Spawned GREEN_DIAMOND_SMALL - should move at half speed from incompatible ports");
        
        spawnPacket(new MessengerPacket(pos.x, pos.y, PacketType.GREEN_DIAMOND_LARGE), refSystem);
        Logger.getInstance().info("Spawned GREEN_DIAMOND_LARGE - should accelerate through incompatible ports");
        
        spawnPacket(new MessengerPacket(pos.x, pos.y, PacketType.INFINITY_SYMBOL), refSystem);
        Logger.getInstance().info("Spawned INFINITY_SYMBOL - should have constant acceleration/deceleration");
        
        spawnPacket(new MessengerPacket(pos.x, pos.y, PacketType.PADLOCK_ICON), refSystem);
        Logger.getInstance().info("Spawned PADLOCK_ICON - should be more resilient to collisions");
        
        spawnPacket(new MessengerPacket(pos.x, pos.y, PacketType.CAMOUFLAGE_ICON_SMALL), refSystem);
        Logger.getInstance().info("Spawned CAMOUFLAGE_ICON_SMALL - should slow down near malicious systems");
    }

    private void regenerateWirePaths() {
        // Regenerate all wire paths to reflect new system positions
        for (Wire wire : wires) {
            wire.regeneratePath();
        }
    }
}
