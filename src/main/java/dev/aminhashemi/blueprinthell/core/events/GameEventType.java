package dev.aminhashemi.blueprinthell.core.events;

/**
 * Enumeration of game event types.
 * Follows the Observer pattern for loose coupling.
 */
public enum GameEventType {
    
    // ==================== PACKET EVENTS ====================
    PACKET_SPAWNED,
    PACKET_ARRIVED,
    PACKET_LOST,
    PACKET_COLLIDED,
    PACKET_DESTROYED,
    
    // ==================== WIRE EVENTS ====================
    WIRE_CREATED,
    WIRE_REMOVED,
    WIRE_SELECTED,
    WIRE_DESELECTED,
    WIRE_DAMAGED,
    WIRE_DESTROYED,
    
    // ==================== SYSTEM EVENTS ====================
    SYSTEM_DRAGGED,
    SYSTEM_DROPPED,
    SYSTEM_SELECTED,
    SYSTEM_DESELECTED,
    
    // ==================== LEVEL EVENTS ====================
    LEVEL_STARTED,
    LEVEL_COMPLETED,
    LEVEL_FAILED,
    LEVEL_RESET,
    LEVEL_LOADED,
    
    // ==================== GAME STATE EVENTS ====================
    GAME_STARTED,
    GAME_PAUSED,
    GAME_RESUMED,
    GAME_ENDED,
    GAME_WON,
    GAME_LOST,
    
    // ==================== TEST EVENTS ====================
    TEST_STARTED,
    TEST_COMPLETED,
    TEST_FAILED,
    TEST_RESET,
    
    // ==================== SHOP EVENTS ====================
    SHOP_OPENED,
    SHOP_CLOSED,
    ITEM_PURCHASED,
    ITEM_SOLD,
    
    // ==================== COIN EVENTS ====================
    COINS_ADDED,
    COINS_REMOVED,
    COINS_CHANGED,
    
    // ==================== WIRE LENGTH EVENTS ====================
    WIRE_LENGTH_ADDED,
    WIRE_LENGTH_REMOVED,
    WIRE_LENGTH_CHANGED,
    
    // ==================== UI EVENTS ====================
    UI_ELEMENT_CLICKED,
    UI_ELEMENT_HOVERED,
    UI_ELEMENT_FOCUSED,
    UI_ELEMENT_BLURRED,
    
    // ==================== INPUT EVENTS ====================
    KEY_PRESSED,
    KEY_RELEASED,
    MOUSE_PRESSED,
    MOUSE_RELEASED,
    MOUSE_DRAGGED,
    MOUSE_MOVED,
    MOUSE_CLICKED,
    
    // ==================== RENDERING EVENTS ====================
    RENDER_STARTED,
    RENDER_COMPLETED,
    FRAME_RENDERED,
    
    // ==================== AUDIO EVENTS ====================
    SOUND_PLAYED,
    MUSIC_STARTED,
    MUSIC_STOPPED,
    MUSIC_PAUSED,
    MUSIC_RESUMED,
    
    // ==================== NETWORK EVENTS ====================
    CONNECTION_ESTABLISHED,
    CONNECTION_LOST,
    DATA_RECEIVED,
    DATA_SENT,
    
    // ==================== ERROR EVENTS ====================
    ERROR_OCCURRED,
    WARNING_ISSUED,
    EXCEPTION_THROWN
}
