package dev.aminhashemi.blueprinthell.test;

import dev.aminhashemi.blueprinthell.shared.network.*;
import dev.aminhashemi.blueprinthell.shared.manager.GameManager;
import dev.aminhashemi.blueprinthell.view.GamePanel;

import javax.swing.*;

/**
 * Simple test class to verify the client-server architecture works
 */
public class NetworkTest {
    
    public static void main(String[] args) {
        System.out.println("=== Blueprint Hell Network Architecture Test ===");
        
        // Test 1: Network Message
        testNetworkMessage();
        
        // Test 2: Client ID Generation
        testClientId();
        
        // Test 3: Game Manager Initialization
        testGameManager();
        
        System.out.println("=== All tests completed ===");
    }
    
    private static void testNetworkMessage() {
        System.out.println("Testing NetworkMessage...");
        
        try {
            NetworkMessage message = new NetworkMessage(
                NetworkMessage.MessageType.PLAYER_INPUT, 
                "test-client"
            );
            
            String json = message.toJson();
            NetworkMessage parsed = NetworkMessage.fromJson(json);
            
            if (message.type == parsed.type && message.clientId.equals(parsed.clientId)) {
                System.out.println("✅ NetworkMessage test passed");
            } else {
                System.out.println("❌ NetworkMessage test failed");
            }
        } catch (Exception e) {
            System.out.println("❌ NetworkMessage test failed: " + e.getMessage());
        }
    }
    
    private static void testClientId() {
        System.out.println("Testing ClientId...");
        
        try {
            ClientId clientId1 = new ClientId();
            ClientId clientId2 = new ClientId();
            
            if (!clientId1.getId().equals(clientId2.getId())) {
                System.out.println("✅ ClientId test passed - IDs are unique");
            } else {
                System.out.println("❌ ClientId test failed - IDs are not unique");
            }
        } catch (Exception e) {
            System.out.println("❌ ClientId test failed: " + e.getMessage());
        }
    }
    
    private static void testGameManager() {
        System.out.println("Testing GameManager...");
        
        try {
            // Create a dummy GamePanel
            JFrame frame = new JFrame();
            GamePanel panel = new GamePanel();
            frame.add(panel);
            
            // Test offline mode initialization
            GameManager manager = new GameManager(GameManager.GameMode.OFFLINE, panel);
            
            if (manager.initialize()) {
                System.out.println("✅ GameManager offline mode initialization passed");
            } else {
                System.out.println("❌ GameManager offline mode initialization failed");
            }
            
            frame.dispose();
        } catch (Exception e) {
            System.out.println("❌ GameManager test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
