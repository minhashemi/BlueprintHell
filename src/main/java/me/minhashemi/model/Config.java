package me.minhashemi.model;

import javax.swing.KeyStroke;

public class Config {
    // screen
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static boolean isMusicOn = true;
    public static int lastPlayedStage = 1;
    public static final int CALLOUT_DURATION = 3000; // ms

    // wires
    public static final int TOLERANCE = 10; // distance
    public static final int PORT_MARGIN = 5; // Margin from the edge of the packet to the first port
    public static final int PORT_SPACING = 20; // Spacing between ports (vertically)
    public static double remainingWireLength = 1000.0;

    // packet
    public static final int PORT_SIZE = 10;
    public static final int STANDARD_HEIGHT = 30;
    public static final int NETSYS_WIDTH = 60;
    public static final int MAX_BUFFER_SIZE = 5;
    public static final int PACKET_SIZE = 15;
    
    // Key bindings
    public static KeyStroke SPAWN_PACKET_KEY = KeyStroke.getKeyStroke("SPACE");
    public static KeyStroke PAUSE_KEY = KeyStroke.getKeyStroke("P");
    
    // Key binding names for display
    public static final String SPAWN_PACKET_ACTION = "Spawn Packet";
    public static final String PAUSE_ACTION = "Pause Game";
}
