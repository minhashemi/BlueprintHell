package dev.aminhashemi.blueprinthell.model.entities;

import dev.aminhashemi.blueprinthell.core.GameEngine;
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

    // The signature is now consistent for all entities
    public abstract void update(GameEngine engine);
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
