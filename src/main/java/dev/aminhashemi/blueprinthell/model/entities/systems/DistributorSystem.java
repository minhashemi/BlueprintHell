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
import java.util.List;
import java.util.Random;

/**
 * DistributorSystem - A system that splits bulk packets into bit packets
 * 
 * Behavior according to documentation:
 * - Splits bulk packets into bit packets (messenger packets of size 1 with different colors)
 * - Acts like normal systems for regular (non-bulk) packets
 * - Bit packets are distinguished by different colors
 */
public class DistributorSystem extends System {

    private final Random random = new Random();
    private static final Color[] BIT_PACKET_COLORS = {
        Color.decode("#FF0000"), // Red
        Color.decode("#00FF00"), // Green
        Color.decode("#0000FF"), // Blue
        Color.decode("#FFFF00"), // Yellow
        Color.decode("#FF00FF"), // Magenta
        Color.decode("#00FFFF"), // Cyan
        Color.decode("#FFA500"), // Orange
        Color.decode("#800080")  // Purple
    };

    public DistributorSystem(int x, int y, LevelData.SystemData data) {
        super(x, y, Config.SYSTEM_WIDTH, Config.SYSTEM_HEIGHT, data);
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(Config.SystemColors.DISTRIBUTOR_COLOR); // Bright cyan
        g.fillRect(x, y, width, height);
        g.setColor(Config.SYSTEM_BORDER_COLOR); // White border
        g.drawRect(x, y, width, height);
        g.setColor(Config.SYSTEM_TEXT_COLOR); // White text
        g.drawString("DIST", x + 5, y + 15);
        g.drawString("RIB", x + 5, y + 30);

        for (Port port : inputPorts) {
            port.draw(g);
        }
        for (Port port : outputPorts) {
            port.draw(g);
        }
    }

    @Override
    public void update(GameEngine engine) {
        // Distributor systems don't need special update logic
    }

    @Override
    public void receiveMovingPacket(MovingPacket movingPacket, GameEngine engine) {
        Packet packet = movingPacket.getPacket();
        Logger.getInstance().info("Packet " + packet.getType() + " entered DistributorSystem at (" + x + ", " + y + ")");
        
        // Check if packet is a bulk packet
        if (packet instanceof BulkPacket) {
            BulkPacket bulkPacket = (BulkPacket) packet;
            Logger.getInstance().info("Splitting bulk packet of size " + bulkPacket.getSize() + " into bit packets");
            
            // Split bulk packet into bit packets
            List<MovingPacket> bitPackets = splitBulkPacket(bulkPacket, movingPacket, engine);
            
            // Add all bit packets to the game engine
            for (MovingPacket bitPacket : bitPackets) {
                engine.addMovingPacket(bitPacket);
            }
            
            // Remove the original bulk packet
            engine.removeMovingPacket(movingPacket);
            
            Logger.getInstance().info("Bulk packet split into " + bitPackets.size() + " bit packets");
        } else {
            // Regular packets are handled normally
            Logger.getInstance().info("Regular packet - routing normally through DistributorSystem");
            super.receiveMovingPacket(movingPacket, engine);
        }
    }

    /**
     * Splits a bulk packet into bit packets (messenger packets of size 1)
     */
    private List<MovingPacket> splitBulkPacket(BulkPacket bulkPacket, MovingPacket originalMovingPacket, GameEngine engine) {
        List<MovingPacket> bitPackets = new ArrayList<>();
        int bulkSize = bulkPacket.getSize();
        
        // Create bit packets equal to the bulk packet size
        for (int i = 0; i < bulkSize; i++) {
            // Select a random color for this bit packet
            Color bitColor = BIT_PACKET_COLORS[random.nextInt(BIT_PACKET_COLORS.length)];
            
            // Create a messenger packet of size 1
            MessengerPacket bitPacket = new MessengerPacket(
                bulkPacket.getX() + (i * 5), // Slight offset to avoid overlap
                bulkPacket.getY() + (i * 5),
                PacketType.SQUARE_MESSENGER // Use SQUARE_MESSENGER as base type
            );
            
            // Set size to 1 and custom color
            bitPacket.setSize(1);
            bitPacket.setColor(bitColor);
            bitPacket.setCoinReward(1); // Bit packets give 1 coin each
            
            // Copy other properties from bulk packet
            bitPacket.setNoise(bulkPacket.getNoise());
            bitPacket.setCurrentSpeed(bulkPacket.getCurrentSpeed());
            
            // Create moving packet
            MovingPacket bitMovingPacket = new MovingPacket(bitPacket, originalMovingPacket.getWire());
            bitMovingPacket.setPlayerSpawned(originalMovingPacket.isPlayerSpawned());
            
            // Set a unique identifier for this bit packet (for merging later)
            bitPacket.setMetadata("bulk_id", String.valueOf(bulkPacket.hashCode()));
            bitPacket.setMetadata("bit_index", String.valueOf(i));
            bitPacket.setMetadata("total_bits", String.valueOf(bulkSize));
            
            bitPackets.add(bitMovingPacket);
        }
        
        return bitPackets;
    }
}
