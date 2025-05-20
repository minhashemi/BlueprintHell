package me.minhashemi.model.shop.items;

import me.minhashemi.model.shop.ShopEffect;
import me.minhashemi.view.GameScreen;

public class DisableImpactWavesEffect implements ShopEffect {
    private final GameScreen gameScreen;

    public DisableImpactWavesEffect(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public void applyEffect() {
        if (gameScreen != null) {
            gameScreen.getPacketManager().disableWaveForSeconds(10);
        }
    }
}