package me.minhashemi.model.shop;

public class ShopItem {
    private String name;
    private int price;
    private String description;

    public ShopItem(String name, int price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return getName();
    }
}
