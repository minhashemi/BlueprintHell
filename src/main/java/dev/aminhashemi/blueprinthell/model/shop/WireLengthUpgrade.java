package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

/**
 * Wire Length Upgrade shop item
 * Follows Single Responsibility Principle - only handles wire length upgrades
 */
public class WireLengthUpgrade implements ShopItem {
    private final int cost;
    private final int amount;
    private final Runnable onPurchase;
    
    public WireLengthUpgrade(Runnable onPurchase) {
        this.cost = Config.Shop.WIRE_LENGTH_UPGRADE_COST;
        this.amount = Config.Shop.WIRE_LENGTH_UPGRADE_AMOUNT;
        this.onPurchase = onPurchase;
    }
    
    @Override
    public String getName() {
        return "📏 Wire Length +1000m";
    }
    
    @Override
    public String getDescription() {
        return "Increases your total wire length by 1000 meters";
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
        Logger.getInstance().info("Purchased Wire Length Upgrade! +" + amount + "m wire length");
        onPurchase.run();
    }
    
    @Override
    public String getKeyBinding() {
        return "1";
    }
}
