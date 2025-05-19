package me.minhashemi.model.shop;

public class ShopItem {
    private String name;
    private int price;
    private String description;
    private String effectType;

    public ShopItem(String name, int price, String description, String effectType) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.effectType = effectType;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }
    public String getEffectType() { return effectType; }

    @Override
    public String toString() {
        return getName();
    }
}
