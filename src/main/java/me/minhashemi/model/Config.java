package me.minhashemi.model;

public class Config {
    // level
    public static int LEVEL_NUM = 1;
    // screen
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int FPS = 60;
    public static boolean isMusicOn = true;
    public static boolean recordTime = false;
    public static int lastPlayedStage = 1;
    public static final int CALLOUT_DURATION = 3000; // ms

    // wires
    public static final int TOLERANCE = 10; // distance
    public static final int PORT_MARGIN = 5; // Margin from the edge of the packet to the first port
    public static final int PORT_SPACING = 20; // Spacing between ports (vertically)
    public static double remainingWireLength = 1000.0;
    public static double lastTotalWireLength = 0.0;

    // packet
    public static final int PORT_SIZE = 10;
    public static final int STANDARD_HEIGHT = 30;
    public static final int NETSYS_WIDTH = 60;
    public static final int MAX_BUFFER_SIZE = 5;
}
