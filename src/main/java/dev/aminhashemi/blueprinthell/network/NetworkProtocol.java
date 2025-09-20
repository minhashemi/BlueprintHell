package dev.aminhashemi.blueprinthell.network;

import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Network protocol for client-server communication
 */
public class NetworkProtocol {
    
    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Message types for client-server communication
     */
    public enum MessageType {
        // Authentication
        AUTH_REQUEST,
        AUTH_RESPONSE,
        
        // User data
        GET_USER_PROFILE,
        UPDATE_USER_PROFILE,
        USER_PROFILE_RESPONSE,
        
        // Leaderboard
        GET_LEADERBOARD,
        LEADERBOARD_RESPONSE,
        ADD_GAME_RECORD,
        
        // Game data
        GET_GAME_DATA,
        GAME_DATA_RESPONSE,
        
        // Error
        ERROR_RESPONSE
    }
    
    /**
     * Base message class
     */
    public static class Message {
        public MessageType type;
        public String macAddress;
        public long timestamp;
        public String data;
        
        public Message() {
            this.timestamp = System.currentTimeMillis();
        }
        
        public Message(MessageType type, String macAddress) {
            this();
            this.type = type;
            this.macAddress = macAddress;
        }
    }
    
    /**
     * Authentication request
     */
    public static class AuthRequest extends Message {
        public String username;
        
        public AuthRequest(String macAddress, String username) {
            super(MessageType.AUTH_REQUEST, macAddress);
            this.username = username;
        }
    }
    
    /**
     * Authentication response
     */
    public static class AuthResponse extends Message {
        public boolean success;
        public String message;
        public UserProfile userProfile;
        
        public AuthResponse(String macAddress, boolean success, String message) {
            super(MessageType.AUTH_RESPONSE, macAddress);
            this.success = success;
            this.message = message;
        }
    }
    
    /**
     * User profile response
     */
    public static class UserProfileResponse extends Message {
        public UserProfile userProfile;
        
        public UserProfileResponse(String macAddress, UserProfile userProfile) {
            super(MessageType.USER_PROFILE_RESPONSE, macAddress);
            this.userProfile = userProfile;
        }
    }
    
    /**
     * Leaderboard response
     */
    public static class LeaderboardResponse extends Message {
        public LeaderboardData leaderboardData;
        
        public LeaderboardResponse(String macAddress, LeaderboardData leaderboardData) {
            super(MessageType.LEADERBOARD_RESPONSE, macAddress);
            this.leaderboardData = leaderboardData;
        }
    }
    
    /**
     * Game record data
     */
    public static class GameRecordData extends Message {
        public String levelName;
        public LeaderboardData.PlayerRecord record;
        
        public GameRecordData(String macAddress, String levelName, LeaderboardData.PlayerRecord record) {
            super(MessageType.ADD_GAME_RECORD, macAddress);
            this.levelName = levelName;
            this.record = record;
        }
    }
    
    /**
     * Error response
     */
    public static class ErrorResponse extends Message {
        public String errorMessage;
        public int errorCode;
        
        public ErrorResponse(String macAddress, String errorMessage, int errorCode) {
            super(MessageType.ERROR_RESPONSE, macAddress);
            this.errorMessage = errorMessage;
            this.errorCode = errorCode;
        }
    }
    
    /**
     * Serialize message to JSON
     */
    public static String serialize(Message message) {
        return gson.toJson(message);
    }
    
    /**
     * Deserialize JSON to message
     */
    public static Message deserialize(String json) {
        try {
            return gson.fromJson(json, Message.class);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Deserialize to specific message type
     */
    public static <T extends Message> T deserialize(String json, Class<T> clazz) {
        try {
            return gson.fromJson(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }
}
