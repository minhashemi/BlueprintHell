package dev.aminhashemi.blueprinthell.model.entities.packets;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.entities.GameEntity;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.util.HashMap;
import java.util.Map;

public abstract class Packet extends GameEntity {

    protected double noise;
    protected double speed;
    protected double dx, dy;
    protected long creationTime;
    protected boolean isTimedOut;
    protected Map<String, String> metadata; // For storing additional packet information

    public Packet(int x, int y, int width, int height) {
        super(x, y, width, height);
        this.noise = 0;
        this.speed = 0;
        this.dx = 0;
        this.dy = 0;
        this.creationTime = System.currentTimeMillis();
        this.isTimedOut = false;
        this.metadata = new HashMap<>();
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
    
    public long getCreationTime() { return creationTime; }
    public void setCreationTime(long creationTime) { this.creationTime = creationTime; }
    public boolean isTimedOut() { return isTimedOut; }
    public void setTimedOut(boolean timedOut) { this.isTimedOut = timedOut; }
    
    // Metadata methods
    public void setMetadata(String key, String value) { 
        metadata.put(key, value); 
    }
    public String getMetadata(String key) { 
        return metadata.get(key); 
    }
    public boolean hasMetadata(String key) { 
        return metadata.containsKey(key); 
    }
    
    public float getCurrentSpeed() {
        return (float) this.speed;
    }
    
    /**
     * Checks if this packet has timed out based on its creation time
     * @return true if packet has exceeded the timeout duration
     */
    public boolean checkTimeout() {
        if (isTimedOut) return true;
        
        long currentTime = System.currentTimeMillis();
        long timeAlive = currentTime - creationTime;
        
        if (timeAlive > Config.PACKET_TIMEOUT_DURATION) {
            isTimedOut = true;
            return true;
        }
        
        return false;
    }
    
    /**
     * Resets the packet timeout (useful when packet enters a system)
     */
    public void resetTimeout() {
        this.creationTime = System.currentTimeMillis();
        this.isTimedOut = false;
    }
}
