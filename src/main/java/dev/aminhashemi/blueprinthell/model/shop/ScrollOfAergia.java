package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Scroll of Aergia - Phase 2 Shop Item
 * 
 * Stops packet acceleration at a selected point on the network for 20 seconds.
 * When activated, the player selects a point on a wire connection, and all packets
 * passing through that point will have their acceleration set to zero, causing them
 * to move at constant speed along the wire.
 * 
 * Cost: 10 coins
 * Duration: 20 seconds
 * Cooldown: Yes (prevents immediate reuse)
 * Effect: Zero acceleration at selected wire point
 */
public class ScrollOfAergia implements ShopItem {
    private final int cost;
    private final int durationSeconds;
    private final Runnable onPurchase;
    private final Runnable onExpire;
    private boolean isOnCooldown = false;
    
    public ScrollOfAergia(Runnable onPurchase, Runnable onExpire) {
        this.cost = Config.Shop.SCROLL_AERGIA_COST;
        this.durationSeconds = Config.Shop.SCROLL_AERGIA_DURATION;
        this.onPurchase = onPurchase;
        this.onExpire = onExpire;
    }
    
    @Override
    public String getName() {
        return "Scroll of Aergia - Zero Acceleration";
    }
    
    @Override
    public String getDescription() {
        return "Stops packet acceleration at selected point for " + durationSeconds + " seconds";
    }
    
    @Override
    public int getCost() {
        return cost;
    }
    
    @Override
    public boolean canAfford(int playerCoins) {
        return playerCoins >= cost && !isOnCooldown;
    }
    
    @Override
    public void purchase() {
        if (isOnCooldown) {
            Logger.getInstance().info("Scroll of Aergia is on cooldown!");
            return;
        }
        
        Logger.getInstance().info("Scroll of Aergia activated! Select a point on a wire to stop acceleration");
        onPurchase.run();
        
        // Set cooldown
        isOnCooldown = true;
        
        // Schedule expiration
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onExpire.run();
                isOnCooldown = false; // Reset cooldown
                Logger.getInstance().info("Scroll of Aergia expired! Acceleration restored");
            }
        }, durationSeconds * 1000); // Convert to milliseconds
    }
    
    @Override
    public String getKeyBinding() {
        return "4";
    }
    
    public boolean isOnCooldown() {
        return isOnCooldown;
    }
}
