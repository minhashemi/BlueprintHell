package dev.aminhashemi.blueprinthell.view.ui;

import dev.aminhashemi.blueprinthell.view.GameFrame;
import dev.aminhashemi.blueprinthell.utils.PlayerManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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
        add(createMenuButton("Leaderboards"), gbc);
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
                showUsernameDialog();
                break;
            case "Leaderboards":
                gameFrame.switchToPanel(GameFrame.LEADERBOARD_PANEL);
                break;
            case "Game Settings":
                gameFrame.switchToPanel(GameFrame.SETTINGS_PANEL);
                break;
            case "Exit Game":
                System.exit(0);
                break;
        }
    }
    
    /**
     * Shows a dialog to get the player's username before starting the game
     */
    private void showUsernameDialog() {
        // Create a custom dialog for username input
        JDialog usernameDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Enter Your Name", true);
        usernameDialog.setLayout(new BorderLayout());
        usernameDialog.setSize(400, 200);
        usernameDialog.setLocationRelativeTo(this);
        usernameDialog.setResizable(false);
        
        // Main panel with dark theme
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(25, 25, 35));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title label
        JLabel titleLabel = new JLabel("Enter Your Name");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(new Color(25, 25, 35));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel nameLabel = new JLabel("Player Name:");
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        nameLabel.setForeground(Color.WHITE);
        
        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Arial", Font.PLAIN, 14));
        nameField.setBackground(new Color(40, 40, 50));
        nameField.setForeground(Color.WHITE);
        nameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        nameField.setText(PlayerManager.getInstance().getPlayerName());
        nameField.selectAll();
        
        inputPanel.add(nameLabel, BorderLayout.WEST);
        inputPanel.add(nameField, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(25, 25, 35));
        
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(70, 130, 180));
        startButton.setForeground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setPreferredSize(new Dimension(100, 35));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.PLAIN, 14));
        cancelButton.setBackground(new Color(60, 60, 70));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        
        buttonPanel.add(startButton);
        buttonPanel.add(cancelButton);
        
        // Add components to main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        usernameDialog.add(mainPanel);
        
        // Button actions
        startButton.addActionListener(e -> {
            String playerName = nameField.getText().trim();
            if (!playerName.isEmpty()) {
                PlayerManager.getInstance().setPlayerName(playerName);
                usernameDialog.dispose();
                gameFrame.switchToPanel(GameFrame.GAME_PANEL);
            } else {
                JOptionPane.showMessageDialog(usernameDialog, 
                    "Please enter a valid name!", 
                    "Invalid Name", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> usernameDialog.dispose());
        
        // Handle Enter key in text field
        nameField.addActionListener(e -> startButton.doClick());
        
        // Handle Escape key
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke("ESCAPE");
        Action escapeAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                usernameDialog.dispose();
            }
        };
        usernameDialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE");
        usernameDialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);
        
        // Focus on text field
        SwingUtilities.invokeLater(() -> nameField.requestFocusInWindow());
        
        usernameDialog.setVisible(true);
    }
}
