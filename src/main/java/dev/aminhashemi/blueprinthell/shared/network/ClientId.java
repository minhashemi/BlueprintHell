package dev.aminhashemi.blueprinthell.shared.network;

/**
 * Generates unique client identifiers using timestamp and prefix.
 * Ensures uniqueness across multiple clients connecting simultaneously.
 */
public class ClientId {
    private final long timestamp;
    private final String prefix;
    private final String fullId;
    
    public ClientId() {
        this.timestamp = System.currentTimeMillis();
        this.prefix = "C";
        this.fullId = prefix + timestamp;
    }
    
    public ClientId(String customPrefix) {
        this.timestamp = System.currentTimeMillis();
        this.prefix = customPrefix;
        this.fullId = prefix + timestamp;
    }
    
    public String getId() {
        return fullId;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public String getPrefix() {
        return prefix;
    }
    
    @Override
    public String toString() {
        return fullId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ClientId clientId = (ClientId) obj;
        return fullId.equals(clientId.fullId);
    }
    
    @Override
    public int hashCode() {
        return fullId.hashCode();
    }
}
