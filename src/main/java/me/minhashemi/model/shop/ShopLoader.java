package me.minhashemi.model.shop;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public class ShopLoader {
    public static List<ShopItem> loadItems() {
        try {
            String filename = "shop_items.json";
            InputStream input = ShopLoader.class.getClassLoader().getResourceAsStream(filename);
            if (input == null) {
                throw new RuntimeException("Resource not found: " + filename);
            }

            InputStreamReader reader = new InputStreamReader(input);
            Type listType = new TypeToken<List<ShopItem>>() {}.getType();
            return new Gson().fromJson(reader, listType);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Return empty list on failure
        }
    }
}
