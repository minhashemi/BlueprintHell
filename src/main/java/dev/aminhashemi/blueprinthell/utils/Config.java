package dev.aminhashemi.blueprinthell.utils;

import java.awt.Color;
import java.awt.event.KeyEvent;

/**
 * Centralized configuration class for all game constants and parameters.
 * This allows easy modification of game behavior during presentations and development.
 */
public class Config {
    
    // ==================== GAME ENGINE SETTINGS ====================
    
    /** Target FPS for the game loop */
    public static final int TARGET_FPS = 120;
    
    /** Target UPS (Updates Per Second) for the game loop */
    public static final int TARGET_UPS = 200;
    
    /** Game window dimensions */
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    
    /** Game background color */
    public static final Color BACKGROUND_COLOR = new Color(20, 25, 40);
    
    // ==================== WIRE SYSTEM CONFIGURATION ====================
    
    /** Total available wire length in meters */
    public static final int TOTAL_WIRE_LENGTH = 8000;
    
    /** Cost to add an arc point to a wire (in coins) */
    public static final int ARC_POINT_COST = 1;
    
    /** Cost to move a system (in coins) */
    public static final int SYSTEM_MOVE_COST = 1;
    
    /** Maximum number of arc points allowed per wire */
    public static final int MAX_ARC_POINTS_PER_WIRE = 3;
    
    /** Maximum number of bulk packet passes before wire destruction */
    public static final int MAX_BULK_PACKET_PASSES = 3;
    
    /** Click threshold for wire detection in pixels */
    public static final double WIRE_CLICK_THRESHOLD = 8.0;
    
    // ==================== PACKET SYSTEM CONFIGURATION ====================
    
    /** Spawn cooldown to prevent multiple rapid spawns (in milliseconds) */
    public static final long PACKET_SPAWN_COOLDOWN = 500;
    
    /** Spawn protection duration (in milliseconds) */
    public static final long SPAWN_PROTECTION_DURATION = 2000;
    
    /** Noise threshold for packet destruction */
    public static final float PACKET_DESTRUCTION_NOISE_THRESHOLD = 50.0f;
    
    /** Packet loss threshold for game over (percentage) */
    public static final int GAME_OVER_PACKET_LOSS_THRESHOLD = 50;
    
    // ==================== COLLISION SYSTEM CONFIGURATION ====================
    
    /** Minimum distance for packet collision detection */
    public static final double COLLISION_THRESHOLD = 15.0;
    
    /** Noise increase amount on direct collision */
    public static final float COLLISION_NOISE_INCREASE = 30.0f;
    
    /** Wave intensity for impact effects */
    public static final float WAVE_INTENSITY = 8.0f;
    
    /** Cooldown between impacts (in milliseconds) */
    public static final long IMPACT_COOLDOWN_MS = 1000;
    
    /** Chain reaction radius for secondary impacts */
    public static final double CHAIN_REACTION_RADIUS = 80.0;
    
    /** Chain reaction intensity */
    public static final float CHAIN_REACTION_INTENSITY = 20.0f;
    
    /** Maximum chain reaction depth */
    public static final int MAX_CHAIN_REACTIONS = 3;
    
    /** Impact display duration (in milliseconds) */
    public static final long IMPACT_DISPLAY_DURATION = 2000;
    
    /** New impact detection window (in milliseconds) */
    public static final long NEW_IMPACT_WINDOW = 100;
    
    // ==================== TIME TRAVEL SYSTEM CONFIGURATION ====================
    
    /** Snapshot interval in milliseconds (60 FPS = 16ms) */
    public static final int SNAPSHOT_INTERVAL = 16;
    
    /** Time travel window in seconds */
    public static final int TIME_TRAVEL_WINDOW_SECONDS = 5;
    
    /** Maximum number of snapshots to keep in memory */
    public static final int MAX_SNAPSHOTS = 300; // 60 FPS * 5 seconds
    
    /** Time travel input delay (in milliseconds) */
    public static final int TIME_TRAVEL_INPUT_DELAY = 100;
    
