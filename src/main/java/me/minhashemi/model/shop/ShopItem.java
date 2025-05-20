package me.minhashemi.model.shop;

public class ShopItem {
    private String name;
    private static int price;
    private String description;
    private String effectType;

    public ShopItem(String name, int price, String description, String effectType) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.effectType = effectType;
    }

    public String getName() { return name; }
    public static int getPrice() { return price; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return getName();
    }
}
