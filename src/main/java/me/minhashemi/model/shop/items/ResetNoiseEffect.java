package me.minhashemi.model.shop.items;

import me.minhashemi.model.shop.ShopEffect;
import me.minhashemi.view.GameScreen;
import me.minhashemi.model.MovingPacket;
import me.minhashemi.view.HUD;
import me.minhashemi.model.shop.ShopItem;

public class ResetNoiseEffect implements ShopEffect {
    private GameScreen gameScreen = null;
    private HUD hud;

    public ResetNoiseEffect(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public void applyEffect() {
        if (HUD.getCoins() < ShopItem.getPrice()){
            hud.showCallout("🪙 Not enough coins");
            return;
        }
        for (MovingPacket packet : gameScreen.getMovingPackets()) {
            packet.setNoise(0);
        }
        System.out.println("Noise reset on all packets.");
    }
}
