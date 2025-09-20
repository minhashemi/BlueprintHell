package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.network.SimpleNetworkManager;
import dev.aminhashemi.blueprinthell.utils.AudioManager;
import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;

/**
 * Network-enabled version of Main that adds client-server support
 * without changing the original game logic
 */
public class NetworkMain {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0] : "offline";
        
        switch (mode.toLowerCase()) {
            case "server":
                runServerMode();
                break;
            case "client":
                runClientMode();
                break;
            case "offline":
            default:
                runOfflineMode();
                break;
        }
    }
    
    private static void runOfflineMode() {
        // Use the original Main class - no changes!
        SwingUtilities.invokeLater(() -> {
            AudioManager.getInstance().playBackgroundMusic("theme.wav");
            AudioManager.getInstance().setVolume(0.75f);
            new GameFrame(); // Launch main game window
        });
    }
    
    private static void runServerMode() {
        System.out.println("Starting Blueprint Hell Server...");
        
        // Start the new game server
        ServerMain.main(new String[]{});
    }
    
    private static void runClientMode() {
        System.out.println("Starting Blueprint Hell Client...");
        
        // Start network manager
        SimpleNetworkManager networkManager = new SimpleNetworkManager(false);
        if (!networkManager.start()) {
            System.err.println("Failed to connect to server, running offline");
            runOfflineMode();
            return;
        }
        
        // Start the game normally
        SwingUtilities.invokeLater(() -> {
            AudioManager.getInstance().playBackgroundMusic("theme.wav");
            AudioManager.getInstance().setVolume(0.75f);
            new GameFrame(); // Launch main game window
        });
    }
}
