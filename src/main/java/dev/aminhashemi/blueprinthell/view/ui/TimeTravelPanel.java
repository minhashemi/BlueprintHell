package dev.aminhashemi.blueprinthell.view.ui;

import dev.aminhashemi.blueprinthell.core.GameEngine;
import dev.aminhashemi.blueprinthell.core.TimeTravelManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Time Travel Control Panel - provides UI controls for temporal progress
 */
public class TimeTravelPanel extends JPanel {
    
    private final GameEngine gameEngine;
    private final TimeTravelManager timeTravelManager;
    
    // UI Components
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JLabel packetLossLabel;
    private JSlider timeSlider;
    private JButton playPauseButton;
    private JButton executeButton;
    private JButton exitButton;
    private JLabel speedLabel;
    private JSlider speedSlider;
    
    // Timer for updating UI
    private Timer updateTimer;
    
    public TimeTravelPanel(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
        this.timeTravelManager = gameEngine.getTimeTravelManager();
        
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        startUpdateTimer();
    }
    
    private void initializeComponents() {
        // Status label
        statusLabel = new JLabel("Time Travel Mode: OFF");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        statusLabel.setForeground(Color.WHITE);
        
        // Time label
        timeLabel = new JLabel("Time: 0ms / 0ms");
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        timeLabel.setForeground(Color.CYAN);
        
        // Packet loss label
        packetLossLabel = new JLabel("Packet Loss: 0.0%");
        packetLossLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        packetLossLabel.setForeground(Color.RED);
        
        // Time slider
        timeSlider = new JSlider(0, 100, 0);
        timeSlider.setMajorTickSpacing(20);
        timeSlider.setMinorTickSpacing(5);
        timeSlider.setPaintTicks(true);
        timeSlider.setPaintLabels(true);
        timeSlider.setEnabled(false);
        
        // Play/Pause button
        playPauseButton = new JButton("▶ Play");
        playPauseButton.setEnabled(false);
        
        // Execute button
        executeButton = new JButton("🚀 Execute Network");
        executeButton.setEnabled(false);
        
        // Exit button
        exitButton = new JButton("❌ Exit Time Travel");
        exitButton.setEnabled(false);
        
        // Speed controls
        speedLabel = new JLabel("Speed: 1.0x");
        speedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        speedLabel.setForeground(Color.YELLOW);
        
        speedSlider = new JSlider(1, 50, 10); // 0.1x to 5.0x speed
        speedSlider.setMajorTickSpacing(10);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedSlider.setEnabled(false);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        setBackground(new Color(0, 0, 0, 200));
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.CYAN, 2),
            "Time Travel Controls",
            0, 0,
            new Font("Arial", Font.BOLD, 14),
            Color.CYAN
        ));
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(0, 0, 0, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Status section
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        mainPanel.add(statusLabel, gbc);
        
        gbc.gridy = 1;
        mainPanel.add(timeLabel, gbc);
        
        gbc.gridy = 2;
        mainPanel.add(packetLossLabel, gbc);
        
        // Time slider
        gbc.gridy = 3; gbc.gridwidth = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(timeSlider, gbc);
        
        // Control buttons
        gbc.gridy = 4; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;
        gbc.gridx = 0;
        mainPanel.add(playPauseButton, gbc);
        
        gbc.gridx = 1;
        mainPanel.add(executeButton, gbc);
        
        gbc.gridx = 2;
        mainPanel.add(exitButton, gbc);
        
        // Speed controls
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 3;
        mainPanel.add(speedLabel, gbc);
        
        gbc.gridy = 6; gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(speedSlider, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Instructions panel
        JPanel instructionsPanel = new JPanel(new FlowLayout());
        instructionsPanel.setBackground(new Color(0, 0, 0, 0));
        
        JLabel instructions = new JLabel(
            "<html><center>" +
            "<b>Controls:</b><br>" +
            "T - Toggle Time Travel | R - Execute | P - Play/Pause<br>" +
            "← → - Seek Time | Space - Spawn Packets" +
            "</center></html>"
        );
        instructions.setFont(new Font("Arial", Font.PLAIN, 10));
        instructions.setForeground(Color.LIGHT_GRAY);
        instructionsPanel.add(instructions);
        
        add(instructionsPanel, BorderLayout.SOUTH);
    }
    
    private void setupEventHandlers() {
        // Play/Pause button
        playPauseButton.addActionListener(e -> {
            if (timeTravelManager.isTimeTravelMode()) {
                gameEngine.toggleTimeTravelPause();
                updatePlayPauseButton();
            }
        });
        
        // Execute button
        executeButton.addActionListener(e -> {
            if (timeTravelManager.isTimeTravelMode()) {
                gameEngine.startNetworkExecution();
                updateExecuteButton();
            }
        });
        
        // Exit button
        exitButton.addActionListener(e -> {
            gameEngine.exitTimeTravelMode();
            updateAllComponents();
        });
        
        // Time slider
        timeSlider.addChangeListener(e -> {
            if (!timeSlider.getValueIsAdjusting() && timeTravelManager.isTimeTravelMode()) {
                long maxTime = timeTravelManager.getMaxTime();
                if (maxTime > 0) {
                    long targetTime = (long) ((timeSlider.getValue() / 100.0) * maxTime);
                    gameEngine.seekToTime(targetTime);
                }
            }
        });
        
        // Speed slider
        speedSlider.addChangeListener(e -> {
            if (!speedSlider.getValueIsAdjusting() && timeTravelManager.isTimeTravelMode()) {
                double speed = speedSlider.getValue() / 10.0;
                gameEngine.setPlaybackSpeed(speed);
                speedLabel.setText("Speed: " + String.format("%.1f", speed) + "x");
            }
        });
    }
    
    private void startUpdateTimer() {
        updateTimer = new Timer(100, e -> updateAllComponents()); // Update every 100ms
        updateTimer.start();
    }
    
    private void updateAllComponents() {
        boolean isTimeTravelMode = timeTravelManager.isTimeTravelMode();
        boolean isExecuting = timeTravelManager.isExecuting();
        boolean isPaused = timeTravelManager.isPaused();
        
        // Update status
        if (isTimeTravelMode) {
            statusLabel.setText("Time Travel Mode: ON");
            statusLabel.setForeground(Color.GREEN);
        } else {
            statusLabel.setText("Time Travel Mode: OFF");
            statusLabel.setForeground(Color.WHITE);
        }
        
        // Update time display
        long currentTime = timeTravelManager.getCurrentTime();
        long maxTime = timeTravelManager.getMaxTime();
        timeLabel.setText("Time: " + currentTime + "ms / " + maxTime + "ms");
        
        // Update packet loss
        double packetLoss = timeTravelManager.getPacketLossPercentage();
        packetLossLabel.setText("Packet Loss: " + String.format("%.1f", packetLoss) + "%");
        if (packetLoss < 50.0) {
            packetLossLabel.setForeground(Color.GREEN);
        } else {
            packetLossLabel.setForeground(Color.RED);
        }
        
        // Update time slider
        timeSlider.setEnabled(isTimeTravelMode);
        if (maxTime > 0) {
            int sliderValue = (int) ((currentTime * 100) / maxTime);
            timeSlider.setValue(sliderValue);
        } else {
            timeSlider.setValue(0);
        }
        
        // Update buttons
        playPauseButton.setEnabled(isTimeTravelMode);
        executeButton.setEnabled(isTimeTravelMode && !isExecuting);
        exitButton.setEnabled(isTimeTravelMode);
        speedSlider.setEnabled(isTimeTravelMode);
        
        updatePlayPauseButton();
        updateExecuteButton();
    }
    
    private void updatePlayPauseButton() {
        if (timeTravelManager.isPaused()) {
            playPauseButton.setText("▶ Play");
        } else {
            playPauseButton.setText("⏸ Pause");
        }
    }
    
    private void updateExecuteButton() {
        if (timeTravelManager.isExecuting()) {
            executeButton.setText("🔄 Executing...");
            executeButton.setEnabled(false);
        } else {
            executeButton.setText("🚀 Execute Network");
            executeButton.setEnabled(timeTravelManager.isTimeTravelMode());
        }
    }
    
    public void dispose() {
        if (updateTimer != null) {
            updateTimer.stop();
        }
    }
}
