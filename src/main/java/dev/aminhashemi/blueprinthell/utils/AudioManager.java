package dev.aminhashemi.blueprinthell.utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;

/**
 * Manages all audio playback for the game using a Singleton pattern.
 * This ensures that there is only one central point for controlling audio.
 */
public class AudioManager {

    private static final AudioManager instance = new AudioManager();
    private Clip backgroundMusic;

    // Private constructor to prevent instantiation from other classes
    private AudioManager() {
    }

    /**
     * Provides the global access point to the AudioManager instance.
     * @return The single instance of the AudioManager.
     */
    public static AudioManager getInstance() {
        return instance;
    }

    /**
     * Loads and plays the background music on a loop.
     * @param soundFileName The name of the sound file in the resources/sounds folder.
     */
    public void playBackgroundMusic(String soundFileName) {
        try {
            if (backgroundMusic != null && backgroundMusic.isOpen()) {
                backgroundMusic.close();
            }

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
                
                audioSrc = null;
                for (String path : possiblePaths) {
                    java.io.File soundFile = new java.io.File(path);
                    if (soundFile.exists()) {
                        audioSrc = new java.io.FileInputStream(soundFile);
                        Logger.getInstance().info("Using file fallback for background music: " + soundFileName + " from: " + path);
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
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music indefinitely
            backgroundMusic.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            Logger.getInstance().error("Error playing background music", e);
        }
    }

    /**
     * Sets the volume of the background music.
     * @param volume The volume level, from 0.0 (mute) to 1.0 (max).
     */
    public void setVolume(float volume) {
        if (backgroundMusic != null && backgroundMusic.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            if (volume < 0.0f) volume = 0.0f;
            if (volume > 1.0f) volume = 1.0f;

            FloatControl gainControl = (FloatControl) backgroundMusic.getControl(FloatControl.Type.MASTER_GAIN);
            // The gain is logarithmic, so we calculate it from the linear volume value.
            // This formula converts a linear scale (0-1) to a decibel scale.
            float dB = (float) (Math.log(volume) / Math.log(10.0) * 20.0);
            gainControl.setValue(dB);
        }
    }
    
    /**
     * Plays a sound effect once.
     * @param soundFileName The name of the sound file in the resources/sounds folder.
     */
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
