package me.minhashemi.view.overlay;

import me.minhashemi.view.GameScreen;
import me.minhashemi.view.Window;

import javax.swing.*;
import java.awt.*;

public class GameOverOverlay extends JPanel {
    public GameOverOverlay(GameScreen gameScreen, String message, int lostPackets, int totalPackets, int temporalProgress) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBounds(0, 0, gameScreen.getWidth(), gameScreen.getHeight());

        JPanel gameOverPanel = new JPanel();
        gameOverPanel.setLayout(new BoxLayout(gameOverPanel, BoxLayout.Y_AXIS));
        gameOverPanel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black
        gameOverPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        // Message label
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Stats label
        int healthyPackets = totalPackets - lostPackets;
        JLabel statsLabel = new JLabel(
                "<html>Healthy Packets: " + healthyPackets + "<br>" +
                        "Lost Packets: " + lostPackets + "/" + totalPackets + "<br>" +
                        "Time: " + temporalProgress + "</html>",
                SwingConstants.CENTER
        );
        statsLabel.setForeground(Color.WHITE);
        statsLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Retry button
        JButton retryButton = new JButton("Retry");
        retryButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        retryButton.setFont(new Font("Arial", Font.BOLD, 14));
        retryButton.addActionListener(e -> {
            Container topFrame = SwingUtilities.getWindowAncestor(gameScreen);
            if (topFrame instanceof Window window) {
                window.restartGame();
            }
        });

        // Menu button
        JButton menuButton = new JButton("Menu");
        menuButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuButton.setFont(new Font("Arial", Font.BOLD, 14));
        menuButton.addActionListener(e -> {
            Container topFrame = SwingUtilities.getWindowAncestor(gameScreen);
            if (topFrame instanceof Window window) {
                window.quitToMenuFromGame();
            }
        });

        // Assemble the panel
        gameOverPanel.add(Box.createVerticalGlue());
        gameOverPanel.add(messageLabel);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gameOverPanel.add(statsLabel);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        gameOverPanel.add(retryButton);
        gameOverPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        gameOverPanel.add(menuButton);
        gameOverPanel.add(Box.createVerticalGlue());

        add(gameOverPanel, new GridBagConstraints());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 100)); // Semi-transparent background
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}