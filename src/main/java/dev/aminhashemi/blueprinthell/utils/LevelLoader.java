package dev.aminhashemi.blueprinthell.utils;

import com.google.gson.Gson;
import dev.aminhashemi.blueprinthell.model.LevelData;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Utility class for loading level data from JSON files.
 * Handles file I/O and JSON parsing for game levels.
 */
public class LevelLoader {

    /**
     * Loads a level from the resources directory.
     * @param levelNumber The level number to load (1, 2, 3, etc.)
     * @return LevelData object containing level information, or null if loading fails
     */
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
