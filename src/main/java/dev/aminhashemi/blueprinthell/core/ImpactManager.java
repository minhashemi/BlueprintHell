package dev.aminhashemi.blueprinthell.core;

import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.world.Impact;
import dev.aminhashemi.blueprinthell.utils.AudioManager;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import dev.aminhashemi.blueprinthell.utils.Logger;

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
    private static final float WAVE_INTENSITY = 8.0f; // Reduced from 50.0f to prevent instant destruction
    private static final long IMPACT_COOLDOWN_MS = 1000; // 1 second cooldown between impacts
    
    // Chain reaction constants
    private static final double CHAIN_REACTION_RADIUS = 80.0; // Radius for secondary impacts
    private static final float CHAIN_REACTION_INTENSITY = 20.0f; // Intensity for chain reactions
    private static final int MAX_CHAIN_REACTIONS = 3; // Maximum chain reaction depth

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
        
        // Track new impacts for chain reactions
        List<Point> newImpactPoints = new ArrayList<>();
        
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
                    
                    // Check spawn protection before applying collision effects
                    if (packet.hasSpawnProtection()) {
                        Logger.getInstance().info("Packet " + packet.getPacket().getType() + " has spawn protection - skipping collision effects");
                        continue;
                    }
                    
                    // Use packet-specific collision behavior instead of uniform noise increase
                    packet.handleCollision();
                    
                    // Check if packet should be removed
                    if (packet.isLost()) {
                        packetsToRemove.add(packet);
                        audioManager.playSound("boom.wav");
                    }
                } else if (waveEffectsEnabled) {
                    // Apply wave effects to non-colliding packets (but respect spawn protection)
                    if (!packet.hasSpawnProtection()) {
                        packet.applyImpactEffect(impact.getCollisionPoint(), WAVE_INTENSITY);
                    }
                }
            }

            // Keep impacts active for a few frames instead of immediately removing them
            // This allows them to be displayed and counted properly
            long currentTime = System.currentTimeMillis();
            if (currentTime - impact.getCreationTime() > 2000) { // Keep for 2 seconds
                iterator.remove();
            } else {
                // Track new impacts for chain reactions
                if (currentTime - impact.getCreationTime() < 100) { // Only new impacts
                    newImpactPoints.add(impact.getCollisionPoint());
                }
            }
        }
        
        // Trigger chain reactions for new impacts
        for (Point impactPoint : newImpactPoints) {
            createChainReactions(impactPoint, 1, packetsCopy);
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
        
        // Create chain reactions (we'll call this from processImpacts with the full packet list)
    }
    
    /**
     * Creates chain reactions from an impact point.
     * @param impactPoint The center point of the chain reaction
     * @param depth Current depth of the chain reaction (1 = first level)
     * @param allPackets List of all moving packets for chain reaction detection
     */
    private void createChainReactions(Point impactPoint, int depth, List<MovingPacket> allPackets) {
        if (depth > MAX_CHAIN_REACTIONS) {
            return; // Stop chain reactions at maximum depth
        }
        
        // Find all packets within the chain reaction radius
        List<MovingPacket> affectedPackets = new ArrayList<>();
        
        for (MovingPacket packet : allPackets) {
            if (packet.isLost()) continue; // Skip lost packets
            
            Point packetPos = packet.getPacket().getPosition();
            double distance = packetPos.distance(impactPoint);
            
            if (distance <= CHAIN_REACTION_RADIUS && distance > 0) {
                affectedPackets.add(packet);
            }
        }
        
        // Create secondary impacts for affected packets
        for (int i = 0; i < affectedPackets.size(); i++) {
            for (int j = i + 1; j < affectedPackets.size(); j++) {
                MovingPacket p1 = affectedPackets.get(i);
                MovingPacket p2 = affectedPackets.get(j);
                
                // Check if they're close enough for a chain reaction
                Point pos1 = p1.getPacket().getPosition();
                Point pos2 = p2.getPacket().getPosition();
                double packetDistance = pos1.distance(pos2);
                
                if (packetDistance <= COLLISION_THRESHOLD * 1.5) { // Slightly larger threshold for chain reactions
                    // Create secondary impact
                    Point secondaryPoint = new Point(
                        (pos1.x + pos2.x) / 2,
                        (pos1.y + pos2.y) / 2
                    );
                    
                    // Apply chain reaction effects
                    p1.increaseNoise(CHAIN_REACTION_INTENSITY / depth);
                    p2.increaseNoise(CHAIN_REACTION_INTENSITY / depth);
                    
                    // Create visual impact for chain reaction
                    Impact chainImpact = new Impact(p1, p2, secondaryPoint);
                    activeImpacts.add(chainImpact);
                    
                    // Recursively create more chain reactions
                    createChainReactions(secondaryPoint, depth + 1, allPackets);
                }
            }
        }
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
