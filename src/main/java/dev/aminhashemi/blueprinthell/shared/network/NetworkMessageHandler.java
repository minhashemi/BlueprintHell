package dev.aminhashemi.blueprinthell.shared.network;

/**
 * Interface for handling incoming network messages.
 * Implemented by both client and server to process different message types.
 */
public interface NetworkMessageHandler {
    
    /**
     * Handles an incoming network message.
     * @param message The received network message
     * @param senderId The ID of the sender (null for server messages)
     */
    void handleMessage(NetworkMessage message, String senderId);
    
    /**
     * Handles connection events.
     * @param clientId The client ID
     * @param connected True if connected, false if disconnected
     */
    default void handleConnectionEvent(String clientId, boolean connected) {
        // Default implementation does nothing
    }
    
    /**
     * Handles network errors.
     * @param error The error message
     * @param clientId The client ID (null if server error)
     */
    default void handleNetworkError(String error, String clientId) {
        System.err.println("Network error for client " + clientId + ": " + error);
    }
}
