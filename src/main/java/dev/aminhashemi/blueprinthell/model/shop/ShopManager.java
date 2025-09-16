package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Logger;
import java.util.ArrayList;
import java.util.List;

/**
 * Shop Manager following Single Responsibility Principle
 * Manages all shop items and purchases
 */
public class ShopManager {
    private final List<ShopItem> shopItems;
    private int playerCoins;
    
    public ShopManager(int initialCoins) {
        this.shopItems = new ArrayList<>();
        this.playerCoins = initialCoins;
    }
    
    /**
     * Adds a shop item (Open/Closed Principle - open for extension)
     */
    public void addShopItem(ShopItem item) {
        shopItems.add(item);
    }
    
    /**
     * Gets all shop items
     */
    public List<ShopItem> getShopItems() {
        return new ArrayList<>(shopItems);
    }
    
    /**
     * Updates player coins
     */
    public void setPlayerCoins(int coins) {
        this.playerCoins = coins;
    }
    
    /**
     * Gets current player coins
     */
    public int getPlayerCoins() {
        return playerCoins;
    }
    
    /**
     * Attempts to purchase an item by index
     */
    public boolean purchaseItem(int itemIndex) {
        if (itemIndex < 0 || itemIndex >= shopItems.size()) {
            Logger.getInstance().info("Invalid item index: " + itemIndex);
            return false;
        }
        
        ShopItem item = shopItems.get(itemIndex);
        
        if (!item.canAfford(playerCoins)) {
            Logger.getInstance().info("Not enough coins for " + item.getName() + 
                                   "! Need: " + item.getCost() + ", Have: " + playerCoins);
            return false;
        }
        
        // Deduct coins
        playerCoins -= item.getCost();
        
        // Execute purchase
        item.purchase();
        
        Logger.getInstance().info("Successfully purchased " + item.getName() + 
                               "! Remaining coins: " + playerCoins);
        return true;
    }
    
    /**
     * Gets shop item by index
     */
    public ShopItem getItem(int index) {
        if (index < 0 || index >= shopItems.size()) {
            return null;
        }
        return shopItems.get(index);
    }
}
