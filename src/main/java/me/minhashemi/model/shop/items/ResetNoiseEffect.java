package me.minhashemi.model.shop.items;

import me.minhashemi.model.shop.ShopEffect;
import me.minhashemi.view.GameScreen;
import me.minhashemi.model.MovingPacket;

public class ResetNoiseEffect implements ShopEffect {
    private GameScreen gameScreen = null;

    public ResetNoiseEffect(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public void applyEffect() {
        if (gameScreen != null) {
            for (MovingPacket packet : gameScreen.getMovingPackets()) {
                packet.setNoise(0);
            }
            System.out.println("Noise reset on all packets.");
        }
    }
}
