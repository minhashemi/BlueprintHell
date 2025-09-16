package dev.aminhashemi.blueprinthell.model.shop;

/**
 * Interface for shop items following the Interface Segregation Principle
 */
public interface ShopItem {
    String getName();
    String getDescription();
    int getCost();
    boolean canAfford(int playerCoins);
    void purchase();
    String getKeyBinding();
}
