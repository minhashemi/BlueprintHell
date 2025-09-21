package dev.aminhashemi.blueprinthell.model.entities;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import java.awt.*;

/**
 * Abstract base class for all game entities.
 * Provides common functionality for position, bounds checking, and rendering.
 */
public abstract class GameEntity {

    // ==================== POSITION AND DIMENSIONS ====================
    protected int x, y;           // Entity position
    protected int width, height;  // Entity dimensions

    /**
     * Constructs an entity with the specified position and dimensions.
     * @param x X coordinate
     * @param y Y coordinate
     * @param width Entity width
     * @param height Entity height
     */
    public GameEntity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // ==================== ABSTRACT METHODS ====================
    /**
     * Updates the entity's state each frame.
     * @param engine Reference to the game engine
     */
    public abstract void update(GameEngine engine);
    
    /**
     * Renders the entity to the screen.
     * @param g Graphics2D context for drawing
     */
    public abstract void draw(Graphics2D g);

    public boolean contains(Point p) {
        return new Rectangle(x, y, width, height).contains(p);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    public Point getPosition() {
        return new Point(x, y);
    }
}
