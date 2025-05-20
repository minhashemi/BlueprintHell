package me.minhashemi.view;

import me.minhashemi.model.shop.ShopItem;
import me.minhashemi.model.shop.ShopPanel;
import me.minhashemi.model.shop.items.DisableCollisionsEffect;
import me.minhashemi.model.shop.items.DisableImpactWavesEffect;

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
                int newCoins = hud.getCoins() - item.getPrice();
                hud.updateHUD(hud.getTemporalProgress(), gameScreen.getPacketManager().getLostPackets(), newCoins);
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