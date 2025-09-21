package dev.aminhashemi.blueprinthell.core.state;

import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.MovingPacket;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Encapsulates the current game state.
 * Follows Single Responsibility Principle by managing only game state data.
 */
public class GameState {
    
    // ==================== GAME ENTITIES ====================
    private final List<System> systems = new CopyOnWriteArrayList<>();
    private final List<Wire> wires = new CopyOnWriteArrayList<>();
    private final List<MovingPacket> movingPackets = new CopyOnWriteArrayList<>();
    
    // ==================== GAME MECHANICS ====================
    private int totalWireLength = 10000;
    private int usedWireLength = 0;
    private int coins = 0;
    private double packetSpeedMultiplier = 1.0;
    
    // ==================== INTERACTION STATE ====================
    private System draggedSystem = null;
    private Wire selectedWire = null;
    private boolean inWiringMode = false;
    private boolean isShopOpen = false;
    private Point currentMousePos = new Point();
    
    // ==================== LEVEL STATE ====================
    private int currentLevelNumber = 1;
    private String currentLevelName = "Level 1";
    private int maxLevelNumber = 3;
    
    // ==================== TEST STATE ====================
    private boolean isTestRunning = false;
    private int testPacketsReleased = 0;
    private int testPacketsReturned = 0;
    private int testPacketsLost = 0;
    private long testStartTime = 0;
    private long lastPacketReleaseTime = 0;
    private boolean testCompleted = false;
    private boolean gameWon = false;
    private boolean gameLost = false;
    
    // ==================== TIME TRAVEL STATE ====================
    private boolean isTimeTravelMode = false;
    private boolean isPaused = false;
    private int currentSnapshotIndex = -1;
    private int timeTravelWindowSeconds = 30;
    
    // ==================== SHOP STATE ====================
    private boolean impactWavesDisabled = false;
    private boolean packetCollisionsDisabled = false;
    private boolean packetNoiseZeroed = false;
    
    // ==================== GETTERS AND SETTERS ====================
    
    // Systems
    public List<System> getSystems() {
        return new ArrayList<>(systems);
    }
    
    public void addSystem(System system) {
        systems.add(system);
    }
    
    public void removeSystem(System system) {
        systems.remove(system);
    }
    
    public void clearSystems() {
        systems.clear();
    }
    
    // Wires
    public List<Wire> getWires() {
        return new ArrayList<>(wires);
    }
    
    public void addWire(Wire wire) {
        wires.add(wire);
    }
    
    public void removeWire(Wire wire) {
        wires.remove(wire);
    }
    
    public void clearWires() {
        wires.clear();
    }
    
    // Moving Packets
    public List<MovingPacket> getMovingPackets() {
        return new ArrayList<>(movingPackets);
    }
    
    public void addMovingPacket(MovingPacket packet) {
        movingPackets.add(packet);
    }
    
    public void removeMovingPacket(MovingPacket packet) {
        movingPackets.remove(packet);
    }
    
    public void clearMovingPackets() {
        movingPackets.clear();
    }
    
    // Wire Length
    public int getTotalWireLength() {
        return totalWireLength;
    }
    
    public void setTotalWireLength(int totalWireLength) {
        this.totalWireLength = totalWireLength;
    }
    
    public int getUsedWireLength() {
        return usedWireLength;
    }
    
    public void setUsedWireLength(int usedWireLength) {
        this.usedWireLength = usedWireLength;
    }
    
    public int getAvailableWireLength() {
        return totalWireLength - usedWireLength;
    }
    
    // Coins
    public int getCoins() {
        return coins;
    }
    
    public void setCoins(int coins) {
        this.coins = coins;
    }
    
    public void addCoins(int amount) {
        this.coins += amount;
    }
    
    public void removeCoins(int amount) {
        this.coins = Math.max(0, this.coins - amount);
    }
    
    // Packet Speed
    public double getPacketSpeedMultiplier() {
        return packetSpeedMultiplier;
    }
    
    public void setPacketSpeedMultiplier(double packetSpeedMultiplier) {
        this.packetSpeedMultiplier = packetSpeedMultiplier;
    }
    
    // Interaction State
    public System getDraggedSystem() {
        return draggedSystem;
    }
    
    public void setDraggedSystem(System draggedSystem) {
        this.draggedSystem = draggedSystem;
    }
    
    public Wire getSelectedWire() {
        return selectedWire;
    }
    
    public void setSelectedWire(Wire selectedWire) {
        this.selectedWire = selectedWire;
    }
    
    public boolean isInWiringMode() {
        return inWiringMode;
    }
    
    public void setInWiringMode(boolean inWiringMode) {
        this.inWiringMode = inWiringMode;
    }
    
    public boolean isShopOpen() {
        return isShopOpen;
    }
    
    public void setShopOpen(boolean shopOpen) {
        isShopOpen = shopOpen;
    }
    
    public Point getCurrentMousePos() {
        return currentMousePos;
    }
    
    public void setCurrentMousePos(Point currentMousePos) {
        this.currentMousePos = currentMousePos;
    }
    
