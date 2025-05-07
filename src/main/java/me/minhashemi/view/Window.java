package me.minhashemi.view;

import me.minhashemi.model.Config;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Window extends JFrame {
    private Clip clip;
    private JPanel mainPanel;

    public Window() {
        super("BluePrint Hell");
        initUI();
        if (Config.isMusicOn) playMusic();
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
            if (Config.isMusicOn) playMusic();
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
        String playerName = JOptionPane.showInputDialog(this, "Enter Your name: ");
        if (playerName != null && !playerName.trim().isEmpty()) {
            int lastStage = Config.lastPlayedStage;
            startStage(lastStage);
        }
    }

//    private void startStage(int stageNumber) {
//        // Minimize all other windows
//        for (Frame frame : Frame.getFrames()) {
//            if (frame != this) frame.setState(Frame.ICONIFIED);
//        }
//
//        // Set frame properties as described
//        setResizable(false);
//        setUndecorated(true);
//        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//
//        // Load stage view
//        mainPanel.removeAll();
//        System.out.println("working!");
////        JPanel gameScreen = new GameScreen(Config.WIDTH, Config.HEIGHT, stageNumber);
////        mainPanel.add(gameScreen, "GameScreen");
////        showCard("GameScreen");
//
//        revalidate();
//        repaint();
//    }

private void startStage(int stageNumber) {
    dispose(); // Make frame undisplayable
    setUndecorated(true);
    setResizable(false);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    if (gd.isFullScreenSupported()) {
        gd.setFullScreenWindow(this); // goes fullscreen
    } else {
        setSize(Config.WIDTH, Config.HEIGHT);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    mainPanel = new JPanel(new BorderLayout());
    JLabel label = new JLabel("Stage " + stageNumber + " started!");
    label.setFont(new Font("Arial", Font.BOLD, 24));
    label.setHorizontalAlignment(SwingConstants.CENTER);
    mainPanel.add(label, BorderLayout.CENTER);

    setContentPane(mainPanel);
    revalidate();
    repaint();
}


    private void showCard(String name) {
        CardLayout cl = (CardLayout) mainPanel.getLayout();
        cl.show(mainPanel, name);
    }

    private void playMusic() {
        if (clip != null && clip.isRunning()) return;

        try {
            InputStream audioSrc = getClass().getClassLoader().getResourceAsStream("theme.wav");
            if (audioSrc == null) throw new IOException("Resource not found: theme.wav");

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Failed to play music: " + e.getMessage());
        }
    }

    private void stopMusic() {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
