package me.minhashemi.model.shop.items;

import me.minhashemi.model.shop.ShopEffect;

public class DisableCollisionsEffect implements ShopEffect {
    @Override
    public void applyEffect() {
        System.out.println("Collisions disabled for 5 seconds.");
        // TODO: Implement actual effect
    }
}
