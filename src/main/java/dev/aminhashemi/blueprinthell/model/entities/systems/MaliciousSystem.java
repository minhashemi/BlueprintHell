package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.entities.packets.ProtectedPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.TrojanPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.model.entities.systems.PortType;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * MaliciousSystem - A system that corrupts and interferes with packets
 * 
 * Behavior according to documentation:
 * - Sends packets to incompatible ports (opposite of normal behavior)
 * - Adds noise to packets that have no noise
 * - Converts packets to trojan packets with certain probability
 * - Has no effect on protected packets
 */
public class MaliciousSystem extends System {

    private final Random random = new Random();
    private static final double TROJAN_CONVERSION_PROBABILITY = Config.TROJAN_CONVERSION_PROBABILITY; // 30% chance to convert to trojan
    private static final double NOISE_ADDITION_PROBABILITY = Config.NOISE_ADDITION_PROBABILITY; // 50% chance to add noise

    public MaliciousSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, Config.SYSTEM_WIDTH, Config.SYSTEM_HEIGHT, data);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Config.SystemColors.MALICIOUS_COLOR); // Bright crimson
        g.fillRect(x, y, width, height);
        g.setColor(Config.SYSTEM_BORDER_COLOR); // White border
        g.drawRect(x, y, width, height);
        g.setColor(Config.SYSTEM_TEXT_COLOR); // White text
        g.drawString("MAL", x + 10, y + 20);

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update(GameEngine engine) {
        // Malicious systems don't need update logic
    }

    @Override
    public void receiveMovingPacket(MovingPacket movingPacket, GameEngine engine) {
        Packet packet = movingPacket.getPacket();
        Logger.getInstance().info("Packet " + packet.getType() + " entered MaliciousSystem at (" + x + ", " + y + ")");
        
        // Check if packet is protected - convert back to original type
        if (packet instanceof ProtectedPacket) {
            ProtectedPacket protectedPacket = (ProtectedPacket) packet;
            Logger.getInstance().info("Protected packet converted back to original type " + protectedPacket.getOriginalType() + " by MaliciousSystem");
            
            // Convert back to original packet type
            Packet originalPacket = createOriginalPacket(protectedPacket);
            MovingPacket originalMovingPacket = new MovingPacket(originalPacket, movingPacket.getWire());
            originalMovingPacket.setPlayerSpawned(movingPacket.isPlayerSpawned());
            
            // Apply malicious effects to the original packet
            Packet corruptedPacket = applyMaliciousEffects(originalPacket);
            MovingPacket corruptedMovingPacket = new MovingPacket(corruptedPacket, movingPacket.getWire());
            corruptedMovingPacket.setPlayerSpawned(movingPacket.isPlayerSpawned());
            
            // Route to incompatible port (malicious behavior)
            routeToIncompatiblePort(corruptedMovingPacket, engine);
            return;
        }
        
        // Apply malicious effects
        Packet corruptedPacket = applyMaliciousEffects(packet);
        
        // Create new moving packet with corrupted packet
        MovingPacket corruptedMovingPacket = new MovingPacket(
            corruptedPacket, 
            movingPacket.getWire()
        );
        
        // Route to incompatible port (malicious behavior)
        routeToIncompatiblePort(corruptedMovingPacket, engine);
    }

    /**
     * Creates the original packet from a protected packet
     */
    private Packet createOriginalPacket(ProtectedPacket protectedPacket) {
        PacketType originalType = protectedPacket.getOriginalType();
        
        // Create the original packet based on its type
        switch (originalType) {
            case SQUARE_MESSENGER:
            case TRIANGLE_MESSENGER:
                return new MessengerPacket(protectedPacket.getX(), protectedPacket.getY(), originalType);
            case INFINITY_SYMBOL:
                return new MessengerPacket(protectedPacket.getX(), protectedPacket.getY(), originalType);
            default:
                // Fallback to messenger packet
                return new MessengerPacket(protectedPacket.getX(), protectedPacket.getY(), PacketType.SQUARE_MESSENGER);
        }
    }

    /**
     * Applies malicious effects to a packet
     */
    private Packet applyMaliciousEffects(Packet originalPacket) {
        Packet corruptedPacket = originalPacket;
        
        // Add noise if packet has no noise
        if (originalPacket.getNoise() == 0.0f && random.nextDouble() < NOISE_ADDITION_PROBABILITY) {
            originalPacket.setNoise(Config.NOISE_ADDITION_AMOUNT); // Add some noise
            Logger.getInstance().info("Added noise to packet " + originalPacket.getType());
        }
        
        // Convert to trojan packet with probability
        if (random.nextDouble() < TROJAN_CONVERSION_PROBABILITY) {
            corruptedPacket = TrojanPacket.fromPacket(originalPacket);
            Logger.getInstance().info("Converted packet " + originalPacket.getType() + " to trojan packet");
        }
        
        return corruptedPacket;
    }

    /**
     * Routes packet to an incompatible port (malicious behavior)
     * Unlike normal systems that route to compatible ports, malicious systems
     * intentionally route to incompatible ports to cause problems
     */
    private void routeToIncompatiblePort(MovingPacket movingPacket, GameEngine engine) {
        Packet packet = movingPacket.getPacket();
        List<Port> availableOutputs = getOutputPorts();
        
        if (availableOutputs.isEmpty()) {
            Logger.getInstance().warning("MaliciousSystem has no output ports - packet lost");
            return;
        }
        
        // Find incompatible ports (opposite of normal behavior)
        List<Port> incompatiblePorts = availableOutputs.stream()
            .filter(port -> !isCompatible(packet.getType(), port.getType()))
            .toList();
        
        Port targetPort;
        if (!incompatiblePorts.isEmpty()) {
            // Route to incompatible port (malicious behavior)
            targetPort = incompatiblePorts.get(random.nextInt(incompatiblePorts.size()));
            Logger.getInstance().info("MaliciousSystem routing to incompatible port: " + targetPort.getType());
        } else {
            // Fallback to random port if all ports are compatible
            targetPort = availableOutputs.get(random.nextInt(availableOutputs.size()));
            Logger.getInstance().info("MaliciousSystem routing to random port: " + targetPort.getType());
        }
        
        // Create new packet and route it
        Packet newPacket = createPacketForPort(packet, targetPort.getType());
        engine.routePacket(newPacket, this);
    }

    /**
     * Checks if a packet type is compatible with a port type
     */
    private boolean isCompatible(PacketType packetType, PortType portType) {
        // Basic compatibility logic - can be expanded
        return (packetType == PacketType.SQUARE_MESSENGER && portType == PortType.SQUARE) ||
               (packetType == PacketType.TRIANGLE_MESSENGER && portType == PortType.TRIANGLE) ||
               (packetType == PacketType.GREEN_DIAMOND_SMALL && portType == PortType.SQUARE) ||
               (packetType == PacketType.GREEN_DIAMOND_LARGE && portType == PortType.TRIANGLE);
    }

    /**
     * Creates a new packet for the target port type
     */
    private Packet createPacketForPort(Packet originalPacket, PortType portType) {
        // Create a new packet of the same type but positioned at the port
        MessengerPacket newPacket = new MessengerPacket(
            originalPacket.getX(), 
            originalPacket.getY(), 
            originalPacket.getType()
        );
        newPacket.setSpeed(originalPacket.getSpeed());
        newPacket.setNoise(originalPacket.getNoise());
        
        return newPacket;
    }
}
