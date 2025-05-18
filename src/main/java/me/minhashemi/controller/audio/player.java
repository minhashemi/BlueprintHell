package me.minhashemi.controller.audio;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class player {
    private static Clip backgroundClip;
    private static FloatControl volumeControl;

    public static void playMusic(String fname) {
        if (backgroundClip != null && backgroundClip.isRunning()) return;

        try {
            InputStream audioSrc = player.class.getClassLoader().getResourceAsStream(fname + ".wav");
            if (audioSrc == null) throw new IOException("Resource not found: " + fname + ".wav");

            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            backgroundClip = AudioSystem.getClip();
            backgroundClip.open(audioStream);

            if (backgroundClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) backgroundClip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(0.8f); // default volume
            }

            backgroundClip.loop(Clip.LOOP_CONTINUOUSLY);
            backgroundClip.start();
        } catch (Exception e) {
            System.err.println("Failed to play music: " + e.getMessage());
        }
    }

    public static void stopMusic() {
        if (backgroundClip != null) {
            backgroundClip.stop();
            backgroundClip.close();
        }
    }

    public static void setVolume(float level) {
        if (volumeControl != null) {
            float min = volumeControl.getMinimum();
            float max = volumeControl.getMaximum();
            float volume = min + (max - min) * level;
            volumeControl.setValue(volume);
        }
    }

    public static void playEffect(String fname) {
        new Thread(() -> {
            try {
                InputStream audioSrc = player.class.getClassLoader().getResourceAsStream(fname + ".wav");
                if (audioSrc == null) throw new IOException("Resource not found: " + fname + ".wav");

                InputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                Clip effectClip = AudioSystem.getClip();
                effectClip.open(audioStream);
                effectClip.start();
            } catch (Exception e) {
                System.err.println("Failed to play sound effect: " + e.getMessage());
            }
        }).start();
    }
}
