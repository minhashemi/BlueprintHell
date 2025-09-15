package dev.aminhashemi.blueprinthell.model;

import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import java.util.List;
import java.util.Map;

/**
 * Represents the complete game state for saving/loading
 */
public class SaveData {
    // Game state
    public int coins;
    public int usedWireLength;
    public int totalWireLength;
    public boolean isWiringMode;
    
    // Systems data
    public List<SystemSaveData> systems;
    
    // Wires data
    public List<WireSaveData> wires;
    
    // Moving packets data
    public List<MovingPacketSaveData> movingPackets;
    
    // Wire length tracking
    public Map<String, Integer> wireLengths;
    
    // Constructor
    public SaveData() {
        this.systems = new java.util.ArrayList<>();
        this.wires = new java.util.ArrayList<>();
        this.movingPackets = new java.util.ArrayList<>();
        this.wireLengths = new java.util.HashMap<>();
    }
    
    /**
     * System save data
     */
    public static class SystemSaveData {
        public String type;
        public int x, y;
        public String id;
        public List<PortSaveData> inputPorts;
        public List<PortSaveData> outputPorts;
        public Map<String, Object> properties;
        
        public SystemSaveData() {
            this.inputPorts = new java.util.ArrayList<>();
            this.outputPorts = new java.util.ArrayList<>();
            this.properties = new java.util.HashMap<>();
        }
    }
    
    /**
     * Port save data
     */
    public static class PortSaveData {
        public String type;
        public int index;
        public boolean isInput;
        
        public PortSaveData() {}
    }
    
    /**
     * Wire save data
     */
    public static class WireSaveData {
        public String startPortId;
        public String endPortId;
        public List<ArcPointSaveData> arcPoints;
        public String style;
        public int bulkPacketPasses;
        public boolean isDestroyed;
        
        public WireSaveData() {
            this.arcPoints = new java.util.ArrayList<>();
        }
    }
    
    /**
     * Arc point save data
     */
    public static class ArcPointSaveData {
        public int x, y;
        public boolean isDragging;
        
        public ArcPointSaveData() {}
    }
    
    /**
     * Moving packet save data
     */
    public static class MovingPacketSaveData {
        public PacketSaveData packet;
        public String sourceSystemId;
        public String destinationSystemId;
        public String wireId;
        public double progress;
        public double noiseLevel;
        public boolean hasSpawnProtection;
        public long spawnProtectionEndTime;
        public double currentSpeed;
        public double acceleration;
        public boolean isOnCurve;
        public int wirePassCount;
        
        public MovingPacketSaveData() {}
    }
    
    /**
     * Packet save data
     */
    public static class PacketSaveData {
        public String type;
        public int x, y;
        public String packetClass;
        public Map<String, Object> properties;
        
        public PacketSaveData() {
            this.properties = new java.util.HashMap<>();
        }
    }
}
