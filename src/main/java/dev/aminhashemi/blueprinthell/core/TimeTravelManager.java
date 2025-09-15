package dev.aminhashemi.blueprinthell.core;

import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages time travel functionality - temporal progress system
 * Allows players to navigate through time to see packet states and modify connections
 */
public class TimeTravelManager {
    
    // Time travel state
    private boolean isTimeTravelMode = false;
    private boolean isExecuting = false;
    private long currentTime = 0;
    private long maxTime = 0;
    private double playbackSpeed = 1.0;
    private boolean isPaused = false;
    
    // Time snapshots - store game state at different time points
    private final Map<Long, TimeSnapshot> timeSnapshots = new ConcurrentHashMap<>();
    private final List<Long> snapshotTimestamps = new ArrayList<>();
    
    // Execution results
    private int totalPacketsSpawned = 0;
    private int packetsReturned = 0;
    private int packetsLost = 0;
    private double packetLossPercentage = 0.0;
    private boolean levelCompleted = false;
    
    // Time travel controls
    private static final long SNAPSHOT_INTERVAL = 1000; // Take snapshot every 1 second
    private static final long MAX_SIMULATION_TIME = 30000; // 30 seconds max simulation
    
    /**
     * Represents a snapshot of the game state at a specific time
     */
    public static class TimeSnapshot {
        public final long timestamp;
        public final List<MovingPacket> packets;
        public final List<System> systems;
        public final List<Wire> wires;
        public final int coins;
        public final int usedWireLength;
        
        public TimeSnapshot(long timestamp, List<MovingPacket> packets, 
                          List<System> systems, List<Wire> wires, 
                          int coins, int usedWireLength) {
            this.timestamp = timestamp;
            this.packets = new ArrayList<>(packets);
            this.systems = new ArrayList<>(systems);
            this.wires = new ArrayList<>(wires);
            this.coins = coins;
            this.usedWireLength = usedWireLength;
        }
    }
    
    /**
     * Enters time travel mode
     */
    public void enterTimeTravelMode() {
        if (isTimeTravelMode) return;
        
        Logger.getInstance().info("Entering Time Travel Mode");
        isTimeTravelMode = true;
        isPaused = true;
        currentTime = 0;
        maxTime = 0;
        
        // Clear previous snapshots
        timeSnapshots.clear();
        snapshotTimestamps.clear();
        
        // Reset execution results
        totalPacketsSpawned = 0;
        packetsReturned = 0;
        packetsLost = 0;
        packetLossPercentage = 0.0;
        levelCompleted = false;
    }
    
    /**
     * Exits time travel mode
     */
    public void exitTimeTravelMode() {
        if (!isTimeTravelMode) return;
        
        Logger.getInstance().info("Exiting Time Travel Mode");
        isTimeTravelMode = false;
        isExecuting = false;
        isPaused = false;
        currentTime = 0;
        maxTime = 0;
    }
    
    /**
     * Takes a snapshot of the current game state
     */
    public void takeSnapshot(GameEngine engine) {
        if (!isTimeTravelMode) return;
        
        TimeSnapshot snapshot = new TimeSnapshot(
            currentTime,
            new ArrayList<>(engine.getMovingPackets()),
            new ArrayList<>(engine.getSystems()),
            new ArrayList<>(engine.getWires()),
            engine.getCoins(),
            engine.getUsedWireLength()
        );
        
        timeSnapshots.put(currentTime, snapshot);
        snapshotTimestamps.add(currentTime);
        Collections.sort(snapshotTimestamps);
        
        Logger.getInstance().debug("Snapshot taken at time: " + currentTime + "ms");
    }
    
    /**
     * Restores game state to a specific time
     */
    public void restoreToTime(GameEngine engine, long targetTime) {
        if (!isTimeTravelMode) return;
        
        // Find the closest snapshot
        TimeSnapshot snapshot = findClosestSnapshot(targetTime);
        if (snapshot == null) {
            Logger.getInstance().warning("No snapshot found for time: " + targetTime);
            return;
        }
        
        // Restore game state
        engine.clearGameState();
        
        // Restore systems
        for (System system : snapshot.systems) {
            engine.addSystem(system);
        }
        
        // Restore wires
        for (Wire wire : snapshot.wires) {
            engine.addWire(wire);
        }
        
        // Restore moving packets
        for (MovingPacket packet : snapshot.packets) {
            engine.addMovingPacket(packet);
        }
        
        // Restore other state
        engine.setCoins(snapshot.coins);
        engine.setUsedWireLength(snapshot.usedWireLength);
        
        currentTime = targetTime;
        Logger.getInstance().debug("Restored to time: " + targetTime + "ms");
    }
    
