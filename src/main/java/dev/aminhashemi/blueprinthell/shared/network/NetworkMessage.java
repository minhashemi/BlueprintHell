package dev.aminhashemi.blueprinthell.shared.network;

import dev.aminhashemi.blueprinthell.model.SaveData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Network message wrapper for client-server communication.
 * Supports different message types and reuses existing SaveData serialization.
 */
public class NetworkMessage {
    
    public enum MessageType {
        // Client to Server
        PLAYER_INPUT,
        MOUSE_CLICK,
        MOUSE_MOVE,
        MOUSE_DRAG,
        KEYBOARD_INPUT,
        SYSTEM_PLACEMENT,
        WIRE_CREATION,
        SHOP_PURCHASE,
        GAME_COMMAND,
        HEARTBEAT,
        
        // Server to Client
        GAME_STATE_UPDATE,
        PACKET_MOVEMENT_UPDATE,
        SYSTEM_UPDATE,
        WIRE_UPDATE,
        IMPACT_EFFECT,
        GAME_EVENT,
        CONNECTION_ACK,
        ERROR_MESSAGE
    }
    
    public MessageType type;
    public String clientId;
    public long timestamp;
    public String data; // JSON string containing SaveData or specific data
    public int sequenceNumber; // For UDP ordering
    public boolean requiresAck; // For TCP reliability
    
    public NetworkMessage() {
        this.timestamp = System.currentTimeMillis();
        this.sequenceNumber = 0;
        this.requiresAck = false;
    }
    
    public NetworkMessage(MessageType type, String clientId) {
        this();
        this.type = type;
        this.clientId = clientId;
    }
    
    public NetworkMessage(MessageType type, String clientId, SaveData saveData) {
        this(type, clientId);
        this.data = saveDataToJson(saveData);
    }
    
    public NetworkMessage(MessageType type, String clientId, String data) {
        this(type, clientId);
        this.data = data;
    }
    
    public static String saveDataToJson(SaveData saveData) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(saveData);
    }
    
    public static SaveData jsonToSaveData(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, SaveData.class);
    }
    
    public String toJson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }
    
    public static NetworkMessage fromJson(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.fromJson(json, NetworkMessage.class);
    }
    
    public SaveData getSaveData() {
        if (data != null && !data.isEmpty()) {
            return jsonToSaveData(data);
        }
        return null;
    }
    
    public void setSaveData(SaveData saveData) {
        this.data = saveDataToJson(saveData);
    }
}
