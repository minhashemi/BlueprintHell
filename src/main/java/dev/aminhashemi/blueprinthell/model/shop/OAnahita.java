package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

/**
 * O' Anahita - Phase 1 Shop Item
 * 
 * Sets noise of all packets in the network to zero.
 * This effect is permanent for all existing packets until new packets are spawned.
 * New packets will have their normal noise values, but existing packets will maintain zero noise.
 * 
 * Cost: 5 coins
 * Duration: Permanent for existing packets
 * Effect: Sets all current packet noise to zero
 */
public class OAnahita implements ShopItem {
    private final int cost;
    private final Runnable onPurchase;
    
    public OAnahita(Runnable onPurchase) {
        this.cost = Config.Shop.O_ANAHITA_COST;
        this.onPurchase = onPurchase;
    }
    
    @Override
    public String getName() {
        return "O' Anahita - Noise Zero";
    }
    
    @Override
    public String getDescription() {
        return "Sets noise of all current packets to zero";
    }
    
    @Override
    public int getCost() {
        return cost;
    }
    
    @Override
    public boolean canAfford(int playerCoins) {
        return playerCoins >= cost;
    }
    
    @Override
    public void purchase() {
        Logger.getInstance().info("O' Anahita activated! All packet noise set to zero");
        onPurchase.run();
    }
    
    @Override
    public String getKeyBinding() {
        return "3";
    }
}
