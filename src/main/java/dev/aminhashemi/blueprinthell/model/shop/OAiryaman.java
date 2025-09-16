package dev.aminhashemi.blueprinthell.model.shop;

import dev.aminhashemi.blueprinthell.utils.Config;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.util.Timer;
import java.util.TimerTask;

/**
 * O' Airyaman - Phase 1 Shop Item
 * 
 * Disables packet collisions for 5 seconds.
 * When activated, all packets in the network can pass through each other
 * without any collision detection or response for the specified duration.
 * 
 * Cost: 4 coins
 * Duration: 5 seconds
 * Effect: Disables packet collisions globally
 */
public class OAiryaman implements ShopItem {
    private final int cost;
    private final int durationSeconds;
    private final Runnable onPurchase;
    private final Runnable onExpire;
    
    public OAiryaman(Runnable onPurchase, Runnable onExpire) {
        this.cost = Config.Shop.O_AIRYAMAN_COST;
        this.durationSeconds = Config.Shop.O_AIRYAMAN_DURATION;
        this.onPurchase = onPurchase;
        this.onExpire = onExpire;
    }
    
    @Override
    public String getName() {
        return "O' Airyaman - Collision Disable";
    }
    
    @Override
    public String getDescription() {
        return "Disables packet collisions for " + durationSeconds + " seconds";
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
        Logger.getInstance().info("O' Airyaman activated! Packet collisions disabled for " + durationSeconds + " seconds");
        onPurchase.run();
        
        // Schedule expiration
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                onExpire.run();
                Logger.getInstance().info("O' Airyaman expired! Packet collisions re-enabled");
            }
        }, durationSeconds * 1000); // Convert to milliseconds
    }
    
    @Override
    public String getKeyBinding() {
        return "2";
    }
}
