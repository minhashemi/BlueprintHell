package dev.aminhashemi.blueprinthell.core.interfaces;

import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.MovingPacket;

import java.util.List;

/**
 * Interface for game state management following Single Responsibility Principle.
 * Handles all game state operations including systems, wires, and packets.
 */
public interface GameState {
    
    // ==================== SYSTEM MANAGEMENT ====================
    
    /**
     * Gets all systems in the game.
     * @return List of all systems
     */
    List<System> getSystems();
    
    /**
     * Adds a system to the game.
     * @param system The system to add
     */
    void addSystem(System system);
    
    /**
     * Removes a system from the game.
     * @param system The system to remove
     */
    void removeSystem(System system);
    
    // ==================== WIRE MANAGEMENT ====================
    
    /**
     * Gets all wires in the game.
     * @return List of all wires
     */
    List<Wire> getWires();
    
    /**
     * Adds a wire to the game.
     * @param wire The wire to add
     */
    void addWire(Wire wire);
    
    /**
     * Removes a wire from the game.
     * @param wire The wire to remove
     */
    void removeWire(Wire wire);
    
    // ==================== PACKET MANAGEMENT ====================
    
    /**
     * Gets all moving packets in the game.
     * @return List of all moving packets
     */
    List<MovingPacket> getMovingPackets();
    
    /**
     * Adds a moving packet to the game.
     * @param packet The packet to add
     */
    void addMovingPacket(MovingPacket packet);
    
    /**
     * Removes a moving packet from the game.
     * @param packet The packet to remove
     */
    void removeMovingPacket(MovingPacket packet);
    
    // ==================== GAME STATE PROPERTIES ====================
    
    /**
     * Gets the current coin count.
     * @return Current coins
     */
    int getCoins();
    
    /**
     * Sets the coin count.
     * @param coins New coin count
     */
    void setCoins(int coins);
    
    /**
     * Adds coins to the current count.
     * @param amount Amount to add
     */
    void addCoins(int amount);
    
    /**
     * Gets the total wire length available.
     * @return Total wire length
     */
    int getTotalWireLength();
    
    /**
     * Sets the total wire length available.
     * @param length New total wire length
     */
    void setTotalWireLength(int length);
    
    /**
     * Gets the used wire length.
     * @return Used wire length
     */
    int getUsedWireLength();
    
    /**
     * Sets the used wire length.
     * @param length New used wire length
     */
    void setUsedWireLength(int length);
    
    /**
     * Gets the remaining wire length.
     * @return Remaining wire length
     */
    int getRemainingWireLength();
    
    // ==================== GAME MODE STATES ====================
    
    /**
     * Checks if the game is in wiring mode.
     * @return True if in wiring mode
     */
    boolean isWiringMode();
    
    /**
     * Sets the wiring mode state.
     * @param wiringMode New wiring mode state
     */
    void setWiringMode(boolean wiringMode);
    
    /**
     * Checks if the game is paused.
     * @return True if paused
     */
    boolean isPaused();
    
    /**
     * Sets the paused state.
     * @param paused New paused state
     */
    void setPaused(boolean paused);
    
    /**
     * Checks if the game is in time travel mode.
     * @return True if in time travel mode
     */
    boolean isTimeTravelMode();
    
    /**
     * Sets the time travel mode state.
     * @param timeTravelMode New time travel mode state
     */
    void setTimeTravelMode(boolean timeTravelMode);
    
    // ==================== WIRE LENGTH TRACKING ====================
    
    /**
     * Gets the length of a specific wire.
     * @param wire The wire to check
     * @return Length of the wire
     */
    int getWireLength(Wire wire);
    
    /**
     * Sets the length of a specific wire.
     * @param wire The wire to set length for
     * @param length New length
     */
    void setWireLength(Wire wire, int length);
    
    /**
     * Gets all wire lengths as a map.
     * @return Map of wires to their lengths
     */
    java.util.Map<Wire, Integer> getWireLengths();
    
    /**
     * Sets all wire lengths from a map.
     * @param wireLengths Map of wires to their lengths
     */
    void setWireLengths(java.util.Map<Wire, Integer> wireLengths);
}