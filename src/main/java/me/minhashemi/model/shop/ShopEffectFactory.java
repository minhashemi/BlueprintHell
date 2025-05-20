package me.minhashemi.model.shop;

import me.minhashemi.model.shop.items.*;
import me.minhashemi.view.GameScreen;

public class ShopEffectFactory {
    public static ShopEffect getEffect(String effectType, GameScreen gameScreen) {
        if (gameScreen == null) {
            throw new IllegalArgumentException("GameScreen cannot be null");
        }
        switch (effectType) {
            case "DISABLE_IMPACT_WAVES":
                return new DisableImpactWavesEffect(gameScreen);
            case "DISABLE_COLLISIONS":
                return new DisableCollisionsEffect(gameScreen);
            case "RESET_NOISE":
                return new ResetNoiseEffect(gameScreen);
            default:
                throw new IllegalArgumentException("Unknown effect type: " + effectType);
        }
    }
}