    /** Snapshots directory name */
    public static final String SNAPSHOTS_DIRECTORY = "snapshots";
    
    // ==================== HUD CONFIGURATION ====================
    
    /** HUD display duration when toggled (in milliseconds) */
    public static final long HUD_DISPLAY_DURATION = 3000;
    
    /** HUD panel dimensions */
    public static final int HUD_WIDTH = 280;
    public static final int HUD_HEIGHT = 160;
    public static final int HUD_MARGIN = 20;
    
    /** HUD background transparency */
    public static final int HUD_BACKGROUND_ALPHA = 180;
    public static final int HUD_BORDER_ALPHA = 100;
    
    /** HUD text colors */
    public static final Color HUD_WIRE_LENGTH_SAFE_COLOR = Color.CYAN;
    public static final Color HUD_WIRE_LENGTH_WARNING_COLOR = Color.ORANGE;
    public static final Color HUD_WIRE_LENGTH_DANGER_COLOR = Color.RED;
    public static final Color HUD_PACKET_LOSS_COLOR = Color.RED;
    public static final Color HUD_COINS_COLOR = Color.decode("#FFD700");
    public static final Color HUD_ACTIVE_SYSTEMS_COLOR = Color.GREEN;
    public static final Color HUD_WIRE_CONNECTIONS_COLOR = Color.BLUE;
    
    /** Wire length warning thresholds */
    public static final int WIRE_LENGTH_WARNING_THRESHOLD = 2000;
    public static final int WIRE_LENGTH_DANGER_THRESHOLD = 500;
    
    // ==================== PORT CONFIGURATION ====================
    
    /** Port size in pixels */
    public static final int PORT_SIZE = 10;
    
    // ==================== PACKET TYPE CONFIGURATION ====================
    
    /** Phase 1 Packet Types */
    public static class Phase1Packets {
        public static final Color SQUARE_MESSENGER_COLOR = Color.decode("#FF4757");
        public static final Color TRIANGLE_MESSENGER_COLOR = Color.decode("#2ED573");
        public static final int PROTECTED_PACKET_COINS = 5; // Protected packets give 5 coins
        public static final int PROTECTED_PACKET_SIZE = 2; // Size value for protected packets
    }
    
    /** Phase 2 Packet Types */
    public static class Phase2Packets {
        // Green Diamond Packets
        public static final Color GREEN_DIAMOND_SMALL_COLOR = Color.decode("#00FFFF");
        public static final int GREEN_DIAMOND_SMALL_SIZE = 2;
        public static final int GREEN_DIAMOND_SMALL_COINS = 2;
        
        public static final Color GREEN_DIAMOND_LARGE_COLOR = Color.decode("#00FF00");
        public static final int GREEN_DIAMOND_LARGE_SIZE = 3;
        public static final int GREEN_DIAMOND_LARGE_COINS = 3;
        
        // Infinity Symbol Packet
        public static final Color INFINITY_SYMBOL_COLOR = Color.decode("#FFFF00");
        public static final int INFINITY_SYMBOL_SIZE = 1;
        public static final int INFINITY_SYMBOL_COINS = 1;
        
        // Padlock Icon Packet (Protected)
        public static final Color PADLOCK_ICON_COLOR = Color.decode("#FF69B4");
        public static final int PADLOCK_ICON_SIZE = 2;
        public static final int PADLOCK_ICON_COINS = 5;
        
        // Camouflage Packets (Confidential)
        public static final Color CAMOUFLAGE_SMALL_COLOR = Color.decode("#FFD700");
        public static final int CAMOUFLAGE_SMALL_SIZE = 4;
        public static final int CAMOUFLAGE_SMALL_COINS = 3;
        
        public static final Color CAMOUFLAGE_LARGE_COLOR = Color.decode("#FF00FF");
        public static final int CAMOUFLAGE_LARGE_SIZE = 6;
        public static final int CAMOUFLAGE_LARGE_COINS = 4;
        
        // Bulk Packets
        public static final Color BULK_SMALL_COLOR = Color.decode("#8B4513");
        public static final int BULK_SMALL_SIZE = 8;
        public static final int BULK_SMALL_COINS = 8;
        
