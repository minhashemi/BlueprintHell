package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.GameEntity;

public abstract class Packet extends GameEntity {

    protected double noise;
    protected double speed;
    protected double dx, dy;

    public Packet(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.noise = 0;
        this.speed = 0;
        this.dx = 0;
        this.dy = 0;
    }

    @Override
    public abstract void update(GameEngine engine);
    public abstract PacketType getType();
    
    // Getters and setters
    public double getNoise() { return noise; }
    public void setNoise(double noise) { this.noise = noise; }
    public double getSpeed() { return speed; }
    public void setSpeed(double speed) { this.speed = speed; }
    public double getDx() { return dx; }
    public void setDx(double dx) { this.dx = dx; }
    public double getDy() { return dy; }
    public void setDy(double dy) { this.dy = dy; }
}
