package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.utils.AudioManager;
import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;

/**
 * Main entry point for Blueprint Hell - a network simulation game
 * Initializes audio and creates the main game window
 */
public class Main {

    public static void main(String[] args) {
        // Initialize game on EDT for thread safety
        SwingUtilities.invokeLater(() -> {
            AudioManager.getInstance().playBackgroundMusic("theme.wav");
            AudioManager.getInstance().setVolume(0.75f);
            new GameFrame(); // Launch main game window
        });
    }
}