        public static final Color BULK_LARGE_COLOR = Color.decode("#654321");
        public static final int BULK_LARGE_SIZE = 10;
        public static final int BULK_LARGE_COINS = 10;
    }
    
    // ==================== PORT TYPE CONFIGURATION ====================
    
    /** Port Type Colors */
    public static class PortColors {
        // Phase 1 Port Types
        public static final Color SQUARE_COLOR = Color.decode("#FF4757");
        public static final Color TRIANGLE_COLOR = Color.decode("#2ED573");
        
        // Phase 2 Port Types
        public static final Color DIAMOND_COLOR = Color.decode("#00FFFF");
        public static final Color INFINITY_COLOR = Color.decode("#FFFF00");
        public static final Color PADLOCK_COLOR = Color.decode("#FF69B4");
        public static final Color CAMOUFLAGE_COLOR = Color.decode("#FFD700");
        public static final Color VPN_COLOR = Color.decode("#FF00FF");
        public static final Color MALICIOUS_COLOR = Color.decode("#FF3838");
        public static final Color SPY_COLOR = Color.decode("#FF9F43");
    }
    
    // ==================== CONFIDENTIAL PACKET CONFIGURATION ====================
    
    /** Confidential packet speed check interval (in milliseconds) */
    public static final long CONFIDENTIAL_SPEED_CHECK_INTERVAL = 500;
    
    /** Confidential packet speed reduction factors */
    public static final float CONFIDENTIAL_SLOWDOWN_FACTOR = 0.3f;
    public static final float CONFIDENTIAL_SPEED_RECOVERY_FACTOR = 0.1f;
    public static final float CONFIDENTIAL_SPY_SLOWDOWN_FACTOR = 0.2f;
    public static final float CONFIDENTIAL_SPY_SPEED_REDUCTION = 0.3f;
    
    /** Confidential packet distance maintenance */
    public static final double CONFIDENTIAL_MIN_DISTANCE = 50.0;
    public static final float CONFIDENTIAL_DISTANCE_ADJUSTMENT = 0.1f;
    public static final float CONFIDENTIAL_MAX_SPEED_MULTIPLIER = 1.2f;
    public static final float CONFIDENTIAL_MIN_SPEED_MULTIPLIER = 0.5f;
    
    // ==================== BULK PACKET CONFIGURATION ====================
    
    /** Bulk packet movement parameters */
    public static final float BULK_CURVE_ACCELERATION = 0.05f;
    public static final float BULK_STRAIGHT_DECELERATION = 0.02f;
    public static final float BULK_MAX_SPEED = 2.0f;
    public static final float BULK_MIN_SPEED = 1.0f;
    
    /** Bulk packet deviation parameters */
    public static final double BULK_DEVIATION_CHANCE = 0.2;
    public static final float BULK_DEVIATION_AMOUNT = 0.5f;
    public static final float BULK_DEVIATION_BOUNDS = 1.0f;
    
    /** Bulk packet random movement */
    public static final double BULK_RANDOM_MOVEMENT_CHANCE = 0.1;
    public static final float BULK_RANDOM_MOVEMENT_AMOUNT = 0.3f;
    
    // ==================== SHOP CONFIGURATION ====================
    
    /** Shop item prices */
    public static class ShopPrices {
        public static final int O_ATAR_PRICE = 3;
        public static final int O_AIRYAMAN_PRICE = 4;
        public static final int O_ANAHITA_PRICE = 5;
    }
    
    /** Shop item durations (in seconds) */
    public static class ShopDurations {
        public static final int O_ATAR_DURATION = 10;
        public static final int O_AIRYAMAN_DURATION = 5;
    }
    
    // ==================== LEVEL CONFIGURATION ====================
    
    /** Default level settings */
    public static class LevelDefaults {
        public static final int INITIAL_COINS = 20;
        public static final int INITIAL_WIRE_LENGTH = 5000;
        public static final int PACKET_GENERATION_COUNT = 15;
        public static final double PACKET_GENERATION_FREQUENCY = 2.0;
    }
    
