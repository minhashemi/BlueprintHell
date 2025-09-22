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
import dev.aminhashemi.blueprinthell.model.entities.packets.TrojanPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ConfidentialPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.BulkPacket;
import dev.aminhashemi.blueprinthell.model.world.ArcPoint;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.view.TestResultDialog;
import dev.aminhashemi.blueprinthell.model.enums.GameState;
import dev.aminhashemi.blueprinthell.model.world.Impact;
import dev.aminhashemi.blueprinthell.utils.LevelLoader;
import dev.aminhashemi.blueprinthell.utils.AudioManager;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.SaveManager;
import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.model.SaveData;
import dev.aminhashemi.blueprinthell.model.shop.ShopManager;
import dev.aminhashemi.blueprinthell.model.shop.OAtar;
import dev.aminhashemi.blueprinthell.model.shop.OAiryaman;
import dev.aminhashemi.blueprinthell.model.shop.OAnahita;
import dev.aminhashemi.blueprinthell.model.shop.ScrollOfAergia;
import dev.aminhashemi.blueprinthell.model.shop.ScrollOfSisyphus;
import dev.aminhashemi.blueprinthell.model.shop.ScrollOfEliphas;
import dev.aminhashemi.blueprinthell.view.GamePanel;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.utils.PlayerManager;
import dev.aminhashemi.blueprinthell.utils.DatabaseLeaderboardManager;
import dev.aminhashemi.blueprinthell.model.TestPacketType;
import dev.aminhashemi.blueprinthell.model.TestConfiguration;
import dev.aminhashemi.blueprinthell.view.TestResultDialog;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Core game engine that manages the game loop, entities, and game state.
 * Handles packet routing, collision detection, time travel, and wire management.
 * Implements the main game logic and coordinates between different game systems.
 */
public class GameEngine implements Runnable {
    
    // ==================== CORE GAME COMPONENTS ====================
    private final GamePanel gamePanel;           // UI panel for rendering
    private Thread gameThread;                   // Main game loop thread
    private volatile boolean running = false;    // Game loop control flag
    private final int FPS_SET = Config.TARGET_FPS;   // Target frames per second
    private final int UPS_SET = Config.TARGET_UPS;   // Target updates per second
    private LeaderboardData leaderboardData;     // Leaderboard data for tracking records
    private Object gameFrame;                    // Reference to game frame for UI updates

    // ==================== GAME ENTITIES ====================
    private List<System> systems;                // All network systems (Reference, VPN, Malicious, Spy)
    private List<Wire> wires;                    // All wire connections between systems
    private List<MovingPacket> movingPackets;    // All packets currently in transit
    private ImpactManager impactManager;         // Handles packet collisions and impacts

    // ==================== INTERACTION STATE ====================
    private System draggedSystem = null;         // System being dragged by mouse
    private ArcPoint draggedArcPoint = null;     // Arc point being dragged for wire modification
    private Point dragOffset = null;             // Mouse offset when dragging
    private boolean inWiringMode = false;        // Whether user is in wire creation mode
    private boolean isShopOpen = false;          // Whether shop overlay is open
    private ShopManager shopManager;             // Shop management system
    private Wire previewWire = null;             // Preview wire being created
    private Point currentMousePos = new Point(); // Current mouse position for preview
    
    // ==================== GAME STATE ====================
    private long lastUpdateTime;                 // Last update timestamp for FPS calculation
    private int totalWireLength = Config.TOTAL_WIRE_LENGTH; // Total available wire length
    private int usedWireLength = 0;              // Wire length already used
    private double packetSpeedMultiplier = 1.0;  // Global packet speed multiplier
    private Map<Wire, Integer> wireLengths = new HashMap<>(); // Track individual wire lengths
    private int currentLevelNumber = 1;          // Current level number
    private String currentLevelName = "Level 1"; // Current level name
    private TestConfiguration testConfiguration; // Configuration for test packet spawning
    private int maxLevelNumber = 3;              // Maximum level number
    
    // Phase 1 temporary effects
    private boolean impactWavesDisabled = false;        // O' Atar effect
    private boolean packetCollisionsDisabled = false;   // O' Airyaman effect
    private boolean packetNoiseZeroed = false;          // O' Anahita effect
    
    // Game Test State
    private boolean isTestRunning = false;              // Whether network test is running
    private int testPacketsReleased = 0;                // Number of packets released during test
    private int testPacketsReturned = 0;                // Number of packets that reached END reference system
    private int testPacketsLost = 0;                    // Number of packets destroyed by noise/impact
    private long testStartTime = 0;                     // When the test started
    private long lastPacketReleaseTime = 0;             // Last time a packet was released
    private boolean testCompleted = false;              // Whether test has been completed
    private boolean gameWon = false;                    // Whether player won the level
    private boolean gameLost = false;                   // Whether player lost the level
    
    // Wire Selection State
    private Wire selectedWire = null;                   // Currently selected wire for removal
    private int coins = 0;                       // Current coins - start at 0
    private long lastSpawnTime = 0;              // Prevent multiple spawns
    private static final long SPAWN_COOLDOWN = Config.PACKET_SPAWN_COOLDOWN; // 500ms between spawns
    
    // ==================== TIME TRAVEL SYSTEM ====================
    private boolean isTimeTravelMode = false;    // Whether currently in time travel mode
    private boolean isPaused = false;            // Whether game is paused
    private long gameStartTime = 0;              // Game start timestamp
    private long currentGameTime = 0;            // Current game time in milliseconds
    private long lastSnapshotTime = 0;           // Last time a snapshot was created
    private int snapshotInterval = Config.SNAPSHOT_INTERVAL; // 60 FPS = 16ms intervals
    private int timeTravelWindowSeconds = Config.TIME_TRAVEL_WINDOW_SECONDS; // How many seconds back we can go
    private int maxSnapshots = Config.MAX_SNAPSHOTS; // 60 FPS * 5 seconds = 300 snapshots
    
    // In-memory snapshots for fast navigation
    private List<SaveData> timeSnapshots = new ArrayList<>(); // Game state snapshots
    private int currentSnapshotIndex = -1;       // Current snapshot being viewed
    private String snapshotsDirectory = Config.SNAPSHOTS_DIRECTORY; // Directory for disk snapshots
    private int snapshotCounter = 0;             // Counter for snapshot file naming
    
    // Time travel controls
    private boolean timeTravelLeftPressed = false;  // Left arrow key state
    private boolean timeTravelRightPressed = false; // Right arrow key state
    private long lastTimeTravelInput = 0;           // Last time travel input timestamp
    private int timeTravelInputDelay = Config.TIME_TRAVEL_INPUT_DELAY; // ms between time travel inputs
    
    /**
     * Constructs the game engine and initializes all game systems.
     * @param gamePanel The UI panel for rendering the game
     */
    public GameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        systems = new ArrayList<>();
        wires = new ArrayList<>();
        movingPackets = new ArrayList<>();
        impactManager = new ImpactManager();
        leaderboardData = new LeaderboardData(); // Initialize with empty data
        // Load leaderboard data asynchronously from database
        DatabaseLeaderboardManager.loadLeaderboard().thenAccept(data -> {
            if (data != null) {
                this.leaderboardData = data;
            }
        });
        
        // Initialize test configuration
        initializeTestConfiguration();
        
