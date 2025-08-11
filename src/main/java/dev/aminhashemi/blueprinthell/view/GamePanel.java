package dev.aminhashemi.blueprinthell.view;

import javax.swing.*;
import java.awt.*;

/**
 * The panel where the main game is rendered and played.
 * This will eventually contain the game loop and drawing logic.
 */
public class GamePanel extends JPanel {

    public GamePanel() {
        initPanel();
    }

    private void initPanel() {
        // Set the name for CardLayout
        setName(GameFrame.GAME_PANEL);

        setPreferredSize(new Dimension(1280, 720));
        setBackground(Color.BLACK);
        setFocusable(true); // This is crucial for the panel to receive keyboard input
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // In the future, all game entities will be drawn here.
        // For now, let's just draw a placeholder message.

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        String message = "Game Screen - Entities will be drawn here!";
        int stringWidth = g2d.getFontMetrics().stringWidth(message);
        g2d.drawString(message, (getWidth() - stringWidth) / 2, getHeight() / 2);
    }
}
