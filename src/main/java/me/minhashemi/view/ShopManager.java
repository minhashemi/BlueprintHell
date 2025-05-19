package me.minhashemi.view;

import me.minhashemi.model.shop.ShopItem;
import me.minhashemi.model.shop.ShopPanel;

import javax.swing.*;
import java.awt.*;

public class ShopManager {
    private final GameScreen gameScreen;
    private final HUD hud;
    private JPanel shopOverlay;

    public ShopManager(GameScreen gameScreen, HUD hud) {
        this.gameScreen = gameScreen;
        this.hud = hud;
        this.shopOverlay = null;
    }

    public void openShop() {
        if (shopOverlay != null) return;

        shopOverlay = new JPanel(null);
        shopOverlay.setBounds(0, 0, gameScreen.getWidth(), gameScreen.getHeight());
        shopOverlay.setOpaque(false);

        ShopPanel shopPanel = new ShopPanel(new ShopPanel.ShopListener() {
            @Override
            public void onBuy(ShopItem item) {
                // Check if player has enough coins
                if (hud.getCoins() < item.getPrice()) {
                    hud.showCallout("Not enough coins!");
                    return;
                }

                // Apply the effect based on effect type
                applyShopEffect(item);

                // Deduct coins
                int newCoins = hud.getCoins() - item.getPrice();
                hud.updateHUD(hud.getTemporalProgress(), gameScreen.getPacketManager().getLostPackets(), newCoins);
                
                // Show confirmation message
                hud.showCallout("Purchased: " + item.getName());
                
                closeShop();
            }

            @Override
            public void onCancel() {
                closeShop();
            }
        });

        shopPanel.setBounds(gameScreen.getWidth() / 2 - 250, gameScreen.getHeight() / 2 - 200, 500, 400);
        shopOverlay.add(shopPanel);
        gameScreen.add(shopOverlay);
        gameScreen.setComponentZOrder(shopOverlay, 0);
        gameScreen.revalidate();
        gameScreen.repaint();

        gameScreen.pauseGame();
    }

    private void applyShopEffect(ShopItem item) {
        String effectType = item.getEffectType();
        
        switch (effectType) {
            case "DISABLE_IMPACT_WAVES":
                // O' Atar - Disable impact waves for 10 seconds
                gameScreen.getPacketManager().disableWaveForSeconds(10);
                hud.setImpactWavesDisabled(true, 10);
                System.out.println("O' Atar activated! Impact waves disabled for 10 seconds");
                break;
                
            case "DISABLE_COLLISIONS":
                // O' Airyaman - Disable packet collisions for 5 seconds
                gameScreen.getPacketManager().disableImpactForSeconds(5);
                hud.setCollisionsDisabled(true, 5);
                System.out.println("O' Airyaman activated! Packet collisions disabled for 5 seconds");
                break;
                
            case "RESET_NOISE":
                // O' Anahita - Reset all packet noise
                gameScreen.getPacketManager().resetAllNoise();
                System.out.println("O' Anahita activated! All packet noise reset to zero");
                break;
                
            default:
                System.out.println("Unknown effect type: " + effectType);
                break;
        }
    }

    public void closeShop() {
        if (shopOverlay != null) {
            gameScreen.remove(shopOverlay);
            shopOverlay = null;
            gameScreen.revalidate();
            gameScreen.repaint();
            gameScreen.resumeGame();
        }
    }
}