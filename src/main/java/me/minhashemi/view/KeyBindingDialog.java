package me.minhashemi.view;

import me.minhashemi.model.Config;
import me.minhashemi.utils.KeyBindingManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

/**
 * Dialog for changing key bindings
 */
public class KeyBindingDialog extends JDialog {
    private KeyBindingManager keyBindingManager;
    private Map<String, KeyStroke> currentBindings;
    private JLabel[] keyLabels;
    private JButton[] changeButtons;
    private String[] actions;
    private String waitingForAction = null;
    private JButton waitingButton = null;
    
    public KeyBindingDialog(Frame parent) {
        super(parent, "Key Bindings", true);
        this.keyBindingManager = KeyBindingManager.getInstance();
        this.currentBindings = keyBindingManager.getAllKeyBindings();
        
        initializeActions();
        setupUI();
        centerOnParent(parent);
    }
    
    private void initializeActions() {
        actions = new String[]{
            Config.SPAWN_PACKET_ACTION,
            Config.PAUSE_ACTION
        };
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setResizable(false);
        
        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(30, 30, 30));
        
        // Title
        JLabel titleLabel = new JLabel("Key Bindings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(titleLabel);
        
        // Instructions
        JLabel instructionLabel = new JLabel("Click on an action to change its key binding");
        instructionLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        instructionLabel.setForeground(Color.LIGHT_GRAY);
        instructionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        mainPanel.add(instructionLabel);
        
        // Key bindings panel
        JPanel bindingsPanel = new JPanel();
        bindingsPanel.setLayout(new GridBagLayout());
        bindingsPanel.setBackground(new Color(40, 40, 40));
        bindingsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        keyLabels = new JLabel[actions.length];
        changeButtons = new JButton[actions.length];
        
        for (int i = 0; i < actions.length; i++) {
            final int actionIndex = i; // Make final for lambda
            String action = actions[i];
            KeyStroke currentKey = currentBindings.get(action);
            
            // Action label
            JLabel actionLabel = new JLabel(action + ":");
            actionLabel.setFont(new Font("Arial", Font.BOLD, 14));
            actionLabel.setForeground(Color.WHITE);
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.WEST;
            bindingsPanel.add(actionLabel, gbc);
            
            // Current key label
            keyLabels[i] = new JLabel(keyBindingManager.getKeyDisplayString(currentKey));
            keyLabels[i].setFont(new Font("Arial", Font.PLAIN, 14));
            keyLabels[i].setForeground(Color.YELLOW);
            keyLabels[i].setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.CENTER;
            bindingsPanel.add(keyLabels[i], gbc);
            
            // Change button
            changeButtons[i] = new JButton("Change");
            changeButtons[i].setFont(new Font("Arial", Font.PLAIN, 12));
            changeButtons[i].setBackground(new Color(70, 130, 180));
            changeButtons[i].setForeground(Color.WHITE);
            changeButtons[i].setFocusPainted(false);
            changeButtons[i].setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            changeButtons[i].addActionListener(e -> startKeyChange(actionIndex));
            gbc.gridx = 2;
            gbc.anchor = GridBagConstraints.EAST;
            bindingsPanel.add(changeButtons[i], gbc);
        }
        
        mainPanel.add(bindingsPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(new Color(30, 30, 30));
        
        JButton resetButton = new JButton("Reset to Defaults");
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setBackground(new Color(220, 20, 60));
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        resetButton.addActionListener(e -> resetToDefaults());
        
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Arial", Font.BOLD, 14));
        closeButton.setBackground(new Color(100, 100, 100));
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel);
        add(mainPanel, BorderLayout.CENTER);
        
        // Set up key listener for the entire dialog
        setupKeyListener();
        
        pack();
    }
    
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (waitingForAction != null) {
                    // Don't process ESC when waiting for a key
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        cancelKeyChange();
                        return;
                    }
                    
