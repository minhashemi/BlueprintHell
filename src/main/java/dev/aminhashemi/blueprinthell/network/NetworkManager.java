package dev.aminhashemi.blueprinthell.network;

import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.core.constants.GameConstants;
import dev.aminhashemi.blueprinthell.core.exception.GlobalExceptionManager;
import dev.aminhashemi.blueprinthell.core.exception.ExceptionResponse;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages network communication for the game.
 * Handles both client and server network operations.
 */
public class NetworkManager {
    
    private Socket clientSocket;
    private ServerSocket serverSocket;
    private PrintWriter out;
    private BufferedReader in;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    /**
     * Connects to a server as a client
     * @param serverIP Server IP address
     * @param port Server port
     * @return True if connection successful
     */
    public boolean connectToServer(String serverIP, int port) {
        try {
            Logger.getInstance().info("Attempting to connect to server " + serverIP + ":" + port);
            
            clientSocket = new Socket(serverIP, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
            connected.set(true);
            running.set(true);
            
            // Start listening for server messages
            startMessageListener();
            
            Logger.getInstance().info("Successfully connected to server");
            return true;
            
        } catch (IOException e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            Logger.getInstance().error("Failed to connect to server: " + response.getMessage());
            connected.set(false);
            return false;
        }
    }
    
    /**
     * Starts the server
     * @param port Server port
     * @return True if server started successfully
     */
    public boolean startServer(int port) {
        try {
            Logger.getInstance().info("Starting server on port " + port);
            
            serverSocket = new ServerSocket(port);
            running.set(true);
            
            // Start accepting connections
            startConnectionAcceptor();
            
            Logger.getInstance().info("Server started successfully");
            return true;
            
        } catch (IOException e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            Logger.getInstance().error("Failed to start server: " + response.getMessage());
            return false;
        }
    }
    
    /**
     * Sends a message to the server
     * @param message Message to send
     */
    public void sendMessage(String message) {
        if (out != null && connected.get()) {
            out.println(message);
            Logger.getInstance().info("Sent message: " + message);
        } else {
            Logger.getInstance().warning("Cannot send message - not connected");
        }
    }
    
    /**
     * Sends game input to server
     * @param inputType Type of input (key, mouse, etc.)
     * @param inputData Input data
     */
    public void sendGameInput(String inputType, String inputData) {
        String message = "INPUT:" + inputType + ":" + inputData;
        sendMessage(message);
    }
    
    /**
     * Sends game state to client
     * @param gameState Game state data
     */
    public void sendGameState(String gameState) {
        String message = "STATE:" + gameState;
        sendMessage(message);
    }
    
    /**
     * Checks if connected to server
     * @return True if connected
     */
    public boolean isConnected() {
        return connected.get() && clientSocket != null && !clientSocket.isClosed();
    }
    
    /**
     * Checks if server is running
     * @return True if server is running
     */
    public boolean isServerRunning() {
        return running.get() && serverSocket != null && !serverSocket.isClosed();
    }
    
    /**
     * Disconnects from server
     */
    public void disconnect() {
        try {
            running.set(false);
            connected.set(false);
            
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (clientSocket != null) {
                clientSocket.close();
            }
            if (serverSocket != null) {
                serverSocket.close();
            }
            
            Logger.getInstance().info("Disconnected from network");
            
        } catch (IOException e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            Logger.getInstance().error("Error during disconnect: " + response.getMessage());
        }
    }
    
    /**
     * Starts listening for messages from server
     */
    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String message;
                while (running.get() && (message = in.readLine()) != null) {
                    handleIncomingMessage(message);
                }
            } catch (IOException e) {
                if (running.get()) {
                    ExceptionResponse response = GlobalExceptionManager.handleException(e);
                    Logger.getInstance().error("Error reading from server: " + response.getMessage());
                    connected.set(false);
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }
    
    /**
     * Starts accepting client connections
     */
    private void startConnectionAcceptor() {
        Thread acceptorThread = new Thread(() -> {
            try {
                while (running.get()) {
                    Socket clientSocket = serverSocket.accept();
                    handleNewClient(clientSocket);
                }
            } catch (IOException e) {
                if (running.get()) {
                    ExceptionResponse response = GlobalExceptionManager.handleException(e);
                    Logger.getInstance().error("Error accepting connections: " + response.getMessage());
                }
            }
        });
        acceptorThread.setDaemon(true);
        acceptorThread.start();
    }
    
    /**
     * Handles incoming messages from server
     * @param message Incoming message
     */
    private void handleIncomingMessage(String message) {
        Logger.getInstance().info("Received message: " + message);
        
        if (message.startsWith("STATE:")) {
            // Handle game state update
            String gameState = message.substring(6);
            handleGameStateUpdate(gameState);
        } else if (message.startsWith("INPUT:")) {
            // Handle input confirmation
            String inputData = message.substring(6);
            handleInputConfirmation(inputData);
        }
    }
    
    /**
     * Handles new client connection
     * @param clientSocket Client socket
     */
    private void handleNewClient(Socket clientSocket) {
        try {
            // Get client info
            String clientIP = clientSocket.getInetAddress().getHostAddress();
            String clientMAC = getClientMACAddress(clientSocket);
            
            Logger.getInstance().info("New client connected: " + clientIP);
            System.out.println("🔗 New Client Connected!");
            System.out.println("📱 IP Address: " + clientIP);
            System.out.println("🆔 MAC Address: " + clientMAC);
            System.out.println("⏰ Time: " + java.time.LocalTime.now());
            System.out.println("-" + "-".repeat(40));
            
            // Handle client communication
            handleClientCommunication(clientSocket);
            
        } catch (Exception e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            Logger.getInstance().error("Error handling new client: " + response.getMessage());
        }
    }
    
    /**
     * Handles communication with a specific client
     * @param clientSocket Client socket
     */
    private void handleClientCommunication(Socket clientSocket) {
        Thread clientThread = new Thread(() -> {
            try {
                PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader clientIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                
                String message;
                while (running.get() && (message = clientIn.readLine()) != null) {
                    handleClientMessage(message, clientOut);
                }
                
            } catch (IOException e) {
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                Logger.getInstance().error("Error communicating with client: " + response.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    ExceptionResponse response = GlobalExceptionManager.handleException(e);
                    Logger.getInstance().error("Error closing client socket: " + response.getMessage());
                }
            }
        });
        clientThread.setDaemon(true);
        clientThread.start();
    }
    
    /**
     * Handles messages from clients
     * @param message Client message
     * @param clientOut Client output stream
     */
    private void handleClientMessage(String message, PrintWriter clientOut) {
        Logger.getInstance().info("Received from client: " + message);
        
        if (message.startsWith("INPUT:")) {
            // Handle game input from client
            String inputData = message.substring(6);
            handleClientInput(inputData, clientOut);
        }
    }
    
    /**
     * Handles game input from client
     * @param inputData Input data
     * @param clientOut Client output stream
     */
    private void handleClientInput(String inputData, PrintWriter clientOut) {
        // Process the input and update game state
        // This would integrate with your existing game engine
        
        // Send updated game state back to client
        String gameState = "UPDATED_GAME_STATE"; // This would be actual game state
        clientOut.println("STATE:" + gameState);
    }
    
    /**
     * Handles game state update from server
     * @param gameState Game state data
     */
    private void handleGameStateUpdate(String gameState) {
        // Update local game state
        // This would integrate with your existing game engine
        Logger.getInstance().info("Updating game state: " + gameState);
    }
    
    /**
     * Handles input confirmation from server
     * @param inputData Input data
     */
    private void handleInputConfirmation(String inputData) {
        // Confirm input was processed
        Logger.getInstance().info("Input confirmed: " + inputData);
    }
    
    /**
     * Gets client MAC address (simplified)
     * @param clientSocket Client socket
     * @return MAC address string
     */
    private String getClientMACAddress(Socket clientSocket) {
        try {
            InetAddress clientIP = clientSocket.getInetAddress();
            NetworkInterface networkInterface = NetworkInterface.getByInetAddress(clientIP);
            if (networkInterface != null) {
                byte[] mac = networkInterface.getHardwareAddress();
                if (mac != null) {
                    StringBuilder macAddress = new StringBuilder();
                    for (int i = 0; i < mac.length; i++) {
                        macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : ""));
                    }
                    return macAddress.toString();
                }
            }
        } catch (Exception e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            Logger.getInstance().warning("Could not get MAC address: " + response.getMessage());
        }
        return "Unknown";
    }
}
