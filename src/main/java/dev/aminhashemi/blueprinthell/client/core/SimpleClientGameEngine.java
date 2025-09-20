package dev.aminhashemi.blueprinthell.client.core;

import dev.aminhashemi.blueprinthell.shared.network.*;
import dev.aminhashemi.blueprinthell.model.*;
import dev.aminhashemi.blueprinthell.view.GamePanel;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simplified client-side game engine for testing
 */
public class SimpleClientGameEngine implements NetworkMessageHandler, GameStateSync, Runnable {
    
    private final GamePanel gamePanel;
    private final NetworkManager networkManager;
    private final ClientId clientId;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Thread gameThread;
    
    private SaveData currentGameState;
    private final Object gameStateLock = new Object();
    
    private boolean connectedToServer = false;
    private boolean offlineMode = false;
    private long lastServerMessage = 0;
    private final long CONNECTION_TIMEOUT = 10000; // 10 seconds
    
    public SimpleClientGameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        this.clientId = new ClientId();
        this.networkManager = new NetworkManager(false);
        this.networkManager.setMessageHandler(this);
        this.networkManager.setClientId(clientId.getId());
        this.currentGameState = new SaveData();
    }
    
    public boolean start() {
        // Try to connect to server first
        if (networkManager.start()) {
            connectedToServer = true;
            java.lang.System.out.println("Connected to server as client: " + clientId.getId());
        } else {
            // Fall back to offline mode
            offlineMode = true;
            java.lang.System.out.println("Failed to connect to server, running in offline mode");
        }
        
        running.set(true);
        gameThread = new Thread(this);
        gameThread.start();
        
        return true;
    }
    
    public void stop() {
        running.set(false);
        networkManager.stop();
        
        if (gameThread != null) {
            try {
                gameThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        java.lang.System.out.println("Client game engine stopped");
    }
    
    @Override
    public void run() {
        while (running.get()) {
            // Simple game loop
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    @Override
    public void handleMessage(NetworkMessage message, String senderId) {
        lastServerMessage = java.lang.System.currentTimeMillis();
        
        switch (message.type) {
            case GAME_STATE_UPDATE:
                handleGameStateUpdate(message);
                break;
            case PACKET_MOVEMENT_UPDATE:
                handlePacketMovementUpdate(message);
                break;
            case SYSTEM_UPDATE:
                handleSystemUpdate(message);
                break;
            case WIRE_UPDATE:
                handleWireUpdate(message);
                break;
            case IMPACT_EFFECT:
                handleImpactEffect(message);
                break;
            case GAME_EVENT:
                handleGameEvent(message);
                break;
            case CONNECTION_ACK:
                handleConnectionAck(message);
                break;
            case ERROR_MESSAGE:
                handleErrorMessage(message);
                break;
            default:
                java.lang.System.out.println("Unknown message type: " + message.type);
        }
    }
    
    private void handleGameStateUpdate(NetworkMessage message) {
        SaveData newGameState = message.getSaveData();
        if (newGameState != null) {
            synchronized (gameStateLock) {
                currentGameState = newGameState;
            }
        }
    }
    
    private void handlePacketMovementUpdate(NetworkMessage message) {
        java.lang.System.out.println("Received packet movement update");
    }
    
    private void handleSystemUpdate(NetworkMessage message) {
        java.lang.System.out.println("Received system update");
    }
    
    private void handleWireUpdate(NetworkMessage message) {
        java.lang.System.out.println("Received wire update");
    }
    
    private void handleImpactEffect(NetworkMessage message) {
        java.lang.System.out.println("Received impact effect");
    }
    
    private void handleGameEvent(NetworkMessage message) {
        java.lang.System.out.println("Received game event: " + message.data);
    }
    
    private void handleConnectionAck(NetworkMessage message) {
        java.lang.System.out.println("Connection acknowledged by server");
        connectedToServer = true;
        offlineMode = false;
    }
    
    private void handleErrorMessage(NetworkMessage message) {
        java.lang.System.err.println("Server error: " + message.data);
    }
    
    @Override
    public void handleConnectionEvent(String clientId, boolean connected) {
        if (connected) {
            java.lang.System.out.println("Connected to server");
            connectedToServer = true;
            offlineMode = false;
        } else {
            java.lang.System.out.println("Disconnected from server");
            connectedToServer = false;
            offlineMode = true;
        }
    }
    
    @Override
    public void handleNetworkError(String error, String clientId) {
        java.lang.System.err.println("Network error: " + error);
        if (connectedToServer) {
            java.lang.System.out.println("Switching to offline mode due to network error");
            connectedToServer = false;
            offlineMode = true;
        }
    }
    
    // GameStateSync implementation
    @Override
    public void syncGameState(SaveData gameState) {
        synchronized (gameStateLock) {
            currentGameState = gameState;
        }
    }
    
    @Override
    public void syncDeltaState(SaveData deltaState) {
        // Handle delta state updates
    }
    
    @Override
    public void syncSnapshot(SaveData snapshot, int snapshotIndex) {
        synchronized (gameStateLock) {
            currentGameState = snapshot;
        }
    }
    
    @Override
    public void syncPacketMovement(String packetData) {
        // Handle packet movement updates
    }
    
    @Override
    public void syncSystemUpdate(String systemData) {
        // Handle system updates
    }
    
    @Override
    public void syncWireUpdate(String wireData) {
        // Handle wire updates
    }
    
    @Override
    public void syncImpactEffect(String impactData) {
        // Handle impact effects
    }
    
    // Input handling
    public void handleMouseClick(Point point, boolean leftButton) {
        if (offlineMode) {
            return;
        }
        
        NetworkMessage message = new NetworkMessage(
            leftButton ? NetworkMessage.MessageType.MOUSE_CLICK : NetworkMessage.MessageType.MOUSE_CLICK,
            clientId.getId(),
            "{\"x\":" + point.x + ",\"y\":" + point.y + ",\"left\":" + leftButton + "}"
        );
        
        networkManager.sendTcpMessage(message, null);
    }
    
    public void handleMouseMove(Point point) {
        if (offlineMode) {
            return;
        }
        
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.MOUSE_MOVE,
            clientId.getId(),
            "{\"x\":" + point.x + ",\"y\":" + point.y + "}"
        );
        
        networkManager.sendUdpMessage(message, null);
    }
    
    public void handleMouseDrag(Point point) {
        if (offlineMode) {
            return;
        }
        
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.MOUSE_DRAG,
            clientId.getId(),
            "{\"x\":" + point.x + ",\"y\":" + point.y + "}"
        );
        
        networkManager.sendUdpMessage(message, null);
    }
    
    public void handleKeyPress(int keyCode) {
        if (offlineMode) {
            return;
        }
        
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.KEYBOARD_INPUT,
            clientId.getId(),
            "{\"keyCode\":" + keyCode + ",\"pressed\":true}"
        );
        
        networkManager.sendTcpMessage(message, null);
    }
    
    public void handleKeyRelease(int keyCode) {
        if (offlineMode) {
            return;
        }
        
        NetworkMessage message = new NetworkMessage(
            NetworkMessage.MessageType.KEYBOARD_INPUT,
            clientId.getId(),
            "{\"keyCode\":" + keyCode + ",\"pressed\":false}"
        );
        
        networkManager.sendTcpMessage(message, null);
    }
    
    // Getters
    public SaveData getCurrentGameState() {
        synchronized (gameStateLock) {
            return currentGameState;
        }
    }
    
    public boolean isConnectedToServer() {
        return connectedToServer;
    }
    
    public boolean isOfflineMode() {
        return offlineMode;
    }
    
    public String getClientId() {
        return clientId.getId();
    }
}
