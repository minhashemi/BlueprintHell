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
import dev.aminhashemi.blueprinthell.model.entities.systems.SpySystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.Port;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ConfidentialPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.BulkPacket;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;

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
    private long lastUpdateTime;
    private long gameStartTime; // Game start time for HUD progress calculation
    private int totalWireLength = 8000; // Total available wire length
    private int usedWireLength = 0; // Wire length already used
    private Map<Wire, Integer> wireLengths = new HashMap<>(); // Track individual wire lengths
    private int coins = 0; // Current coins - start at 0
    private long lastSpawnTime = 0; // Prevent multiple spawns
    private static final long SPAWN_COOLDOWN = 500; // 500ms between spawns
    
    public GameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        systems = new ArrayList<>();
        wires = new ArrayList<>();
        movingPackets = new ArrayList<>();
        impactManager = new ImpactManager();
        lastUpdateTime = java.lang.System.nanoTime();
        gameStartTime = java.lang.System.currentTimeMillis(); // Initialize game start time
        init();
    }

    private void init() {
        LevelData levelData = LevelLoader.loadLevel(3); // Load level 3 to test SpySystem
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
                case "SPY":
                    systems.add(new SpySystem(sysData.position.x, sysData.position.y, sysData));
                    break;
                default:
                    Logger.getInstance().error("Unknown system type in level file: " + sysData.type);
                    break;
            }
        }
        
        // Set up spy network connections
        setupSpyNetwork();
        
        // Set up wire callbacks for dynamic length updates
        setupWireCallbacks();
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
        // Update all systems (ReferenceSystem has special spawning logic)
        for (System system : systems) {
            if (system instanceof ReferenceSystem) {
                ((ReferenceSystem) system).update(this);
            } else {
                system.update(this);
            }
        }

        // Update active packets
        for (MovingPacket movingPacket : new ArrayList<>(movingPackets)) {
            if (movingPacket.isLost()) {
                continue;
            }
            movingPacket.update(this);
        }
        
        // Handle packet collisions and cleanup
        impactManager.detectCollisions(movingPackets);
        List<MovingPacket> destroyedPackets = impactManager.processImpacts(movingPackets);
        movingPackets.removeAll(destroyedPackets);
        cleanupLostPackets();
        
        // Handle wire degradation and cleanup
        cleanupDestroyedWires();
        
        updateHUD();
    }

    /** Removes packets lost due to high noise levels */
    private void cleanupLostPackets() {
        List<MovingPacket> packetsToRemove = new ArrayList<>();
        
        for (MovingPacket packet : movingPackets) {
            if (packet.hasSpawnProtection()) {
                continue; // Skip protected packets
            }
            
            if (packet.isLost()) {
                packetsToRemove.add(packet);
            }
        }
        
        // Remove lost packets and play sound effect
        for (MovingPacket packet : packetsToRemove) {
            movingPackets.remove(packet);
            AudioManager.getInstance().playSound("boom.wav");
        }
    }
    
    /** Removes destroyed wires and updates wire length tracking */
    private void cleanupDestroyedWires() {
        List<Wire> wiresToRemove = new ArrayList<>();
        
        for (Wire wire : wires) {
            if (wire.isDestroyed()) {
                wiresToRemove.add(wire);
                Logger.getInstance().warning("Wire destroyed after " + wire.getBulkPacketPasses() + " bulk packet passes");
            }
        }
        
        // Remove destroyed wires and update wire length
        for (Wire wire : wiresToRemove) {
            wires.remove(wire);
            // Remove from wire length tracking
            Integer wireLength = wireLengths.remove(wire);
            if (wireLength != null) {
                usedWireLength -= wireLength;
                Logger.getInstance().info("Wire removed. Length freed: " + wireLength + "m. Remaining: " + getRemainingWireLength() + "m");
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
        
        // Display impact system info
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Active Impacts: " + getActiveImpactCount(), 10, 40);
        
        // Draw impact effects
        for (Impact impact : impactManager.getActiveImpacts()) {
            Point collisionPoint = impact.getCollisionPoint();
            g.setColor(Color.RED);
            g.fillOval(collisionPoint.x - 5, collisionPoint.y - 5, 10, 10);
            g.setColor(Color.WHITE);
            g.drawString("IMPACT", collisionPoint.x + 10, collisionPoint.y + 5);
        }
        
        // Display packet status
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
        
        // Display wire system status
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        g.drawString("Wire System: Collision Detection Active", 10, 60);
        g.drawString("Arc Points: 1 coin each", 10, 80);
        g.drawString("Wire Degradation: 3 bulk packet limit", 10, 100);
    }

    private void drawPreviewWire(Graphics2D g) {
        List<Point> points = new ArrayList<>();
        points.add(previewWire.getStartPort().getCenter());
        previewWire.getArcPoints().forEach(arcPoint -> points.add(arcPoint.getPosition()));
        points.add(currentMousePos);

        // Check if the preview wire path is valid
        boolean isValid = true;
        for (int i = 0; i < points.size() - 1; i++) {
            Point start = points.get(i);
            Point end = points.get(i + 1);
            
            for (System system : systems) {
                if (system != previewWire.getStartPort().getParentSystem() && 
                    lineIntersectsSystem(start, end, system)) {
                    isValid = false;
                    break;
                }
            }
            if (!isValid) break;
        }

        // Draw wire in green if valid, red if invalid
        g.setColor(isValid ? Color.GREEN : Color.RED);
        g.setStroke(new BasicStroke(2));

        for (int i = 0; i < points.size() - 1; i++) {
            g.drawLine(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
        }
        
        // Add text indicator
        if (!isValid) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.drawString("INVALID PATH", currentMousePos.x + 10, currentMousePos.y - 10);
        }
    }

    public void handlePacketArrival(MovingPacket arrivedPacket) {
        movingPackets.remove(arrivedPacket);
        System destinationSystem = arrivedPacket.getDestinationSystem();
        
        // Track bulk packet passes for wire degradation
        if (arrivedPacket.getPacket() instanceof BulkPacket) {
            Wire wire = arrivedPacket.getWire();
            wire.recordBulkPacketPass();
            Logger.getInstance().info("Bulk packet passed through wire. Passes: " + wire.getBulkPacketPasses() + "/3");
        }
        
        destinationSystem.receivePacket(arrivedPacket.getPacket(), this);
        
        // Only add coins when packet reaches the END reference system (the one with only inputs)
        if (isReferenceSystem(destinationSystem) && destinationSystem.getOutputPorts().isEmpty()) {
            // End reference system - add coins
            addCoinsForPacketEntry(arrivedPacket.getPacket());
            Logger.getInstance().info("Packet " + arrivedPacket.getPacket().getType() + " reached END reference system! Coins added.");
        } else {
            Logger.getInstance().info("Packet " + arrivedPacket.getPacket().getType() + " reached intermediate system - no coins added.");
        }
        
        // Check if this packet traveled from a reference system to a reference system
        System sourceSystem = arrivedPacket.getSourceSystem();
        if (isReferenceSystem(sourceSystem) && isReferenceSystem(destinationSystem)) {
            // Reward coins for successful packet delivery from reference to reference
            addCoins(1);
            Logger.getInstance().info("Packet successfully delivered from reference to reference! +1 coin awarded.");
        }
    }
    
    /**
     * Adds coins when a packet enters a system based on packet type and size
     * This is called when packets enter systems, not when they reach destinations
     */
    public void addCoinsForPacketEntry(Packet packet) {
        int coinsToAdd = 0;
        
        // Handle specialized packet types
        if (packet instanceof ProtectedPacket) {
            coinsToAdd = ((ProtectedPacket) packet).getCoinReward();
        } else if (packet instanceof ConfidentialPacket) {
            coinsToAdd = ((ConfidentialPacket) packet).getCoinReward();
        } else if (packet instanceof BulkPacket) {
            coinsToAdd = ((BulkPacket) packet).getCoinReward();
        } else {
            // Handle regular packet types
            PacketType packetType = packet.getType();
            coinsToAdd = packetType.getCoinReward();
        }
        
        if (coinsToAdd > 0) {
            addCoins(coinsToAdd);
            Logger.getInstance().info("Packet " + packet.getType() + " entered system! +" + coinsToAdd + " coins added.");
        }
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

        // PortType packetPortType = getPortTypeForPacket(packet.getType()); // Not used in current implementation
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
        
        // Prevent premature coin addition
        
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
            MovingPacket movingPacket = new MovingPacket(packet, targetWire);
            
            // Add spawn protection to prevent immediate destruction
            movingPacket.setSpawnProtection(true);
            
            movingPackets.add(movingPacket);
            Logger.getInstance().info("Packet " + packet.getType() + " spawned with spawn protection. Total packets: " + movingPackets.size());
        } else {
            Logger.getInstance().warning("No available wires for packet spawning from " + spawner);
        }
    }

    public void handleManualPacketSpawn() {
        // Check spawn cooldown to prevent multiple spawns
        long currentTime = java.lang.System.currentTimeMillis();
        if (currentTime - lastSpawnTime < SPAWN_COOLDOWN) {
            Logger.getInstance().info("Spawn cooldown active - skipping spawn request");
            return;
        }
        
        Logger.getInstance().info("=== MANUAL PACKET SPAWN REQUESTED ===");
        
        // Find all reference systems that have output ports with connected wires
        List<ReferenceSystem> availableSpawners = new ArrayList<>();
        
        for (System system : systems) {
            if (system instanceof ReferenceSystem) {
                ReferenceSystem refSystem = (ReferenceSystem) system;
                
                // Check if this reference system has any output ports with connected wires
                boolean hasConnectedOutputs = false;
                for (Port port : refSystem.getOutputPorts()) {
                    // Check if this port has any wires starting from it
                    for (Wire wire : wires) {
                        if (wire.getStartPort() == port) {
                            hasConnectedOutputs = true;
                            break;
                        }
                    }
                    if (hasConnectedOutputs) break;
                }
                
                if (hasConnectedOutputs) {
                    availableSpawners.add(refSystem);
                    Logger.getInstance().info("Found available spawner: " + refSystem + " with connected outputs");
                }
            }
        }
        
        if (availableSpawners.isEmpty()) {
            Logger.getInstance().info("No reference systems with connected output ports found for packet spawning");
            return;
        }
        
        Logger.getInstance().info("Available spawners: " + availableSpawners.size());
        
        // Randomly select one available spawner
        ReferenceSystem selectedSpawner = availableSpawners.get((int) (Math.random() * availableSpawners.size()));
        Logger.getInstance().info("Selected spawner: " + selectedSpawner);
        
        // Find all output ports of the selected spawner that have wires
        List<Port> connectedOutputs = new ArrayList<>();
        for (Port port : selectedSpawner.getOutputPorts()) {
            for (Wire wire : wires) {
                if (wire.getStartPort() == port) {
                    connectedOutputs.add(port);
                    break;
                }
            }
        }
        
        if (connectedOutputs.isEmpty()) {
            Logger.getInstance().warning("Selected spawner has no connected output ports");
            return;
        }
        
        Logger.getInstance().info("Connected outputs: " + connectedOutputs.size());
        
        // Randomly select one connected output port
        Port selectedPort = connectedOutputs.get((int) (Math.random() * connectedOutputs.size()));
        Logger.getInstance().info("Selected port: " + selectedPort.getType());
        
        // Determine packet type based on the selected port
        PacketType packetType = getPacketTypeForPort(selectedPort.getType());
        
        if (packetType != null) {
            Point pos = selectedSpawner.getPosition();
            spawnPacket(new MessengerPacket(pos.x, pos.y, packetType), selectedSpawner);
            lastSpawnTime = currentTime; // Update spawn time
            Logger.getInstance().info("=== PACKET SPAWNED: " + packetType + " ===");
        }
    }
    
    /**
     * Gets the appropriate packet type for a given port type
     */
    private PacketType getPacketTypeForPort(PortType portType) {
        switch (portType) {
            case SQUARE:
                return PacketType.SQUARE_MESSENGER;
            case TRIANGLE:
                return PacketType.TRIANGLE_MESSENGER;
            case DIAMOND:
                // Randomly choose one diamond type
                return Math.random() < 0.5 ? PacketType.GREEN_DIAMOND_SMALL : PacketType.GREEN_DIAMOND_LARGE;
            case INFINITY:
                return PacketType.INFINITY_SYMBOL;
            case PADLOCK:
                return PacketType.PADLOCK_ICON;
            case CAMOUFLAGE:
                // Randomly choose one camouflage type
                return Math.random() < 0.5 ? PacketType.CAMOUFLAGE_ICON_SMALL : PacketType.CAMOUFLAGE_ICON_LARGE;
            default:
                return null;
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
            
            // Recalculate wire lengths when systems move
            Logger.getInstance().info("System moved - recalculating all wire lengths...");
            recalculateAllWireLengths();
        } else if (draggedArcPoint != null) {
            draggedArcPoint.setPosition(point);
            
            // Recalculate wire lengths when arc points move
            // Find the wire containing this arc point and update its length
            for (Wire wire : wires) {
                if (wire.getArcPoints().contains(draggedArcPoint)) {
                    Logger.getInstance().info("Arc point moved - updating wire length...");
                    // Force a path regeneration and length update
                    wire.regeneratePath();
                    updateWireLengthForWire(wire);
                    break;
                }
            }
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
                
                // Check if wire path is valid (doesn't pass through systems)
                if (isWirePathValid(previewWire)) {
                    // Set up the wire path change callback for dynamic length updates
                    previewWire.setOnPathChangedCallback(() -> {
                        Logger.getInstance().info("Wire path changed - updating length...");
                        updateWireLengthForWire(previewWire);
                    });
                    
                    wires.add(previewWire);
                    updateWireLength(previewWire); // Update used wire length when a new wire is created
                    Logger.getInstance().info("Wire created successfully");
                } else {
                    Logger.getInstance().warning("Cannot create wire - path passes through systems");
                }
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
        // Allow adding arc points to existing wires regardless of wiring mode
        Wire wire = findWireAt(point);
        if (wire != null) {
            // Check if we can add more arc points (limit is 3)
            if (wire.getArcPoints().size() < 3) {
                // Check if player has enough coins (1 coin per arc point)
                if (coins >= 1) {
                    // Deduct coin cost
                    coins -= 1;
                    Logger.getInstance().info("Arc point cost: -1 coin. Remaining coins: " + coins);
                    
                    // Add the arc point
                    wire.addArcPoint(point);
                    
                    // Update wire length when arc points are added
                    Logger.getInstance().info("Arc point added to wire - updating length...");
                    updateWireLengthForWire(wire);
                } else {
                    Logger.getInstance().warning("Cannot add arc point - insufficient coins (need 1, have " + coins + ")");
                }
            } else {
                Logger.getInstance().info("Cannot add more arc points - limit reached (3)");
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
        final double CLICK_THRESHOLD = 8.0; // Increased threshold for easier wire detection
        Wire closestWire = null;
        double minDistance = Double.MAX_VALUE;

        for (Wire wire : wires) {
            List<Point> points = wire.getAllPoints();
            if (points.size() < 2) continue;
            
            for (int i = 0; i < points.size() - 1; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get(i + 1);
                
                // Calculate distance from point to line segment
                double distance = pointToLineDistance(point, p1, p2);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    closestWire = wire;
                }
            }
        }

        if (minDistance < CLICK_THRESHOLD) {
            Logger.getInstance().info("Wire found at distance: " + minDistance + " pixels");
            return closestWire;
        }
        return null;
    }
    
    /**
     * Calculates the distance from a point to a line segment
     */
    private double pointToLineDistance(Point point, Point lineStart, Point lineEnd) {
        double A = point.x - lineStart.x;
        double B = point.y - lineStart.y;
        double C = lineEnd.x - lineStart.x;
        double D = lineEnd.y - lineStart.y;

        double dot = A * C + B * D;
        double lenSq = C * C + D * D;
        
        if (lenSq == 0) return point.distance(lineStart);
        
        double param = dot / lenSq;
        
        double xx, yy;
        if (param < 0) {
            xx = lineStart.x;
            yy = lineStart.y;
        } else if (param > 1) {
            xx = lineEnd.x;
            yy = lineEnd.y;
        } else {
            xx = lineStart.x + param * C;
            yy = lineStart.y + param * D;
        }
        
        double dx = point.x - xx;
        double dy = point.y - yy;
        return Math.sqrt(dx * dx + dy * dy);
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

    /**
     * Returns the count of active systems in the network
     */
    public int getActiveSystemCount() {
        int count = 0;
        for (System system : systems) {
            if (system != null) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Returns the current network status
     */
    public String getNetworkStatus() {
        if (systems.isEmpty()) {
            return "No Systems";
        }
        
        int connectedSystems = 0;
        for (System system : systems) {
            if (system != null && !system.getInputPorts().isEmpty()) {
                connectedSystems++;
            }
        }
        
        if (connectedSystems == 0) {
            return "Disconnected";
        } else if (connectedSystems == systems.size()) {
            return "Fully Connected";
        } else {
            return "Partially Connected";
        }
    }
    
    /**
     * Returns the count of active ports in the network
     */
    public int getActivePortCount() {
        int count = 0;
        for (System system : systems) {
            if (system != null) {
                count += system.getInputPorts().size();
                count += system.getOutputPorts().size();
            }
        }
        return count;
    }
    
    /**
     * Returns the count of wire connections in the network
     */
    public int getWireConnectionCount() {
        return wires.size();
    }

    /**
     * Updates the HUD with current game data
     */
    private void updateHUD() {
        if (gamePanel != null) {
            // Get remaining wire length from game engine
            int remainingWireLength = getRemainingWireLength();
            
            // Calculate temporal progress (simplified - can be enhanced based on level objectives)
            int temporalProgress = Math.min(100, (int)((java.lang.System.currentTimeMillis() - gameStartTime) / 1000)); // Progress over time
            
            // Get packet loss count
            int packetLoss = getPacketLossCount();
            
            // Get current coins from game engine
            int currentCoins = getCoins();
            
            // Update HUD data
            gamePanel.updateHUDData(remainingWireLength, temporalProgress, packetLoss, currentCoins);
        }
    }
    
    /**
     * Returns the count of lost packets
     */
    private int getPacketLossCount() {
        int lostCount = 0;
        for (MovingPacket packet : movingPackets) {
            if (packet.isLost()) {
                lostCount++;
            }
        }
        return lostCount;
    }

    /**
     * Calculates the length of a wire based on its path
     */
    private int calculateWireLength(Wire wire) {
        if (wire == null) {
            return 0;
        }
        
        // Use the wire's built-in length calculation method for accuracy
        double length = wire.calculateLength();
        
        // Convert to integer meters (assuming 1 pixel = 1 meter for simplicity)
        return (int) length;
    }
    
    /**
     * Updates the used wire length when a new wire is created
     */
    private void updateWireLength(Wire wire) {
        int wireLength = calculateWireLength(wire);
        usedWireLength += wireLength;
        wireLengths.put(wire, wireLength); // Store the original length
        Logger.getInstance().info("Wire created with length: " + wireLength + "m. Total used: " + usedWireLength + "m");
    }

    /**
     * Returns the remaining wire length available
     */
    public int getRemainingWireLength() {
        return Math.max(0, totalWireLength - usedWireLength);
    }
    
    /**
     * Returns the total wire length used so far
     */
    public int getUsedWireLength() {
        return usedWireLength;
    }

    /**
     * Recalculates wire length for all wires and updates the used wire length
     * This should be called whenever wire paths are modified
     */
    public void recalculateAllWireLengths() {
        int totalUsed = 0;
        
        for (Wire wire : wires) {
            int wireLength = calculateWireLength(wire);
            totalUsed += wireLength;
            wireLengths.put(wire, wireLength); // Update stored length
        }
        
        usedWireLength = totalUsed;
        Logger.getInstance().info("Wire lengths recalculated. Total used: " + usedWireLength + "m");
    }
    
    /**
     * Updates the wire length for a specific wire and recalculates total
     * Call this when a specific wire's path is modified
     */
    public void updateWireLengthForWire(Wire wire) {
        // Get the original length of the wire
        Integer originalLength = wireLengths.get(wire);
        
        if (originalLength == null) {
            Logger.getInstance().warning("Original length not found for wire: " + wire);
            return;
        }

        // Recalculate the new length
        int newLength = calculateWireLength(wire);
        
        // Calculate the difference
        int lengthDifference = newLength - originalLength;
        
        // Update the total used length
        usedWireLength = usedWireLength - originalLength + newLength;
        
        // Update the stored original length
        wireLengths.put(wire, newLength);
        
        Logger.getInstance().info("Wire length updated dynamically: " + originalLength + "m -> " + newLength + "m. Total used: " + usedWireLength + "m. Remaining: " + getRemainingWireLength() + "m. Difference: " + lengthDifference + "m");
    }

    /**
     * Validates that a wire path doesn't pass through any systems
     */
    private boolean isWirePathValid(Wire wire) {
        List<Point> path = wire.getAllPoints();
        if (path.size() < 2) return true;
        
        // Check each segment of the wire path
        for (int i = 0; i < path.size() - 1; i++) {
            Point start = path.get(i);
            Point end = path.get(i + 1);
            
            // Check if this segment intersects with any system
            for (System system : systems) {
                if (system == wire.getStartPort().getParentSystem() || 
                    system == wire.getEndPort().getParentSystem()) {
                    continue; // Skip the systems the wire connects to
                }
                
                if (lineIntersectsSystem(start, end, system)) {
                    return false; // Wire passes through a system
                }
            }
        }
        
        return true; // Wire path is valid
    }
    
    /**
     * Checks if a line segment intersects with a system's bounds
     */
    private boolean lineIntersectsSystem(Point start, Point end, System system) {
        Rectangle systemBounds = system.getBounds();
        
        // Check if line segment intersects with system rectangle
        return systemBounds.intersectsLine(start.x, start.y, end.x, end.y);
    }

    /**
     * Sets up spy network connections between all SpySystems
     */
    private void setupSpyNetwork() {
        List<SpySystem> spySystems = new ArrayList<>();
        
        // Collect all SpySystems
        for (System system : systems) {
            if (system instanceof SpySystem) {
                spySystems.add((SpySystem) system);
            }
        }
        
        // Set up spy network for each SpySystem
        for (SpySystem spySystem : spySystems) {
            spySystem.setSpyNetwork(spySystems);
        }
        
        if (!spySystems.isEmpty()) {
            Logger.getInstance().info("Spy network established with " + spySystems.size() + " SpySystems");
        }
    }

    /**
     * Sets up path change callbacks for all existing wires
     */
    private void setupWireCallbacks() {
        for (Wire wire : wires) {
            wire.setOnPathChangedCallback(() -> {
                Logger.getInstance().info("Existing wire path changed - updating length...");
                updateWireLengthForWire(wire);
            });
        }
        Logger.getInstance().info("Wire callbacks set up for " + wires.size() + " wires");
    }

    /**
     * Adds coins to the total
     */
    public void addCoins(int amount) {
        coins += amount;
        Logger.getInstance().info("Coins added: +" + amount + ". Total coins: " + coins);
    }
    
    /**
     * Checks if a system is a reference system
     */
    private boolean isReferenceSystem(System system) {
        return system instanceof ReferenceSystem;
    }
    
    /**
     * Gets the current coin count
     */
    public int getCoins() {
        return coins;
    }
}
