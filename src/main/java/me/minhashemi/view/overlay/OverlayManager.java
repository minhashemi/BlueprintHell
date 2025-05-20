package me.minhashemi.view.overlay;

import me.minhashemi.controller.audio.player;
import me.minhashemi.view.GameScreen;

import javax.swing.*;

public class OverlayManager {
    private final GameScreen gameScreen;
    private boolean victoryShown;
    private boolean gameOverShown;

    public OverlayManager(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
        this.victoryShown = false;
        this.gameOverShown = false;
    }

    public void checkAndShowOverlays(int lostPackets, int totalPackets) {
        if (!victoryShown && gameScreen.getMovingPackets().isEmpty() && gameScreen.getPacketManager().allBlocksGreen()) {
            victoryShown = true;
            player.playEffect("victory");
            SwingUtilities.invokeLater(() -> showVictoryMessage("Victory! All packets delivered."));
        } else if (!gameOverShown && lostPackets >= totalPackets / 2) {
            gameOverShown = true;
            gameScreen.stopGameTimer();
            player.playEffect("lose");
            SwingUtilities.invokeLater(() -> showGameOverMessage("Game Over! Too many packets lost."));
        }
    }

    private void showVictoryMessage(String msg) {
        VictoryOverlay overlay = new VictoryOverlay(gameScreen, msg);
        gameScreen.setLayout(null);
        overlay.setSize(gameScreen.getSize());
        gameScreen.add(overlay);
        gameScreen.setComponentZOrder(overlay, 0);
        gameScreen.revalidate();
        gameScreen.repaint();
    }

    private void showGameOverMessage(String msg) {
        GameOverOverlay overlay = new GameOverOverlay(gameScreen, msg, gameScreen.getPacketManager().getLostPackets(), gameScreen.getPacketManager().getTotalPackets(), gameScreen.getHUD().getTemporalProgress());
        gameScreen.setLayout(null);
        overlay.setSize(gameScreen.getSize());
        gameScreen.add(overlay);
        gameScreen.setComponentZOrder(overlay, 0);
        player.playEffect("collide");
        gameScreen.revalidate();
        gameScreen.repaint();
    }

    public boolean isVictoryShown() {
        return victoryShown;
    }

    public boolean isGameOverShown() {
        return gameOverShown;
    }
}