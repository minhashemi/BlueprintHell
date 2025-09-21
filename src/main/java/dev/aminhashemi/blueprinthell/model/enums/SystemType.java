package dev.aminhashemi.blueprinthell.model.enums;

import java.awt.Color;
import dev.aminhashemi.blueprinthell.core.constants.GameConstants;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;

/**
 * Enum representing different types of network systems in the game.
 * Each system type has specific behaviors and visual characteristics.
 * 
 * This enum follows the Open/Closed Principle - new system types can be added
 * without modifying existing code, making the system easily extensible.
 */
public enum SystemType {
    
    /**
     * Reference System - The starting and ending point for packets
     * - Color: Blue
     * - Behavior: Source and destination for packets
     * - Ports: All types accepted
     */
    REFERENCE("Reference", Color.BLUE, "Starting and ending point for packets"),
    
    /**
     * VPN System - Protects packets from malicious interference
     * - Color: Magenta
     * - Behavior: Converts packets to protected packets
     * - Ports: VPN ports only
     */
    VPN("VPN", Color.MAGENTA, "Protects packets from malicious interference"),
    
    /**
     * Malicious System - Corrupts and interferes with packets
     * - Color: Red
     * - Behavior: Adds noise, sends to wrong ports, creates trojans
     * - Ports: All types (but sends to wrong ones)
     */
    MALICIOUS("Malicious", Color.RED, "Corrupts and interferes with packets"),
    
    /**
     * Spy System - Teleports packets and destroys confidential ones
     * - Color: Orange
     * - Behavior: Teleports packets, destroys confidential packets
     * - Ports: Spy ports only
     */
    SPY("Spy", Color.ORANGE, "Teleports packets and destroys confidential ones");
    
    private final String displayName;
    private final Color color;
    private final String description;
    
    /**
     * Constructs a SystemType with the specified properties.
     * 
     * @param displayName Human-readable name for the system
     * @param color Visual color representation
     * @param description Brief description of the system's behavior
     */
    SystemType(String displayName, Color color, String description) {
        this.displayName = displayName;
        this.color = color;
        this.description = description;
    }
    
    /**
     * Gets the human-readable display name.
     * 
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Gets the visual color representation.
     * 
     * @return The color
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Gets the description of the system's behavior.
     * 
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Gets the default width for this system type.
     * 
     * @return The default width
     */
    public int getDefaultWidth() {
        return 60; // Default system width
    }
    
    /**
     * Gets the default height for this system type.
     * 
     * @return The default height
     */
    public int getDefaultHeight() {
        return 60; // Default system height
    }
    
    /**
     * Checks if this system type can process the given packet type.
     * 
     * @param packetType The packet type to check
     * @return True if this system can process the packet type
     */
    public boolean canProcessPacket(PacketType packetType) {
        switch (this) {
            case REFERENCE:
                return true; // Reference systems accept all packet types
            case VPN:
                return packetType == PacketType.PADLOCK_ICON; // VPN processes protected packets
            case MALICIOUS:
                return true; // Malicious systems can process all packets
            case SPY:
                return packetType == PacketType.CAMOUFLAGE_ICON_SMALL || 
                       packetType == PacketType.CAMOUFLAGE_ICON_LARGE; // Spy processes confidential packets
            default:
                return false;
        }
    }
    
    /**
     * Gets the system type from a string identifier.
     * 
     * @param typeString The string identifier
     * @return The corresponding SystemType, or null if not found
     */
    public static SystemType fromString(String typeString) {
        if (typeString == null) {
            return null;
        }
        
        for (SystemType type : values()) {
            if (type.name().equalsIgnoreCase(typeString) || 
                type.displayName.equalsIgnoreCase(typeString)) {
                return type;
            }
        }
        return null;
    }
}
