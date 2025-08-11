package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.model.entities.Entity;

/**
 * An abstract base class for all packet types in the game.
 * It extends Entity and adds properties common to all packets.
 */
public abstract class Packet extends Entity {

    protected double noise;
    protected double speed;
    // We can use a simple Vector2D or just dx/dy for direction
    protected double dx, dy;

    public Packet(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.noise = 0;
        this.speed = 0;
        this.dx = 0;
        this.dy = 0;
    }
}
