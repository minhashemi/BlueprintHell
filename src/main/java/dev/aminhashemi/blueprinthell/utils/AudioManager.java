package dev.aminhashemi.blueprinthell.utils;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

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

            URL soundURL = getClass().getResource("/sounds/" + soundFileName);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + soundFileName);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY); // Loop the music indefinitely
            backgroundMusic.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing background music: " + e.getMessage());
            e.printStackTrace();
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
        try {
            // Try multiple resource loading approaches for Maven exec compatibility
            URL soundURL = null;
            
            // First try the class loader approach (works better with Maven exec)
            soundURL = AudioManager.class.getClassLoader().getResource("sounds/" + soundFileName);
            
            if (soundURL == null) {
                // Try the class resource approach
                soundURL = AudioManager.class.getResource("/sounds/" + soundFileName);
            }
            
            if (soundURL == null) {
                // Try the instance resource approach
                soundURL = getClass().getResource("/sounds/" + soundFileName);
            }
            
            if (soundURL == null) {
                // Try file-based loading from target/classes (Maven exec fallback)
                try {
                    // Get the absolute path to the target/classes directory
                    String currentDir = System.getProperty("user.dir");
                    java.io.File soundFile = new java.io.File(currentDir + "/target/classes/sounds/" + soundFileName);
                    if (soundFile.exists()) {
                        soundURL = soundFile.toURI().toURL();
                        System.out.println("Loaded sound from file: " + soundFile.getAbsolutePath());
                    } else {
                        System.err.println("Sound file not found at: " + soundFile.getAbsolutePath());
                    }
                } catch (Exception e) {
                    System.err.println("File loading failed: " + e.getMessage());
                }
            }
            
            if (soundURL == null) {
                System.err.println("All resource loading attempts failed for: " + soundFileName);
                return;
            }

            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip soundClip = AudioSystem.getClip();
            soundClip.open(audioStream);
            soundClip.start();
            System.out.println("Playing sound: " + soundFileName);

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
