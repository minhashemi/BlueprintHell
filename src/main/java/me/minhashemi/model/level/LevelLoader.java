package me.minhashemi.model.level;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.InputStreamReader;
import java.io.InputStream;

public class LevelLoader {
    public static LevelData loadLevel(int levelNumber) {
        String filename = String.format("levels/level%d.json", levelNumber);
        InputStream is = LevelLoader.class.getClassLoader().getResourceAsStream(filename);

        if (is == null) {
            throw new RuntimeException("Could not find level file: " + filename);
        }

        Gson gson = new GsonBuilder().create();
        return gson.fromJson(new InputStreamReader(is), LevelData.class);
    }
}
