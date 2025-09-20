package dev.aminhashemi.blueprinthell.shared.network;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages TCP and UDP network communication with thread pools.
 * Handles both client and server networking with automatic reconnection.
 */
public class NetworkManager implements NetworkMessageHandler {
    
    private static final int TCP_PORT = 8888;
    private static final int UDP_PORT = 8889;
    private static final int MAX_CLIENTS = 50;
    private static final int HEARTBEAT_INTERVAL = 5000; // 5 seconds
    private static final int CONNECTION_TIMEOUT = 30000; // 30 seconds
    
    private final ExecutorService tcpExecutor;
    private final ExecutorService udpExecutor;
    private final ScheduledExecutorService heartbeatExecutor;
    
    private ServerSocket tcpServerSocket;
    private DatagramSocket udpSocket;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    private final Map<String, Socket> clientConnections = new ConcurrentHashMap<>();
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final Map<String, InetAddress> clientAddresses = new ConcurrentHashMap<>();
    private final Map<String, Integer> clientUdpPorts = new ConcurrentHashMap<>();
    
    private NetworkMessageHandler messageHandler;
    private String clientId;
    private boolean isServer;
    
    public NetworkManager(boolean isServer) {
        this.isServer = isServer;
        this.tcpExecutor = Executors.newFixedThreadPool(10);
        this.udpExecutor = Executors.newFixedThreadPool(5);
        this.heartbeatExecutor = Executors.newScheduledThreadPool(2);
    }
    
    public void setMessageHandler(NetworkMessageHandler handler) {
        this.messageHandler = handler;
    }
    
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
    
    public boolean start() {
        try {
            if (isServer) {
                return startServer();
            } else {
                return startClient();
            }
        } catch (Exception e) {
            System.err.println("Failed to start network manager: " + e.getMessage());
            return false;
        }
    }
    
    private boolean startServer() throws IOException {
        tcpServerSocket = new ServerSocket(TCP_PORT);
        udpSocket = new DatagramSocket(UDP_PORT);
        
        running.set(true);
        
        // Start TCP server
        tcpExecutor.submit(this::acceptTcpConnections);
        
        // Start UDP server
        udpExecutor.submit(this::handleUdpMessages);
        
        // Start heartbeat monitor
        heartbeatExecutor.scheduleAtFixedRate(this::monitorHeartbeats, 
            HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
        
        System.out.println("Server started on TCP:" + TCP_PORT + " UDP:" + UDP_PORT);
        return true;
    }
    
    private boolean startClient() throws IOException {
        udpSocket = new DatagramSocket();
        
        running.set(true);
        
        // Start UDP client
        udpExecutor.submit(this::handleUdpMessages);
        
        // Start heartbeat sender
        heartbeatExecutor.scheduleAtFixedRate(this::sendHeartbeat, 
            HEARTBEAT_INTERVAL, HEARTBEAT_INTERVAL, TimeUnit.MILLISECONDS);
        
        System.out.println("Client started on UDP:" + udpSocket.getLocalPort());
        return true;
    }
    
    private void acceptTcpConnections() {
        while (running.get()) {
            try {
                Socket clientSocket = tcpServerSocket.accept();
                String clientId = "C" + System.currentTimeMillis();
                
                clientConnections.put(clientId, clientSocket);
                lastHeartbeat.put(clientId, System.currentTimeMillis());
                
                // Handle client connection in separate thread
                tcpExecutor.submit(() -> handleTcpClient(clientId, clientSocket));
                
                System.out.println("Client connected: " + clientId);
                
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Error accepting TCP connection: " + e.getMessage());
                }
            }
        }
    }
    