    // Level State
    public int getCurrentLevelNumber() {
        return currentLevelNumber;
    }
    
    public void setCurrentLevelNumber(int currentLevelNumber) {
        this.currentLevelNumber = currentLevelNumber;
    }
    
    public String getCurrentLevelName() {
        return currentLevelName;
    }
    
    public void setCurrentLevelName(String currentLevelName) {
        this.currentLevelName = currentLevelName;
    }
    
    public int getMaxLevelNumber() {
        return maxLevelNumber;
    }
    
    public void setMaxLevelNumber(int maxLevelNumber) {
        this.maxLevelNumber = maxLevelNumber;
    }
    
    // Test State
    public boolean isTestRunning() {
        return isTestRunning;
    }
    
    public void setTestRunning(boolean testRunning) {
        isTestRunning = testRunning;
    }
    
    public int getTestPacketsReleased() {
        return testPacketsReleased;
    }
    
    public void setTestPacketsReleased(int testPacketsReleased) {
        this.testPacketsReleased = testPacketsReleased;
    }
    
    public int getTestPacketsReturned() {
        return testPacketsReturned;
    }
    
    public void setTestPacketsReturned(int testPacketsReturned) {
        this.testPacketsReturned = testPacketsReturned;
    }
    
    public int getTestPacketsLost() {
        return testPacketsLost;
    }
    
    public void setTestPacketsLost(int testPacketsLost) {
        this.testPacketsLost = testPacketsLost;
    }
    
    public long getTestStartTime() {
        return testStartTime;
    }
    
    public void setTestStartTime(long testStartTime) {
        this.testStartTime = testStartTime;
    }
    
    public long getLastPacketReleaseTime() {
        return lastPacketReleaseTime;
    }
    
    public void setLastPacketReleaseTime(long lastPacketReleaseTime) {
        this.lastPacketReleaseTime = lastPacketReleaseTime;
    }
    
    public boolean isTestCompleted() {
        return testCompleted;
    }
    
    public void setTestCompleted(boolean testCompleted) {
        this.testCompleted = testCompleted;
    }
    
    public boolean isGameWon() {
        return gameWon;
    }
    
    public void setGameWon(boolean gameWon) {
        this.gameWon = gameWon;
    }
    
    public boolean isGameLost() {
        return gameLost;
    }
    
    public void setGameLost(boolean gameLost) {
        this.gameLost = gameLost;
    }
    
    // Time Travel State
    public boolean isTimeTravelMode() {
        return isTimeTravelMode;
    }
    
    public void setTimeTravelMode(boolean timeTravelMode) {
        isTimeTravelMode = timeTravelMode;
    }
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public void setPaused(boolean paused) {
        isPaused = paused;
    }
    
    public int getCurrentSnapshotIndex() {
        return currentSnapshotIndex;
    }
    
    public void setCurrentSnapshotIndex(int currentSnapshotIndex) {
        this.currentSnapshotIndex = currentSnapshotIndex;
    }
    
    public int getTimeTravelWindowSeconds() {
        return timeTravelWindowSeconds;
    }
    
    public void setTimeTravelWindowSeconds(int timeTravelWindowSeconds) {
        this.timeTravelWindowSeconds = timeTravelWindowSeconds;
    }
    
    // Shop State
    public boolean isImpactWavesDisabled() {
        return impactWavesDisabled;
    }
    
    public void setImpactWavesDisabled(boolean impactWavesDisabled) {
        this.impactWavesDisabled = impactWavesDisabled;
    }
    
    public boolean isPacketCollisionsDisabled() {
        return packetCollisionsDisabled;
    }
    
    public void setPacketCollisionsDisabled(boolean packetCollisionsDisabled) {
        this.packetCollisionsDisabled = packetCollisionsDisabled;
    }
    
    public boolean isPacketNoiseZeroed() {
        return packetNoiseZeroed;
    }
    
    public void setPacketNoiseZeroed(boolean packetNoiseZeroed) {
        this.packetNoiseZeroed = packetNoiseZeroed;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Resets all game state to initial values
     */
    public void reset() {
        clearSystems();
        clearWires();
        clearMovingPackets();
        
        usedWireLength = 0;
        coins = 0;
        packetSpeedMultiplier = 1.0;
        
        draggedSystem = null;
        selectedWire = null;
        inWiringMode = false;
        isShopOpen = false;
        currentMousePos = new Point();
        
        currentLevelNumber = 1;
        currentLevelName = "Level 1";
        
        isTestRunning = false;
        testPacketsReleased = 0;
        testPacketsReturned = 0;
        testPacketsLost = 0;
        testStartTime = 0;
        lastPacketReleaseTime = 0;
        testCompleted = false;
        gameWon = false;
        gameLost = false;
        
        isTimeTravelMode = false;
        isPaused = false;
        currentSnapshotIndex = -1;
        
        impactWavesDisabled = false;
        packetCollisionsDisabled = false;
        packetNoiseZeroed = false;
    }
}
