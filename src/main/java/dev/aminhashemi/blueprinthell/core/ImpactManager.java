package dev.aminhashemi.blueprinthell.core;

import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.world.Impact;
import dev.aminhashemi.blueprinthell.utils.AudioManager;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages packet collisions and impact effects.
 * Handles collision detection, impact creation, and wave effects.
 */
public class ImpactManager {
    private final List<Impact> activeImpacts;
    private final AudioManager audioManager;
    private boolean impactDetectionEnabled;
    private boolean waveEffectsEnabled;
    
    // Collision detection constants
    private static final double COLLISION_THRESHOLD = 15.0; // Minimum distance for collision
    private static final float NOISE_INCREASE_AMOUNT = 30.0f; // Noise increase on direct collision
    private static final float WAVE_INTENSITY = 50.0f; // Base intensity for wave effects
    private static final long IMPACT_COOLDOWN_MS = 1000; // 1 second cooldown between impacts

    public ImpactManager() {
        this.activeImpacts = new ArrayList<>();
        this.audioManager = AudioManager.getInstance();
        this.impactDetectionEnabled = true;
        this.waveEffectsEnabled = true;
    }

    /**
     * Detects collisions between all moving packets.
     */
    public void detectCollisions(List<MovingPacket> packets) {
        if (!impactDetectionEnabled || packets.size() < 2) {
            return;
        }

        for (int i = 0; i < packets.size(); i++) {
            for (int j = i + 1; j < packets.size(); j++) {
                MovingPacket p1 = packets.get(i);
                MovingPacket p2 = packets.get(j);

                if (p1 == p2 || p1.getWire() == null || p2.getWire() == null) {
                    continue;
                }

                // Check if packets have collided
                if (packetsCollided(p1, p2)) {
                    createImpact(p1, p2);
                }
            }
        }
    }

    /**
     * Processes active impacts and applies effects to packets.
     * Returns a list of packets that should be removed due to high noise.
     */
    public List<MovingPacket> processImpacts(List<MovingPacket> packets) {
        List<MovingPacket> packetsToRemove = new ArrayList<>();
        Iterator<Impact> iterator = activeImpacts.iterator();
        
        // Create a copy of packets to avoid concurrent modification
        List<MovingPacket> packetsCopy = new ArrayList<>(packets);
        
        while (iterator.hasNext()) {
            Impact impact = iterator.next();
            
            if (impact.isDisabled()) {
                iterator.remove();
                continue;
            }

            // Apply direct collision effects
            for (MovingPacket packet : packetsCopy) {
                if (impact.contains(packet, impact.getPacket1()) || 
                    impact.contains(packet, impact.getPacket2())) {
                    packet.increaseNoise(NOISE_INCREASE_AMOUNT);
                    
                    // Check if packet should be removed
                    if (packet.isLost()) {
                        packetsToRemove.add(packet);
                        audioManager.playSound("boom.wav");
                    }
                } else if (waveEffectsEnabled) {
                    // Apply wave effects to non-colliding packets
                    packet.applyImpactEffect(impact.getCollisionPoint(), WAVE_INTENSITY);
                }
            }

            // Keep impacts active for a few frames instead of immediately removing them
            // This allows them to be displayed and counted properly
            long currentTime = System.currentTimeMillis();
            if (currentTime - impact.getCreationTime() > 500) { // Keep for 500ms
                iterator.remove();
            }
        }
        
        return packetsToRemove;
    }

    /**
     * Creates a new impact between two colliding packets.
     */
    private void createImpact(MovingPacket p1, MovingPacket p2) {
        // Check if this impact already exists or if packets are in cooldown
        if (impactExists(p1, p2) || packetsInCooldown(p1, p2)) {
            return;
        }

        Point collisionPoint = calculateCollisionPoint(p1, p2);
        Impact impact = new Impact(p1, p2, collisionPoint);
        activeImpacts.add(impact);

        // Play collision sound
        audioManager.playSound("collide.wav");
    }

    /**
     * Checks if packets have collided using distance-based detection.
     */
    private boolean packetsCollided(MovingPacket p1, MovingPacket p2) {
        Point pos1 = p1.getPacket().getPosition();
        Point pos2 = p2.getPacket().getPosition();
        double distance = pos1.distance(pos2);
        
        return distance < COLLISION_THRESHOLD;
    }

    /**
     * Calculates the collision point between two packets.
     */
    private Point calculateCollisionPoint(MovingPacket p1, MovingPacket p2) {
        Point pos1 = p1.getPacket().getPosition();
        Point pos2 = p2.getPacket().getPosition();
        
        // Return midpoint between the two packets
        return new Point(
            (pos1.x + pos2.x) / 2,
            (pos1.y + pos2.y) / 2
        );
    }

    /**
     * Checks if an impact already exists between the two packets.
     */
    private boolean impactExists(MovingPacket p1, MovingPacket p2) {
        return activeImpacts.stream()
            .anyMatch(impact -> impact.contains(p1, p2));
    }

    /**
     * Checks if packets are in cooldown period to prevent excessive collisions.
     */
    private boolean packetsInCooldown(MovingPacket p1, MovingPacket p2) {
        long currentTime = System.currentTimeMillis();
        
        return activeImpacts.stream()
            .anyMatch(impact -> {
                if (impact.contains(p1, p2)) {
                    return (currentTime - impact.getCreationTime()) < IMPACT_COOLDOWN_MS;
                }
                return false;
            });
    }

    /**
     * Enables or disables impact detection.
     */
    public void setImpactDetectionEnabled(boolean enabled) {
        this.impactDetectionEnabled = enabled;
    }

    /**
     * Enables or disables wave effects.
     */
    public void setWaveEffectsEnabled(boolean enabled) {
        this.waveEffectsEnabled = enabled;
    }

    /**
     * Gets the number of active impacts.
     */
    public int getActiveImpactCount() {
        return activeImpacts.size();
    }
    
    /**
     * Gets the list of active impacts for rendering purposes.
     */
    public List<Impact> getActiveImpacts() {
        return new ArrayList<>(activeImpacts);
    }

    /**
     * Clears all active impacts.
     */
    public void clearImpacts() {
        activeImpacts.clear();
    }
}
