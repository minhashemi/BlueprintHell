package me.minhashemi.view.overlay;


import me.minhashemi.model.Config;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.model.level.LevelLoader;
import me.minhashemi.view.GameScreen;

import javax.swing.*;
import java.awt.*;

public class VictoryOverlay extends JPanel {
    public VictoryOverlay(GameScreen gameScreen, String message) {
        setLayout(new GridBagLayout());
        setOpaque(false);
        setBounds(0, 0, gameScreen.getWidth(), gameScreen.getHeight());

        JPanel victoryPanel = new JPanel();
        victoryPanel.setLayout(new BoxLayout(victoryPanel, BoxLayout.Y_AXIS));
        victoryPanel.setBackground(Color.DARK_GRAY);
        victoryPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 24));
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton nextLevelButton = new JButton("Next Level");
        nextLevelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextLevelButton.addActionListener(e -> {
            Container topFrame = SwingUtilities.getWindowAncestor(gameScreen);
            if (topFrame instanceof JFrame frame) {
                frame.getContentPane().removeAll();
                Config.lastPlayedStage++;
                LevelData nextLevel = LevelLoader.loadLevel(Config.lastPlayedStage);
                frame.setContentPane(new GameScreen(nextLevel));
                frame.revalidate();
                frame.repaint();
            }
        });

        JButton closeButton = new JButton("Close");
        closeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeButton.addActionListener(e -> {
            gameScreen.remove(this);
            gameScreen.revalidate();
            gameScreen.repaint();
        });

        victoryPanel.add(Box.createVerticalGlue());
        victoryPanel.add(messageLabel);
        victoryPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        victoryPanel.add(nextLevelButton);
        victoryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        victoryPanel.add(closeButton);
        victoryPanel.add(Box.createVerticalGlue());

        add(victoryPanel, new GridBagConstraints());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
