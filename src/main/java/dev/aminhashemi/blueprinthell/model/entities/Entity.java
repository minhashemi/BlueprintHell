package dev.aminhashemi.blueprinthell.model.entities;

import java.awt.*;

/**
 * The abstract base class for any object that exists within the game world.
 * It provides fundamental properties like position (x, y) and dimensions (width, height).
 */
public abstract class Entity {

    protected int x, y;
    protected int width, height;

    public Entity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // Abstract methods to be implemented by all concrete entities
    public abstract void update();
    public abstract void draw(Graphics2D g);

    // Getters
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
}
