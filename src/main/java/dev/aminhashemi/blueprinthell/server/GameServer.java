package dev.aminhashemi.blueprinthell.server;

import dev.aminhashemi.blueprinthell.network.NetworkProtocol;
import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.core.constants.GameConstants;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Game server for handling client connections and data management
 * Updated to work with unified main architecture
 */
public class GameServer {
    private static final int SERVER_PORT = GameConstants.DEFAULT_SERVER_PORT;
    private DatabaseServerDataManager dataManager;
    private ServerSocket serverSocket;
    private ExecutorService clientThreadPool;
    private final AtomicBoolean running = new AtomicBoolean(false);
    
    public GameServer() {
        this.dataManager = new DatabaseServerDataManager();
        this.clientThreadPool = Executors.newFixedThreadPool(10);
    }
    
    /**
     * Start the server
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            running.set(true);
            Logger.getInstance().info("Game server started on port " + SERVER_PORT);
            
            // Start server in background thread
            Thread serverThread = new Thread(() -> {
                try {
                    while (running.get()) {
                        Socket clientSocket = serverSocket.accept();
                        clientThreadPool.submit(new ClientHandler(clientSocket, dataManager));
                    }
                } catch (IOException e) {
                    if (running.get()) {
                        Logger.getInstance().error("Server error", e);
                    }
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();
            
        } catch (IOException e) {
            Logger.getInstance().error("Failed to start server", e);
        }
    }
    
    /**
     * Stop the server
     */
    public void stop() {
        running.set(false);
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            clientThreadPool.shutdown();
            Logger.getInstance().info("Game server stopped");
        } catch (IOException e) {
            Logger.getInstance().error("Error stopping server", e);
        }
    }
    
    /**
     * Check if server is running
     * @return True if server is running
     */
    public boolean isRunning() {
        return running.get() && serverSocket != null && !serverSocket.isClosed();
    }
    
    /**
     * Client handler for processing individual client requests
     */
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private DatabaseServerDataManager dataManager;
        private BufferedReader reader;
        private PrintWriter writer;
        
        public ClientHandler(Socket clientSocket, DatabaseServerDataManager dataManager) {
            this.clientSocket = clientSocket;
            this.dataManager = dataManager;
        }
        
        @Override
        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                
                String inputLine;
                while ((inputLine = reader.readLine()) != null) {
                    processMessage(inputLine);
                }
            } catch (IOException e) {
                Logger.getInstance().error("Client handler error", e);
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    Logger.getInstance().error("Error closing client socket", e);
                }
            }
        }
        
        private void processMessage(String messageJson) {
            try {
                NetworkProtocol.Message message = NetworkProtocol.deserialize(messageJson);
                if (message == null) {
                    sendError("Invalid message format", 400);
                    return;
                }
                
                switch (message.type) {
                    case AUTH_REQUEST:
                        handleAuthRequest(NetworkProtocol.deserialize(messageJson, NetworkProtocol.AuthRequest.class));
                        break;
                    case GET_USER_PROFILE:
                        handleGetUserProfile(message);
                        break;
                    case UPDATE_USER_PROFILE:
                        handleUpdateUserProfile(message);
                        break;
                    case GET_LEADERBOARD:
                        handleGetLeaderboard(message);
                        break;
                    case ADD_GAME_RECORD:
                        handleAddGameRecord(NetworkProtocol.deserialize(messageJson, NetworkProtocol.GameRecordData.class));
                        break;
                    default:
                        sendError("Unknown message type", 400);
                }
            } catch (Exception e) {
                Logger.getInstance().error("Error processing message", e);
                sendError("Internal server error", 500);
            }
        }
        
        private void handleAuthRequest(NetworkProtocol.AuthRequest authRequest) {
            if (authRequest == null || authRequest.macAddress == null) {
                sendError("Invalid auth request", 400);
                return;
            }
            
            UserProfile profile = dataManager.getUserProfile(authRequest.macAddress);
            if (authRequest.username != null && !authRequest.username.isEmpty()) {
                profile.setUsername(authRequest.username);
            }
            profile.updateLastLogin();
            dataManager.updateUserProfile(profile);
            
            NetworkProtocol.AuthResponse response = new NetworkProtocol.AuthResponse(
                authRequest.macAddress, true, "Authentication successful"
            );
            response.userProfile = profile;
            sendMessage(response);
        }
        
        private void handleGetUserProfile(NetworkProtocol.Message message) {
            UserProfile profile = dataManager.getUserProfile(message.macAddress);
            NetworkProtocol.UserProfileResponse response = new NetworkProtocol.UserProfileResponse(
                message.macAddress, profile
            );
            sendMessage(response);
        }
        
        private void handleUpdateUserProfile(NetworkProtocol.Message message) {
            try {
                UserProfile profile = NetworkProtocol.gson.fromJson(message.data, UserProfile.class);
                dataManager.updateUserProfile(profile);
                sendMessage(new NetworkProtocol.Message(NetworkProtocol.MessageType.USER_PROFILE_RESPONSE, message.macAddress));
            } catch (Exception e) {
                sendError("Invalid user profile data", 400);
            }
        }
        
        private void handleGetLeaderboard(NetworkProtocol.Message message) {
            LeaderboardData leaderboard = dataManager.getGlobalLeaderboard();
            NetworkProtocol.LeaderboardResponse response = new NetworkProtocol.LeaderboardResponse(
                message.macAddress, leaderboard
            );
            sendMessage(response);
        }
        
        private void handleAddGameRecord(NetworkProtocol.GameRecordData gameRecordData) {
            if (gameRecordData == null || gameRecordData.record == null) {
                sendError("Invalid game record data", 400);
                return;
            }
            
            // Add to global leaderboard
            dataManager.addGameRecord(gameRecordData.levelName, gameRecordData.record);
            
            // Add to user profile
            UserProfile profile = dataManager.getUserProfile(gameRecordData.macAddress);
            UserProfile.GameRecord userRecord = new UserProfile.GameRecord(
                gameRecordData.levelName,
                gameRecordData.record.completionTime,
                gameRecordData.record.xpEarned,
                gameRecordData.record.coinsEarned,
                gameRecordData.record.packetLossPercentage
            );
            profile.addGameRecord(userRecord);
            profile.addXP(gameRecordData.record.xpEarned);
            profile.addCoins(gameRecordData.record.coinsEarned);
            dataManager.updateUserProfile(profile);
            
            sendMessage(new NetworkProtocol.Message(NetworkProtocol.MessageType.LEADERBOARD_RESPONSE, gameRecordData.macAddress));
        }
        
        private void sendMessage(NetworkProtocol.Message message) {
            String json = NetworkProtocol.serialize(message);
            writer.println(json);
        }
        
        private void sendError(String errorMessage, int errorCode) {
            NetworkProtocol.ErrorResponse error = new NetworkProtocol.ErrorResponse("", errorMessage, errorCode);
            sendMessage(error);
        }
    }
}
