package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * O' Atar - Phase 1 Shop Item
 * 
 * Disables Impact wave effects for 10 seconds.
 * When activated, all Impact waves in the network are temporarily disabled,
 * preventing packets from being affected by wave-based movement changes.
 * 
 * Cost: 3 coins
 * Duration: 10 seconds
 * Effect: Disables Impact waves globally
 */
public class OAtar implements ShopItem {
    private final int cost;
    private final int durationSeconds;
    private final Runnable onPurchase;
    private final Runnable onExpire;
    
    public OAtar(Runnable onPurchase, Runnable onExpire) {
        this.cost = Config.Shop.O_ATAR_COST;
        this.durationSeconds = Config.Shop.O_ATAR_DURATION;
        this.onPurchase = onPurchase;
        this.onExpire = onExpire;
    }
    
    @Override
    public String getName() {
        return "O' Atar - Impact Disable";
    }
    
    @Override
    public String getDescription() {
        return "Disables Impact waves for " + durationSeconds + " seconds";
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
        Logger.getInstance().info("O' Atar activated! Impact waves disabled for " + durationSeconds + " seconds");
        onPurchase.run();
        
        // Schedule expiration
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onExpire.run();
                Logger.getInstance().info("O' Atar expired! Impact waves re-enabled");
            }
        }, durationSeconds * 1000); // Convert to milliseconds
    }
    
    @Override
    public String getKeyBinding() {
        return "1";
    }
}