                    KeyStroke newKeyStroke = KeyStroke.getKeyStrokeForEvent(e);
                    if (newKeyStroke != null) {
                        completeKeyChange(newKeyStroke);
                    }
                }
            }
        });
        setFocusable(true);
    }
    
    private void startKeyChange(int actionIndex) {
        waitingForAction = actions[actionIndex];
        waitingButton = changeButtons[actionIndex];
        
        // Update UI to show waiting state
        waitingButton.setText("Press Key...");
        waitingButton.setBackground(new Color(255, 165, 0)); // Orange
        waitingButton.setEnabled(false);
        
        // Update instruction
        JLabel instructionLabel = (JLabel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        instructionLabel.setText("Press a key for " + waitingForAction + " (ESC to cancel)");
        instructionLabel.setForeground(Color.ORANGE);
        
        // Focus the dialog to receive key events
        requestFocus();
    }
    
    private void completeKeyChange(KeyStroke newKeyStroke) {
        if (waitingForAction == null) return;
        
        // Check if key is already in use
        boolean success = keyBindingManager.setKeyBinding(waitingForAction, newKeyStroke);
        
        if (success) {
            // Update the display
            int actionIndex = getActionIndex(waitingForAction);
            if (actionIndex >= 0) {
                keyLabels[actionIndex].setText(keyBindingManager.getKeyDisplayString(newKeyStroke));
            }
            
            // Show success message
            JLabel instructionLabel = (JLabel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
            instructionLabel.setText("Key binding updated successfully!");
            instructionLabel.setForeground(Color.GREEN);
        } else {
            // Show error message
            JLabel instructionLabel = (JLabel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
            instructionLabel.setText("Key is already in use! Please choose a different key.");
            instructionLabel.setForeground(Color.RED);
        }
        
        // Reset waiting state
        waitingButton.setText("Change");
        waitingButton.setBackground(new Color(70, 130, 180));
        waitingButton.setEnabled(true);
        waitingForAction = null;
        waitingButton = null;
        
        // Reset instruction after a delay
        Timer timer = new Timer(2000, e -> {
            JLabel instructionLabel = (JLabel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
            instructionLabel.setText("Click on an action to change its key binding");
            instructionLabel.setForeground(Color.LIGHT_GRAY);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void cancelKeyChange() {
        if (waitingForAction == null) return;
        
        // Reset waiting state
        waitingButton.setText("Change");
        waitingButton.setBackground(new Color(70, 130, 180));
        waitingButton.setEnabled(true);
        waitingForAction = null;
        waitingButton = null;
        
        // Reset instruction
        JLabel instructionLabel = (JLabel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        instructionLabel.setText("Click on an action to change its key binding");
        instructionLabel.setForeground(Color.LIGHT_GRAY);
    }
    
    private int getActionIndex(String action) {
        for (int i = 0; i < actions.length; i++) {
            if (actions[i].equals(action)) {
                return i;
            }
        }
        return -1;
    }
    
    private void resetToDefaults() {
        keyBindingManager.resetToDefaults();
        currentBindings = keyBindingManager.getAllKeyBindings();
        
        // Update all displays
        for (int i = 0; i < actions.length; i++) {
            String action = actions[i];
            KeyStroke currentKey = currentBindings.get(action);
            keyLabels[i].setText(keyBindingManager.getKeyDisplayString(currentKey));
        }
        
        // Show success message
        JLabel instructionLabel = (JLabel) ((JPanel) getContentPane().getComponent(0)).getComponent(1);
        instructionLabel.setText("Key bindings reset to defaults!");
        instructionLabel.setForeground(Color.GREEN);
        
        // Reset message after a delay
        Timer timer = new Timer(2000, e -> {
            instructionLabel.setText("Click on an action to change its key binding");
            instructionLabel.setForeground(Color.LIGHT_GRAY);
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    private void centerOnParent(Component parent) {
        if (parent != null) {
            setLocationRelativeTo(parent);
        } else {
            setLocationRelativeTo(null);
        }
    }
    
    public static void showDialog(Component parent) {
        KeyBindingDialog dialog = new KeyBindingDialog(parent instanceof Frame ? (Frame) parent : null);
        dialog.setVisible(true);
    }
}
