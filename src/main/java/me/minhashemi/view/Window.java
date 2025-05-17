package me.minhashemi.view;

import me.minhashemi.model.Config;
import me.minhashemi.model.level.LevelData;
import me.minhashemi.model.level.LevelLoader;
import me.minhashemi.controller.audio.player;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import static me.minhashemi.controller.audio.player.playMusic;
import static me.minhashemi.controller.audio.player.stopMusic;

public class Window extends JFrame {

    private JPanel mainPanel;

    public Window() {
        super("BluePrint Hell");
        initUI();
        if (Config.isMusicOn)
            playMusic("theme");
    }

    private void initUI() {
        mainPanel = new JPanel(new CardLayout());
        mainPanel.add(createMainMenu(), "MainMenu");
        mainPanel.add(createSettingsMenu(), "Settings");
        mainPanel.add(createStageMenu(), "Stages");

        add(mainPanel);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(Config.WIDTH, Config.HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel createMainMenu() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JButton continueButton = new JButton("Start");
        continueButton.addActionListener(e -> handleContinue());

        JButton stagesButton = new JButton("Levels");
        stagesButton.addActionListener(e -> showCard("Stages"));

        JButton settingsButton = new JButton("Game Settings");
        settingsButton.addActionListener(e -> showCard("Settings"));

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> System.exit(0));

        gbc.gridy = 0;
        panel.add(continueButton, gbc);
        gbc.gridy = 1;
        panel.add(stagesButton, gbc);
        gbc.gridy = 2;
        panel.add(settingsButton, gbc);
        gbc.gridy = 3;
        panel.add(quitButton, gbc);

        return panel;
    }

    private JPanel createSettingsMenu() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JCheckBox musicToggle = new JCheckBox("Theme Song", Config.isMusicOn);
        musicToggle.addActionListener(e -> {
            Config.isMusicOn = musicToggle.isSelected();
            if (Config.isMusicOn) playMusic("theme");
            else stopMusic();
        });

        JCheckBox recordToggle = new JCheckBox("Record time?", Config.recordTime);
        recordToggle.addActionListener(e -> Config.recordTime = recordToggle.isSelected());

        JButton backButton = new JButton("Return");
        backButton.addActionListener(e -> showCard("MainMenu"));

        panel.add(musicToggle);
        panel.add(recordToggle);
        panel.add(backButton);
        return panel;
    }

    private JPanel createStageMenu() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        for (int i = 1; i <= 5; i++) {
            JButton stageButton = new JButton("Level " + i);
            int finalI = i;
            stageButton.addActionListener(e -> startStage(finalI));
            panel.add(stageButton);
        }

        JButton backButton = new JButton("Return");
        backButton.addActionListener(e -> showCard("MainMenu"));
        panel.add(backButton);

        return panel;
    }

    private void handleContinue() {
//        String playerName = JOptionPane.showInputDialog(this, "Enter Your name: ");
//        if (playerName != null && !playerName.trim().isEmpty()) {
//            int lastStage = Config.lastPlayedStage;
//            startStage(lastStage);
//        }
        startStage(Config.lastPlayedStage);
    }

    private void startStage(int stageNumber) {
        LevelData levelData = LevelLoader.loadLevel(stageNumber);

        // Remove previous content
        getContentPane().removeAll();

        // Set to full screen
        dispose(); // Remove decorations before going fullscreen
        setUndecorated(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            gd.setFullScreenWindow(this);
        } else {
            // fallback if fullscreen is not supported
            setSize(Config.WIDTH, Config.HEIGHT);
            setLocationRelativeTo(null);
            setVisible(true);
        }

        // Add new game screen
        GameScreen gameScreen = new GameScreen(levelData);
        setContentPane(gameScreen);

        revalidate();
        repaint();
    }



    private void showCard(String name) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, name);
    }



    public void quitToMenuFromGame() {
        // Exit full-screen if active
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.getFullScreenWindow() == this) {
            gd.setFullScreenWindow(null);
        }

        // Remove decorations and reset window
        dispose();  // allow setting decorations again
        setUndecorated(false);
        setResizable(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Re-add main panel (menus)
        setContentPane(mainPanel);

        pack(); // recompute layout
        setSize(Config.WIDTH, Config.HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);

        // Go to main menu screen
        showCard("MainMenu");
    }

}