    private void handleTcpClient(String clientId, Socket clientSocket) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter writer = new PrintWriter(
                clientSocket.getOutputStream(), true)) {
            
            String line;
            while (running.get() && (line = reader.readLine()) != null) {
                try {
                    NetworkMessage message = NetworkMessage.fromJson(line);
                    message.clientId = clientId;
                    handleMessage(message, clientId);
                } catch (Exception e) {
                    System.err.println("Error processing TCP message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("TCP client disconnected: " + clientId);
        } finally {
            disconnectClient(clientId);
        }
    }
    
    private void handleUdpMessages() {
        byte[] buffer = new byte[8192];
        while (running.get()) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                udpSocket.receive(packet);
                
                String messageJson = new String(packet.getData(), 0, packet.getLength());
                NetworkMessage message = NetworkMessage.fromJson(messageJson);
                
                if (isServer) {
                    // Server: broadcast to all clients except sender
                    broadcastUdpMessage(message, message.clientId);
                } else {
                    // Client: handle incoming message
                    handleMessage(message, null);
                }
                
            } catch (IOException e) {
                if (running.get()) {
                    System.err.println("Error handling UDP message: " + e.getMessage());
                }
            }
        }
    }
    
    public void sendTcpMessage(NetworkMessage message, String targetClientId) {
        if (!isServer) {
            // Client sending to server
            try (Socket socket = new Socket("localhost", TCP_PORT);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
                writer.println(message.toJson());
            } catch (IOException e) {
                System.err.println("Failed to send TCP message: " + e.getMessage());
            }
        } else {
            // Server sending to specific client
            Socket clientSocket = clientConnections.get(targetClientId);
            if (clientSocket != null && !clientSocket.isClosed()) {
                try (PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)) {
                    writer.println(message.toJson());
                } catch (IOException e) {
                    System.err.println("Failed to send TCP to client " + targetClientId + ": " + e.getMessage());
                }
            }
        }
    }
    
    public void sendUdpMessage(NetworkMessage message, String targetClientId) {
        try {
            byte[] data = message.toJson().getBytes();
            
            if (isServer) {
                // Server sending to specific client
                InetAddress clientAddress = clientAddresses.get(targetClientId);
                Integer clientPort = clientUdpPorts.get(targetClientId);
                if (clientAddress != null && clientPort != null) {
                    DatagramPacket packet = new DatagramPacket(data, data.length, clientAddress, clientPort);
                    udpSocket.send(packet);
                }
            } else {
                // Client sending to server
                InetAddress serverAddress = InetAddress.getByName("localhost");
                DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, UDP_PORT);
                udpSocket.send(packet);
            }
        } catch (IOException e) {
            System.err.println("Failed to send UDP message: " + e.getMessage());
        }
    }
    
    public void broadcastUdpMessage(NetworkMessage message, String excludeClientId) {
        if (!isServer) return;
        
        byte[] data = message.toJson().getBytes();
        
        for (Map.Entry<String, InetAddress> entry : clientAddresses.entrySet()) {
            String clientId = entry.getKey();
            if (clientId.equals(excludeClientId)) continue;
            
            InetAddress address = entry.getValue();
            Integer port = clientUdpPorts.get(clientId);
            
            if (address != null && port != null) {
                try {
                    DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                    udpSocket.send(packet);
                } catch (IOException e) {
                    System.err.println("Failed to broadcast to client " + clientId + ": " + e.getMessage());
                }
            }
        }
    }
    
    private void sendHeartbeat() {
        if (clientId != null) {
            NetworkMessage heartbeat = new NetworkMessage(
                NetworkMessage.MessageType.HEARTBEAT, clientId);
            sendUdpMessage(heartbeat, null);
        }
    }
    
    private void monitorHeartbeats() {
        long currentTime = System.currentTimeMillis();
        
        for (Map.Entry<String, Long> entry : lastHeartbeat.entrySet()) {
            String clientId = entry.getKey();
            long lastHeartbeatTime = entry.getValue();
            
            if (currentTime - lastHeartbeatTime > CONNECTION_TIMEOUT) {
                System.out.println("Client " + clientId + " timed out");
                disconnectClient(clientId);
            }
        }
    }
    
    private void disconnectClient(String clientId) {
        Socket socket = clientConnections.remove(clientId);
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
        
        lastHeartbeat.remove(clientId);
        clientAddresses.remove(clientId);
        clientUdpPorts.remove(clientId);
        
        if (messageHandler != null) {
            messageHandler.handleConnectionEvent(clientId, false);
        }
    }
    
    @Override
    public void handleMessage(NetworkMessage message, String senderId) {
        if (messageHandler != null) {
            messageHandler.handleMessage(message, senderId);
        }
    }
    
    @Override
    public void handleConnectionEvent(String clientId, boolean connected) {
        if (messageHandler != null) {
            messageHandler.handleConnectionEvent(clientId, connected);
        }
    }
    
    @Override
    public void handleNetworkError(String error, String clientId) {
        if (messageHandler != null) {
            messageHandler.handleNetworkError(error, clientId);
        }
    }
    
    public void stop() {
        running.set(false);
        
        try {
            if (tcpServerSocket != null) {
                tcpServerSocket.close();
            }
            if (udpSocket != null) {
                udpSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing sockets: " + e.getMessage());
        }
        
        // Close all client connections
        for (Socket socket : clientConnections.values()) {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
        
        tcpExecutor.shutdown();
        udpExecutor.shutdown();
        heartbeatExecutor.shutdown();
        
        System.out.println("Network manager stopped");
    }
    
    public boolean isRunning() {
        return running.get();
    }
    
    public int getConnectedClientCount() {
        return clientConnections.size();
    }
}
