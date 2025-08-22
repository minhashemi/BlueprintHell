package dev.aminhashemi.blueprinthell.utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

/** Manages game audio playback using Singleton pattern */
public class AudioManager {

    private static final AudioManager instance = new AudioManager();
    private Clip backgroundMusic;

    // Private constructor for Singleton
    private AudioManager() {
    }

    /** Returns the global AudioManager instance */
    public static AudioManager getInstance() {
        return instance;
    }

    /** Loads and plays background music on loop */
    public void playBackgroundMusic(String soundFileName) {
        try {
            if (backgroundMusic != null && backgroundMusic.isOpen()) {
                backgroundMusic.close();
            }

            // Try resource loading first, then file fallbacks
            InputStream audioSrc = AudioManager.class.getClassLoader().getResourceAsStream("sounds/" + soundFileName);
            if (audioSrc == null) {
                String[] possiblePaths = {
                    System.getProperty("user.dir") + "/target/classes/sounds/" + soundFileName,
                    System.getProperty("user.dir") + "/src/main/resources/sounds/" + soundFileName,
                    "target/classes/sounds/" + soundFileName,
                    "src/main/resources/sounds/" + soundFileName
                };
                
                for (String path : possiblePaths) {
                    java.io.File soundFile = new java.io.File(path);
                    if (soundFile.exists()) {
                        audioSrc = new java.io.FileInputStream(soundFile);
                        break;
                    }
                }
                
                if (audioSrc == null) {
                    Logger.getInstance().error("Resource not found: sounds/" + soundFileName);
                    return;
                }
            }

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop indefinitely
            backgroundMusic.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            Logger.getInstance().error("Error playing background music", e);
        }
    }

    /** Sets background music volume (0.0 = mute, 1.0 = max) */
    public void setVolume(float volume) {
        if (backgroundMusic != null && backgroundMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            if (volume < 0.0f) volume = 0.0f;
            if (volume > 1.0f) volume = 1.0f;

            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            // Convert linear volume to decibel scale
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }
    
    /** Plays a sound effect once */
    public void playSound(String soundFileName) {
        new Thread(() -> {
            try {
                // Strategy 1: Try resource loading (works in IDE and JAR)
                InputStream audioSrc = AudioManager.class.getClassLoader().getResourceAsStream("sounds/" + soundFileName);
                if (audioSrc == null) {
                    // Strategy 2: Try multiple absolute path fallbacks for Maven exec
                    String[] possiblePaths = {
                        System.getProperty("user.dir") + "/target/classes/sounds/" + soundFileName,
                        System.getProperty("user.dir") + "/src/main/resources/sounds/" + soundFileName,
                        "target/classes/sounds/" + soundFileName,
                        "src/main/resources/sounds/" + soundFileName
                    };
                    
                    Logger.getInstance().debug("Current directory: " + System.getProperty("user.dir"));
                    audioSrc = null;
                    for (String path : possiblePaths) {
                        java.io.File soundFile = new java.io.File(path);
                        boolean exists = soundFile.exists();
                        Logger.getInstance().debug("Checking path: " + path + " (exists: " + exists + ")");
                        if (exists) {
                            audioSrc = new java.io.FileInputStream(soundFile);
                            Logger.getInstance().info("Using file fallback for: " + soundFileName + " from: " + path);
                            break;
                        }
                    }
                    
                    if (audioSrc == null) {
                        Logger.getInstance().error("Resource not found: sounds/" + soundFileName + " (tried resource and multiple file paths)");
                        return;
                    }
                }

                InputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                Clip effectClip = AudioSystem.getClip();
                effectClip.open(audioStream);
                effectClip.start();
                
                Logger.getInstance().info("Successfully playing sound: " + soundFileName);
                
            } catch (Exception e) {
                Logger.getInstance().error("Failed to play sound effect", e);
            }
        }).start();
    }
}
