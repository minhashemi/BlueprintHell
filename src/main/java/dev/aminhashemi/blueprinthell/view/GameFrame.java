package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.view.ui.MainMenuPanel;
import dev.aminhashemi.blueprinthell.view.ui.SettingsPanel;
import dev.aminhashemi.blueprinthell.view.ui.LeaderboardPanel;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window that manages different UI panels.
 * Uses CardLayout to switch between main menu, game, and settings screens.
 */
public class GameFrame extends JFrame {

    // ==================== UI COMPONENTS ====================
    private final CardLayout cardLayout;    // Layout manager for panel switching
    private final JPanel mainPanel;        // Container for all panels
    private GameEngine gameEngine;         // Core game engine instance
    private GamePanel gamePanel;           // Game panel reference
    private LeaderboardPanel leaderboardPanel; // Leaderboard panel reference

    // ==================== PANEL CONSTANTS ====================
    public static final String MAIN_MENU_PANEL = "MainMenuPanel";
    public static final String GAME_PANEL = "GamePanel";
    public static final String SETTINGS_PANEL = "SettingsPanel";
    public static final String LEADERBOARD_PANEL = "LeaderboardPanel";

    /**
     * Constructs the main game window and initializes all UI panels.
     * Sets up the game engine and establishes connections between components.
     */
    public GameFrame() {
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create UI panels
        MainMenuPanel mainMenuPanel = new MainMenuPanel(this);
        this.gamePanel = new GamePanel();
        SettingsPanel settingsPanel = new SettingsPanel(this);
        this.leaderboardPanel = new LeaderboardPanel(this);

        // Create and link game engine
        gameEngine = new GameEngine(gamePanel);
        gamePanel.setGameEngine(gameEngine);
        gameEngine.setGameFrame(this);

        // Add panels to main container
        mainPanel.add(mainMenuPanel, MAIN_MENU_PANEL);
        mainPanel.add(gamePanel, GAME_PANEL);
        mainPanel.add(settingsPanel, SETTINGS_PANEL);
        mainPanel.add(leaderboardPanel, LEADERBOARD_PANEL);

        add(mainPanel);
        cardLayout.show(mainPanel, MAIN_MENU_PANEL);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Switches to the specified panel and starts game loop if needed.
     * @param panelName The name of the panel to switch to
     */
    public void switchToPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        mainPanel.revalidate();
        mainPanel.repaint();

        // Start game loop when entering game panel
        if (panelName.equals(GAME_PANEL)) {
            gameEngine.startGameLoop();
        }
        
        // Reload leaderboard data when entering leaderboard panel
        if (panelName.equals(LEADERBOARD_PANEL)) {
            updateLeaderboard();
        }

        Component componentToFocus = findComponentByName(mainPanel, panelName);
        if (componentToFocus != null) {
            componentToFocus.requestFocusInWindow();
        }
    }

    private Component findComponentByName(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (name.equals(component.getName())) {
                return component;
            }
        }
        return null;
    }
    
    /**
     * Gets the leaderboard panel for external access
     */
    public LeaderboardPanel getLeaderboardPanel() {
        return leaderboardPanel;
    }
    
    /**
     * Updates the leaderboard panel with current game data
     */
    public void updateLeaderboard() {
        if (leaderboardPanel != null && gameEngine != null) {
            LeaderboardData leaderboardData = gameEngine.getLeaderboardData();
            if (leaderboardData != null) {
                // Update the leaderboard panel with the game engine's data
                leaderboardPanel.getLeaderboardData().levelRecords.putAll(leaderboardData.levelRecords);
                leaderboardPanel.getLeaderboardData().globalRecords.addAll(leaderboardData.globalRecords);
                leaderboardPanel.getLeaderboardData().currentPlayerStats = leaderboardData.currentPlayerStats;
                leaderboardPanel.updateTables();
                leaderboardPanel.updatePlayerStats();
            }
        }
    }
    
    /**
     * Gets the game panel instance
     * @return Game panel
     */
    public GamePanel getGamePanel() {
        return gamePanel;
    }
}
