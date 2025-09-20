package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.server.GameServer;
import dev.aminhashemi.blueprinthell.utils.Logger;

/**
 * Main class for running the game server
 */
public class ServerMain {
    public static void main(String[] args) {
        Logger.getInstance().info("Starting BlueprintHell Game Server...");
        
        GameServer server = new GameServer();
        
        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.getInstance().info("Shutting down server...");
            server.stop();
        }));
        
        try {
            server.start();
        } catch (Exception e) {
            Logger.getInstance().error("Server failed to start", e);
            System.exit(1);
        }
    }
}