    // ==================== SYSTEM CONFIGURATION ====================
    
    /** System storage capacity */
    public static final int SYSTEM_STORAGE_CAPACITY = 5;
    
    /** System dimensions */
    public static final int SYSTEM_WIDTH = 100;
    public static final int SYSTEM_HEIGHT = 60;
    
    // ==================== AUDIO CONFIGURATION ====================
    
    /** Audio file names */
    public static class AudioFiles {
        public static final String THEME_MUSIC = "theme.wav";
        public static final String COLLISION_SOUND = "collide.wav";
        public static final String CONNECTION_SOUND = "connect.wav";
        public static final String PACKET_LOSS_SOUND = "boom.wav";
        public static final String VICTORY_SOUND = "victory.wav";
        public static final String LOSE_SOUND = "lose.wav";
    }
    
    // ==================== RENDERING CONFIGURATION ====================
    
    /** Font settings */
    public static class Fonts {
        public static final String DEFAULT_FONT_FAMILY = "Arial";
        public static final int HUD_TITLE_SIZE = 16;
        public static final int HUD_TEXT_SIZE = 14;
        public static final int HUD_SMALL_TEXT_SIZE = 12;
        public static final int HUD_TINY_TEXT_SIZE = 10;
        public static final int PACKET_INFO_SIZE = 8;
    }
    
    /** Rendering hints */
    public static final boolean ENABLE_ANTIALIASING = true;
    
    // ==================== DEBUG CONFIGURATION ====================
    
    /** Debug settings */
    public static final boolean ENABLE_DEBUG_LOGGING = true;
    public static final boolean ENABLE_PACKET_TRACKING = true;
    public static final boolean ENABLE_WIRE_LENGTH_LOGGING = true;
    
    /** Debug colors */
    public static final Color DEBUG_IMPACT_COLOR = Color.RED;
    public static final Color DEBUG_PACKET_LOST_COLOR = Color.RED;
    public static final Color DEBUG_PACKET_NOISE_COLOR = Color.ORANGE;
    public static final Color DEBUG_WIRE_VALID_COLOR = Color.GREEN;
    public static final Color DEBUG_WIRE_INVALID_COLOR = Color.RED;
    
    // ==================== GAME BALANCE CONFIGURATION ====================
    
    /** Game balance parameters */
    public static class GameBalance {
        /** Probability of system congestion for confidential packets */
        public static final double CONFIDENTIAL_CONGESTION_CHANCE = 0.4;
        
        /** Probability of being near spy system for confidential packets */
        public static final double CONFIDENTIAL_SPY_PROXIMITY_CHANCE = 0.2;
        
        /** Probability of being too close to other packets for large confidential packets */
        public static final double CONFIDENTIAL_DISTANCE_VIOLATION_CHANCE = 0.3;
        
        /** Random movement chance for bulk packets */
        public static final double BULK_RANDOM_MOVEMENT_CHANCE = 0.1;
    }
    
    // ==================== SAVE SYSTEM CONFIGURATION ====================
    
    /** Save file settings */
    public static final String SAVE_FILE_NAME = "save_game.json";
    public static final String SNAPSHOT_FILE_PREFIX = "snapshot_";
    public static final String SNAPSHOT_FILE_SUFFIX = ".json";
    
    // ==================== INPUT CONFIGURATION ====================
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Gets the appropriate color for wire length based on remaining amount
     */
    public static Color getWireLengthColor(int remainingLength) {
        if (remainingLength > WIRE_LENGTH_WARNING_THRESHOLD) {
            return HUD_WIRE_LENGTH_SAFE_COLOR;
        } else if (remainingLength > WIRE_LENGTH_DANGER_THRESHOLD) {
            return HUD_WIRE_LENGTH_WARNING_COLOR;
        } else {
            return HUD_WIRE_LENGTH_DANGER_COLOR;
        }
    }
    
