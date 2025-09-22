package dev.aminhashemi.blueprinthell.core.constants;

import java.awt.Color;

/**
 * Centralized constants and configuration for the game.
 * Contains both compile-time constants and runtime configuration values.
 * Follows Single Responsibility Principle by managing all game constants and config.
 */
public final class GameConstants {
    
    // Private constructor to prevent instantiation
    private GameConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    // ==================== WINDOW SETTINGS ====================
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;
    public static final String WINDOW_TITLE = "BlueprintHell";
    
    // ==================== GAME LOOP SETTINGS ====================
    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 60;
    
    // ==================== COLORS ====================
    public static final Color BACKGROUND_COLOR = new Color(20, 20, 20);
    public static final Color WIRE_COLOR = new Color(100, 150, 255);
    public static final Color UI_COLOR = Color.WHITE;
    public static final Color SELECTED_WIRE_COLOR = Color.YELLOW;
    public static final Color PREVIEW_WIRE_COLOR = new Color(255, 255, 0, 128);
    
    // ==================== GAME MECHANICS ====================
    public static final int TOTAL_WIRE_LENGTH = 10000;
    public static final int PACKET_SPAWN_DELAY = 1000;
    public static final double PACKET_SPEED = 100.0;
    
    // ==================== TEST SETTINGS ====================
    public static final int TEST_PACKET_COUNT = 10;
    public static final double MAX_PACKET_LOSS_PERCENTAGE = 50.0;
    public static final int PACKET_RELEASE_INTERVAL = 200; // Note: This is overridden by Config.GameConditions.PACKET_RELEASE_INTERVAL
    public static final int TEST_DURATION = 15000;
    
    // ==================== UI SETTINGS ====================
    public static final boolean SHOW_GRID = true;
    public static final boolean SHOW_DEBUG_INFO = false;
    
    // ==================== WIRE SETTINGS ====================
    public static final double WIRE_REFUND_PERCENTAGE = 0.8;
    public static final int WIRE_SELECTION_RADIUS = 10;
    public static final int WIRE_STROKE_WIDTH = 2;
    public static final int SELECTED_WIRE_STROKE_WIDTH = 3;
    
    // ==================== SYSTEM SETTINGS ====================
    public static final int SYSTEM_SIZE = 40;
    public static final int PORT_SIZE = 8;
    public static final int CONNECTION_RADIUS = 50;
    
    // ==================== LEVEL SETTINGS ====================
    public static final int MAX_LEVELS = 3;
    public static final int INITIAL_COINS = 20;
    public static final int INITIAL_WIRE_LENGTH = 5000;
    
    // ==================== RENDERING SETTINGS ====================
    public static final boolean ENABLE_ANTIALIASING = true;
    public static final boolean ENABLE_SMOOTH_RENDERING = true;
    public static final int RENDER_BUFFER_SIZE = 2;
    
    // ==================== AUDIO SETTINGS ====================
    public static final boolean ENABLE_SOUND = true;
    public static final float MASTER_VOLUME = 0.7f;
    
    // ==================== NETWORK SETTINGS ====================
    public static final int DEFAULT_SERVER_PORT = 8080;
    public static final String DEFAULT_SERVER_HOST = "localhost";
    public static final int CONNECTION_TIMEOUT = 5000;
    
    // ==================== SHOP SETTINGS ====================
    public static final boolean ENABLE_SHOP = true;
    public static final int SHOP_ITEM_COUNT = 6;
    
    // ==================== PACKET SETTINGS ====================
    public static final int PACKET_SIZE = 8;
    public static final int PACKET_COLLISION_RADIUS = 5;
    public static final int PACKET_TIMEOUT = 30000;
    
    // ==================== DEBUG SETTINGS ====================
    public static final boolean ENABLE_DEBUG_LOGGING = true;
    public static final boolean ENABLE_PERFORMANCE_MONITORING = false;
    public static final boolean ENABLE_MEMORY_MONITORING = false;
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Gets a color with alpha transparency
     * @param color Base color
     * @param alpha Alpha value (0-255)
     * @return Color with alpha
     */
    public static Color getColorWithAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    
    /**
     * Clamps a value between min and max
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
    
    /**
     * Clamps a value between min and max
     * @param value Value to clamp
     * @param min Minimum value
     * @param max Maximum value
     * @return Clamped value
     */
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
