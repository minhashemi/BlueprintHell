package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Scroll of Eliphas - Phase 2 Shop Item
 * 
 * Restores packet center of gravity to wire alignment for 30 seconds.
 * When activated, the player selects a point on a wire connection, and all packets
 * passing through that point will have their center of gravity continuously restored
 * to align with the wire. This prevents tunneling and maintains proper collision logic.
 * 
 * Cost: 20 coins
 * Duration: 30 seconds
 * Effect: Continuous center of gravity restoration at selected point
 */
public class ScrollOfEliphas implements ShopItem {
    private final int cost;
    private final int durationSeconds;
    private final Runnable onPurchase;
    private final Runnable onExpire;
    
    public ScrollOfEliphas(Runnable onPurchase, Runnable onExpire) {
        this.cost = Config.Shop.SCROLL_ELIPHAS_COST;
        this.durationSeconds = Config.Shop.SCROLL_ELIPHAS_DURATION;
        this.onPurchase = onPurchase;
        this.onExpire = onExpire;
    }
    
    @Override
    public String getName() {
        return "Scroll of Eliphas - Gravity Restore";
    }
    
    @Override
    public String getDescription() {
        return "Restores packet center of gravity to wire for " + durationSeconds + " seconds";
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
        Logger.getInstance().info("Scroll of Eliphas activated! Select a point on a wire to restore gravity");
        onPurchase.run();
        
        // Schedule expiration
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onExpire.run();
                Logger.getInstance().info("Scroll of Eliphas expired! Gravity restoration stopped");
            }
        }, durationSeconds * 1000); // Convert to milliseconds
    }
    
    @Override
    public String getKeyBinding() {
        return "6";
    }
}
