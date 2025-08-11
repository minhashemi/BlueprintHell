package dev.aminhashemi.blueprinthell.model.entities;

import java.awt.*;

public abstract class Entity {

    protected int x, y;
    protected int width, height;

    public Entity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void update();
    public abstract void draw(Graphics2D g);

    public boolean contains(Point p) {
        return new Rectangle(x, y, width, height).contains(p);
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // --- NEW METHOD ---
    /**
     * Returns the rectangular bounds of this entity.
     * Useful for collision detection.
     * @return A Rectangle object representing the entity's bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }


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
