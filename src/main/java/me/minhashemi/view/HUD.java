package me.minhashemi.view;

import me.minhashemi.model.Config;

import javax.swing.*;
import java.awt.*;

public class HUD {
    private int temporalProgress = 0;
    private int packetLoss = 0;
    private static int coins = 0;
    private int totalPackets; // Total packets for the level
    private String calloutMessage = null;
    private long calloutTimestamp = 0;
    
    // Active effects tracking
    private boolean impactWavesDisabled = false;
    private boolean collisionsDisabled = false;
    private long effectEndTime = 0;

    public HUD(int totalPackets) {
        this.totalPackets = totalPackets;
        this.coins = 10; // Start with 10 coins for testing
        // Initialize timer for callout messages
        Timer calloutTimer = new Timer(100, e -> {
            if (calloutMessage != null && System.currentTimeMillis() - calloutTimestamp > Config.CALLOUT_DURATION) {
                calloutMessage = null;
            }
            
            // Check if effects have expired
            if (System.currentTimeMillis() > effectEndTime) {
                impactWavesDisabled = false;
                collisionsDisabled = false;
            }
        });
        calloutTimer.start();
    }

    public void showCallout(String message) {
        this.calloutMessage = message;
        this.calloutTimestamp = System.currentTimeMillis();
    }

    public void updateHUD(int temporalProgress, int packetLoss, int coins) {
        this.temporalProgress = temporalProgress;
        this.packetLoss = packetLoss;
        this.coins = coins;
    }

    public void setImpactWavesDisabled(boolean disabled, int durationSeconds) {
        this.impactWavesDisabled = disabled;
        if (disabled) {
            this.effectEndTime = System.currentTimeMillis() + (durationSeconds * 1000);
        }
    }

    public void setCollisionsDisabled(boolean disabled, int durationSeconds) {
        this.collisionsDisabled = disabled;
        if (disabled) {
            this.effectEndTime = System.currentTimeMillis() + (durationSeconds * 1000);
        }
    }

    public void render(Graphics g, int panelWidth) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // HUD
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int hudY = 20;
        g.drawString("Remaining Wire Length: " + (int) Config.remainingWireLength, 10, hudY);
        g.drawString("Temporal Progress: " + temporalProgress, 250, hudY);
        g.drawString("Packet Loss: " + packetLoss + "/" + totalPackets, 450, hudY);
        g.drawString("Coins: " + coins, 600, hudY);

        // Active effects indicator
        if (impactWavesDisabled || collisionsDisabled) {
            g.setColor(Color.YELLOW);
            g.setFont(new Font("SansSerif", Font.BOLD, 12));
            int effectY = hudY + 20;
            if (impactWavesDisabled) {
                g.drawString("O' Atar Active - Impact Waves Disabled", 10, effectY);
                effectY += 15;
            }
            if (collisionsDisabled) {
                g.drawString("O' Airyaman Active - Collisions Disabled", 10, effectY);
            }
        }

        if (calloutMessage != null && System.currentTimeMillis() - calloutTimestamp < Config.CALLOUT_DURATION) {
            int boxWidth = g.getFontMetrics().stringWidth(calloutMessage) + 20;
            int boxHeight = 30;
            int x = (panelWidth - boxWidth) / 2;
            int y = 40;
            g.setColor(new Color(255, 220, 220));
            g.fillRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            g.setColor(Color.RED);
            g.drawRoundRect(x, y, boxWidth, boxHeight, 10, 10);
            g.drawString(calloutMessage, x + 10, y + 20);
        }
    }

    public int getTemporalProgress() {
        return temporalProgress;
    }

    public int getPacketLoss() {
        return packetLoss;
    }

    public static int getCoins() {
        return coins;
    }

    public int getTotalPackets() {
        return totalPackets;
    }
}