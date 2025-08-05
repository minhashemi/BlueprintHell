package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.Entity;

public abstract class Packet extends Entity {

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
}
