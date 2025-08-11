package dev.aminhashemi.blueprinthell.view.ui;

import dev.aminhashemi.blueprinthell.utils.AudioManager;
import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;
import java.awt.*;

/**
 * The settings screen for the game.
 * It allows the player to adjust game settings like volume.
 */
public class SettingsPanel extends JPanel {

    private final GameFrame gameFrame;

    public SettingsPanel(GameFrame gameFrame) {
        this.gameFrame = gameFrame;
        initUI();
    }

    private void initUI() {
        setName(GameFrame.SETTINGS_PANEL); // Set name for CardLayout
        setLayout(new GridBagLayout());
        setBackground(new Color(25, 25, 35));
        setPreferredSize(new Dimension(800, 600));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.insets = new Insets(15, 0, 15, 0);

        // --- Volume Control ---
        JLabel volumeLabel = new JLabel("Volume");
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        volumeLabel.setForeground(Color.WHITE);
        add(volumeLabel, gbc);

        JSlider volumeSlider = new JSlider(0, 100, 75); // Min, Max, Initial value
        volumeSlider.setPaintTicks(true);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setForeground(Color.WHITE);
        volumeSlider.setBackground(getBackground());
        volumeSlider.addChangeListener(e -> {
            // Convert slider value (0-100) to a float volume (0.0-1.0)
            float volume = volumeSlider.getValue() / 100.0f;
            AudioManager.getInstance().setVolume(volume);
        });
        add(volumeSlider, gbc);

        // --- Back Button ---
        JButton backButton = new JButton("Back to Main Menu");
        backButton.setFont(new Font("Arial", Font.PLAIN, 24));
        backButton.addActionListener(e -> gameFrame.switchToPanel(GameFrame.MAIN_MENU_PANEL));
        add(backButton, gbc);
    }
}
