package dev.aminhashemi.blueprinthell.core.observer;

/**
 * Represents a game event.
 * Follows Observer pattern for loose coupling.
 */
public class GameEvent {
    
    public enum EventType {
        PACKET_ARRIVED,
        PACKET_LOST,
        WIRE_CREATED,
        WIRE_REMOVED,
        LEVEL_COMPLETED,
        LEVEL_FAILED,
        COINS_CHANGED,
        WIRE_LENGTH_CHANGED,
        SYSTEM_DRAGGED,
        SHOP_OPENED,
        SHOP_CLOSED
    }
    
    private final EventType type;
    private final Object data;
    private final long timestamp;
    
    public GameEvent(EventType type, Object data) {
        this.type = type;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    public EventType getType() {
        return type;
    }
    
    public Object getData() {
        return data;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "GameEvent{" +
                "type=" + type +
                ", data=" + data +
                ", timestamp=" + timestamp +
                '}';
    }
}
