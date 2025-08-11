package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.model.entities.Entity;

/**
 * An abstract base class for all network system types in the game.
 * It extends Entity and will hold system-specific data like ports.
 */
public abstract class System extends Entity {

    public System(int x, int y, int width, int height) {
        super(x, y, width, height);
        // In the future, we will initialize input/output port lists here
    }
}