    /**
     * Finds the closest snapshot to the target time
     */
    private TimeSnapshot findClosestSnapshot(long targetTime) {
        if (snapshotTimestamps.isEmpty()) return null;
        
        // Binary search for closest timestamp
        int index = Collections.binarySearch(snapshotTimestamps, targetTime);
        if (index >= 0) {
            // Exact match
            return timeSnapshots.get(snapshotTimestamps.get(index));
        } else {
            // No exact match, find closest
            int insertionPoint = -index - 1;
            if (insertionPoint == 0) {
                return timeSnapshots.get(snapshotTimestamps.get(0));
            } else if (insertionPoint == snapshotTimestamps.size()) {
                return timeSnapshots.get(snapshotTimestamps.get(snapshotTimestamps.size() - 1));
            } else {
                // Choose the closer one
                long prev = snapshotTimestamps.get(insertionPoint - 1);
                long next = snapshotTimestamps.get(insertionPoint);
                long closest = (targetTime - prev < next - targetTime) ? prev : next;
                return timeSnapshots.get(closest);
            }
        }
    }
    
    /**
     * Updates time travel state
     */
    public void update(GameEngine engine) {
        if (!isTimeTravelMode) return;
        
        if (isExecuting && !isPaused) {
            // Advance time during execution
            currentTime += (long)(16 * playbackSpeed); // Assuming 60 FPS
            
            // Spawn packets periodically during execution
            if (currentTime % 2000 == 0 && currentTime > 0) { // Spawn every 2 seconds
                engine.handleManualPacketSpawn();
                Logger.getInstance().debug("Auto-spawned packet at " + currentTime + "ms");
            }
            
            // Take periodic snapshots
            if (currentTime % SNAPSHOT_INTERVAL == 0) {
                takeSnapshot(engine);
                Logger.getInstance().debug("Snapshot taken at " + currentTime + "ms");
            }
            
            // Check if simulation is complete
            if (currentTime >= MAX_SIMULATION_TIME) {
                finishExecution(engine);
            }
        }
        
        // Update max time
        if (currentTime > maxTime) {
            maxTime = currentTime;
        }
    }
    
    /**
     * Starts network execution
     */
    public void startExecution(GameEngine engine) {
        if (!isTimeTravelMode || isExecuting) return;
        
        Logger.getInstance().info("Starting Network Execution");
        isExecuting = true;
        isPaused = false;
        currentTime = 0;
        
        // Clear previous results
        totalPacketsSpawned = 0;
        packetsReturned = 0;
        packetsLost = 0;
        packetLossPercentage = 0.0;
        levelCompleted = false;
        
        // Take initial snapshot
        takeSnapshot(engine);
    }
    
    /**
     * Finishes network execution and calculates results
     */
    public void finishExecution(GameEngine engine) {
        if (!isExecuting) return;
        
        Logger.getInstance().info("Finishing Network Execution");
        isExecuting = false;
        isPaused = true;
        
        // Calculate packet loss percentage
        if (totalPacketsSpawned > 0) {
            packetLossPercentage = (double) packetsLost / totalPacketsSpawned * 100.0;
        }
        
        // Check win condition (<50% packet loss)
        levelCompleted = packetLossPercentage < 50.0;
        
        Logger.getInstance().info("Execution Results:");
        Logger.getInstance().info("  Total Packets: " + totalPacketsSpawned);
        Logger.getInstance().info("  Packets Returned: " + packetsReturned);
        Logger.getInstance().info("  Packets Lost: " + packetsLost);
        Logger.getInstance().info("  Packet Loss: " + String.format("%.1f", packetLossPercentage) + "%");
        Logger.getInstance().info("  Level Completed: " + levelCompleted);
    }
    
    /**
     * Records a packet spawn during execution
     */
    public void recordPacketSpawn() {
        if (isExecuting) {
            totalPacketsSpawned++;
        }
    }
    
    /**
     * Records a packet return during execution
     */
    public void recordPacketReturn() {
        if (isExecuting) {
            packetsReturned++;
        }
    }
    
    /**
     * Records a packet loss during execution
     */
    public void recordPacketLoss() {
        if (isExecuting) {
            packetsLost++;
        }
    }
    
    // Getters and setters
    public boolean isTimeTravelMode() { return isTimeTravelMode; }
    public boolean isExecuting() { return isExecuting; }
    public boolean isPaused() { return isPaused; }
    public long getCurrentTime() { return currentTime; }
    public long getMaxTime() { return maxTime; }
    public double getPlaybackSpeed() { return playbackSpeed; }
    public int getTotalPacketsSpawned() { return totalPacketsSpawned; }
    public int getPacketsReturned() { return packetsReturned; }
    public int getPacketsLost() { return packetsLost; }
    public double getPacketLossPercentage() { return packetLossPercentage; }
    public boolean isLevelCompleted() { return levelCompleted; }
    
    public void setPaused(boolean paused) { this.isPaused = paused; }
    public void setPlaybackSpeed(double speed) { this.playbackSpeed = Math.max(0.1, Math.min(5.0, speed)); }
    
    /**
     * Seeks to a specific time
     */
    public void seekToTime(GameEngine engine, long time) {
        if (!isTimeTravelMode) return;
        
        long targetTime = Math.max(0, Math.min(time, maxTime));
        restoreToTime(engine, targetTime);
    }
    
    /**
     * Gets available snapshot timestamps
     */
    public List<Long> getSnapshotTimestamps() {
        return new ArrayList<>(snapshotTimestamps);
    }
}
