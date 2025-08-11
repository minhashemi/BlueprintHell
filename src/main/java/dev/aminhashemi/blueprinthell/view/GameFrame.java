package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.view.ui.MainMenuPanel;

import javax.swing.*;
import java.awt.*;

/**
 * The main window (JFrame) of the game.
 * It uses a CardLayout to switch between different panels (e.g., MainMenu, GamePanel).
 * This class is the central hub for the UI.
 */
public class GameFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;

    // Constants for panel names to avoid magic strings
    public static final String MAIN_MENU_PANEL = "MainMenuPanel";
    public static final String GAME_PANEL = "GamePanel";

    public GameFrame() {
        // --- MODIFICATIONS FOR PROJECT REQUIREMENTS ---
        setUndecorated(true); // Removes the title bar (movable, closable, etc.)
        setResizable(false); // This was already correctly set

        // Basic window setup
        setTitle("Blueprint Hell"); // Title is not visible on undecorated frame, but good practice
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Use CardLayout to manage different screens (MainMenu, GamePanel, etc.)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create the panels
        MainMenuPanel mainMenuPanel = new MainMenuPanel(this); // Pass this frame to the menu
        GamePanel gamePanel = new GamePanel();

        // Add panels to the CardLayout with unique names
        // *** THIS LINE IS NOW FIXED ***
        mainPanel.add(mainMenuPanel, MAIN_MENU_PANEL);
        mainPanel.add(gamePanel, GAME_PANEL);

        // Add the main panel to the frame
        add(mainPanel);

        // Show the main menu first
        cardLayout.show(mainPanel, MAIN_MENU_PANEL);

        pack(); // Sizes the frame so all its contents are at or above their preferred sizes
        setLocationRelativeTo(null); // Center the window on the screen
        setVisible(true);
    }

    /**
     * Public method to allow other panels to switch the view.
     * @param panelName The name of the panel to show (e.g., GAME_PANEL).
     */
    public void switchToPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        // After switching, it's important to request focus for the new panel
        // so it can receive key events.
        Component componentToFocus = findComponentByName(mainPanel, panelName);
        if (componentToFocus != null) {
            componentToFocus.requestFocusInWindow();
        }
    }

    /**
     * Finds a component within a container by its name.
     * @param container The container to search in.
     * @param name The name of the component to find.
     * @return The found component, or null if not found.
     */
    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            // Check if the component's name is not null and matches
            if (name.equals(component.getName())) {
                return component;
            }
        }
        return null; // Return null if no component with the given name is found
    }
}
