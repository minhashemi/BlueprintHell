package dev.aminhashemi.blueprinthell.view.ui;

import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;
import java.awt.*;

/**
 * The main menu screen for the game.
 * It displays the primary navigation options to the player.
 */
public class MainMenuPanel extends JPanel {

    private final GameFrame gameFrame;

    public MainMenuPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        initUI();
    }

    private void initUI() {
        // *** ADD THIS LINE ***
        // Set the name for the CardLayout to find this panel
        setName(GameFrame.MAIN_MENU_PANEL);

        // Use GridBagLayout for flexible, centered component placement
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0); // Top and bottom padding

        // Set a dark, thematic background color
        setBackground(new Color(25, 25, 35));

        // Title Label
        JLabel titleLabel = new JLabel("BLUEPRINT HELL");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, gbc);

        // Create and add buttons
        add(createMenuButton("Start Game"), gbc);
        add(createMenuButton("Game Stages"), gbc);
        add(createMenuButton("Game Settings"), gbc);
        add(createMenuButton("Exit Game"), gbc);

        // Set preferred size for the panel
        setPreferredSize(new Dimension(800, 600));
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 24));
        button.setFocusable(false); // Prevents the dotted border around the text
        button.addActionListener(e -> handleButtonClick(text));
        return button;
    }

    private void handleButtonClick(String buttonText) {
        switch (buttonText) {
            case "Start Game":
                // Tell the GameFrame to switch to the GamePanel
                gameFrame.switchToPanel(GameFrame.GAME_PANEL);
                break;
            case "Game Stages":
                // Placeholder for now
                JOptionPane.showMessageDialog(this, "Game Stages not implemented yet.");
                break;
            case "Game Settings":
                // Placeholder for now
                JOptionPane.showMessageDialog(this, "Game Settings not implemented yet.");
                break;
            case "Exit Game":
                // Exit the application
                System.exit(0);
                break;
        }
    }
}
