package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

/**
 * Scroll of Sisyphus - Phase 2 Shop Item
 * 
 * Allows the player to move a non-reference system within a specified radius.
 * The movement must not exceed the available wire length in the level, and
 * the new position must not cause wires to pass through other systems.
 * 
 * Cost: 15 coins
 * Duration: Instant (one-time use)
 * Effect: Move a non-reference system to a new position
 */
public class ScrollOfSisyphus implements ShopItem {
    private final int cost;
    private final Runnable onPurchase;
    
    public ScrollOfSisyphus(Runnable onPurchase) {
        this.cost = Config.Shop.SCROLL_SISYPHUS_COST;
        this.onPurchase = onPurchase;
    }
    
    @Override
    public String getName() {
        return "Scroll of Sisyphus - System Move";
    }
    
    @Override
    public String getDescription() {
        return "Move a non-reference system within radius (respects wire length limits)";
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
        Logger.getInstance().info("Scroll of Sisyphus activated! Select a non-reference system to move");
        onPurchase.run();
    }
    
    @Override
    public String getKeyBinding() {
        return "5";
    }
}
