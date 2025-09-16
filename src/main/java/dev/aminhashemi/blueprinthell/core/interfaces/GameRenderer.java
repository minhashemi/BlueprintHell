package dev.aminhashemi.blueprinthell.core.interfaces;

import java.awt.Graphics2D;

/**
 * Interface for game rendering following Single Responsibility Principle.
 * Handles all rendering operations for the game.
 */
public interface GameRenderer {
    
    /**
     * Renders the game to the provided graphics context.
     * @param g2d Graphics2D context for rendering
     */
    void render(Graphics2D g2d);
    
    /**
     * Renders the HUD (Heads-Up Display) to the provided graphics context.
     * @param g2d Graphics2D context for rendering
     */
    void renderHUD(Graphics2D g2d);
    
    /**
     * Renders debug information to the provided graphics context.
     * @param g2d Graphics2D context for rendering
     */
    void renderDebug(Graphics2D g2d);
}