package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.utils.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        JLabel titleLabel = new JLabel(won ? "🎉 LEVEL COMPLETED! 🎉" : "💀 LEVEL FAILED 💀");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(won ? Color.GREEN : Color.RED);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel);
        
        // Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(new Color(40, 40, 40));
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Packet statistics
        JLabel packetsLabel = new JLabel(String.format("Packets Released: %d", packetsReleased));
        packetsLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        packetsLabel.setForeground(Color.WHITE);
        packetsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(packetsLabel);
        
        JLabel returnedLabel = new JLabel(String.format("Packets Reached Destination: %d", packetsReturned));
        returnedLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        returnedLabel.setForeground(Color.GREEN);
        returnedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(returnedLabel);
        
        int packetsLost = packetsReleased - packetsReturned;
        JLabel lostLabel = new JLabel(String.format("Packets Lost/Destroyed: %d", packetsLost));
        lostLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        lostLabel.setForeground(Color.RED);
        lostLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(lostLabel);
        
        JLabel lossLabel = new JLabel(String.format("Packet Loss: %.1f%%", packetLossPercentage));
        lossLabel.setFont(new Font("Arial", Font.BOLD, 16));
        lossLabel.setForeground(packetLossPercentage < 50.0 ? Color.GREEN : Color.RED);
        lossLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        lossLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        resultsPanel.add(lossLabel);
        
        // Target requirement
        JLabel targetLabel = new JLabel("Target: < 50% packet loss");
        targetLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        targetLabel.setForeground(Color.LIGHT_GRAY);
        targetLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultsPanel.add(targetLabel);
        
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
            // Lose buttons
            JButton resetButton = new JButton("Reset Level");
            resetButton.setFont(new Font("Arial", Font.BOLD, 14));
            resetButton.setBackground(new Color(200, 0, 0));
            resetButton.setForeground(Color.WHITE);
            resetButton.setFocusPainted(false);
            resetButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            resetButton.addActionListener(e -> {
                resetLevel = true;
                dispose();
            });
            
            JButton closeButton = new JButton("Close");
            closeButton.setFont(new Font("Arial", Font.BOLD, 14));
            closeButton.setBackground(new Color(100, 100, 100));
            closeButton.setForeground(Color.WHITE);
            closeButton.setFocusPainted(false);
            closeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            closeButton.addActionListener(e -> {
                closed = true;
                dispose();
            });
            
            buttonPanel.add(resetButton);
            buttonPanel.add(Box.createHorizontalStrut(10));
            buttonPanel.add(closeButton);
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
