package me.minhashemi.view;

import me.minhashemi.model.Packet;
import me.minhashemi.model.PortType;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GameScreen extends JPanel {
    public GameScreen() {
        setLayout(null);
        setBackground(Color.DARK_GRAY);

        Packet packet1 = new Packet(
                List.of(PortType.SQUARE, PortType.TRIANGLE),
                List.of(PortType.SQUARE)
        );

        Packet packet2 = new Packet(
                List.of(PortType.TRIANGLE),
                List.of(PortType.TRIANGLE, PortType.SQUARE, PortType.TRIANGLE)
        );

        addPacket(packet1, 100, 100);
        addPacket(packet2, 250, 200);
    }

    private void addPacket(Packet packet, int x, int y) {
        PacketComponent comp = new PacketComponent(packet);
        comp.setBounds(x, y, 100, packet.getHeight());
        add(comp);
    }
}
