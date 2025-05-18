package me.minhashemi.model.shop;

import me.minhashemi.model.shop.items.*;

public class ShopEffectFactory {
    public static ShopEffect getEffect(String effectType) {
        switch (effectType) {
            case "DISABLE_IMPACT_WAVES":
                return new DisableImpactWavesEffect();
            case "DISABLE_COLLISIONS":
                return new DisableCollisionsEffect();
            case "RESET_NOISE":
                return new ResetNoiseEffect();
            default:
                throw new IllegalArgumentException("Unknown effect type: " + effectType);
        }
    }
}
