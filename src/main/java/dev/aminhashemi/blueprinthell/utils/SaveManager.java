package dev.aminhashemi.blueprinthell.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.aminhashemi.blueprinthell.model.SaveData;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.entities.systems.ReferenceSystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.VPNSystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.MaliciousSystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.SpySystem;
import dev.aminhashemi.blueprinthell.model.entities.systems.Port;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.ConfidentialPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.BulkPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.world.ArcPoint;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.core.GameEngine;

import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages saving and loading game state
 */
public class SaveManager {
    private static final String SAVE_FILE = "save_game.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Saves the current game state to a JSON file
     */
    public static boolean saveGame(GameEngine engine) {
        try {
            SaveData saveData = createSaveData(engine);
            
            try (FileWriter writer = new FileWriter(SAVE_FILE)) {
                gson.toJson(saveData, writer);
            }
            
            return true;
        } catch (Exception e) {
            java.lang.System.out.println("Failed to save game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Converts SaveData to JSON string for disk storage
     */
    public static String saveDataToJson(SaveData saveData) {
        return gson.toJson(saveData);
    }
    
    /**
     * Loads game state from a specific file
     */
    public static boolean loadGameFromFile(GameEngine engine, String filename) {
        try {
            try (FileReader reader = new FileReader(filename)) {
                SaveData saveData = gson.fromJson(reader, SaveData.class);
                restoreGameState(engine, saveData);
                return true;
            }
        } catch (Exception e) {
            java.lang.System.out.println("Failed to load game from file " + filename + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Loads game state from a JSON file
     */
    public static boolean loadGame(GameEngine engine) {
        try {
            SaveData saveData;
            try (FileReader reader = new FileReader(SAVE_FILE)) {
                saveData = gson.fromJson(reader, SaveData.class);
            }
            
            if (saveData == null) {
                java.lang.System.out.println("Failed to parse save file");
                return false;
            }
            
            restoreGameState(engine, saveData);
            return true;
        } catch (Exception e) {
            java.lang.System.out.println("Failed to load game: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Creates SaveData from GameEngine
     */
    public static SaveData createSaveData(GameEngine engine) {
        SaveData saveData = new SaveData();
        
        // Save basic game state
        saveData.coins = engine.getCoins();
        saveData.usedWireLength = engine.getUsedWireLength();
        saveData.totalWireLength = engine.getTotalWireLength();
        saveData.isWiringMode = engine.isWiringMode();
        
        // Save shop and effects state
        saveData.impactWavesDisabled = engine.isImpactWavesDisabled();
        saveData.packetCollisionsDisabled = engine.isPacketCollisionsDisabled();
        saveData.packetNoiseZeroed = engine.isPacketNoiseZeroed();
        saveData.gameStartTime = engine.getGameStartTime();
        saveData.currentGameTime = engine.getCurrentGameTime();
        
        // Save test system state
        saveData.isTestRunning = engine.isTestRunning();
        saveData.testPacketsReleased = engine.getTestPacketsReleased();
        saveData.testPacketsReturned = engine.getTestPacketsReturned();
        saveData.testStartTime = engine.getTestStartTime();
        saveData.lastPacketReleaseTime = engine.getLastPacketReleaseTime();
        saveData.testCompleted = engine.isTestCompleted();
        saveData.gameWon = engine.isGameWon();
        saveData.gameLost = engine.isGameLost();
        
        // Save time travel state
        saveData.isTimeTravelMode = engine.isTimeTravelMode();
        saveData.isPaused = engine.isPaused();
        saveData.currentSnapshotIndex = engine.getCurrentSnapshotIndex();
        saveData.timeTravelWindowSeconds = engine.getTimeTravelWindowSeconds();
        
        // Save systems
        for (System system : engine.getSystems()) {
            if (system != null) {
                saveData.systems.add(createSystemSaveData(system));
            }
        }
        
        // Save wires
        for (Wire wire : engine.getWires()) {
            if (wire != null && wire.getStartPort() != null && wire.getEndPort() != null) {
                saveData.wires.add(createWireSaveData(wire));
            }
        }
        
        // Save moving packets
        for (MovingPacket movingPacket : engine.getMovingPackets()) {
            if (movingPacket != null && !movingPacket.isLost()) {
                saveData.movingPackets.add(createMovingPacketSaveData(movingPacket));
            }
        }
        
        // Save wire lengths
        for (Map.Entry<Wire, Integer> entry : engine.getWireLengths().entrySet()) {
            Wire wire = entry.getKey();
            if (wire != null && wire.getStartPort() != null && wire.getEndPort() != null) {
                String wireKey = wire.getStartPort().getId() + "_" + wire.getEndPort().getId();
                saveData.wireLengths.put(wireKey, entry.getValue());
            }
        }
        
        return saveData;
    }
    
    /**
     * Creates SystemSaveData from a System
     */
    private static SaveData.SystemSaveData createSystemSaveData(System system) {
        SaveData.SystemSaveData systemData = new SaveData.SystemSaveData();
        systemData.type = getSystemType(system);
        systemData.x = system.getX();
        systemData.y = system.getY();
        systemData.id = system.getId();
        
        // Save input ports
        for (Port port : system.getInputPorts()) {
            SaveData.PortSaveData portData = new SaveData.PortSaveData();
            portData.type = port.getType().name();
            portData.index = port.getIndex();
            portData.isInput = port.isInput();
            systemData.inputPorts.add(portData);
        }
        
        // Save output ports
        for (Port port : system.getOutputPorts()) {
            SaveData.PortSaveData portData = new SaveData.PortSaveData();
            portData.type = port.getType().name();
            portData.index = port.getIndex();
            portData.isInput = port.isInput();
            systemData.outputPorts.add(portData);
        }
        
        // Save system-specific properties
        if (system instanceof ReferenceSystem) {
            ReferenceSystem refSystem = (ReferenceSystem) system;
            systemData.properties.put("packetGenerationRate", refSystem.getPacketGenerationRate());
            systemData.properties.put("lastSpawnTime", refSystem.getLastSpawnTime());
        } else if (system instanceof SpySystem) {
            SpySystem spySystem = (SpySystem) system;
            systemData.properties.put("isActive", spySystem.isActive());
        }
        
        return systemData;
    }
    
    /**
     * Creates WireSaveData from a Wire
     */
    private static SaveData.WireSaveData createWireSaveData(Wire wire) {
        SaveData.WireSaveData wireData = new SaveData.WireSaveData();
        wireData.startPortId = wire.getStartPort().getId();
        wireData.endPortId = wire.getEndPort().getId();
        wireData.style = wire.getStyle().name();
        wireData.bulkPacketPasses = wire.getBulkPacketPasses();
        wireData.isDestroyed = wire.isDestroyed();
        
        // Save arc points
        for (ArcPoint arcPoint : wire.getArcPoints()) {
            SaveData.ArcPointSaveData arcData = new SaveData.ArcPointSaveData();
            arcData.x = arcPoint.getPosition().x;
            arcData.y = arcPoint.getPosition().y;
            arcData.isDragging = arcPoint.isDragging();
            wireData.arcPoints.add(arcData);
        }
        
        return wireData;
    }
    
    /**
     * Creates MovingPacketSaveData from a MovingPacket
     */
    private static SaveData.MovingPacketSaveData createMovingPacketSaveData(MovingPacket movingPacket) {
        SaveData.MovingPacketSaveData packetData = new SaveData.MovingPacketSaveData();
        
        // Save packet
        packetData.packet = createPacketSaveData(movingPacket.getPacket());
        
        // Save movement data
        packetData.sourceSystemId = movingPacket.getSourceSystem().getId();
        packetData.destinationSystemId = movingPacket.getDestinationSystem().getId();
        packetData.wireId = movingPacket.getWire().getStartPort().getId() + "_" + movingPacket.getWire().getEndPort().getId();
        packetData.progress = movingPacket.getProgress();
        packetData.noiseLevel = movingPacket.getNoiseLevel();
        packetData.hasSpawnProtection = movingPacket.hasSpawnProtection();
        packetData.spawnProtectionEndTime = movingPacket.getSpawnProtectionEndTime();
        packetData.currentSpeed = movingPacket.getCurrentSpeed();
        packetData.acceleration = movingPacket.getAcceleration();
        packetData.isOnCurve = movingPacket.isOnCurve();
        packetData.wirePassCount = movingPacket.getWirePassCount();
        
        return packetData;
    }
    
    /**
     * Creates PacketSaveData from a Packet
     */
    private static SaveData.PacketSaveData createPacketSaveData(Packet packet) {
        SaveData.PacketSaveData packetData = new SaveData.PacketSaveData();
        packetData.type = packet.getType().name();
        packetData.x = packet.getX();
        packetData.y = packet.getY();
        packetData.packetClass = packet.getClass().getSimpleName();
        
        // Save packet-specific properties
        if (packet instanceof ProtectedPacket) {
            ProtectedPacket protectedPacket = (ProtectedPacket) packet;
            packetData.properties.put("originalPacketType", protectedPacket.getOriginalPacketType().name());
            packetData.properties.put("isVisible", protectedPacket.isVisible());
            packetData.properties.put("lastVisibilityToggle", protectedPacket.getLastVisibilityToggle());
        } else if (packet instanceof ConfidentialPacket) {
            ConfidentialPacket confidentialPacket = (ConfidentialPacket) packet;
            packetData.properties.put("confidentialType", confidentialPacket.getConfidentialType().name());
            packetData.properties.put("lastSpeedCheck", confidentialPacket.getLastSpeedCheck());
        } else if (packet instanceof BulkPacket) {
            BulkPacket bulkPacket = (BulkPacket) packet;
            packetData.properties.put("bulkType", bulkPacket.getBulkType().name());
            packetData.properties.put("isOnCurve", bulkPacket.isOnCurve());
        }
        
        return packetData;
    }
    
    /**
     * Restores game state from SaveData
     */
    public static void restoreGameState(GameEngine engine, SaveData saveData) {
        // Clear current state
        engine.clearGameState();
        
        // Restore basic game state
        engine.setCoins(saveData.coins);
        engine.setUsedWireLength(saveData.usedWireLength);
        engine.setTotalWireLength(saveData.totalWireLength);
        engine.setWiringMode(saveData.isWiringMode);
        
        // Restore shop and effects state
        engine.setImpactWavesDisabled(saveData.impactWavesDisabled);
        engine.setPacketCollisionsDisabled(saveData.packetCollisionsDisabled);
        engine.setPacketNoiseZeroed(saveData.packetNoiseZeroed);
        engine.setGameStartTime(saveData.gameStartTime);
        engine.setCurrentGameTime(saveData.currentGameTime);
        
        // Restore test system state
        engine.setTestRunning(saveData.isTestRunning);
        engine.setTestPacketsReleased(saveData.testPacketsReleased);
        engine.setTestPacketsReturned(saveData.testPacketsReturned);
        engine.setTestStartTime(saveData.testStartTime);
        engine.setLastPacketReleaseTime(saveData.lastPacketReleaseTime);
        engine.setTestCompleted(saveData.testCompleted);
        engine.setGameWon(saveData.gameWon);
        engine.setGameLost(saveData.gameLost);
        
        // Restore time travel state
        engine.setTimeTravelMode(saveData.isTimeTravelMode);
        engine.setPaused(saveData.isPaused);
        engine.setCurrentSnapshotIndex(saveData.currentSnapshotIndex);
        engine.setTimeTravelWindowSeconds(saveData.timeTravelWindowSeconds);
        
        // Create system map for wire restoration
        Map<String, System> systemMap = new HashMap<>();
        
        // Restore systems
        for (SaveData.SystemSaveData systemData : saveData.systems) {
            System system = createSystemFromSaveData(systemData);
            if (system != null) {
                engine.addSystem(system);
                systemMap.put(systemData.id, system);
            }
        }
        
        // Restore wires
        Map<String, Wire> wireMap = new HashMap<>();
        for (SaveData.WireSaveData wireData : saveData.wires) {
            Wire wire = createWireFromSaveData(wireData, systemMap);
            if (wire != null) {
                engine.addWire(wire);
                String wireKey = wireData.startPortId + "_" + wireData.endPortId;
                wireMap.put(wireKey, wire);
            }
        }
        
        // Restore moving packets
        for (SaveData.MovingPacketSaveData packetData : saveData.movingPackets) {
            MovingPacket movingPacket = createMovingPacketFromSaveData(packetData, systemMap, wireMap);
            if (movingPacket != null) {
                engine.addMovingPacket(movingPacket);
            }
        }
        
        // Restore wire lengths
        Map<Wire, Integer> wireLengthMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : saveData.wireLengths.entrySet()) {
            Wire wire = wireMap.get(entry.getKey());
            if (wire != null) {
                wireLengthMap.put(wire, entry.getValue());
            }
        }
        engine.setWireLengths(wireLengthMap);
    }
    
    /**
     * Creates System from SystemSaveData
     */
    private static System createSystemFromSaveData(SaveData.SystemSaveData systemData) {
        System system = null;
        
        // Create LevelData with saved port information
        LevelData.SystemData data = new LevelData.SystemData();
        data.inputPorts = new java.util.ArrayList<>();
        data.outputPorts = new java.util.ArrayList<>();
        
        // Restore input ports from save data
        for (SaveData.PortSaveData portData : systemData.inputPorts) {
            LevelData.PortData port = new LevelData.PortData();
            port.type = portData.type;
            data.inputPorts.add(port);
        }
        
        // Restore output ports from save data
        for (SaveData.PortSaveData portData : systemData.outputPorts) {
            LevelData.PortData port = new LevelData.PortData();
            port.type = portData.type;
            data.outputPorts.add(port);
        }
        
        // Create system based on type
        switch (systemData.type) {
            case "REFERENCE":
                system = new ReferenceSystem(systemData.x, systemData.y, data);
                break;
            case "VPN":
                system = new VPNSystem(systemData.x, systemData.y, data);
                break;
            case "MALICIOUS":
                system = new MaliciousSystem(systemData.x, systemData.y, data);
                break;
            case "SPY":
                system = new SpySystem(systemData.x, systemData.y, data);
                break;
        }
        
        if (system != null) {
            system.setId(systemData.id);
            
            // Restore system-specific properties
            if (system instanceof ReferenceSystem) {
                ReferenceSystem refSystem = (ReferenceSystem) system;
                if (systemData.properties.containsKey("packetGenerationRate")) {
                    Object value = systemData.properties.get("packetGenerationRate");
                    if (value instanceof Number) {
                        refSystem.setPacketGenerationRate(((Number) value).intValue());
                    }
                }
                if (systemData.properties.containsKey("lastSpawnTime")) {
                    Object value = systemData.properties.get("lastSpawnTime");
                    if (value instanceof Number) {
                        refSystem.setLastSpawnTime(((Number) value).longValue());
                    }
                }
            } else if (system instanceof SpySystem) {
                SpySystem spySystem = (SpySystem) system;
                if (systemData.properties.containsKey("isActive")) {
                    Object value = systemData.properties.get("isActive");
                    if (value instanceof Boolean) {
                        spySystem.setActive((Boolean) value);
                    }
                }
            }
        }
        
        return system;
    }
    
    /**
     * Creates Wire from WireSaveData
     */
    private static Wire createWireFromSaveData(SaveData.WireSaveData wireData, Map<String, System> systemMap) {
        // Find start and end ports
        Port startPort = null;
        Port endPort = null;
        
        for (System system : systemMap.values()) {
            for (Port port : system.getInputPorts()) {
                if (port.getId().equals(wireData.startPortId)) {
                    startPort = port;
                }
                if (port.getId().equals(wireData.endPortId)) {
                    endPort = port;
                }
            }
            for (Port port : system.getOutputPorts()) {
                if (port.getId().equals(wireData.startPortId)) {
                    startPort = port;
                }
                if (port.getId().equals(wireData.endPortId)) {
                    endPort = port;
                }
            }
        }
        
        if (startPort == null || endPort == null) {
            return null;
        }
        
        Wire wire = new Wire(startPort);
        wire.setEndPort(endPort);
        wire.setStyle(Wire.WireStyle.valueOf(wireData.style));
        
        // Restore arc points
        for (SaveData.ArcPointSaveData arcData : wireData.arcPoints) {
            ArcPoint arcPoint = new ArcPoint(arcData.x, arcData.y);
            arcPoint.setDragging(arcData.isDragging);
            wire.addArcPoint(arcPoint);
        }
        
        // Restore wire state
        for (int i = 0; i < wireData.bulkPacketPasses; i++) {
            wire.recordBulkPacketPass();
        }
        
        return wire;
    }
    
    /**
     * Creates MovingPacket from MovingPacketSaveData
     */
    private static MovingPacket createMovingPacketFromSaveData(SaveData.MovingPacketSaveData packetData, 
                                                              Map<String, System> systemMap, 
                                                              Map<String, Wire> wireMap) {
        Packet packet = createPacketFromSaveData(packetData.packet);
        System sourceSystem = systemMap.get(packetData.sourceSystemId);
        System destinationSystem = systemMap.get(packetData.destinationSystemId);
        Wire wire = wireMap.get(packetData.wireId);
        
        if (packet == null || sourceSystem == null || destinationSystem == null || wire == null) {
            return null;
        }
        
        MovingPacket movingPacket = new MovingPacket(packet, sourceSystem, destinationSystem, wire);
        movingPacket.setProgress(packetData.progress);
        movingPacket.setNoiseLevel((float) packetData.noiseLevel);
        movingPacket.setSpawnProtection(packetData.hasSpawnProtection, packetData.spawnProtectionEndTime);
        movingPacket.setCurrentSpeed(packetData.currentSpeed);
        movingPacket.setAcceleration(packetData.acceleration);
        movingPacket.setOnCurve(packetData.isOnCurve);
        movingPacket.setWirePassCount(packetData.wirePassCount);
        
        // Update packet position based on progress
        movingPacket.updatePositionFromProgress();
        
        return movingPacket;
    }
    
    /**
     * Creates Packet from PacketSaveData
     */
    private static Packet createPacketFromSaveData(SaveData.PacketSaveData packetData) {
        Packet packet = null;
        
        switch (packetData.packetClass) {
            case "MessengerPacket":
                PacketType messengerType = PacketType.valueOf(packetData.type);
                packet = new MessengerPacket(packetData.x, packetData.y, messengerType);
                break;
            case "ProtectedPacket":
                PacketType originalType = PacketType.valueOf(packetData.properties.get("originalPacketType").toString());
                packet = new ProtectedPacket(packetData.x, packetData.y, originalType);
                break;
            case "ConfidentialPacket":
                ConfidentialPacket.ConfidentialType confType = ConfidentialPacket.ConfidentialType.valueOf(packetData.properties.get("confidentialType").toString());
                packet = new ConfidentialPacket(packetData.x, packetData.y, confType);
                break;
            case "BulkPacket":
                BulkPacket.BulkType bulkType = BulkPacket.BulkType.valueOf(packetData.properties.get("bulkType").toString());
                packet = new BulkPacket(packetData.x, packetData.y, bulkType);
                break;
        }
        
        if (packet != null) {
            // Restore packet-specific properties
            if (packet instanceof ProtectedPacket) {
                ProtectedPacket protectedPacket = (ProtectedPacket) packet;
                if (packetData.properties.containsKey("isVisible")) {
                    boolean isVisible = (Boolean) packetData.properties.get("isVisible");
                    if (!isVisible) {
                        protectedPacket.toggleVisibility();
                    }
                }
            } else if (packet instanceof ConfidentialPacket) {
                ConfidentialPacket confidentialPacket = (ConfidentialPacket) packet;
                if (packetData.properties.containsKey("lastSpeedCheck")) {
                    Object value = packetData.properties.get("lastSpeedCheck");
                    if (value instanceof Number) {
                        confidentialPacket.setLastSpeedCheck(((Number) value).longValue());
                    }
                }
            } else if (packet instanceof BulkPacket) {
                BulkPacket bulkPacket = (BulkPacket) packet;
                if (packetData.properties.containsKey("isOnCurve")) {
                    boolean isOnCurve = (Boolean) packetData.properties.get("isOnCurve");
                    bulkPacket.setOnCurve(isOnCurve);
                }
            }
        }
        
        return packet;
    }
    
    /**
     * Gets system type string from System instance
     */
    private static String getSystemType(System system) {
        if (system instanceof ReferenceSystem) {
            return "REFERENCE";
        } else if (system instanceof VPNSystem) {
            return "VPN";
        } else if (system instanceof MaliciousSystem) {
            return "MALICIOUS";
        } else if (system instanceof SpySystem) {
            return "SPY";
        }
        return "UNKNOWN";
    }
}