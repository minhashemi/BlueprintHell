package dev.aminhashemi.blueprinthell.model;

import java.util.List;

// This is the top-level class that represents the entire level JSON file.
public class LevelData {
    public int levelNumber;
    public String levelName;
    public PlayerStart playerStart;
    public List<SystemData> systems;

    public static class PlayerStart {
        public int initialCoins;
        public int initialWireLength;
    }

    public static class SystemData {
        public String id;
        public String type;
        public Position position;
        public List<PortData> inputPorts;
        public List<PortData> outputPorts;
        public PacketGeneration packetGeneration; // Can be null if not a reference system
    }

    public static class Position {
        public int x;
        public int y;
    }

    public static class PortData {
        public String type;
    }

    public static class PacketGeneration {
        public String packetType;
        public int count;
        public double frequency;
    }
}
