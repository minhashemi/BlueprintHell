package dev.aminhashemi.blueprinthell.model.entities.systems;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.model.LevelData;
import dev.aminhashemi.blueprinthell.model.MovingPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.Packet;
import dev.aminhashemi.blueprinthell.model.entities.packets.PacketType;
import dev.aminhashemi.blueprinthell.model.entities.packets.BulkPacket;
import dev.aminhashemi.blueprinthell.model.entities.packets.MessengerPacket;
import dev.aminhashemi.blueprinthell.model.entities.systems.Port;
import dev.aminhashemi.blueprinthell.model.world.Wire;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.Config;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MergerSystem - A system that merges bit packets back into bulk packets
 * 
 * Behavior according to documentation:
 * - Merges bit packets back into bulk packets
 * - Acts like normal systems for regular (non-bit) packets
 * - Only merges bit packets that belong to the same original bulk packet
 */
public class MergerSystem extends System {

    // Map to track bit packets by their bulk packet ID
    private Map<String, List<MovingPacket>> bitPacketGroups = new HashMap<>();

    public MergerSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, Config.SYSTEM_WIDTH, Config.SYSTEM_HEIGHT, data);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Config.SystemColors.MERGER_COLOR); // Bright orange
        g.fillRect(x, y, width, height);
        g.setColor(Config.SYSTEM_BORDER_COLOR); // White border
        g.drawRect(x, y, width, height);
        g.setColor(Config.SYSTEM_TEXT_COLOR); // White text
        g.drawString("MER", x + 5, y + 15);
        g.drawString("GER", x + 5, y + 30);

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update(GameEngine engine) {
        // Check for complete bit packet groups and merge them
        List<String> groupsToMerge = new ArrayList<>();
        
        for (Map.Entry<String, List<MovingPacket>> entry : bitPacketGroups.entrySet()) {
            String bulkId = entry.getKey();
            List<MovingPacket> bitPackets = entry.getValue();
            
            // Check if all bit packets for this bulk packet are present
            if (isCompleteBitPacketGroup(bitPackets)) {
                groupsToMerge.add(bulkId);
            }
        }
        
        // Merge complete groups
        for (String bulkId : groupsToMerge) {
            List<MovingPacket> bitPackets = bitPacketGroups.get(bulkId);
            mergeBitPackets(bitPackets, engine);
            bitPacketGroups.remove(bulkId);
        }
    }

    @Override
    public void receiveMovingPacket(MovingPacket movingPacket, GameEngine engine) {
        Packet packet = movingPacket.getPacket();
        Logger.getInstance().info("Packet " + packet.getType() + " entered MergerSystem at (" + x + ", " + y + ")");
        
        // Check if packet is a bit packet (messenger packet of size 1 with metadata)
        if (isBitPacket(packet)) {
            MessengerPacket bitPacket = (MessengerPacket) packet;
            String bulkId = bitPacket.getMetadata("bulk_id");
            
            if (bulkId != null) {
                Logger.getInstance().info("Bit packet received for bulk ID: " + bulkId);
                
                // Add to bit packet group
                bitPacketGroups.computeIfAbsent(bulkId, k -> new ArrayList<>()).add(movingPacket);
                
                // Remove from game engine (will be merged later)
                engine.removeMovingPacket(movingPacket);
                
                Logger.getInstance().info("Bit packet added to group. Group size: " + bitPacketGroups.get(bulkId).size());
            } else {
                // Not a valid bit packet, route normally
                super.receiveMovingPacket(movingPacket, engine);
            }
        } else {
            // Regular packets are handled normally
            Logger.getInstance().info("Regular packet - routing normally through MergerSystem");
            super.receiveMovingPacket(movingPacket, engine);
        }
    }

    /**
     * Checks if a packet is a bit packet (messenger packet of size 1 with metadata)
     */
    private boolean isBitPacket(Packet packet) {
        if (packet instanceof MessengerPacket) {
            MessengerPacket messengerPacket = (MessengerPacket) packet;
            return messengerPacket.getSize() == 1 && 
                   messengerPacket.getMetadata("bulk_id") != null &&
                   messengerPacket.getMetadata("bit_index") != null;
        }
        return false;
    }

    /**
     * Checks if a bit packet group is complete
     */
    private boolean isCompleteBitPacketGroup(List<MovingPacket> bitPackets) {
        if (bitPackets.isEmpty()) return false;
        
        // Get expected total bits from first packet
        Packet firstPacket = bitPackets.get(0).getPacket();
        if (firstPacket instanceof MessengerPacket) {
            MessengerPacket firstBitPacket = (MessengerPacket) firstPacket;
            String totalBitsStr = firstBitPacket.getMetadata("total_bits");
            if (totalBitsStr != null) {
                int expectedTotal = Integer.parseInt(totalBitsStr);
                return bitPackets.size() >= expectedTotal;
            }
        }
        return false;
    }

    /**
     * Merges a complete group of bit packets back into a bulk packet
     */
    private void mergeBitPackets(List<MovingPacket> bitPackets, GameEngine engine) {
        if (bitPackets.isEmpty()) return;
        
        // Get properties from first bit packet
        MovingPacket firstBitPacket = bitPackets.get(0);
        Packet firstPacket = firstBitPacket.getPacket();
        
        if (firstPacket instanceof MessengerPacket) {
            MessengerPacket firstBitPacketData = (MessengerPacket) firstPacket;
            String totalBitsStr = firstBitPacketData.getMetadata("total_bits");
            
            if (totalBitsStr != null) {
                int totalBits = Integer.parseInt(totalBitsStr);
                
                // Create new bulk packet
                BulkPacket bulkPacket = new BulkPacket(
                    firstPacket.getX(),
                    firstPacket.getY(),
                    BulkPacket.BulkType.SMALL // Default to SMALL, could be determined from metadata
                );
                
                // Set properties based on bit packets
                bulkPacket.setNoise(firstPacket.getNoise());
                bulkPacket.setCurrentSpeed(firstPacket.getCurrentSpeed());
                
                // Create moving packet
                MovingPacket bulkMovingPacket = new MovingPacket(bulkPacket, firstBitPacket.getWire());
                bulkMovingPacket.setPlayerSpawned(firstBitPacket.isPlayerSpawned());
                
                // Add to game engine
                engine.addMovingPacket(bulkMovingPacket);
                
                Logger.getInstance().info("Merged " + bitPackets.size() + " bit packets into bulk packet of size " + totalBits);
            }
        }
    }
}
