package me.minhashemi.utils;

import me.minhashemi.model.Config;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages key bindings for the game
 */
public class KeyBindingManager {
    private static KeyBindingManager instance;
    private Map<String, KeyStroke> keyBindings;
    
    private KeyBindingManager() {
        keyBindings = new HashMap<>();
        loadDefaultKeyBindings();
    }
    
    public static KeyBindingManager getInstance() {
        if (instance == null) {
            instance = new KeyBindingManager();
        }
        return instance;
    }
    
    private void loadDefaultKeyBindings() {
        keyBindings.put(Config.SPAWN_PACKET_ACTION, Config.SPAWN_PACKET_KEY);
        keyBindings.put(Config.PAUSE_ACTION, Config.PAUSE_KEY);
    }
    
    /**
     * Get the key stroke for a specific action
     */
    public KeyStroke getKeyStroke(String action) {
        return keyBindings.get(action);
    }
    
    /**
     * Set a new key binding for an action
     */
    public boolean setKeyBinding(String action, KeyStroke newKeyStroke) {
        // Check if the key is already used by another action
        for (Map.Entry<String, KeyStroke> entry : keyBindings.entrySet()) {
            if (!entry.getKey().equals(action) && entry.getValue().equals(newKeyStroke)) {
                return false; // Key already in use
            }
        }
        
        keyBindings.put(action, newKeyStroke);
        updateConfigKeyStrokes();
        return true;
    }
    
    /**
     * Get all current key bindings
     */
    public Map<String, KeyStroke> getAllKeyBindings() {
        return new HashMap<>(keyBindings);
    }
    
    /**
     * Update the Config class with current key bindings
     */
    private void updateConfigKeyStrokes() {
        Config.SPAWN_PACKET_KEY = keyBindings.get(Config.SPAWN_PACKET_ACTION);
        Config.PAUSE_KEY = keyBindings.get(Config.PAUSE_ACTION);
    }
    
    /**
     * Get a display string for a key stroke
     */
    public String getKeyDisplayString(KeyStroke keyStroke) {
        if (keyStroke == null) return "None";
        
        String keyText = KeyEvent.getKeyText(keyStroke.getKeyCode());
        if (keyStroke.getModifiers() > 0) {
            // Use InputEvent.getModifiersExText instead of deprecated method
            String modifiers = java.awt.event.InputEvent.getModifiersExText(keyStroke.getModifiers());
            return modifiers + "+" + keyText;
        }
        return keyText;
    }
    
    /**
     * Reset all key bindings to default
     */
    public void resetToDefaults() {
        loadDefaultKeyBindings();
        updateConfigKeyStrokes();
    }
}
