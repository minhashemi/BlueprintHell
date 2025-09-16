package dev.aminhashemi.blueprinthell.model;

import java.util.List;

/**
 * Data structure representing a game level loaded from JSON.
 * Contains all the information needed to initialize a level including systems, wires, and player settings.
 */
public class LevelData {
    public int levelNumber;              // Level identifier
    public String levelName;             // Human-readable level name
    public PlayerStart playerStart;      // Initial player state
    public List<SystemData> systems;     // All systems in the level
    public List<WireData> wires;         // All wire connections in the level

    /**
     * Initial player state when starting a level.
     */
    public static class PlayerStart {
        public int initialCoins;         // Starting coin count
        public int initialWireLength;    // Starting wire length available
    }

    /**
     * Data for a network system in the level.
     */
    public static class SystemData {
        public String id;                        // Unique system identifier
        public String type;                      // System type (REFERENCE, VPN, MALICIOUS, SPY)
        public Position position;                // System position on screen
        public List<PortData> inputPorts;        // Input ports for receiving packets
        public List<PortData> outputPorts;       // Output ports for sending packets
        public PacketGeneration packetGeneration; // Packet generation settings (null for non-reference systems)
    }

    /**
     * 2D position coordinates.
     */
    public static class Position {
        public int x;  // X coordinate
        public int y;  // Y coordinate
    }

    /**
     * Port configuration data.
     */
    public static class PortData {
        public String type;  // Port type (SQUARE, TRIANGLE, DIAMOND, etc.)
    }

    /**
     * Packet generation settings for reference systems.
     */
    public static class PacketGeneration {
        public String packetType;  // Type of packets to generate
        public int count;          // Number of packets to generate
        public double frequency;   // Generation frequency (packets per second)
    }
    
    /**
     * Wire connection data between systems.
     */
    public static class WireData {
        public String startSystemId;  // ID of the source system
        public int startPortIndex;    // Index of the source port
        public String endSystemId;    // ID of the destination system
        public int endPortIndex;      // Index of the destination port
    }
}
