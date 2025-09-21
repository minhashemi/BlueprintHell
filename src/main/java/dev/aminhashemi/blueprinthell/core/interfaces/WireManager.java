package dev.aminhashemi.blueprinthell.core.interfaces;

import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.model.world.ArcPoint;

import java.awt.Point;
import java.util.List;

/**
 * Interface for managing wire operations.
 * Follows Interface Segregation Principle by focusing only on wire management.
 */
public interface WireManager {
    
    /**
     * Starts wire creation mode
     */
    void startWiringMode();
    
    /**
     * Stops wire creation mode
     */
    void stopWiringMode();
    
    /**
     * Checks if in wiring mode
     * @return True if in wiring mode
     */
    boolean isInWiringMode();
    
    /**
     * Handles mouse press for wire creation
     * @param point Mouse position
     * @param ctrlPressed Whether Ctrl key is pressed
     */
    void handleMousePress(Point point, boolean ctrlPressed);
    
    /**
     * Handles mouse drag for wire creation
     * @param point Mouse position
     */
    void handleMouseDrag(Point point);
    
    /**
     * Handles mouse release for wire creation
     * @param point Mouse position
     */
    void handleMouseRelease(Point point);
    
    /**
     * Creates a wire between two systems
     * @param startSystem Starting system
     * @param endSystem Ending system
     * @param arcPoints Arc points for the wire
     * @return Created wire
     */
    Wire createWire(System startSystem, System endSystem, List<ArcPoint> arcPoints);
    
    /**
     * Removes a wire
     * @param wire Wire to remove
     */
    void removeWire(Wire wire);
    
    /**
     * Selects a wire
     * @param wire Wire to select
     */
    void selectWire(Wire wire);
    
    /**
     * Removes the selected wire
     */
    void removeSelectedWire();
    
    /**
     * Gets all wires
     * @return List of wires
     */
    List<Wire> getWires();
    
    /**
     * Gets the selected wire
     * @return Selected wire or null
     */
    Wire getSelectedWire();
    
    /**
     * Gets available wire length
     * @return Available wire length
     */
    int getAvailableWireLength();
    
    /**
     * Gets used wire length
     * @return Used wire length
     */
    int getUsedWireLength();
}
