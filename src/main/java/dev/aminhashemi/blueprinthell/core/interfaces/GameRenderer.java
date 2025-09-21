package dev.aminhashemi.blueprinthell.core.interfaces;

import java.awt.Graphics2D;

/**
 * Interface for game rendering operations.
 * Follows Interface Segregation Principle by focusing only on rendering.
 */
public interface GameRenderer {
    
    /**
     * Renders the game state
     * @param g2d Graphics2D context
     */
    void render(Graphics2D g2d);
    
    /**
     * Renders the game background
     * @param g2d Graphics2D context
     */
    void renderBackground(Graphics2D g2d);
    
    /**
     * Renders all systems
     * @param g2d Graphics2D context
     */
    void renderSystems(Graphics2D g2d);
    
    /**
     * Renders all wires
     * @param g2d Graphics2D context
     */
    void renderWires(Graphics2D g2d);
    
    /**
     * Renders all moving packets
     * @param g2d Graphics2D context
     */
    void renderPackets(Graphics2D g2d);
    
    /**
     * Renders the UI overlay
     * @param g2d Graphics2D context
     */
    void renderUI(Graphics2D g2d);
    
    /**
     * Renders the shop overlay
     * @param g2d Graphics2D context
     */
    void renderShop(Graphics2D g2d);
}