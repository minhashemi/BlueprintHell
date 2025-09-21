package dev.aminhashemi.blueprinthell.core.factory;

import dev.aminhashemi.blueprinthell.model.entities.systems.*;
import dev.aminhashemi.blueprinthell.model.enums.SystemType;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.core.constants.GameConstants;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.awt.Point;

/**
 * Concrete implementation of ISystemFactory.
 * 
 * This class follows the Factory Pattern and provides a centralized way
 * to create systems of different types. It's easily extensible for new
 * system types without modifying existing code.
 * 
 * It also follows the Single Responsibility Principle by focusing solely
 * on system creation.
 */
public class SystemFactory implements ISystemFactory {
    
    private static final Logger logger = Logger.getInstance();
    
    /**
     * Creates a system of the specified type at the given position.
     * 
     * @param systemType The type of system to create
     * @param position The position to create the system at
     * @param systemData The system data from level configuration
     * @return The created system, or null if creation failed
     */
    @Override
    public dev.aminhashemi.blueprinthell.model.entities.systems.System createSystem(SystemType systemType, Point position, LevelData.SystemData systemData) {
        if (systemType == null || position == null) {
            logger.warning("Cannot create system: systemType or position is null");
            return null;
        }
        
        try {
            return switch (systemType) {
                case REFERENCE -> 
                    new ReferenceSystem(position.x, position.y, systemData);
                    
                case VPN -> 
                    new VPNSystem(position.x, position.y, systemData);
                    
                case MALICIOUS -> 
                    new MaliciousSystem(position.x, position.y, systemData);
                    
                case SPY -> 
                    new SpySystem(position.x, position.y, systemData);
                    
                default -> {
                    logger.warning("Unknown system type: " + systemType);
                    yield null;
                }
            };
        } catch (Exception e) {
            logger.error("Failed to create system of type " + systemType, e);
            return null;
        }
    }
    
    /**
     * Creates a system of the specified type with default data.
     * 
     * @param systemType The type of system to create
     * @param position The position to create the system at
     * @return The created system, or null if creation failed
     */
    @Override
    public dev.aminhashemi.blueprinthell.model.entities.systems.System createSystem(SystemType systemType, Point position) {
        // Create default system data
        LevelData.SystemData defaultData = new LevelData.SystemData();
        defaultData.type = systemType.name().toLowerCase();
        defaultData.id = systemType.name() + "_" + position.x + "_" + position.y;
        
        return createSystem(systemType, position, defaultData);
    }
    
    /**
     * Creates a system from a string identifier.
     * 
     * @param systemTypeString The string identifier for the system type
     * @param position The position to create the system at
     * @param systemData The system data from level configuration
     * @return The created system, or null if creation failed
     */
    @Override
    public dev.aminhashemi.blueprinthell.model.entities.systems.System createSystem(String systemTypeString, Point position, LevelData.SystemData systemData) {
        if (systemTypeString == null) {
            logger.warning("Cannot create system: systemTypeString is null");
            return null;
        }
        
        SystemType systemType = SystemType.fromString(systemTypeString);
        if (systemType == null) {
            logger.warning("Unknown system type string: " + systemTypeString);
            return null;
        }
        
        return createSystem(systemType, position, systemData);
    }
    
    /**
     * Gets all available system types that can be created.
     * 
     * @return Array of available system types
     */
    @Override
    public SystemType[] getAvailableSystemTypes() {
        return SystemType.values();
    }
    
    /**
     * Checks if a system type is supported by this factory.
     * 
     * @param systemType The system type to check
     * @return True if the system type is supported
     */
    @Override
    public boolean supportsSystemType(SystemType systemType) {
        return systemType != null;
    }
    
    /**
     * Checks if a system type string is supported by this factory.
     * 
     * @param systemTypeString The system type string to check
     * @return True if the system type string is supported
     */
    @Override
    public boolean supportsSystemType(String systemTypeString) {
        return systemTypeString != null && SystemType.fromString(systemTypeString) != null;
    }
    
    /**
     * Gets the default width for a system type.
     * 
     * @param systemType The system type
     * @return The default width
     */
    @Override
    public int getDefaultWidth(SystemType systemType) {
        if (systemType == null) {
            return 60; // Default system width
        }
        return systemType.getDefaultWidth();
    }
    
    /**
     * Gets the default height for a system type.
     * 
     * @param systemType The system type
     * @return The default height
     */
    @Override
    public int getDefaultHeight(SystemType systemType) {
        if (systemType == null) {
            return 60; // Default system height
        }
        return systemType.getDefaultHeight();
    }
    
    /**
     * Creates a system with random properties for testing.
     * 
     * @param position The position to create the system at
     * @return The created system, or null if creation failed
     */
    public dev.aminhashemi.blueprinthell.model.entities.systems.System createRandomSystem(Point position) {
        SystemType[] availableTypes = getAvailableSystemTypes();
        if (availableTypes.length == 0) {
            logger.warning("No available system types for random creation");
            return null;
        }
        
        SystemType randomType = availableTypes[(int) (Math.random() * availableTypes.length)];
        return createSystem(randomType, position);
    }
}
