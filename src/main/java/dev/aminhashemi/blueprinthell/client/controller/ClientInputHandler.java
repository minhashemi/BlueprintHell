package dev.aminhashemi.blueprinthell.client.controller;

import dev.aminhashemi.blueprinthell.client.core.SimpleClientGameEngine;
import dev.aminhashemi.blueprinthell.utils.Config;

import javax.swing.SwingUtilities;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Point;

/**
 * Client-side input handler that processes user input and sends it to the server.
 * Also handles offline mode input processing.
 */
public class ClientInputHandler extends MouseAdapter {

    private final SimpleClientGameEngine clientGameEngine;

    public ClientInputHandler(SimpleClientGameEngine clientGameEngine) {
        this.clientGameEngine = clientGameEngine;
    }

    // ==================== MOUSE EVENTS ====================

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            clientGameEngine.handleMouseClick(e.getPoint(), true);
        } else if (SwingUtilities.isRightMouseButton(e)) {
            clientGameEngine.handleMouseClick(e.getPoint(), false);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            // Handle left mouse release
            handleLeftMouseRelease(e.getPoint());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Handle mouse drag
        handleMouseDrag(e.getPoint());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Handle mouse move
        handleMouseMove(e.getPoint());
    }

    private void handleLeftMouseRelease(Point point) {
        // Send mouse release to server
        clientGameEngine.handleMouseClick(point, false);
    }

    private void handleMouseDrag(Point point) {
        // Send mouse drag to server
        // This could be optimized to send only significant movements
        clientGameEngine.handleMouseClick(point, true);
    }

    private void handleMouseMove(Point point) {
        // Handle mouse move (usually for UI updates)
        // Don't send every mouse move to server, only significant ones
    }

    // ==================== KEYBOARD EVENTS ====================

    public static class KeyInput extends KeyAdapter {
        private final SimpleClientGameEngine clientGameEngine;

        public KeyInput(SimpleClientGameEngine clientGameEngine) {
            this.clientGameEngine = clientGameEngine;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            // Send key press to server
            clientGameEngine.handleKeyPress(e.getKeyCode());
            
            // Handle local UI updates
            handleLocalKeyPress(e);
        }

        @Override
        public void keyReleased(KeyEvent e) {
            // Send key release to server
            clientGameEngine.handleKeyRelease(e.getKeyCode());
            
            // Handle local UI updates
            handleLocalKeyRelease(e);
        }

        private void handleLocalKeyPress(KeyEvent e) {
            // Handle keys that should work locally even in offline mode
            switch (e.getKeyCode()) {
                case KeyEvent.VK_H:
                    // Toggle HUD (local UI)
                    System.out.println("HUD toggled");
                    break;
                case KeyEvent.VK_ESCAPE:
                    // Handle escape key (local UI)
                    System.out.println("Escape pressed");
                    break;
                default:
                    // Other keys are handled by server
                    break;
            }
        }

        private void handleLocalKeyRelease(KeyEvent e) {
            // Handle key releases that should work locally
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                    // Wiring mode release (local UI)
                    System.out.println("Wiring mode released");
                    break;
                default:
                    // Other key releases are handled by server
                    break;
            }
        }
    }

    // ==================== GAME-SPECIFIC INPUT HANDLING ====================

    /**
     * Handles wiring mode input
     */
    public void handleWiringMode(boolean enabled) {
        if (enabled) {
            System.out.println("Wiring mode enabled");
        } else {
            System.out.println("Wiring mode disabled");
        }
    }

    /**
     * Handles pause/play input
     */
    public void handlePauseToggle() {
        System.out.println("Pause toggled");
    }

    /**
     * Handles time travel input
     */
    public void handleTimeTravel(boolean enabled) {
        if (enabled) {
            System.out.println("Time travel enabled");
        } else {
            System.out.println("Time travel disabled");
        }
    }

    /**
     * Handles save/load input
     */
    public void handleSave() {
        System.out.println("Save requested");
    }

    public void handleLoad() {
        System.out.println("Load requested");
    }

    /**
     * Handles shop input
     */
    public void handleShopToggle() {
        System.out.println("Shop toggled");
    }

    /**
     * Handles packet spawn input
     */
    public void handlePacketSpawn() {
        System.out.println("Packet spawn requested");
    }

    /**
     * Handles test system input
     */
    public void handleTestStart() {
        System.out.println("Test started");
    }

    public void handleTestReset() {
        System.out.println("Test reset");
    }

    // ==================== CONNECTION STATUS HANDLING ====================

    /**
     * Handles connection status changes
     */
    public void handleConnectionStatusChange(boolean connected) {
        if (connected) {
            System.out.println("Connected to server - input will be sent to server");
        } else {
            System.out.println("Disconnected from server - running in offline mode");
        }
    }

    /**
     * Handles offline mode activation
     */
    public void handleOfflineMode() {
        System.out.println("Switched to offline mode - input will be handled locally");
    }

    // ==================== INPUT VALIDATION ====================

    /**
     * Validates input before sending to server
     */
    private boolean isValidInput(Point point) {
        // Check if point is within game bounds
        return point.x >= 0 && point.y >= 0 && 
               point.x < Config.WINDOW_WIDTH && point.y < Config.WINDOW_HEIGHT;
    }

    /**
     * Validates key input before sending to server
     */
    private boolean isValidKeyInput(int keyCode) {
        // Check if key code is valid
        return keyCode > 0 && keyCode < 256;
    }

    // ==================== INPUT RATE LIMITING ====================

    private long lastMouseInput = 0;
    private long lastKeyboardInput = 0;
    private static final long INPUT_RATE_LIMIT = 16; // ~60 FPS

    /**
     * Checks if enough time has passed since last mouse input
     */
    private boolean canSendMouseInput() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastMouseInput >= INPUT_RATE_LIMIT) {
            lastMouseInput = currentTime;
            return true;
        }
        return false;
    }

    /**
     * Checks if enough time has passed since last keyboard input
     */
    private boolean canSendKeyboardInput() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastKeyboardInput >= INPUT_RATE_LIMIT) {
            lastKeyboardInput = currentTime;
            return true;
        }
        return false;
    }
}
