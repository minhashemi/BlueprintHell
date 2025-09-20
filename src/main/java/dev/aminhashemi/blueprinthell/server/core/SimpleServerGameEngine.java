package dev.aminhashemi.blueprinthell.server.core;

import dev.aminhashemi.blueprinthell.shared.network.*;
import dev.aminhashemi.blueprinthell.model.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simplified server-side game engine for testing
 */
public class SimpleServerGameEngine implements NetworkMessageHandler, GameStateSync {
    
    private final NetworkManager networkManager;
    private final ConcurrentHashMap<String, SaveData> clientStates = new ConcurrentHashMap<>();
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private SaveData currentGameState;
    
    public SimpleServerGameEngine() {
        this.networkManager = new NetworkManager(true);
        this.networkManager.setMessageHandler(this);
        this.currentGameState = new SaveData();
    }
    
    public boolean start() {
        if (networkManager.start()) {
            running.set(true);
            java.lang.System.out.println("Server game engine started");
            return true;
        }
        return false;
    }
    
    public void stop() {
        running.set(false);
        networkManager.stop();
        java.lang.System.out.println("Server game engine stopped");
    }
    
    public void update() {
        if (!running.get()) {
            return;
        }
        
        // Broadcast game state update to all clients
        broadcastGameStateUpdate();
    }
    
    private void broadcastGameStateUpdate() {
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.GAME_STATE_UPDATE, "SERVER", currentGameState);
        
        // Broadcast to all connected clients
        for (String clientId : clientStates.keySet()) {
            networkManager.sendUdpMessage(message, clientId);
        }
    }
    
    @Override
    public void handleMessage(NetworkMessage message, String senderId) {
        if (senderId != null) {
            clientStates.put(senderId, message.getSaveData());
        }
        
        switch (message.type) {
            case PLAYER_INPUT:
                handlePlayerInput(message, senderId);
                break;
            case MOUSE_CLICK:
                handleMouseClick(message, senderId);
                break;
            case KEYBOARD_INPUT:
                handleKeyboardInput(message, senderId);
                break;
            case SYSTEM_PLACEMENT:
                handleSystemPlacement(message, senderId);
                break;
            case WIRE_CREATION:
                handleWireCreation(message, senderId);
                break;
            case SHOP_PURCHASE:
                handleShopPurchase(message, senderId);
                break;
            case GAME_COMMAND:
                handleGameCommand(message, senderId);
                break;
            case HEARTBEAT:
                handleHeartbeat(senderId);
                break;
            default:
                java.lang.System.out.println("Unknown message type: " + message.type);
        }
    }
    
    private void handlePlayerInput(NetworkMessage message, String senderId) {
        java.lang.System.out.println("Received player input from " + senderId);
    }
    
    private void handleMouseClick(NetworkMessage message, String senderId) {
        java.lang.System.out.println("Received mouse click from " + senderId);
    }
    
    private void handleKeyboardInput(NetworkMessage message, String senderId) {
        java.lang.System.out.println("Received keyboard input from " + senderId);
    }
    
    private void handleSystemPlacement(NetworkMessage message, String senderId) {
        java.lang.System.out.println("Received system placement from " + senderId);
    }
    
    private void handleWireCreation(NetworkMessage message, String senderId) {
        java.lang.System.out.println("Received wire creation from " + senderId);
    }
    
    private void handleShopPurchase(NetworkMessage message, String senderId) {
        java.lang.System.out.println("Received shop purchase from " + senderId);
    }
    
    private void handleGameCommand(NetworkMessage message, String senderId) {
        java.lang.System.out.println("Received game command from " + senderId);
    }
    
    private void handleHeartbeat(String senderId) {
        if (senderId != null) {
            java.lang.System.out.println("Received heartbeat from " + senderId);
        }
    }
    
    @Override
    public void handleConnectionEvent(String clientId, boolean connected) {
        if (connected) {
            java.lang.System.out.println("Client connected: " + clientId);
            // Send initial game state to new client
            NetworkMessage message = new NetworkMessage(
                NetworkMessage.MessageType.GAME_STATE_UPDATE, "SERVER", currentGameState);
            networkManager.sendTcpMessage(message, clientId);
        } else {
            java.lang.System.out.println("Client disconnected: " + clientId);
            clientStates.remove(clientId);
        }
    }
    
    @Override
    public void handleNetworkError(String error, String clientId) {
        java.lang.System.err.println("Network error for client " + clientId + ": " + error);
    }
    
    // GameStateSync implementation
    @Override
    public void syncGameState(SaveData gameState) {
        currentGameState = gameState;
        broadcastGameStateUpdate();
    }
    
    @Override
    public void syncDeltaState(SaveData deltaState) {
        // Handle delta state updates
    }
    
    @Override
    public void syncSnapshot(SaveData snapshot, int snapshotIndex) {
        // Handle snapshot synchronization
    }
    
    @Override
    public void syncPacketMovement(String packetData) {
        // Broadcast packet movement to all clients
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.PACKET_MOVEMENT_UPDATE, "SERVER", packetData);
        
        for (String clientId : clientStates.keySet()) {
            networkManager.sendUdpMessage(message, clientId);
        }
    }
    
    @Override
    public void syncSystemUpdate(String systemData) {
        // Broadcast system update to all clients
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.SYSTEM_UPDATE, "SERVER", systemData);
        
        for (String clientId : clientStates.keySet()) {
            networkManager.sendUdpMessage(message, clientId);
        }
    }
    
    @Override
    public void syncWireUpdate(String wireData) {
        // Broadcast wire update to all clients
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.WIRE_UPDATE, "SERVER", wireData);
        
        for (String clientId : clientStates.keySet()) {
            networkManager.sendUdpMessage(message, clientId);
        }
    }
    
    @Override
    public void syncImpactEffect(String impactData) {
        // Broadcast impact effect to all clients
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.IMPACT_EFFECT, "SERVER", impactData);
        
        for (String clientId : clientStates.keySet()) {
            networkManager.sendUdpMessage(message, clientId);
        }
    }
    
    // Getters
    public boolean isRunning() {
        return running.get();
    }
    
    public int getConnectedClientCount() {
        return clientStates.size();
    }
}
