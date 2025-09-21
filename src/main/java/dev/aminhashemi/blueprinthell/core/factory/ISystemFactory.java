package dev.aminhashemi.blueprinthell.core.factory;

import dev.aminhashemi.blueprinthell.model.entities.systems.System;
import dev.aminhashemi.blueprinthell.model.enums.SystemType;
import dev.aminhashemi.blueprinthell.model.LevelData;

import java.awt.Point;

/**
 * Factory interface for creating systems.
 * 
 * This interface follows the Factory Pattern and allows for easy extension
 * of new system types without modifying existing code.
 * 
 * It also follows the Open/Closed Principle - open for extension, closed for modification.
 */
public interface ISystemFactory {
    
    /**
     * Creates a system of the specified type at the given position.
     * 
     * @param systemType The type of system to create
     * @param position The position to create the system at
     * @param systemData The system data from level configuration
     * @return The created system, or null if creation failed
     */
    System createSystem(SystemType systemType, Point position, LevelData.SystemData systemData);
    
    /**
     * Creates a system of the specified type with default data.
     * 
     * @param systemType The type of system to create
     * @param position The position to create the system at
     * @return The created system, or null if creation failed
     */
    System createSystem(SystemType systemType, Point position);
    
    /**
     * Creates a system from a string identifier.
     * 
     * @param systemTypeString The string identifier for the system type
     * @param position The position to create the system at
     * @param systemData The system data from level configuration
     * @return The created system, or null if creation failed
     */
    System createSystem(String systemTypeString, Point position, LevelData.SystemData systemData);
    
    /**
     * Gets all available system types that can be created.
     * 
     * @return Array of available system types
     */
    SystemType[] getAvailableSystemTypes();
    
    /**
     * Checks if a system type is supported by this factory.
     * 
     * @param systemType The system type to check
     * @return True if the system type is supported
     */
    boolean supportsSystemType(SystemType systemType);
    
    /**
     * Checks if a system type string is supported by this factory.
     * 
     * @param systemTypeString The system type string to check
     * @return True if the system type string is supported
     */
    boolean supportsSystemType(String systemTypeString);
    
    /**
     * Gets the default width for a system type.
     * 
     * @param systemType The system type
     * @return The default width
     */
    int getDefaultWidth(SystemType systemType);
    
    /**
     * Gets the default height for a system type.
     * 
     * @param systemType The system type
     * @return The default height
     */
    int getDefaultHeight(SystemType systemType);
}
