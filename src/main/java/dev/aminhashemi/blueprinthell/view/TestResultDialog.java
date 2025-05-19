package dev.aminhashemi.blueprinthell.view;

import javax.swing.*;
import java.awt.*;

/**
 * Dialog shown after test completion showing win/lose result
 * Provides options to reset level or proceed to next level
 */
public class TestResultDialog extends JDialog {
    private boolean resetLevel = false;
    private boolean nextLevel = false;
    private boolean closed = false;
    
    public TestResultDialog(Frame parent, boolean won, double packetLossPercentage, int packetsReleased, int packetsReturned) {
        super(parent, "Test Results", true);
        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        
        setupUI(won, packetLossPercentage, packetsReleased, packetsReturned);
        centerOnParent(parent);
    }
    
    private void setupUI(boolean won, double packetLossPercentage, int packetsReleased, int packetsReturned) {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30));
        
        // Title
        String titleText = won ? "🎉 LEVEL COMPLETED! 🎉" : "💀 GAME OVER 💀";
        JLabel titleLabel = new JLabel(titleText);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(won ? Color.GREEN : Color.RED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel);
        
        // Game over explanation
        if (!won) {
            JLabel explanationLabel = new JLabel("Network is defective - Packet Loss exceeded 50%");
            explanationLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            explanationLabel.setForeground(Color.ORANGE);
            explanationLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            explanationLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            mainPanel.add(explanationLabel);
        }
        
        // Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(new Color(40, 40, 40));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Packet statistics - Enhanced for better clarity
        int packetsLost = packetsReleased - packetsReturned;
        int healthyPackets = packetsReturned; // Packets that successfully reached destination
        
        // Total packets released
        JLabel totalLabel = new JLabel(String.format("Total Packets Released: %d", packetsReleased));
        totalLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalLabel.setForeground(Color.WHITE);
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(totalLabel);
        
        // Healthy packets (successfully delivered)
        JLabel healthyLabel = new JLabel(String.format("✅ Healthy Packets in Network: %d", healthyPackets));
        healthyLabel.setFont(new Font("Arial", Font.BOLD, 16));
        healthyLabel.setForeground(Color.GREEN);
        healthyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        healthyLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        resultsPanel.add(healthyLabel);
        
        // Lost packets
        JLabel lostLabel = new JLabel(String.format("❌ Packets Lost/Destroyed: %d", packetsLost));
        lostLabel.setFont(new Font("Arial", Font.BOLD, 14));
        lostLabel.setForeground(Color.RED);
        lostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(lostLabel);
        
        // Packet loss percentage - prominently displayed
        JLabel lossLabel = new JLabel(String.format("📊 Packet Loss: %.1f%%", packetLossPercentage));
        lossLabel.setFont(new Font("Arial", Font.BOLD, 18));
        lossLabel.setForeground(packetLossPercentage < 50.0 ? Color.GREEN : Color.RED);
        lossLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lossLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        resultsPanel.add(lossLabel);
        
        // Target requirement
        JLabel targetLabel = new JLabel("🎯 Target: < 50% packet loss");
        targetLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        targetLabel.setForeground(Color.LIGHT_GRAY);
        targetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(targetLabel);
        
        // Additional info for game over
        if (!won) {
            JLabel resetInfoLabel = new JLabel("Level will reset completely (except Skill Tree)");
            resetInfoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
            resetInfoLabel.setForeground(Color.YELLOW);
            resetInfoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            resetInfoLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
            resultsPanel.add(resetInfoLabel);
        }
        
        mainPanel.add(resultsPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(30, 30, 30));
        
        if (won) {
            // Win buttons
            JButton nextLevelButton = new JButton("Next Level →");
            nextLevelButton.setFont(new Font("Arial", Font.BOLD, 14));
            nextLevelButton.setBackground(new Color(0, 150, 0));
            nextLevelButton.setForeground(Color.WHITE);
            nextLevelButton.setFocusPainted(false);
            nextLevelButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            nextLevelButton.addActionListener(e -> {
                nextLevel = true;
                dispose();
            });
            
            JButton resetButton = new JButton("Reset Level");
            resetButton.setFont(new Font("Arial", Font.BOLD, 14));
            resetButton.setBackground(new Color(100, 100, 100));
            resetButton.setForeground(Color.WHITE);
            resetButton.setFocusPainted(false);
            resetButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            resetButton.addActionListener(e -> {
                resetLevel = true;
                dispose();
            });
            
            buttonPanel.add(nextLevelButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            buttonPanel.add(resetButton);
        } else {
            // Game Over buttons
            JButton resetButton = new JButton("🔄 Reset Level");
            resetButton.setFont(new Font("Arial", Font.BOLD, 14));
            resetButton.setBackground(new Color(200, 0, 0));
            resetButton.setForeground(Color.WHITE);
            resetButton.setFocusPainted(false);
            resetButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            resetButton.setToolTipText("Reset the level completely (except Skill Tree)");
            resetButton.addActionListener(e -> {
                resetLevel = true;
                dispose();
            });
            
            JButton menuButton = new JButton("🏠 Main Menu");
            menuButton.setFont(new Font("Arial", Font.BOLD, 14));
            menuButton.setBackground(new Color(100, 100, 100));
            menuButton.setForeground(Color.WHITE);
            menuButton.setFocusPainted(false);
            menuButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            menuButton.setToolTipText("Return to main menu");
            menuButton.addActionListener(e -> {
                closed = true;
                dispose();
            });
            
            buttonPanel.add(resetButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            buttonPanel.add(menuButton);
        }
        
        mainPanel.add(buttonPanel);
        
        add(mainPanel, BorderLayout.CENTER);
        pack();
    }
    
    private void centerOnParent(Frame parent) {
        if (parent != null) {
            setLocationRelativeTo(parent);
        } else {
            setLocationRelativeTo(null);
        }
    }
    
    public boolean shouldResetLevel() {
        return resetLevel;
    }
    
    public boolean shouldGoToNextLevel() {
        return nextLevel;
    }
    
    public boolean wasClosed() {
        return closed;
    }
    
    /**
     * Shows the dialog and returns the user's choice
     */
    public static TestResult showDialog(Frame parent, boolean won, double packetLossPercentage, int packetsReleased, int packetsReturned) {
        TestResultDialog dialog = new TestResultDialog(parent, won, packetLossPercentage, packetsReleased, packetsReturned);
        dialog.setVisible(true);
        
        if (dialog.shouldResetLevel()) {
            return TestResult.RESET_LEVEL;
        } else if (dialog.shouldGoToNextLevel()) {
            return TestResult.NEXT_LEVEL;
        } else {
            return TestResult.CLOSE;
        }
    }
    
    public enum TestResult {
        RESET_LEVEL,
        NEXT_LEVEL,
        CLOSE
    }
}
