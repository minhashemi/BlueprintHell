package dev.aminhashemi.blueprinthell.model.effects;

import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.systems.PortType;
import dev.aminhashemi.blueprinthell.model.entities.systems.System;

import java.awt.Point;

/**
 * Handles packet effects and interactions.
 * Single responsibility: managing packet effects and port compatibility.
 */
public class PacketEffects {
    
    private final Packet packet;
    private float noiseLevel = 0.0f;
    private boolean isLost = false;
    
    public PacketEffects(Packet packet) {
        this.packet = packet;
    }
    
    /**
     * Applies port compatibility effects to the packet.
     */
    public void applyPortCompatibilityEffect(PortType portType, boolean isCompatible) {
        switch (packet.getType()) {
            case GREEN_DIAMOND_SMALL:
                if (portType == PortType.DIAMOND && isCompatible) {
                    // Diamond packets work better with diamond ports
                    // Note: Packet doesn't have setSize method, so we'll just track the effect
                } else if (!isCompatible) {
                    // Incompatible ports cause issues
                    increaseNoise(0.1f);
                }
                break;
                
            case GREEN_DIAMOND_LARGE:
                if (portType == PortType.DIAMOND && isCompatible) {
                    // Diamond packets work better with diamond ports
                } else if (!isCompatible) {
                    increaseNoise(0.15f);
                }
                break;
                
            case INFINITY_SYMBOL:
                // Infinity symbols are more resilient
                if (!isCompatible) {
                    increaseNoise(0.05f);
                }
                break;
                
            case BULK_PACKET_SMALL, BULK_PACKET_LARGE:
                // Bulk packets are sensitive to port compatibility
                if (!isCompatible) {
                    increaseNoise(0.3f);
                }
                break;
                
            case PADLOCK_ICON:
                // Protected packets have some resistance
                if (!isCompatible) {
                    increaseNoise(0.1f);
                }
                break;
        }
    }
    
    /**
     * Handles collision effects for the packet.
     */
    public void handleCollision() {
        switch (packet.getType()) {
            case INFINITY_SYMBOL:
                // Infinity symbols reverse direction on collision
                // This would be handled by the movement system
                break;
                
            case BULK_PACKET_SMALL, BULK_PACKET_LARGE:
                // Bulk packets can split on collision
                if (Math.random() < 0.3) {
                    increaseNoise(0.2f);
                }
                break;
                
            case PADLOCK_ICON:
                // Protected packets have collision resistance
                increaseNoise(0.1f);
                break;
                
            case CAMOUFLAGE_ICON_SMALL, CAMOUFLAGE_ICON_LARGE:
                // Confidential packets are very sensitive to collisions
                increaseNoise(0.3f);
                break;
                
            default:
                increaseNoise(0.2f);
                break;
        }
    }
    
    /**
     * Applies impact effects from external sources.
     */
    public void applyImpactEffect(Point impactPoint, float intensity) {
        Point packetPos = packet.getPosition();
        double distance = packetPos.distance(impactPoint);
        
        // Closer impacts have stronger effects
        float effectStrength = (float) (intensity / (1.0 + distance / 100.0));
        increaseNoise(effectStrength * 0.1f);
    }
    
    /**
     * Increases the packet's noise level.
     */
    public void increaseNoise(float amount) {
        this.noiseLevel += amount;
        // If noise gets too high (50%), packet is lost
        if (noiseLevel > 0.5f) {
            isLost = true;
        }
    }
    
    /**
     * Reduces the packet's noise level.
     */
    public void reduceNoise(float amount) {
        this.noiseLevel = Math.max(0.0f, this.noiseLevel - amount);
    }
    
    // Getters and setters
    public float getNoiseLevel() {
        return noiseLevel;
    }
    
    public void setNoiseLevel(float noiseLevel) {
        this.noiseLevel = noiseLevel;
    }
    
    public boolean isLost() {
        return isLost;
    }
    
    public void setLost(boolean lost) {
        this.isLost = lost;
    }
}
