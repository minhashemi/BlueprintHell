package dev.aminhashemi.blueprinthell.view.ui;

import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {

    private final GameFrame gameFrame;

    public MainMenuPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        initUI();
    }

    private void initUI() {
        setName(GameFrame.MAIN_MENU_PANEL);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);
        setBackground(new Color(25, 25, 35));

        JLabel titleLabel = new JLabel("BLUEPRINT HELL");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, gbc);

        add(createMenuButton("Start Game"), gbc);
        add(createMenuButton("Game Stages"), gbc);
        add(createMenuButton("Game Settings"), gbc);
        add(createMenuButton("Exit Game"), gbc);

        setPreferredSize(new Dimension(800, 600));
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 24));
        button.setFocusable(false);
        button.addActionListener(e -> handleButtonClick(text));
        return button;
    }

    private void handleButtonClick(String buttonText) {
        switch (buttonText) {
            case "Start Game":
                gameFrame.switchToPanel(GameFrame.GAME_PANEL);
                break;
            case "Game Stages":
                JOptionPane.showMessageDialog(this, "Game Stages not implemented yet.");
                break;
            case "Game Settings":
                // This now switches to the settings panel
                gameFrame.switchToPanel(GameFrame.SETTINGS_PANEL);
                break;
            case "Exit Game":
                System.exit(0);
                break;
        }
    }
}
