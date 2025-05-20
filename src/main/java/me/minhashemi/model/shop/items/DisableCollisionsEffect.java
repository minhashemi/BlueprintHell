package me.minhashemi.model.shop.items;

import me.minhashemi.model.shop.ShopEffect;
import me.minhashemi.view.GameScreen;

public class DisableCollisionsEffect implements ShopEffect {
    private final GameScreen gameScreen;

    public DisableCollisionsEffect(GameScreen gameScreen) {
        this.gameScreen = gameScreen;
    }

    @Override
    public void applyEffect() {
        if (gameScreen != null) {
            gameScreen.getPacketManager().disableImpactForSeconds(5);
        }
    }
}