        lastUpdateTime = java.lang.System.nanoTime();
        gameStartTime = java.lang.System.currentTimeMillis(); // Initialize game start time
        init();
    }
    
    /**
     * Initializes the game by loading level data and setting up systems.
     * Creates all network systems, wires, and establishes spy network connections.
     */
    private void init() {
        LevelData levelData = LevelLoader.loadLevel(1); // Load level 1 - original starting level
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
        
        // Load wires from level data
        loadWiresFromLevel(levelData);
        
        // Set up wire callbacks for dynamic length updates
        setupWireCallbacks();
        
        // Set initial wire length from level data
        if (levelData.playerStart != null) {
            totalWireLength = levelData.playerStart.initialWireLength;
            Logger.getInstance().info("Level loaded with initial wire length: " + totalWireLength + "m");
        }
        
        // Initialize shop manager
        shopManager = new ShopManager(getCoins());
        
        // Add Phase 1 shop items (extensible design)
        shopManager.addShopItem(new OAtar(
            () -> {
                // Activate O' Atar - disable impact waves
                impactWavesDisabled = true;
                Logger.getInstance().info("O' Atar activated! Impact waves disabled");
            },
            () -> {
                // Deactivate O' Atar - re-enable impact waves
                impactWavesDisabled = false;
                Logger.getInstance().info("O' Atar expired! Impact waves re-enabled");
            }
        ));
        
        shopManager.addShopItem(new OAiryaman(
            () -> {
                // Activate O' Airyaman - disable packet collisions
                packetCollisionsDisabled = true;
                Logger.getInstance().info("O' Airyaman activated! Packet collisions disabled");
            },
            () -> {
                // Deactivate O' Airyaman - re-enable packet collisions
                packetCollisionsDisabled = false;
                Logger.getInstance().info("O' Airyaman expired! Packet collisions re-enabled");
            }
        ));
        
        shopManager.addShopItem(new OAnahita(
            () -> {
                // Activate O' Anahita - zero all packet noise
                packetNoiseZeroed = true;
                Logger.getInstance().info("O' Anahita activated! All packet noise set to zero");
            }
        ));
        
        // Add Phase 2 shop items
        shopManager.addShopItem(new ScrollOfAergia(
            () -> {
                // Activate Scroll of Aergia - zero acceleration at selected point
                Logger.getInstance().info("Scroll of Aergia activated! Select a point on a wire to stop acceleration");
                // TODO: Implement point selection and acceleration stopping logic
            },
            () -> {
                // Deactivate Scroll of Aergia - restore acceleration
                Logger.getInstance().info("Scroll of Aergia expired! Acceleration restored");
                // TODO: Implement acceleration restoration logic
            }
        ));
        
        shopManager.addShopItem(new ScrollOfSisyphus(
            () -> {
                // Activate Scroll of Sisyphus - move system
                Logger.getInstance().info("Scroll of Sisyphus activated! Select a non-reference system to move");
                // TODO: Implement system selection and movement logic
            }
        ));
        
        shopManager.addShopItem(new ScrollOfEliphas(
            () -> {
                // Activate Scroll of Eliphas - restore gravity at selected point
                Logger.getInstance().info("Scroll of Eliphas activated! Select a point on a wire to restore gravity");
                // TODO: Implement point selection and gravity restoration logic
            },
            () -> {
                // Deactivate Scroll of Eliphas - stop gravity restoration
                Logger.getInstance().info("Scroll of Eliphas expired! Gravity restoration stopped");
                // TODO: Implement gravity restoration stop logic
            }
        ));
    }

    /**
     * Starts the main game loop in a separate thread.
     * The game loop runs at the target FPS and UPS rates.
     */
    public void startGameLoop() {
        gameThread = new Thread(this);
        running = true;
        gameThread.start();
    }

    /**
     * Main game loop that runs at target FPS and UPS.
     * Separates update logic from rendering for consistent performance.
     */
    @Override
    public void run() {
        double timePerFrame = 1_000_000_000.0 / FPS_SET;   // Time per frame in nanoseconds
        double timePerUpdate = 1_000_000_000.0 / UPS_SET;  // Time per update in nanoseconds
        long previousTime = java.lang.System.nanoTime();
        double deltaU = 0;  // Accumulated update time
        double deltaF = 0;  // Accumulated frame time
        long lastCheck = java.lang.System.currentTimeMillis();
        int frames = 0;     // Frame counter for FPS display
        int updates = 0;    // Update counter for UPS display

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
    
    /**
     * Main update method called every frame.
     * Updates all game entities, handles collisions, and manages time travel.
     */
    private void update() {
        if (isPaused && !isTimeTravelMode) {
            return; // Don't update when paused
        }
        
        if (isTimeTravelMode) {
            updateTimeTravel();
            // Update HUD during time travel to show current state
            updateHUD();
            return;
        }
        
        // Update test system
        updateTestSystem();
        
        // Update game time
        currentGameTime = java.lang.System.currentTimeMillis() - gameStartTime;
        
        // Create snapshots for time travel BEFORE any state changes
        if (currentGameTime - lastSnapshotTime >= snapshotInterval) {
            // Debug: Check packet count before snapshot
            int packetCount = movingPackets.size();
            if (packetCount > 0) {
                java.lang.System.out.println("Creating snapshot with " + packetCount + " packets, coins: " + coins);
            }
            createTimeSnapshot();
            lastSnapshotTime = currentGameTime;
        }

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
            if (movingPacket == null || movingPacket.isLost()) {
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
                
                // Count test packet losses
                if (isTestRunning && packet.isPlayerSpawned() && !packet.isTestPacketReturned()) {
                    testPacketsLost++;
                    Logger.getInstance().info("💥 Test packet destroyed! Total lost: " + testPacketsLost + "/" + testPacketsReleased);
                }
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
            // Remove all packets that are on this destroyed wire
            List<MovingPacket> packetsToRemove = new ArrayList<>();
            for (MovingPacket packet : movingPackets) {
                if (packet.getWire() == wire) {
                    packetsToRemove.add(packet);
                    Logger.getInstance().warning("Packet lost due to wire destruction: " + packet.getPacket().getType());
                }
            }
            movingPackets.removeAll(packetsToRemove);
            
            // Remove the wire
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
            if (wire != null) {
                // Highlight selected wire
                if (wire == selectedWire) {
                    g.setColor(Color.YELLOW);
                    g.setStroke(new BasicStroke(4));
                    // Draw highlight behind the wire
                    List<Point> points = wire.getAllPoints();
                    for (int i = 0; i < points.size() - 1; i++) {
                        Point p1 = points.get(i);
                        Point p2 = points.get(i + 1);
                        g.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
                wire.draw(g);
            }
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
            g.setColor(Config.WIRING_MODE_COLOR);
            g.setFont(new Font(Config.FONT_NAME, Font.BOLD, Config.WIRING_MODE_FONT_SIZE));
            g.drawString(Config.WIRING_MODE_TEXT, 10, 20);
        }
        
        // Draw shop overlay if open
        if (isShopOpen) {
            drawShopOverlay(g);
        }
        
        // Display impact system info
        g.setColor(Config.IMPACT_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, Config.IMPACT_FONT_SIZE));
        g.drawString("Active Impacts: " + getActiveImpactCount(), 10, 40);
        
        // Draw impact effects
        for (Impact impact : impactManager.getActiveImpacts()) {
            Point collisionPoint = impact.getCollisionPoint();
            g.setColor(Config.IMPACT_COLOR);
            g.fillOval(collisionPoint.x - 5, collisionPoint.y - 5, 10, 10);
            g.setColor(Config.SYSTEM_TEXT_COLOR);
            g.drawString(Config.IMPACT_TEXT, collisionPoint.x + 10, collisionPoint.y + 5);
        }
        
        // Display packet status
        List<MovingPacket> packetsCopy = new ArrayList<>(movingPackets);
        for (MovingPacket packet : packetsCopy) {
            Point pos = packet.getPacket().getPosition();
            if (packet.isLost()) {
                g.setColor(Config.DESTROYED_COLOR);
                g.setFont(new Font(Config.FONT_NAME, Font.BOLD, Config.DESTROYED_FONT_SIZE));
                g.drawString(Config.DESTROYED_TEXT, pos.x + 20, pos.y - 10);
            } else if (packet.getNoiseLevel() > 0) {
                g.setColor(Config.PACKET_COUNT_COLOR);
                g.setFont(new Font(Config.FONT_NAME, Font.PLAIN, Config.MEDIUM_FONT_SIZE));
                String noiseText = String.format("Noise: %.1f", packet.getNoiseLevel());
                g.drawString(noiseText, pos.x + 20, pos.y - 10);
            }
            
            // Phase 2: Display packet type and behavior info
            g.setColor(Color.WHITE);
            g.setFont(new Font(Config.FONT_NAME, Font.PLAIN, Config.SMALL_FONT_SIZE));
            String packetInfo = String.format("%s", packet.getPacket().getType());
            g.drawString(packetInfo, pos.x + 20, pos.y + 20);
        }
        
        // Display wire system status
        g.setColor(Config.TIME_TRAVEL_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, Config.LARGE_FONT_SIZE));
        g.drawString("Wire System: Collision Detection Active", 10, 60);
        g.drawString("Arc Points: 1 coin each", 10, 80);
        g.drawString("Wire Degradation: 3 bulk packet limit", 10, 100);
        
        // Display time travel status
        if (isTimeTravelMode) {
            g.setColor(Config.SNAPSHOT_COLOR);
            g.setFont(new Font(Config.FONT_NAME, Font.BOLD, Config.TIME_TRAVEL_FONT_SIZE));
            g.drawString(Config.TIME_TRAVEL_MODE_TEXT, 10, 140);
            g.setFont(new Font(Config.FONT_NAME, Font.PLAIN, Config.LARGE_FONT_SIZE));
            g.drawString("Snapshot: " + (currentSnapshotIndex + 1) + "/" + timeSnapshots.size(), 10, 160);
            g.drawString("Use LEFT/RIGHT arrows to navigate", 10, 180);
            g.drawString("Press T to exit time travel", 10, 200);
        } else if (isPaused) {
            g.setColor(Config.WIRING_MODE_COLOR);
            g.setFont(new Font(Config.FONT_NAME, Font.BOLD, Config.TIME_TRAVEL_FONT_SIZE));
            g.drawString("GAME PAUSED", 10, 140);
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Press P to resume", 10, 160);
            g.drawString("Press T for time travel", 10, 180);
        } else {
            // Display time travel info when not in time travel mode
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 10));
            g.drawString("Time: " + (currentGameTime / 1000.0) + "s", 10, 140);
            g.drawString("Snapshots: " + timeSnapshots.size() + " (disk: " + getDiskSnapshotCount() + ")", 10, 160);
            g.drawString("SPACE=spawn, P=pause, T=time travel", 10, 180);
        }
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
        
        destinationSystem.receiveMovingPacket(arrivedPacket, this);
        
        // Count test packets that reach the END reference system (only inputs, no outputs)
        if (isTestRunning && arrivedPacket.isPlayerSpawned() && !arrivedPacket.isTestPacketReturned()) {
            // Check if this is an END reference system (has only inputs, no outputs)
            if (isReferenceSystem(destinationSystem) && destinationSystem.getOutputPorts().isEmpty()) {
                arrivedPacket.setTestPacketReturned(true);
                testPacketsReturned++;
                Logger.getInstance().info("✅ Test packet reached END reference system! Total returned: " + testPacketsReturned + "/" + testPacketsReleased);
                
                // Add coins for reaching the END system
                int coinsBefore = coins;
                addCoinsForPacketEntry(arrivedPacket.getPacket());
                Logger.getInstance().info("Coins added: " + coinsBefore + " -> " + coins);
            }
        }
        
        // Log packet arrival (coins already added above for test packets)
        if (!isTestRunning || !arrivedPacket.isPlayerSpawned()) {
            Logger.getInstance().info("Packet " + arrivedPacket.getPacket().getType() + " reached system - no coins added (manual spawn or not test run).");
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
        } else if (packet instanceof TrojanPacket) {
            coinsToAdd = ((TrojanPacket) packet).getCoinReward(); // Trojan packets give 0 coins
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
        
        // Select output port using the new logic: prioritize compatible empty ports, then random selection
        Wire targetWire = selectOutputPort(packet, outgoingWires);
        
        if (targetWire == null) {
            // No available ports - store packet in system if capacity allows
            if (canStorePacketInSystem(currentSystem)) {
                storePacketInSystem(packet, currentSystem);
                Logger.getInstance().info("Packet " + packet.getType() + " stored in system " + currentSystem.getId() + " (no available ports)");
            } else {
                Logger.getInstance().info("Packet " + packet.getType() + " lost - system storage full and no available ports");
                testPacketsLost++;
            }
            return;
        }

        MovingPacket movingPacket = new MovingPacket(packet, targetWire);
        
        // Apply port compatibility effects
        boolean isCompatible = isPortCompatible(targetWire.getStartPort().getType(), packet.getType());
        movingPacket.applyPortCompatibilityEffect(targetWire.getStartPort().getType(), isCompatible);
        
        movingPackets.add(movingPacket);
    }
    
    /**
     * Selects an output port for a packet using the required logic:
     * 1. Prioritize compatible empty ports
     * 2. If no compatible empty ports, select randomly from all empty ports
     * 3. If no empty ports, return null (packet should be stored)
     */
    private Wire selectOutputPort(Packet packet, List<Wire> outgoingWires) {
        // Get all empty ports (ports with no packets on their wires)
        List<Wire> emptyWires = outgoingWires.stream()
                .filter(wire -> !isWireOccupied(wire))
                .collect(Collectors.toList());
        
        if (emptyWires.isEmpty()) {
            return null; // No empty ports available
        }
        
        // Get compatible empty ports
        List<Wire> compatibleEmptyWires = emptyWires.stream()
                .filter(wire -> isPortCompatible(wire.getStartPort().getType(), packet.getType()))
                .collect(Collectors.toList());
        
        // Prioritize compatible empty ports
        if (!compatibleEmptyWires.isEmpty()) {
            int randomIndex = (int) (Math.random() * compatibleEmptyWires.size());
            return compatibleEmptyWires.get(randomIndex);
        } else {
            // No compatible empty ports, select randomly from all empty ports
            int randomIndex = (int) (Math.random() * emptyWires.size());
            return emptyWires.get(randomIndex);
        }
    }
    
    /**
     * Checks if a wire is occupied by any moving packet
     */
    private boolean isWireOccupied(Wire wire) {
        return movingPackets.stream()
                .anyMatch(movingPacket -> movingPacket.getWire() == wire);
    }
    
    /**
     * Checks if a system can store more packets (capacity check)
     */
    private boolean canStorePacketInSystem(System system) {
        // For now, we'll use a simple approach - check if system has storage capacity
        // This could be enhanced to track actual stored packets per system
        return true; // Assume systems can always store packets for now
    }
    
    /**
     * Stores a packet in a system when no output ports are available
     */
    private void storePacketInSystem(Packet packet, System system) {
        // For now, we'll just log this - in a full implementation, 
        // you'd want to track stored packets per system and release them when ports become available
        Logger.getInstance().info("Packet " + packet.getType() + " stored in system " + system.getId());
    }

    public void routeMovingPacket(MovingPacket movingPacket, System currentSystem) {
        if (currentSystem instanceof ReferenceSystem) {
            Logger.getInstance().info("Packet " + movingPacket.getPacket().getType() + " returned home.");
            return;
        }

        List<Wire> outgoingWires = wires.stream()
                .filter(wire -> wire.getStartPort().getParentSystem() == currentSystem)
                .collect(Collectors.toList());

        if (outgoingWires.isEmpty()) {
            Logger.getInstance().info("Packet " + movingPacket.getPacket().getType() + " is stuck. No outgoing wires.");
            return;
        }

        // Select output port using the same logic: prioritize compatible empty ports, then random selection
        Wire targetWire = selectOutputPort(movingPacket.getPacket(), outgoingWires);
        
        if (targetWire == null) {
            // No available ports - store packet in system if capacity allows
            if (canStorePacketInSystem(currentSystem)) {
                storePacketInSystem(movingPacket.getPacket(), currentSystem);
                Logger.getInstance().info("Packet " + movingPacket.getPacket().getType() + " stored in system " + currentSystem.getId() + " (no available ports)");
            } else {
                Logger.getInstance().info("Packet " + movingPacket.getPacket().getType() + " lost - system storage full and no available ports");
                testPacketsLost++;
            }
            return;
        }

        // Create a new MovingPacket with the same packet but new wire
        MovingPacket newMovingPacket = new MovingPacket(movingPacket.getPacket(), targetWire);
        newMovingPacket.setPlayerSpawned(movingPacket.isPlayerSpawned());
        
        // Apply port compatibility effects
        boolean isCompatible = isPortCompatible(targetWire.getStartPort().getType(), movingPacket.getPacket().getType());
        newMovingPacket.applyPortCompatibilityEffect(targetWire.getStartPort().getType(), isCompatible);
        
        movingPackets.add(newMovingPacket);
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
        spawnPacket(packet, spawner, false); // Default to system-spawned
    }
    
    public void spawnPacket(Packet packet, ReferenceSystem spawner, boolean playerSpawned) {
        List<Wire> availableWires = wires.stream()
                .filter(wire -> wire.getStartPort().getParentSystem() == spawner)
                .collect(Collectors.toList());

        if (!availableWires.isEmpty()) {
            Wire targetWire = availableWires.get((int) (Math.random() * availableWires.size()));
            MovingPacket movingPacket = new MovingPacket(packet, targetWire);
            
            // Add spawn protection to prevent immediate destruction
            movingPacket.setSpawnProtection(true);
            
            // Mark if packet was spawned by player
            movingPacket.setPlayerSpawned(playerSpawned);
            
            movingPackets.add(movingPacket);
            Logger.getInstance().info("Packet " + packet.getType() + " spawned with spawn protection. Total packets: " + movingPackets.size());
            Logger.getInstance().info("Packet path has " + movingPacket.getWire().getAllPoints().size() + " points");
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
            spawnPacket(new MessengerPacket(pos.x, pos.y, packetType), selectedSpawner, true); // Mark as player-spawned
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
    
    /**
     * Toggles shop overlay
     */
    public void toggleShop() {
        this.isShopOpen = !this.isShopOpen;
        if (isShopOpen) {
            // Pause the game when shop opens
            pauseGame();
            Logger.getInstance().info("Shop opened - Game paused");
        } else {
            // Resume the game when shop closes
            resumeGame();
            Logger.getInstance().info("Shop closed - Game resumed");
        }
    }
    
    /**
     * Checks if shop is open
     */
    public boolean isShopOpen() {
        return isShopOpen;
    }
    
    /**
     * Closes shop
     */
    public void closeShop() {
        this.isShopOpen = false;
        Logger.getInstance().info("Shop closed");
    }
    
    /**
     * Draws the shop overlay
     */
    private void drawShopOverlay(Graphics2D g) {
        int screenWidth = gamePanel.getWidth();
        int screenHeight = gamePanel.getHeight();
        
        // Draw semi-transparent background
        g.setColor(Config.Shop.SHOP_BACKGROUND_COLOR);
        g.fillRect(0, 0, screenWidth, screenHeight);
        
        // Calculate shop panel position (centered)
        int panelX = (screenWidth - Config.Shop.SHOP_PANEL_WIDTH) / 2;
        int panelY = (screenHeight - Config.Shop.SHOP_PANEL_HEIGHT) / 2;
        
        // Draw shop panel
        g.setColor(Config.Shop.SHOP_PANEL_COLOR);
        g.fillRect(panelX, panelY, Config.Shop.SHOP_PANEL_WIDTH, Config.Shop.SHOP_PANEL_HEIGHT);
        
        // Draw shop border
        g.setColor(Config.Shop.SHOP_BORDER_COLOR);
        g.setStroke(new BasicStroke(2));
        g.drawRect(panelX, panelY, Config.Shop.SHOP_PANEL_WIDTH, Config.Shop.SHOP_PANEL_HEIGHT);
        g.setStroke(new BasicStroke(1));
        
        // Draw shop title
        g.setColor(Config.Shop.SHOP_TITLE_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, 24));
        String title = "🛒 SHOP";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, panelX + (Config.Shop.SHOP_PANEL_WIDTH - titleWidth) / 2, panelY + 40);
        
        // Draw coins display
        g.setColor(Config.Shop.SHOP_PRICE_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, 18));
        String coinsText = "💰 Coins: " + getCoins();
        g.drawString(coinsText, panelX + Config.Shop.SHOP_MARGIN, panelY + 70);
        
        // Draw shop items using ShopManager
        int itemY = panelY + 100;
        g.setFont(new Font(Config.FONT_NAME, Font.PLAIN, 16));
        
        if (shopManager != null) {
            for (int i = 0; i < shopManager.getShopItems().size(); i++) {
                var item = shopManager.getShopItems().get(i);
                drawShopItem(g, panelX, itemY, item.getName(), item.getCost(), item.getKeyBinding());
                itemY += Config.Shop.SHOP_ITEM_HEIGHT;
            }
        }
        
        // Draw close button
        int closeButtonX = panelX + Config.Shop.SHOP_PANEL_WIDTH - Config.Shop.SHOP_BUTTON_WIDTH - Config.Shop.SHOP_MARGIN;
        int closeButtonY = panelY + Config.Shop.SHOP_PANEL_HEIGHT - Config.Shop.SHOP_BUTTON_HEIGHT - Config.Shop.SHOP_MARGIN;
        
        g.setColor(Config.Shop.SHOP_BUTTON_COLOR);
        g.fillRect(closeButtonX, closeButtonY, Config.Shop.SHOP_BUTTON_WIDTH, Config.Shop.SHOP_BUTTON_HEIGHT);
        g.setColor(Config.Shop.SHOP_BORDER_COLOR);
        g.drawRect(closeButtonX, closeButtonY, Config.Shop.SHOP_BUTTON_WIDTH, Config.Shop.SHOP_BUTTON_HEIGHT);
        
        g.setColor(Config.Shop.SHOP_TEXT_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, 14));
        String closeText = "Close (B)";
        int closeTextWidth = g.getFontMetrics().stringWidth(closeText);
        g.drawString(closeText, closeButtonX + (Config.Shop.SHOP_BUTTON_WIDTH - closeTextWidth) / 2, closeButtonY + 20);
        
        // Draw instructions
        g.setColor(Config.Shop.SHOP_TEXT_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.PLAIN, 12));
        String instructions = "Press B1-B4 to buy items, B to close";
        g.drawString(instructions, panelX + Config.Shop.SHOP_MARGIN, panelY + Config.Shop.SHOP_PANEL_HEIGHT - 10);
    }
    
    /**
     * Draws a shop item
     */
    private void drawShopItem(Graphics2D g, int panelX, int itemY, String itemName, int cost, String key) {
        // Item background
        g.setColor(new Color(60, 60, 60));
        g.fillRect(panelX + Config.Shop.SHOP_MARGIN, itemY, Config.Shop.SHOP_PANEL_WIDTH - 2 * Config.Shop.SHOP_MARGIN, Config.Shop.SHOP_ITEM_HEIGHT - 5);
        
        // Item border
        g.setColor(Config.Shop.SHOP_BORDER_COLOR);
        g.drawRect(panelX + Config.Shop.SHOP_MARGIN, itemY, Config.Shop.SHOP_PANEL_WIDTH - 2 * Config.Shop.SHOP_MARGIN, Config.Shop.SHOP_ITEM_HEIGHT - 5);
        
        // Item name
        g.setColor(Config.Shop.SHOP_ITEM_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.BOLD, 14));
        g.drawString(itemName, panelX + Config.Shop.SHOP_MARGIN + 10, itemY + 20);
        
        // Price
        g.setColor(Config.Shop.SHOP_PRICE_COLOR);
        g.drawString("💰 " + cost + " coins", panelX + Config.Shop.SHOP_PANEL_WIDTH - 150, itemY + 20);
        
        // Key binding
        g.setColor(Config.Shop.SHOP_TEXT_COLOR);
        g.setFont(new Font(Config.FONT_NAME, Font.PLAIN, 12));
        g.drawString("(" + key + ")", panelX + Config.Shop.SHOP_PANEL_WIDTH - 50, itemY + 20);
    }
    
    /**
     * Handles shop purchases using ShopManager (SOLID principles)
     */
    public void handleShopPurchase(int itemNumber) {
        if (!isShopOpen || shopManager == null) return;
        
        // Update shop manager with current coins
        shopManager.setPlayerCoins(getCoins());
        
        // Attempt purchase (0-based index)
        if (shopManager.purchaseItem(itemNumber - 1)) {
            // Update game coins to match shop manager
            setCoins(shopManager.getPlayerCoins());
            
            // Update HUD
            if (gamePanel != null) {
                gamePanel.updateHUDData(getRemainingWireLength(), 0, 0, getCoins());
            }
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
    
    public void handleLeftMousePress(Point point, boolean ctrlPressed) {
        if (ctrlPressed) {
            // Ctrl+click to remove wire
            Wire wireToRemove = findWireAt(point);
            if (wireToRemove != null) {
                removeWire(wireToRemove);
                Logger.getInstance().info("Wire removed by Ctrl+click");
            }
        } else {
            // Regular click - select wire if clicked on one
            Wire clickedWire = findWireAt(point);
            if (clickedWire != null) {
                selectedWire = clickedWire;
                Logger.getInstance().info("Wire selected for removal (press Delete to remove)");
            } else {
                selectedWire = null;
            }
            handleLeftMousePress(point);
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
            // Deduct coin cost for moving the system
            coins -= Config.SYSTEM_MOVE_COST;
            Logger.getInstance().info("System moved - cost: -" + Config.SYSTEM_MOVE_COST + " coin. Remaining coins: " + coins);
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
        if (wire.getArcPoints().size() < Config.MAX_ARC_POINTS_PER_WIRE) {
            // Check if player has enough coins (1 coin per arc point)
            if (coins >= Config.ARC_POINT_COST) {
                // Deduct coin cost
                coins -= Config.ARC_POINT_COST;
                Logger.getInstance().info("Arc point cost: -" + Config.ARC_POINT_COST + " coin. Remaining coins: " + coins);
                    
                    // Add the arc point
                    wire.addArcPoint(point);
                    
                    // Update wire length when arc points are added
                    Logger.getInstance().info("Arc point added to wire - updating length...");
                    updateWireLengthForWire(wire);
        } else {
                    Logger.getInstance().warning("Cannot add arc point - insufficient coins (need " + Config.ARC_POINT_COST + ", have " + coins + ")");
                }
            } else {
                Logger.getInstance().info("Cannot add more arc points - limit reached (" + Config.MAX_ARC_POINTS_PER_WIRE + ")");
            }
        }
    }

    private void handleSystemDragPress(Point point) {
        List<System> reversedSystems = new ArrayList<>(systems);
        Collections.reverse(reversedSystems);
        for (System system : reversedSystems) {
            if (system.contains(point)) {
                // Check if player has enough coins to move the system
                if (coins >= Config.SYSTEM_MOVE_COST) {
                    draggedSystem = system;
                    dragOffset = new Point(point.x - system.getX(), point.y - system.getY());
                    Logger.getInstance().info("System drag started - " + Config.SYSTEM_MOVE_COST + " coin will be deducted on release");
                    return;
                } else {
                    Logger.getInstance().warning("Cannot move system - insufficient coins (need " + Config.SYSTEM_MOVE_COST + ", have " + coins + ")");
                    return;
                }
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
        final double CLICK_THRESHOLD = Config.CLICK_THRESHOLD; // Increased threshold for easier wire detection
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
     * Removes a wire from the game
     */
    public void removeWire(Wire wire) {
        if (wires.remove(wire)) {
            // Get the stored wire length and refund some of it
            Integer storedLength = wireLengths.get(wire);
            if (storedLength != null) {
                double refundAmount = storedLength * 0.8; // 80% refund
                totalWireLength += refundAmount;
                usedWireLength -= storedLength;
                wireLengths.remove(wire);
                Logger.getInstance().info("Wire removed - refunded " + String.format("%.1f", refundAmount) + "m wire length");
            }
        }
    }
    
    /**
     * Removes the currently selected wire
     */
    public void removeSelectedWire() {
        if (selectedWire != null) {
            removeWire(selectedWire);
            selectedWire = null;
        } else {
            Logger.getInstance().info("No wire selected. Click on a wire first, then press Delete.");
        }
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
    // public int getActiveSystemCount() {
    //     int count = 0;
    //     for (System system : systems) {
    //         if (system != null) {
    //             count++;
    //         }
    //     }
    //     return count;
    // }
    
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
            
            // Check for real-time game over condition
            checkGameOverCondition();
        }
    }
    
    /**
     * Checks if game over condition is met (packet loss > 50%) and triggers game over
     */
    private void checkGameOverCondition() {
        if (testPacketsReleased > 0) {
            double packetLossPercentage = getPacketLossPercentage();
            if (packetLossPercentage >= Config.GameConditions.MAX_PACKET_LOSS_PERCENTAGE && !gameLost && !gameWon) {
                Logger.getInstance().info("💀 GAME OVER! Packet loss exceeded 50%: " + String.format("%.1f", packetLossPercentage) + "%");
                triggerGameOver();
            }
        }
    }
    
    /**
     * Triggers game over state and shows game over dialog
     */
    private void triggerGameOver() {
        gameLost = true;
        testCompleted = true;
        
        double packetLossPercentage = getPacketLossPercentage();
        int healthyPackets = testPacketsReturned; // Packets that successfully reached destination
        
        Logger.getInstance().info("🎯 GAME OVER! Packets: " + testPacketsReleased + " released, " + 
                                healthyPackets + " healthy, " + testPacketsLost + " lost");
        
        // Show game over dialog
        showGameOverDialog(packetLossPercentage, healthyPackets);
    }
    
    /**
     * Shows game over dialog with healthy packets count and packet loss percentage
     */
    private void showGameOverDialog(double packetLossPercentage, int healthyPackets) {
        javax.swing.SwingUtilities.invokeLater(() -> {
            TestResultDialog.TestResult result = TestResultDialog.showDialog(
                null, 
                false, // won = false (game over)
                packetLossPercentage, 
                testPacketsReleased, 
                healthyPackets
            );
            
            if (result == TestResultDialog.TestResult.RESET_LEVEL) {
                resetCurrentLevel();
            } else if (result == TestResultDialog.TestResult.CLOSE) {
                // Close dialog - for now just log, as gameStateManager is not available in this context
                Logger.getInstance().info("Game over dialog closed");
            }
        });
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
    
    public double getPacketSpeedMultiplier() {
        return packetSpeedMultiplier;
    }
    
    // Phase 1 temporary effects getters
    public boolean isImpactWavesDisabled() {
        return impactWavesDisabled;
    }
    
    public boolean isPacketCollisionsDisabled() {
        return packetCollisionsDisabled;
    }
    
    public boolean isPacketNoiseZeroed() {
        return packetNoiseZeroed;
    }
    
    // Test system getters
    public boolean isTestCompleted() {
        return testCompleted;
    }
    
    public boolean isGameWon() {
        return gameWon;
    }
    
    public boolean isGameLost() {
        return gameLost;
    }
    
    public double getPacketLossPercentage() {
        if (testPacketsReleased == 0) return 0.0;
        // Calculate packet loss based on total planned packets for the test, not just released so far
        int totalPlannedPackets = Config.GameConditions.TEST_PACKET_COUNT;
        return ((double)testPacketsLost / totalPlannedPackets) * 100;
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
     * Loads wires from level data
     */
    private void loadWiresFromLevel(LevelData levelData) {
        if (levelData.wires == null || levelData.wires.isEmpty()) {
            Logger.getInstance().info("No wires defined in level data");
            return;
        }
        
        // Create a map of system IDs to systems for quick lookup
        Map<String, System> systemMap = new HashMap<>();
        for (System system : systems) {
            systemMap.put(system.getId(), system);
        }
        
        int wireCount = 0;
        for (LevelData.WireData wireData : levelData.wires) {
            System startSystem = systemMap.get(wireData.startSystemId);
            System endSystem = systemMap.get(wireData.endSystemId);
            
            if (startSystem == null || endSystem == null) {
                Logger.getInstance().warning("Cannot create wire: system not found. Start: " + wireData.startSystemId + ", End: " + wireData.endSystemId);
                continue;
            }
            
            // Get the ports
            List<Port> startPorts = startSystem.getOutputPorts();
            List<Port> endPorts = endSystem.getInputPorts();
            
            if (wireData.startPortIndex >= startPorts.size() || wireData.endPortIndex >= endPorts.size()) {
                Logger.getInstance().warning("Cannot create wire: port index out of bounds. Start: " + wireData.startPortIndex + ", End: " + wireData.endPortIndex);
                continue;
            }
            
            Port startPort = startPorts.get(wireData.startPortIndex);
            Port endPort = endPorts.get(wireData.endPortIndex);
            
            // Create the wire
            Wire wire = new Wire(startPort);
            wire.setEndPort(endPort);
            wires.add(wire);
            wireCount++;
            
            Logger.getInstance().info("Created wire from " + wireData.startSystemId + " to " + wireData.endSystemId);
        }
        
        Logger.getInstance().info("Loaded " + wireCount + " wires from level data");
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
    
    // ==================== SAVE/LOAD SYSTEM ====================
    
    /**
     * Saves the current game state to a JSON file
     */
    public boolean saveGame() {
        return SaveManager.saveGame(this);
    }
    
    /**
     * Loads game state from a JSON file
     */
    public boolean loadGame() {
        return SaveManager.loadGame(this);
    }
    
    /**
     * Clears the current game state (used when loading)
     */
    public void clearGameState() {
        systems.clear();
        wires.clear();
        movingPackets.clear();
        wireLengths.clear();
        usedWireLength = 0;
        coins = 0;
        inWiringMode = false;
        previewWire = null;
        draggedSystem = null;
        draggedArcPoint = null;
        currentMousePos = new Point(0, 0);
        
        // Reset shop and effects state
        impactWavesDisabled = false;
        packetCollisionsDisabled = false;
        packetNoiseZeroed = false;
        gameStartTime = java.lang.System.currentTimeMillis();
        currentGameTime = 0;
        
        // Reset test system state
        isTestRunning = false;
        testPacketsReleased = 0;
        testPacketsReturned = 0;
        testStartTime = 0;
        lastPacketReleaseTime = 0;
        testCompleted = false;
        gameWon = false;
        gameLost = false;
        
        // Reset time travel state
        isTimeTravelMode = false;
        isPaused = false;
        currentSnapshotIndex = -1;
        timeTravelWindowSeconds = Config.TIME_TRAVEL_WINDOW_SECONDS;
        
        Logger.getInstance().info("Game state cleared");
    }
    
    /**
     * Adds a system to the game (used when loading)
     */
    public void addSystem(System system) {
        systems.add(system);
    }
    
    /**
     * Adds a wire to the game (used when loading)
     */
    public void addWire(Wire wire) {
        wires.add(wire);
    }
    
    /**
     * Adds a moving packet to the game (used when loading)
     */
    public void addMovingPacket(MovingPacket movingPacket) {
        movingPackets.add(movingPacket);
    }
    
    /**
     * Sets the wire lengths map (used when loading)
     */
    public void setWireLengths(Map<Wire, Integer> wireLengths) {
        this.wireLengths = new HashMap<>(wireLengths);
    }
    
    /**
     * Sets the used wire length (used when loading)
     */
    public void setUsedWireLength(int usedWireLength) {
        this.usedWireLength = usedWireLength;
    }
    
    /**
     * Sets the total wire length (used when loading)
     */
    public void setTotalWireLength(int totalWireLength) {
        this.totalWireLength = totalWireLength;
    }
    
    /**
     * Sets the coins (used when loading)
     */
    public void setCoins(int coins) {
        this.coins = coins;
    }
    
    /**
     * Sets the wiring mode (used when loading)
     */
    public void setWiringMode(boolean isWiringMode) {
        this.inWiringMode = isWiringMode;
    }
    
    // ==================== ADDITIONAL GETTERS AND SETTERS FOR ENHANCED SAVE SYSTEM ====================
    
    /**
     * Gets the game start time
     */
    public long getGameStartTime() {
        return gameStartTime;
    }
    
    /**
     * Sets the game start time (used when loading)
     */
    public void setGameStartTime(long gameStartTime) {
        this.gameStartTime = gameStartTime;
    }
    
    /**
     * Sets the current game time (used when loading)
     */
    public void setCurrentGameTime(long currentGameTime) {
        this.currentGameTime = currentGameTime;
    }
    
    /**
     * Sets impact waves disabled state (used when loading)
     */
    public void setImpactWavesDisabled(boolean impactWavesDisabled) {
        this.impactWavesDisabled = impactWavesDisabled;
    }
    
    /**
     * Sets packet collisions disabled state (used when loading)
     */
    public void setPacketCollisionsDisabled(boolean packetCollisionsDisabled) {
        this.packetCollisionsDisabled = packetCollisionsDisabled;
    }
    
    /**
     * Sets packet noise zeroed state (used when loading)
     */
    public void setPacketNoiseZeroed(boolean packetNoiseZeroed) {
        this.packetNoiseZeroed = packetNoiseZeroed;
    }
    
    /**
     * Gets test running state
     */
    public boolean isTestRunning() {
        return isTestRunning;
    }
    
    /**
     * Sets test running state (used when loading)
     */
    public void setTestRunning(boolean isTestRunning) {
        this.isTestRunning = isTestRunning;
    }
    
    /**
     * Gets test packets released count
     */
    public int getTestPacketsReleased() {
        return testPacketsReleased;
    }
    
    /**
     * Sets test packets released count (used when loading)
     */
    public void setTestPacketsReleased(int testPacketsReleased) {
        this.testPacketsReleased = testPacketsReleased;
    }
    
    /**
     * Gets test packets returned count
     */
    public int getTestPacketsReturned() {
        return testPacketsReturned;
    }
    
    /**
     * Sets test packets returned count (used when loading)
     */
    public void setTestPacketsReturned(int testPacketsReturned) {
        this.testPacketsReturned = testPacketsReturned;
    }
    
    /**
     * Gets test start time
     */
    public long getTestStartTime() {
        return testStartTime;
    }
    
    /**
     * Sets test start time (used when loading)
     */
    public void setTestStartTime(long testStartTime) {
        this.testStartTime = testStartTime;
    }
    
    /**
     * Gets last packet release time
     */
    public long getLastPacketReleaseTime() {
        return lastPacketReleaseTime;
    }
    
    /**
     * Sets last packet release time (used when loading)
     */
    public void setLastPacketReleaseTime(long lastPacketReleaseTime) {
        this.lastPacketReleaseTime = lastPacketReleaseTime;
    }
    
    /**
     * Sets test completed state (used when loading)
     */
    public void setTestCompleted(boolean testCompleted) {
        this.testCompleted = testCompleted;
    }
    
    /**
     * Sets game won state (used when loading)
     */
    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }
    
    /**
     * Sets game lost state (used when loading)
     */
    public void setGameLost(boolean gameLost) {
        this.gameLost = gameLost;
    }
    
    /**
     * Sets time travel mode (used when loading)
     */
    public void setTimeTravelMode(boolean isTimeTravelMode) {
        this.isTimeTravelMode = isTimeTravelMode;
    }
    
    /**
     * Sets paused state (used when loading)
     */
    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }
    
    /**
     * Sets current snapshot index (used when loading)
     */
    public void setCurrentSnapshotIndex(int currentSnapshotIndex) {
        this.currentSnapshotIndex = currentSnapshotIndex;
    }
    
    /**
     * Sets time travel window seconds (used when loading)
     */
    public void setTimeTravelWindowSeconds(int timeTravelWindowSeconds) {
        this.timeTravelWindowSeconds = timeTravelWindowSeconds;
    }
    
    // ==================== GETTERS FOR SAVE SYSTEM ====================
    
    /**
     * Gets the total wire length
     */
    public int getTotalWireLength() {
        return totalWireLength;
    }
    
    /**
     * Gets the wiring mode state
     */
    public boolean isWiringMode() {
        return inWiringMode;
    }
    
    /**
     * Gets the systems list
     */
    public List<System> getSystems() {
        return systems;
    }
    
    /**
     * Gets the wires list
     */
    public List<Wire> getWires() {
        return wires;
    }
    
    /**
     * Gets the moving packets list
     */
    public List<MovingPacket> getMovingPackets() {
        return movingPackets;
    }
    
    /**
     * Gets the wire lengths map
     */
    public Map<Wire, Integer> getWireLengths() {
        return wireLengths;
    }
    
    // ==================== TEST SYSTEM ====================
    
    private void updateTestSystem() {
        if (!isTestRunning) {
            return;
        }
        
        long currentTime = java.lang.System.currentTimeMillis();
        
        // Release packets at intervals until we've released all 10
        if (testPacketsReleased < Config.GameConditions.TEST_PACKET_COUNT) {
            if (currentTime - lastPacketReleaseTime >= Config.GameConditions.PACKET_RELEASE_INTERVAL) {
                releaseTestPacket();
                lastPacketReleaseTime = currentTime;
            }
        }
        
        // Check if all packets have been processed (reached destination OR destroyed)
        // We don't need to count here - counting is done in handlePacketArrival and handlePacketDestruction
        if (testPacketsReleased >= Config.GameConditions.TEST_PACKET_COUNT) {
            // All packets have been released, check if they've all been processed
            int totalProcessed = testPacketsReturned + testPacketsLost;
            Logger.getInstance().info("🔍 Test status check: Released=" + testPacketsReleased + 
                                    ", Returned=" + testPacketsReturned + 
                                    ", Lost=" + testPacketsLost + 
                                    ", TotalProcessed=" + totalProcessed + 
                                    ", MovingPackets=" + movingPackets.size());
            
            if (totalProcessed >= testPacketsReleased) {
                Logger.getInstance().info("✅ All packets processed, completing test...");
                completeTest();
            } else {
                // Additional check: if no packets are moving anymore, consider test complete
                boolean anyTestPacketsMoving = false;
                for (MovingPacket packet : movingPackets) {
                    if (packet.isPlayerSpawned() && !packet.isTestPacketReturned()) {
                        anyTestPacketsMoving = true;
                        break;
                    }
                }
                if (!anyTestPacketsMoving) {
                    Logger.getInstance().info("✅ No test packets moving, completing test...");
                    completeTest();
                }
            }
        }
    }
    
    private void releaseTestPacket() {
        // Find a reference system to spawn from
        for (System system : systems) {
            if (system instanceof ReferenceSystem) {
                ReferenceSystem refSystem = (ReferenceSystem) system;
                
                // Create a packet using the test configuration
                Packet testPacket = createConfiguredTestPacket(refSystem.getX(), refSystem.getY());
                
                // Find a wire connected to this reference system to create a moving packet
                for (Wire wire : wires) {
                    if (wire.getStartPort().getParentSystem() == refSystem) {
                        MovingPacket movingPacket = new MovingPacket(testPacket, wire);
                        movingPacket.setPlayerSpawned(true); // Mark as player-spawned for test
                        movingPacket.setTestPacketReturned(false); // Mark as test packet
                        movingPackets.add(movingPacket);
                        testPacketsReleased++;
                        Logger.getInstance().info("Test packet " + testPacketsReleased + " (" + testPacket.getType() + ") released from reference system");
                        return;
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Creates a random test packet of various types
     */
    private Packet createRandomTestPacket(int x, int y) {
        // Define all available packet types for testing
        PacketType[] testPacketTypes = {
            PacketType.SQUARE_MESSENGER,
            PacketType.TRIANGLE_MESSENGER,
            PacketType.GREEN_DIAMOND_SMALL,
            PacketType.GREEN_DIAMOND_LARGE,
            PacketType.INFINITY_SYMBOL,
            PacketType.PADLOCK_ICON,
            PacketType.CAMOUFLAGE_ICON_SMALL,
            PacketType.CAMOUFLAGE_ICON_LARGE,
            PacketType.BULK_PACKET_SMALL,
            PacketType.BULK_PACKET_LARGE
        };
        
        // Randomly select a packet type
        PacketType selectedType = testPacketTypes[(int) (Math.random() * testPacketTypes.length)];
        
        // Create the appropriate packet based on type
        switch (selectedType) {
            case SQUARE_MESSENGER:
            case TRIANGLE_MESSENGER:
                return new MessengerPacket(x, y, selectedType);
            case GREEN_DIAMOND_SMALL:
            case GREEN_DIAMOND_LARGE:
            case INFINITY_SYMBOL:
            case PADLOCK_ICON:
                return new ProtectedPacket(x, y, selectedType);
            case CAMOUFLAGE_ICON_SMALL:
                return new ConfidentialPacket(x, y, ConfidentialPacket.ConfidentialType.SMALL);
            case CAMOUFLAGE_ICON_LARGE:
                return new ConfidentialPacket(x, y, ConfidentialPacket.ConfidentialType.LARGE);
            case BULK_PACKET_SMALL:
                return new BulkPacket(x, y, BulkPacket.BulkType.SMALL);
            case BULK_PACKET_LARGE:
                return new BulkPacket(x, y, BulkPacket.BulkType.LARGE);
            default:
                return new MessengerPacket(x, y, PacketType.SQUARE_MESSENGER);
        }
    }
    
    private void countReturnedPackets() {
        // Count packets that have actually reached their destination systems
        // This is called from handlePacketArrival when packets reach systems
        // No need to count here - the counting is done in handlePacketArrival
    }
    
    private void completeTest() {
        isTestRunning = false;
        testCompleted = true;
        
        // Calculate packet loss percentage based on total planned packets for the test
        double packetLossPercentage = getPacketLossPercentage();
        
        boolean won = packetLossPercentage < Config.GameConditions.MAX_PACKET_LOSS_PERCENTAGE;
        
        Logger.getInstance().info("🎯 TEST COMPLETED! Packets: " + testPacketsReleased + " released, " + 
                                testPacketsReturned + " reached destination, " + testPacketsLost + " lost");
        
        if (won) {
            gameWon = true;
            Logger.getInstance().info("🎉 LEVEL COMPLETED! Packet loss: " + String.format("%.1f", packetLossPercentage) + "% (Target: <" + Config.GameConditions.MAX_PACKET_LOSS_PERCENTAGE + "%)");
            
            // Add leaderboard record for successful completion
            addLeaderboardRecord(packetLossPercentage);
        } else {
            gameLost = true;
            Logger.getInstance().info("💀 LEVEL FAILED! Packet loss: " + String.format("%.1f", packetLossPercentage) + "% (Target: <" + Config.GameConditions.MAX_PACKET_LOSS_PERCENTAGE + "%)");
        }
        
        // Show result dialog
        showTestResultDialog(won, packetLossPercentage);
        
        // Don't clear all packets - let normal game continue
        // The test packets will naturally be removed when they reach their destinations
    }
    
    public void startTest() {
        if (isTestRunning) {
            Logger.getInstance().info("Test is already running!");
            return;
        }
        
        // Reset test state
        resetTestState();
        
        isTestRunning = true;
        testStartTime = java.lang.System.currentTimeMillis();
        lastPacketReleaseTime = testStartTime;
        
        Logger.getInstance().info("🚀 Starting network test! Releasing " + Config.GameConditions.TEST_PACKET_COUNT + " packets...");
    }
    
    public void resetTestState() {
        isTestRunning = false;
        testCompleted = false;
        gameWon = false;
        gameLost = false;
        testPacketsReleased = 0;
        testPacketsReturned = 0;
        testPacketsLost = 0;
        testStartTime = 0;
        lastPacketReleaseTime = 0;
    }
    
    /**
     * Initializes the test configuration based on Config settings
     */
    private void initializeTestConfiguration() {
        List<TestPacketType> enabledTypes = new ArrayList<>();
        
        if (Config.GameConditions.ENABLE_MESSENGER) enabledTypes.add(TestPacketType.MESSENGER);
        if (Config.GameConditions.ENABLE_BULK) enabledTypes.add(TestPacketType.BULK);
        if (Config.GameConditions.ENABLE_SPY) enabledTypes.add(TestPacketType.SPY);
        if (Config.GameConditions.ENABLE_MALICIOUS) enabledTypes.add(TestPacketType.MALICIOUS);
        if (Config.GameConditions.ENABLE_VPN) enabledTypes.add(TestPacketType.VPN);
        
        this.testConfiguration = new TestConfiguration(
            Config.GameConditions.USE_EQUAL_PROBABILITY,
            enabledTypes.toArray(new TestPacketType[0])
        );
        
        Logger.getInstance().info("Test configuration initialized with " + enabledTypes.size() + " packet types");
    }
    
    /**
     * Creates a test packet using the configured packet types
     */
    private Packet createConfiguredTestPacket(int x, int y) {
        TestPacketType selectedType = testConfiguration.getRandomPacketType();
        PacketType packetType = selectedType.toPacketType();
        
        return new MessengerPacket(x, y, packetType);
    }
    
    /**
     * Shows the test result dialog and handles user choice
     */
    private void showTestResultDialog(boolean won, double packetLossPercentage) {
        // Get the parent frame for the dialog
        Frame parentFrame = null;
        if (gamePanel != null && gamePanel.getParent() != null) {
            Component parent = gamePanel.getParent();
            while (parent != null && !(parent instanceof Frame)) {
                parent = parent.getParent();
            }
            if (parent instanceof Frame) {
                parentFrame = (Frame) parent;
            }
        }
        
        TestResultDialog.TestResult result = TestResultDialog.showDialog(
            parentFrame, 
            won, 
            packetLossPercentage, 
            testPacketsReleased, 
            testPacketsReturned
        );
        
        handleTestResult(result);
    }
    
    /**
     * Handles the user's choice from the test result dialog
     */
    private void handleTestResult(TestResultDialog.TestResult result) {
        switch (result) {
            case RESET_LEVEL:
                resetCurrentLevel();
                break;
            case NEXT_LEVEL:
                if (currentLevelNumber < maxLevelNumber) {
                    loadNextLevel();
                } else {
                    Logger.getInstance().info("🎉 Congratulations! You've completed all levels!");
                    // Could show a completion dialog here
                }
                break;
            case CLOSE:
                // Just close the dialog, continue with current level
                break;
        }
    }
    
    /**
     * Resets the current level to its initial state
     */
    private void resetCurrentLevel() {
        Logger.getInstance().info("Resetting level " + currentLevelNumber + "...");
        
        // Reset test state
        resetTestState();
        
        // Clear all packets
        movingPackets.clear();
        
        // Reload the current level (this will set the correct initial coins)
        loadLevel(currentLevelNumber);
        
        Logger.getInstance().info("Level " + currentLevelNumber + " reset successfully!");
    }
    
    /**
     * Loads the next level
     */
    private void loadNextLevel() {
        if (currentLevelNumber < maxLevelNumber) {
            currentLevelNumber++;
            currentLevelName = "Level " + currentLevelNumber;
            
            Logger.getInstance().info("Loading " + currentLevelName + "...");
            
            // Reset test state
            resetTestState();
            
            // Clear all packets
            movingPackets.clear();
            
            // Load the new level (this will set the correct initial coins)
            loadLevel(currentLevelNumber);
            
            Logger.getInstance().info(currentLevelName + " loaded successfully!");
        }
    }
    
    /**
     * Loads a specific level by number
     */
    private void loadLevel(int levelNumber) {
        try {
            LevelData levelData = LevelLoader.loadLevel(levelNumber);
            
            if (levelData != null) {
                // Clear existing systems and wires
                systems.clear();
                wires.clear();
                movingPackets.clear();
                
                // Load the new level data
                loadLevelFromData(levelData);
                
                Logger.getInstance().info("Level " + levelNumber + " loaded successfully!");
            } else {
                Logger.getInstance().error("Failed to load level " + levelNumber);
            }
        } catch (Exception e) {
            Logger.getInstance().error("Error loading level " + levelNumber, e);
        }
    }
    
    /**
     * Loads level data into the game engine
     */
    private void loadLevelFromData(LevelData levelData) {
        if (levelData == null || levelData.systems == null) {
            Logger.getInstance().error("Invalid level data provided");
            return;
        }

        // Load systems
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
                    Logger.getInstance().warning("Unknown system type: " + sysData.type);
                    break;
            }
        }

        // Load wires
        loadWiresFromLevel(levelData);
        
        // Set initial coins and wire length from level data
        if (levelData.playerStart != null) {
            coins = levelData.playerStart.initialCoins;
            totalWireLength = levelData.playerStart.initialWireLength;
            Logger.getInstance().info("Level loaded with initial coins: " + coins + ", wire length: " + totalWireLength + "m");
        }
        
        Logger.getInstance().info("Level data loaded: " + systems.size() + " systems, " + wires.size() + " wires");
    }
    
    /**
     * Gets the maximum level number
     */
    public int getMaxLevelNumber() {
        return maxLevelNumber;
    }
    
    /**
     * Sets the maximum level number
     */
    public void setMaxLevelNumber(int maxLevelNumber) {
        this.maxLevelNumber = maxLevelNumber;
    }
    
    /**
     * Gets the test configuration
     */
    public TestConfiguration getTestConfiguration() {
        return testConfiguration;
    }
    
    /**
     * Sets the test configuration
     */
    public void setTestConfiguration(TestConfiguration testConfiguration) {
        this.testConfiguration = testConfiguration;
    }
    
    /**
     * Adds a leaderboard record for the current game completion
     */
    private void addLeaderboardRecord(double packetLossPercentage) {
        if (leaderboardData != null) {
            long completionTime = currentGameTime;
            int xpEarned = LeaderboardData.calculateXP(completionTime, packetLossPercentage, coins);
            
            String playerName = PlayerManager.getInstance().getPlayerName();
            LeaderboardData.PlayerRecord record = new LeaderboardData.PlayerRecord(
                playerName,
                completionTime,
                xpEarned,
                currentLevelNumber,
                packetLossPercentage,
                coins
            );
            
            leaderboardData.addRecord(currentLevelName, record);
            leaderboardData.updatePlayerStats(playerName, currentLevelNumber, completionTime, xpEarned, coins);
            
            // Save to database in real-time
            DatabaseLeaderboardManager.saveLeaderboard(leaderboardData).thenAccept(success -> {
                if (success) {
                    Logger.getInstance().info("✅ Leaderboard record saved to database in real-time");
                } else {
                    Logger.getInstance().error("❌ Failed to save leaderboard record to database");
                }
            });
            
            Logger.getInstance().info("🎯 LEADERBOARD RECORD ADDED: " + currentLevelName + 
                " - Player: " + playerName +
                " - Time: " + record.getFormattedTime() + 
                " - XP: " + xpEarned + 
                " - Coins: " + coins +
                " - Packet Loss: " + String.format("%.1f", packetLossPercentage) + "%");
            
            // Update the leaderboard panel if game frame is available
            if (gameFrame != null) {
                try {
                    // Use reflection to call updateLeaderboard method
                    java.lang.reflect.Method updateMethod = gameFrame.getClass().getMethod("updateLeaderboard");
                    updateMethod.invoke(gameFrame);
                } catch (Exception e) {
                    Logger.getInstance().error("Failed to update leaderboard panel", e);
                }
            }
        }
    }
    
    /**
     * Gets the leaderboard data for external access
     */
    public LeaderboardData getLeaderboardData() {
        return leaderboardData;
    }
    
    /**
     * Gets the current level number
     */
    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }
    
    /**
     * Gets the current level name
     */
    public String getCurrentLevelName() {
        return currentLevelName;
    }
    
    /**
     * Sets the game frame reference for UI updates
     */
    public void setGameFrame(Object gameFrame) {
        this.gameFrame = gameFrame;
    }
    
    // ==================== TIME TRAVEL SYSTEM ====================
    
    private void updateTimeTravel() {
        // Handle time travel navigation
        long currentTime = java.lang.System.currentTimeMillis();
        
        if (timeTravelLeftPressed && currentTime - lastTimeTravelInput >= timeTravelInputDelay) {
            java.lang.System.out.println("DEBUG: Left arrow pressed in time travel mode");
            navigateTimeBackward();
            lastTimeTravelInput = currentTime;
        }
        
        if (timeTravelRightPressed && currentTime - lastTimeTravelInput >= timeTravelInputDelay) {
            java.lang.System.out.println("DEBUG: Right arrow pressed in time travel mode");
            navigateTimeForward();
            lastTimeTravelInput = currentTime;
        }
    }
    
    private void createTimeSnapshot() {
        try {
            // Create snapshot using the working save logic
            SaveData snapshot = SaveManager.createSaveData(this);
            
            // Add to in-memory snapshots
            timeSnapshots.add(snapshot);
            
            // Keep only last maxSnapshots (FPS * time window)
            if (timeSnapshots.size() > maxSnapshots) {
                timeSnapshots.remove(0);
            }
            
            // Save to disk for network transfer
            saveSnapshotToDisk(snapshot);
            
            // Debug output every 60 snapshots (1 second)
            if (snapshotCounter % 60 == 0) {
                java.lang.System.out.println("Time snapshot created: " + timeSnapshots.size() + " snapshots, " + movingPackets.size() + " packets");
            }
            
        } catch (Exception e) {
            java.lang.System.out.println("Failed to create time snapshot: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves a snapshot to disk for debugging purposes
     */
    private void saveSnapshotToDisk(SaveData snapshot) {
        try {
            // Ensure snapshots directory exists
            File snapshotsDir = new File(snapshotsDirectory);
            if (!snapshotsDir.exists()) {
                snapshotsDir.mkdirs();
            }
            
            // Create filename with timestamp and counter
            String timestamp = String.format("%.3f", currentGameTime / 1000.0);
            String filename = String.format("snapshot_%06d_%s.json", snapshotCounter++, timestamp);
            File snapshotFile = new File(snapshotsDir, filename);
            
            // Convert SaveData to JSON and write to file
            String json = SaveManager.saveDataToJson(snapshot);
            try (FileWriter writer = new FileWriter(snapshotFile)) {
                writer.write(json);
            }
            
            // Clean up old snapshot files (keep only recent ones)
            cleanupOldSnapshotFiles();
            
        } catch (Exception e) {
            java.lang.System.err.println("Error saving snapshot to disk: " + e.getMessage());
        }
    }
    
    /**
     * Cleans up old snapshot files to prevent disk space issues
     */
    private void cleanupOldSnapshotFiles() {
        try {
            File snapshotsDir = new File(snapshotsDirectory);
            if (!snapshotsDir.exists()) return;
            
            File[] files = snapshotsDir.listFiles((dir, name) -> name.startsWith("snapshot_") && name.endsWith(".json"));
            if (files == null) return;
            
            // Keep only the most recent files (exactly FPS * time window)
            int maxFiles = timeTravelWindowSeconds * 60; // FPS * seconds
            if (files.length > maxFiles) {
                // Sort by filename (which includes counter) to get chronological order
                Arrays.sort(files, (a, b) -> {
                    String nameA = a.getName();
                    String nameB = b.getName();
                    // Extract counter from filename: snapshot_XXXXXX_timestamp.json
                    int counterA = Integer.parseInt(nameA.substring(9, 15)); // XXXXXX part
                    int counterB = Integer.parseInt(nameB.substring(9, 15));
                    return Integer.compare(counterA, counterB);
                });
                
                // Delete oldest files
                int filesToDelete = files.length - maxFiles;
                for (int i = 0; i < filesToDelete; i++) {
                    files[i].delete();
                }
            }
        } catch (Exception e) {
            java.lang.System.err.println("Error cleaning up snapshot files: " + e.getMessage());
        }
    }
    
    private void navigateTimeBackward() {
        java.lang.System.out.println("DEBUG: navigateTimeBackward called. Current index: " + currentSnapshotIndex + ", Total snapshots: " + timeSnapshots.size());
        if (currentSnapshotIndex > 0) {
            currentSnapshotIndex--;
            restoreTimeSnapshot(currentSnapshotIndex);
            java.lang.System.out.println("Time travel: Moved backward to snapshot " + (currentSnapshotIndex + 1) + "/" + timeSnapshots.size());
        } else {
            java.lang.System.out.println("Time travel: Already at earliest snapshot");
        }
    }
    
    private void navigateTimeForward() {
        java.lang.System.out.println("DEBUG: navigateTimeForward called. Current index: " + currentSnapshotIndex + ", Total snapshots: " + timeSnapshots.size());
        if (currentSnapshotIndex < timeSnapshots.size() - 1) {
            currentSnapshotIndex++;
            restoreTimeSnapshot(currentSnapshotIndex);
            java.lang.System.out.println("Time travel: Moved forward to snapshot " + (currentSnapshotIndex + 1) + "/" + timeSnapshots.size());
        } else {
            java.lang.System.out.println("Time travel: Already at latest snapshot");
        }
    }
    
    /**
     * Restores game state from in-memory snapshot using working restore logic
     * Special version for time travel that preserves time travel state
     */
    private void restoreTimeSnapshot(int snapshotIndex) {
        if (snapshotIndex >= 0 && snapshotIndex < timeSnapshots.size()) {
            try {
                SaveData snapshot = timeSnapshots.get(snapshotIndex);
                int packetsBefore = movingPackets.size();
                
                // Store current time travel state before restoration
                boolean wasInTimeTravelMode = isTimeTravelMode;
                boolean wasPaused = isPaused;
                int currentSnapshotIdx = currentSnapshotIndex;
                int timeTravelWindow = timeTravelWindowSeconds;
                
                // Store the coin count from the snapshot before restoration
                int snapshotCoins = snapshot.coins;
                
                // Restore the snapshot
                SaveManager.restoreGameState(this, snapshot);
                
                // Restore time travel state (don't let the restoration override it)
                isTimeTravelMode = wasInTimeTravelMode;
                isPaused = wasPaused;
                currentSnapshotIndex = currentSnapshotIdx;
                timeTravelWindowSeconds = timeTravelWindow;
                
                int packetsAfter = movingPackets.size();
                java.lang.System.out.println("Time snapshot restored: " + packetsAfter + " packets, " + systems.size() + " systems, " + wires.size() + " wires (was " + packetsBefore + ")");
                java.lang.System.out.println("Coins restored: " + coins + " (snapshot had: " + snapshotCoins + ")");
                
                // Update HUD to reflect the restored state
                updateHUD();
                
                // Force UI repaint to show the updated HUD
                if (gamePanel != null) {
                    gamePanel.repaint();
                }
            } catch (Exception e) {
                java.lang.System.out.println("Failed to restore time snapshot: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gets list of available snapshot files from disk
     */
    private List<String> getAvailableSnapshotFiles() {
        List<String> snapshotFiles = new ArrayList<>();
        try {
            File snapshotsDir = new File(snapshotsDirectory);
            if (!snapshotsDir.exists()) return snapshotFiles;
            
            File[] files = snapshotsDir.listFiles((dir, name) -> name.startsWith("snapshot_") && name.endsWith(".json"));
            if (files == null) return snapshotFiles;
            
            // Sort by filename (counter) to get chronological order
            Arrays.sort(files, (a, b) -> {
                String nameA = a.getName();
                String nameB = b.getName();
                int counterA = Integer.parseInt(nameA.substring(9, 15)); // XXXXXX part
                int counterB = Integer.parseInt(nameB.substring(9, 15));
                return Integer.compare(counterA, counterB);
            });
            
            for (File file : files) {
                snapshotFiles.add(file.getName());
            }
        } catch (Exception e) {
            java.lang.System.err.println("Error getting snapshot files: " + e.getMessage());
        }
        return snapshotFiles;
    }
    
    /**
     * Restores game state from a disk snapshot file
     */
    private void restoreTimeSnapshotFromDisk(int snapshotIndex, List<String> availableSnapshots) {
        if (snapshotIndex >= 0 && snapshotIndex < availableSnapshots.size()) {
            try {
                String filename = availableSnapshots.get(snapshotIndex);
                File snapshotFile = new File(snapshotsDirectory, filename);
                
                if (snapshotFile.exists()) {
                    // Load and restore the snapshot
                    SaveManager.loadGameFromFile(this, snapshotFile.getAbsolutePath());
                    java.lang.System.out.println("Time snapshot restored from disk: " + filename);
                } else {
                    java.lang.System.err.println("Snapshot file not found: " + filename);
                }
            } catch (Exception e) {
                java.lang.System.err.println("Error restoring snapshot from disk: " + e.getMessage());
            }
        }
    }
    
    
    public void enterTimeTravelMode() {
        java.lang.System.out.println("DEBUG: enterTimeTravelMode called. Snapshots available: " + timeSnapshots.size());
        if (timeSnapshots.isEmpty()) {
            java.lang.System.out.println("No time snapshots available for time travel");
            return;
        }
        
        isTimeTravelMode = true;
        isPaused = true;
        currentSnapshotIndex = timeSnapshots.size() - 1; // Start at most recent
        
        // Don't restore snapshot when entering - just show current state
        java.lang.System.out.println("Entered time travel mode. Use LEFT/RIGHT arrows to navigate time.");
        java.lang.System.out.println("Available snapshots: " + timeSnapshots.size() + ", Starting at: " + (currentSnapshotIndex + 1));
        java.lang.System.out.println("DEBUG: Time travel mode set to: " + isTimeTravelMode + ", Paused: " + isPaused + ", Current index: " + currentSnapshotIndex);
    }
    
    public void exitTimeTravelMode() {
        isTimeTravelMode = false;
        isPaused = false;
        currentSnapshotIndex = -1;
        
        // Don't restore snapshot when exiting - keep current state
        java.lang.System.out.println("Exited time travel mode. Game resumed.");
    }
    
    public void togglePause() {
        if (isTimeTravelMode) {
            exitTimeTravelMode();
        } else {
            isPaused = !isPaused;
            if (isPaused) {
                java.lang.System.out.println("Game paused. Press P to resume or T for time travel.");
            } else {
                java.lang.System.out.println("Game resumed.");
            }
        }
    }
    
    public void setTimeTravelWindow(int seconds) {
        this.timeTravelWindowSeconds = Math.max(1, Math.min(30, seconds)); // 1-30 seconds
        java.lang.System.out.println("Time travel window set to " + timeTravelWindowSeconds + " seconds");
    }
    
    public void setTimeTravelLeftPressed(boolean pressed) {
        this.timeTravelLeftPressed = pressed;
    }
    
    public void setTimeTravelRightPressed(boolean pressed) {
        this.timeTravelRightPressed = pressed;
    }
    
    // Getters for time travel state
    public boolean isTimeTravelMode() { return isTimeTravelMode; }
    public boolean isPaused() { return isPaused; }
    
    /**
     * Pauses the game
     */
    public void pauseGame() {
        isPaused = true;
    }
    
    /**
     * Resumes the game
     */
    public void resumeGame() {
        isPaused = false;
    }
    public long getCurrentGameTime() { return currentGameTime; }
    public int getTimeSnapshotsCount() { return timeSnapshots.size(); }
    public int getCurrentSnapshotIndex() { return currentSnapshotIndex; }
    public int getTimeTravelWindowSeconds() { return timeTravelWindowSeconds; }
    
    /**
     * Gets the count of snapshot files on disk
     */
    public int getDiskSnapshotCount() {
        try {
            File snapshotsDir = new File(snapshotsDirectory);
            if (!snapshotsDir.exists()) return 0;
            
            File[] files = snapshotsDir.listFiles((dir, name) -> name.startsWith("snapshot_") && name.endsWith(".json"));
            return files != null ? files.length : 0;
        } catch (Exception e) {
            return 0;
        }
    }
}
