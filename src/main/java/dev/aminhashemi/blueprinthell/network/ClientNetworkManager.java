package dev.aminhashemi.blueprinthell.network;

import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.utils.MacAddressUtil;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

/**
 * Client-side network manager for communicating with server
 */
public class ClientNetworkManager {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    
    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String macAddress;
    private boolean connected = false;
    
    public ClientNetworkManager() {
        this.macAddress = MacAddressUtil.getMacAddress();
    }
    
    /**
     * Connect to server
     */
    public boolean connect() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);
            connected = true;
            Logger.getInstance().info("Connected to server");
            return true;
        } catch (IOException e) {
            Logger.getInstance().error("Failed to connect to server", e);
            connected = false;
            return false;
        }
    }
    
    /**
     * Disconnect from server
     */
    public void disconnect() {
        try {
            if (socket != null) {
                socket.close();
            }
            connected = false;
            Logger.getInstance().info("Disconnected from server");
        } catch (IOException e) {
            Logger.getInstance().error("Error disconnecting from server", e);
        }
    }
    
    /**
     * Authenticate with server
     */
    public CompletableFuture<UserProfile> authenticate(String username) {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected) {
                return null;
            }
            
            try {
                NetworkProtocol.AuthRequest authRequest = new NetworkProtocol.AuthRequest(macAddress, username);
                sendMessage(authRequest);
                
                String response = reader.readLine();
                NetworkProtocol.AuthResponse authResponse = NetworkProtocol.deserialize(response, NetworkProtocol.AuthResponse.class);
                
                if (authResponse != null && authResponse.success) {
                    Logger.getInstance().info("Authentication successful for user: " + username);
                    return authResponse.userProfile;
                } else {
                    Logger.getInstance().error("Authentication failed: " + (authResponse != null ? authResponse.message : "Unknown error"));
                    return null;
                }
            } catch (IOException e) {
                Logger.getInstance().error("Error during authentication", e);
                return null;
            }
        });
    }
    
    /**
     * Get user profile from server
     */
    public CompletableFuture<UserProfile> getUserProfile() {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected) {
                return null;
            }
            
            try {
                NetworkProtocol.Message request = new NetworkProtocol.Message(NetworkProtocol.MessageType.GET_USER_PROFILE, macAddress);
                sendMessage(request);
                
                String response = reader.readLine();
                NetworkProtocol.UserProfileResponse userResponse = NetworkProtocol.deserialize(response, NetworkProtocol.UserProfileResponse.class);
                
                if (userResponse != null) {
                    return userResponse.userProfile;
                } else {
                    Logger.getInstance().error("Failed to get user profile");
                    return null;
                }
            } catch (IOException e) {
                Logger.getInstance().error("Error getting user profile", e);
                return null;
            }
        });
    }
    
    /**
     * Update user profile on server
     */
    public CompletableFuture<Boolean> updateUserProfile(UserProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected) {
                return false;
            }
            
            try {
                NetworkProtocol.Message request = new NetworkProtocol.Message(NetworkProtocol.MessageType.UPDATE_USER_PROFILE, macAddress);
                request.data = NetworkProtocol.gson.toJson(profile);
                sendMessage(request);
                
                String response = reader.readLine();
                NetworkProtocol.Message responseMessage = NetworkProtocol.deserialize(response);
                
                return responseMessage != null && responseMessage.type == NetworkProtocol.MessageType.USER_PROFILE_RESPONSE;
            } catch (IOException e) {
                Logger.getInstance().error("Error updating user profile", e);
                return false;
            }
        });
    }
    
    /**
     * Get leaderboard from server
     */
    public CompletableFuture<LeaderboardData> getLeaderboard() {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected) {
                return null;
            }
            
            try {
                NetworkProtocol.Message request = new NetworkProtocol.Message(NetworkProtocol.MessageType.GET_LEADERBOARD, macAddress);
                sendMessage(request);
                
                String response = reader.readLine();
                NetworkProtocol.LeaderboardResponse leaderboardResponse = NetworkProtocol.deserialize(response, NetworkProtocol.LeaderboardResponse.class);
                
                if (leaderboardResponse != null) {
                    return leaderboardResponse.leaderboardData;
                } else {
                    Logger.getInstance().error("Failed to get leaderboard");
                    return null;
                }
            } catch (IOException e) {
                Logger.getInstance().error("Error getting leaderboard", e);
                return null;
            }
        });
    }
    
    /**
     * Add game record to server
     */
    public CompletableFuture<Boolean> addGameRecord(String levelName, LeaderboardData.PlayerRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            if (!connected) {
                return false;
            }
            
            try {
                NetworkProtocol.GameRecordData gameRecordData = new NetworkProtocol.GameRecordData(macAddress, levelName, record);
                sendMessage(gameRecordData);
                
                String response = reader.readLine();
                NetworkProtocol.Message responseMessage = NetworkProtocol.deserialize(response);
                
                return responseMessage != null && responseMessage.type == NetworkProtocol.MessageType.LEADERBOARD_RESPONSE;
            } catch (IOException e) {
                Logger.getInstance().error("Error adding game record", e);
                return false;
            }
        });
    }
    
    /**
     * Send message to server
     */
    private void sendMessage(NetworkProtocol.Message message) {
        String json = NetworkProtocol.serialize(message);
        writer.println(json);
    }
    
    /**
     * Check if connected to server
     */
    public boolean isConnected() {
        return connected;
    }
    
    /**
     * Get MAC address
     */
    public String getMacAddress() {
        return macAddress;
    }
}
