package dev.aminhashemi.blueprinthell.shared.connection;

import dev.aminhashemi.blueprinthell.shared.network.*;
import dev.aminhashemi.blueprinthell.shared.mode.OfflineModeManager;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages client-server connections with automatic fallback to offline mode.
 * Handles connection health monitoring and automatic reconnection attempts.
 */
public class ConnectionManager {
    
    private static final int RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY = 3000; // 3 seconds
    private static final long HEALTH_CHECK_INTERVAL = 5000; // 5 seconds
    private static final long CONNECTION_TIMEOUT = 10000; // 10 seconds
    
    private final NetworkManager networkManager;
    private final OfflineModeManager offlineModeManager;
    private final ClientId clientId;
    private final ScheduledExecutorService scheduler;
    
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean connecting = new AtomicBoolean(false);
    private int reconnectAttempts = 0;
    private long lastHeartbeat = 0;
    
    public ConnectionManager(NetworkManager networkManager, OfflineModeManager offlineModeManager) {
        this.networkManager = networkManager;
        this.offlineModeManager = offlineModeManager;
        this.clientId = new ClientId();
        this.scheduler = Executors.newScheduledThreadPool(2);
        
        // Set client ID in network manager
        networkManager.setClientId(clientId.getId());
    }
    
    /**
     * Attempts to connect to the server
     * @return true if connection successful
     */
    public boolean connect() {
        if (connecting.get()) {
            System.out.println("Connection attempt already in progress");
            return false;
        }
        
        connecting.set(true);
        
        try {
            if (networkManager.start()) {
                connected.set(true);
                reconnectAttempts = 0;
                lastHeartbeat = System.currentTimeMillis();
                
                // Start health monitoring
                startHealthMonitoring();
                
                System.out.println("Connected to server as client: " + clientId.getId());
                return true;
            } else {
                System.out.println("Failed to connect to server");
                handleConnectionFailure();
                return false;
            }
        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            handleConnectionFailure();
            return false;
        } finally {
            connecting.set(false);
        }
    }
    
    /**
     * Disconnects from the server
     */
    public void disconnect() {
        if (connected.get()) {
            connected.set(false);
            networkManager.stop();
            scheduler.shutdown();
            System.out.println("Disconnected from server");
        }
    }
    
    /**
     * Attempts to reconnect to the server
     */
    public void reconnect() {
        if (reconnectAttempts >= RECONNECT_ATTEMPTS) {
            System.out.println("Maximum reconnection attempts reached, switching to offline mode");
            offlineModeManager.handleConnectionFailure();
            return;
        }
        
        reconnectAttempts++;
        System.out.println("Attempting to reconnect... (attempt " + reconnectAttempts + "/" + RECONNECT_ATTEMPTS + ")");
        
        scheduler.schedule(() -> {
            if (connect()) {
                System.out.println("Reconnection successful");
                offlineModeManager.handleConnectionSuccess();
            } else {
                System.out.println("Reconnection failed, will retry...");
            }
        }, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Starts health monitoring for the connection
     */
    private void startHealthMonitoring() {
        scheduler.scheduleAtFixedRate(() -> {
            if (connected.get()) {
                checkConnectionHealth();
            }
        }, HEALTH_CHECK_INTERVAL, HEALTH_CHECK_INTERVAL, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Checks the health of the connection
     */
    private void checkConnectionHealth() {
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - lastHeartbeat > CONNECTION_TIMEOUT) {
            System.out.println("Connection health check failed, attempting reconnection");
            handleConnectionFailure();
        }
    }
    
    /**
     * Updates the last heartbeat time
     */
    public void updateHeartbeat() {
        lastHeartbeat = System.currentTimeMillis();
    }
    
    /**
     * Handles connection failure
     */
    private void handleConnectionFailure() {
        connected.set(false);
        networkManager.stop();
        
        // Switch to offline mode
        offlineModeManager.handleConnectionFailure();
        
        // Attempt reconnection
        reconnect();
    }
    
    /**
     * Handles successful connection
     */
    public void handleConnectionSuccess() {
        connected.set(true);
        reconnectAttempts = 0;
        lastHeartbeat = System.currentTimeMillis();
        
        // Switch to online mode
        offlineModeManager.handleConnectionSuccess();
        
        System.out.println("Connection established successfully");
    }
    
    /**
     * Sends a message to the server
     * @param message The message to send
     */
    public void sendMessage(NetworkMessage message) {
        if (connected.get()) {
            networkManager.sendTcpMessage(message, null);
        } else {
            System.out.println("Not connected to server, message not sent");
        }
    }
    
    /**
     * Sends a UDP message to the server
     * @param message The message to send
     */
    public void sendUdpMessage(NetworkMessage message) {
        if (connected.get()) {
            networkManager.sendUdpMessage(message, null);
        } else {
            System.out.println("Not connected to server, UDP message not sent");
        }
    }
    
    /**
     * Checks if connected to server
     * @return true if connected
     */
    public boolean isConnected() {
        return connected.get();
    }
    
    /**
     * Checks if currently connecting
     * @return true if connecting
     */
    public boolean isConnecting() {
        return connecting.get();
    }
    
    /**
     * Gets the client ID
     * @return Client ID string
     */
    public String getClientId() {
        return clientId.getId();
    }
    
    /**
     * Gets the number of reconnection attempts
     * @return Number of attempts
     */
    public int getReconnectAttempts() {
        return reconnectAttempts;
    }
    
    /**
     * Gets the time since last heartbeat
     * @return Time in milliseconds
     */
    public long getTimeSinceLastHeartbeat() {
        return System.currentTimeMillis() - lastHeartbeat;
    }
    
    /**
     * Resets reconnection attempts
     */
    public void resetReconnectAttempts() {
        reconnectAttempts = 0;
    }
    
    /**
     * Forces a reconnection attempt
     */
    public void forceReconnect() {
        if (connected.get()) {
            disconnect();
        }
        reconnect();
    }
    
    /**
     * Gets connection status information
     * @return Status string
     */
    public String getConnectionStatus() {
        if (connected.get()) {
            return "Connected (Client: " + clientId.getId() + ")";
        } else if (connecting.get()) {
            return "Connecting... (Attempt " + reconnectAttempts + "/" + RECONNECT_ATTEMPTS + ")";
        } else {
            return "Disconnected (Offline mode)";
        }
    }
    
    /**
     * Shuts down the connection manager
     */
    public void shutdown() {
        disconnect();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