    /**
     * Checks if packet loss exceeds game over threshold
     */
    public static boolean isGameOver(int packetLoss, int totalPackets) {
        if (totalPackets == 0) return false;
        double lossPercentage = (double) packetLoss / totalPackets * 100;
        return lossPercentage >= GAME_OVER_PACKET_LOSS_THRESHOLD;
    }
    
    /**
     * Calculates the maximum number of snapshots based on FPS and time window
     */
    public static int calculateMaxSnapshots(int fps, int timeWindowSeconds) {
        return fps * timeWindowSeconds;
    }
    
    // ==================== ADDITIONAL HARDCODED VALUES ====================
    
    // Game Engine Constants
    public static final double CLICK_THRESHOLD = 8.0; // Increased threshold for easier wire detection
    public static final String WIRING_MODE_TEXT = "WIRING MODE ACTIVE";
    public static final String IMPACT_TEXT = "IMPACT";
    public static final String DESTROYED_TEXT = "DESTROYED";
    public static final String TIME_TRAVEL_MODE_TEXT = "TIME TRAVEL MODE";
    public static final String FONT_NAME = "Arial";
    public static final int WIRING_MODE_FONT_SIZE = 14;
    public static final int IMPACT_FONT_SIZE = 12;
    public static final int DESTROYED_FONT_SIZE = 10;
    public static final int TIME_TRAVEL_FONT_SIZE = 16;
    public static final int SMALL_FONT_SIZE = 8;
    public static final int MEDIUM_FONT_SIZE = 10;
    public static final int LARGE_FONT_SIZE = 12;
    
    // System Constants
    public static final int PACKET_GENERATION_RATE = 1000; // milliseconds
    public static final double TROJAN_CONVERSION_PROBABILITY = 0.3; // 30% chance to convert to trojan
    public static final double NOISE_ADDITION_PROBABILITY = 0.5; // 50% chance to add noise
    public static final float NOISE_ADDITION_AMOUNT = 0.2f; // Amount of noise to add
    
    // Packet Constants
    public static final long VISIBILITY_TOGGLE_INTERVAL = 2000; // 2 seconds
    public static final int PROTECTED_PACKET_SIZE = 24; // Twice the size of normal packets
    public static final int NORMAL_PACKET_SIZE = 12; // Base packet size
    
    
    // HUD Constants
    public static final int HUD_LINE_HEIGHT = 20;
    public static final int HUD_TEXT_SPACING = 15;
    
    // Color Constants for hardcoded colors
    public static final Color WIRING_MODE_COLOR = Color.YELLOW;
    public static final Color IMPACT_COLOR = Color.RED;
    public static final Color DESTROYED_COLOR = Color.RED;
    public static final Color WIRE_VALID_COLOR = Color.GREEN;
    public static final Color WIRE_INVALID_COLOR = Color.RED;
    public static final Color TIME_TRAVEL_COLOR = Color.CYAN;
    public static final Color SNAPSHOT_COLOR = Color.MAGENTA;
    public static final Color WIRE_LENGTH_COLOR = Color.YELLOW;
    public static final Color SYSTEM_COUNT_COLOR = Color.WHITE;
    public static final Color WIRE_COUNT_COLOR = Color.RED;
    public static final Color PACKET_COUNT_COLOR = Color.ORANGE;
    public static final Color SYSTEM_BORDER_COLOR = Color.WHITE;
    public static final Color SYSTEM_TEXT_COLOR = Color.WHITE;
    public static final Color REFERENCE_TEXT_COLOR = Color.BLACK;
    public static final Color INACTIVE_SYSTEM_COLOR = Color.decode("#95A5A6"); // Gray when inactive
    
    // Port Colors for systems
    public static class SystemColors {
        public static final Color MALICIOUS_COLOR = Color.decode("#FF3838"); // Bright crimson
        public static final Color VPN_COLOR = Color.decode("#A55EEA"); // Bright purple
        public static final Color SPY_COLOR = Color.decode("#FF9F43"); // Bright orange-red
        public static final Color REFERENCE_COLOR = Color.decode("#00D2FF"); // Bright cyan
    }
    
