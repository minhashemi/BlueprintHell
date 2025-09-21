package dev.aminhashemi.blueprinthell.core.events;

/**
 * Represents a game event with type and data.
 * Follows the Observer pattern for loose coupling.
 */
public class GameEvent {
    
    private final GameEventType type;
    private final Object data;
    private final long timestamp;
    private final String source;
    
    /**
     * Creates a new game event
     * @param type Event type
     * @param data Event data
     */
    public GameEvent(GameEventType type, Object data) {
        this(type, data, null);
    }
    
    /**
     * Creates a new game event with source information
     * @param type Event type
     * @param data Event data
     * @param source Event source
     */
    public GameEvent(GameEventType type, Object data, String source) {
        this.type = type;
        this.data = data;
        this.source = source;
        this.timestamp = System.currentTimeMillis();
    }
    
    /**
     * Gets the event type
     * @return Event type
     */
    public GameEventType getType() {
        return type;
    }
    
    /**
     * Gets the event data
     * @return Event data
     */
    public Object getData() {
        return data;
    }
    
    /**
     * Gets the event timestamp
     * @return Event timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Gets the event source
     * @return Event source
     */
    public String getSource() {
        return source;
    }
    
    /**
     * Gets the event data as a specific type
     * @param clazz Type to cast to
     * @param <T> Type parameter
     * @return Casted data or null if not compatible
     */
    @SuppressWarnings("unchecked")
    public <T> T getDataAs(Class<T> clazz) {
        if (data != null && clazz.isAssignableFrom(data.getClass())) {
            return (T) data;
        }
        return null;
    }
    
    /**
     * Checks if the event has data
     * @return True if event has data
     */
    public boolean hasData() {
        return data != null;
    }
    
    /**
     * Checks if the event has a specific source
     * @param source Source to check
     * @return True if event has the specified source
     */
    public boolean hasSource(String source) {
        return this.source != null && this.source.equals(source);
    }
    
    @Override
    public String toString() {
        return "GameEvent{" +
                "type=" + type +
                ", data=" + data +
                ", timestamp=" + timestamp +
                ", source='" + source + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        GameEvent gameEvent = (GameEvent) obj;
        return type == gameEvent.type &&
                timestamp == gameEvent.timestamp &&
                (data != null ? data.equals(gameEvent.data) : gameEvent.data == null) &&
                (source != null ? source.equals(gameEvent.source) : gameEvent.source == null);
    }
    
    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }
}
