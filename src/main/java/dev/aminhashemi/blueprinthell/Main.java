package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.utils.AudioManager;
import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;

/**
 * The main entry point for the Blueprint Hell application.
 * Its sole responsibility is to create and start the main game window on the Event Dispatch Thread (EDT).
 */
public class Main {

    public static void main(String[] args) {
        // It's crucial to create and show Swing components on the Event Dispatch Thread (EDT)
        // to ensure thread safety.
        SwingUtilities.invokeLater(() -> {
            // Start the background music
            AudioManager.getInstance().playBackgroundMusic("theme.wav");
            // Set initial volume (e.g., 75%)
            AudioManager.getInstance().setVolume(0.75f);
            // Create the game frame
            new GameFrame();
        });
    }
}