    // Input Constants
    public static class Input {
        public static final int WIRING_MODE_KEY = KeyEvent.VK_W;
        public static final int PAUSE_KEY = KeyEvent.VK_P;
        public static final int TIME_TRAVEL_KEY = KeyEvent.VK_T;
        public static final int SAVE_KEY = KeyEvent.VK_S;
        public static final int LOAD_KEY = KeyEvent.VK_L;
        public static final int HUD_TOGGLE_KEY = KeyEvent.VK_H;
        public static final int WIRE_CLEAN_KEY = KeyEvent.VK_C; // Hold C and click to clean wires
        public static final int SHOP_KEY = KeyEvent.VK_B; // B key to open/close shop
        public static final int SPAWN_PACKET_KEY = KeyEvent.VK_SPACE; // Space key to spawn packets
        public static final int TIME_LEFT_KEY = KeyEvent.VK_LEFT; // Left arrow for time travel
        public static final int TIME_RIGHT_KEY = KeyEvent.VK_RIGHT; // Right arrow for time travel
        public static final int RUN_TEST_KEY = KeyEvent.VK_G; // G key to run network test
        public static final int RESET_TEST_KEY = KeyEvent.VK_R; // R key to reset test state
    }
    
    // Shop Constants
    public static class Shop {
        // Shop UI Colors
        public static final Color SHOP_BACKGROUND_COLOR = new Color(0, 0, 0, 200); // Semi-transparent black
        public static final Color SHOP_PANEL_COLOR = new Color(50, 50, 50); // Dark gray
        public static final Color SHOP_BORDER_COLOR = Color.WHITE;
        public static final Color SHOP_TEXT_COLOR = Color.WHITE;
        public static final Color SHOP_TITLE_COLOR = Color.YELLOW;
        public static final Color SHOP_ITEM_COLOR = Color.CYAN;
        public static final Color SHOP_PRICE_COLOR = Color.GREEN;
        public static final Color SHOP_BUTTON_COLOR = new Color(70, 130, 180); // Steel blue
        public static final Color SHOP_BUTTON_HOVER_COLOR = new Color(100, 149, 237); // Cornflower blue
        
        // Shop UI Dimensions
        public static final int SHOP_PANEL_WIDTH = 600;
        public static final int SHOP_PANEL_HEIGHT = 400;
        public static final int SHOP_ITEM_HEIGHT = 40;
        public static final int SHOP_BUTTON_WIDTH = 100;
        public static final int SHOP_BUTTON_HEIGHT = 30;
        public static final int SHOP_MARGIN = 20;
        
        // Phase 1 Shop Items (Temporary Effects)
        public static final int O_ATAR_COST = 3; // 3 coins - disables impact waves for 10 seconds
        public static final int O_ATAR_DURATION = 10; // 10 seconds
        
        public static final int O_AIRYAMAN_COST = 4; // 4 coins - disables collisions for 5 seconds
        public static final int O_AIRYAMAN_DURATION = 5; // 5 seconds
        
        public static final int O_ANAHITA_COST = 5; // 5 coins - zeros all packet noise
        
        // Phase 2 Shop Items (Temporary Effects)
        public static final int SCROLL_AERGIA_COST = 10; // 10 coins - zero acceleration for 20 seconds
        public static final int SCROLL_AERGIA_DURATION = 20; // 20 seconds
        
        public static final int SCROLL_SISYPHUS_COST = 15; // 15 coins - move system
        
        public static final int SCROLL_ELIPHAS_COST = 20; // 20 coins - restore gravity for 30 seconds
        public static final int SCROLL_ELIPHAS_DURATION = 30; // 30 seconds
    }
    
    // Game Win/Lose Conditions
    public static class GameConditions {
        public static final int TEST_PACKET_COUNT = 10; // Number of packets to release for testing
        public static final double MAX_PACKET_LOSS_PERCENTAGE = 50.0; // Maximum allowed packet loss percentage to win
        public static final long PACKET_RELEASE_INTERVAL = 1000; // 1 second between packet releases
        public static final long TEST_DURATION = 30000; // 30 seconds total test duration
    }
}
