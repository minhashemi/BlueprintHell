package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.core.GameEngine; // Import GameEngine
import dev.aminhashemi.blueprinthell.view.ui.MainMenuPanel;
import dev.aminhashemi.blueprinthell.view.ui.SettingsPanel;

import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private GameEngine gameEngine; // Add a reference to the engine

    public static final String MAIN_MENU_PANEL = "MainMenuPanel";
    public static final String GAME_PANEL = "GamePanel";
    public static final String SETTINGS_PANEL = "SettingsPanel";

    public GameFrame() {
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        MainMenuPanel mainMenuPanel = new MainMenuPanel(this);
        GamePanel gamePanel = new GamePanel();
        SettingsPanel settingsPanel = new SettingsPanel(this);

        // --- NEW: Create the GameEngine and link it to the GamePanel ---
        gameEngine = new GameEngine(gamePanel);
        gamePanel.setGameEngine(gameEngine);

        mainPanel.add(mainMenuPanel, MAIN_MENU_PANEL);
        mainPanel.add(gamePanel, GAME_PANEL);
        mainPanel.add(settingsPanel, SETTINGS_PANEL);

        add(mainPanel);
        cardLayout.show(mainPanel, MAIN_MENU_PANEL);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void switchToPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
        mainPanel.revalidate();
        mainPanel.repaint();

        // --- NEW: Start the game loop when switching to the game panel ---
        if (panelName.equals(GAME_PANEL)) {
            gameEngine.startGameLoop();
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
}
