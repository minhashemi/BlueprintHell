package dev.aminhashemi.blueprinthell.utils;

import com.google.gson.Gson;
import dev.aminhashemi.blueprinthell.model.LevelData;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class LevelLoader {

    public static LevelData loadLevel(int levelNumber) {
        String fileName = "/levels/level" + levelNumber + ".json";
        Gson gson = new Gson();

        try (InputStream stream = LevelLoader.class.getResourceAsStream(fileName)) {
            if (stream == null) {
                Logger.getInstance().error("Cannot find level file: " + fileName);
                return null;
            }
            Reader reader = new InputStreamReader(stream);
            return gson.fromJson(reader, LevelData.class);

        } catch (Exception e) {
            Logger.getInstance().error("Error loading level file: " + fileName, e);
            return null;
        }
    }
}
