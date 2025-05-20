package me.minhashemi.view;

import me.minhashemi.model.Config;

import javax.swing.*;
import java.awt.*;

public class HUD {
    private int temporalProgress = 0;
    private int packetLoss = 0;
    private static int coins = 0;
    private String calloutMessage = null;
    private long calloutTimestamp = 0;


    public HUD() {
        // Initialize timer for callout messages
        Timer calloutTimer = new Timer(100, e -> {
            if (calloutMessage != null && System.currentTimeMillis() - calloutTimestamp > Config.CALLOUT_DURATION) {
                calloutMessage = null;
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

    public void render(Graphics g, int panelWidth) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // HUD
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 14));
        int hudY = 20;
        g.drawString("Remaining Wire Length: " + (int) Config.remainingWireLength, 10, hudY);
        g.drawString("Temporal Progress: " + temporalProgress, 250, hudY);
        g.drawString("Packet Loss: " + packetLoss, 450, hudY);
        g.drawString("Coins: " + coins, 600, hudY);

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
}