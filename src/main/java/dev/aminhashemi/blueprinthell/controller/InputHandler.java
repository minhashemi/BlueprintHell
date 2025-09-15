package dev.aminhashemi.blueprinthell.controller;

import dev.aminhashemi.blueprinthell.core.GameEngine;

import javax.swing.SwingUtilities;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Handles all mouse and keyboard input for the game.
 */
public class InputHandler extends MouseAdapter {

    private final GameEngine gameEngine;

    public InputHandler(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    // Mouse Events

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            gameEngine.handleLeftMousePress(e.getPoint());
        } else if (SwingUtilities.isRightMouseButton(e)) {
            gameEngine.handleRightMousePress(e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            gameEngine.handleLeftMouseRelease(e.getPoint());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gameEngine.handleMouseDrag(e.getPoint());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gameEngine.handleMouseMove(e.getPoint());
    }

    // Keyboard Events
    public static class KeyInput extends KeyAdapter {
        private final GameEngine gameEngine;

        public KeyInput(GameEngine gameEngine) {
            this.gameEngine = gameEngine;
        }

        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_W:
                    gameEngine.toggleWiringMode(true);
                    break;
                case KeyEvent.VK_SPACE:
                    gameEngine.handleManualPacketSpawn(); // Spawn packets manually
                    break;
                case KeyEvent.VK_S:
                    if (e.isControlDown() || e.isMetaDown()) {
                        // Ctrl+S (Windows/Linux) or Cmd+S (macOS) to save
                        System.out.println("Saving game...");
                        if (gameEngine.saveGame()) {
                            System.out.println("✅ Game saved successfully!");
                            System.out.println("Systems: " + gameEngine.getSystems().size() + 
                                             ", Wires: " + gameEngine.getWires().size() + 
                                             ", Coins: " + gameEngine.getCoins());
                        } else {
                            System.out.println("❌ Failed to save game!");
                        }
                    }
                    break;
                case KeyEvent.VK_L:
                    if (e.isControlDown() || e.isMetaDown()) {
                        // Ctrl+L (Windows/Linux) or Cmd+L (macOS) to load
                        System.out.println("Loading game...");
                        if (gameEngine.loadGame()) {
                            System.out.println("✅ Game loaded successfully!");
                            System.out.println("Systems: " + gameEngine.getSystems().size() + 
                                             ", Wires: " + gameEngine.getWires().size() + 
                                             ", Coins: " + gameEngine.getCoins());
                        } else {
                            System.out.println("❌ Failed to load game!");
                        }
                    }
                    break;
                case KeyEvent.VK_T:
                    // T key to toggle time travel mode
                    gameEngine.toggleTimeTravelMode();
                    System.out.println("Time Travel Mode: " + 
                        (gameEngine.getTimeTravelManager().isTimeTravelMode() ? "ON" : "OFF"));
                    break;
                case KeyEvent.VK_R:
                    // R key to start network execution
                    if (gameEngine.getTimeTravelManager().isTimeTravelMode()) {
                        gameEngine.startNetworkExecution();
                        System.out.println("🚀 Starting Network Execution!");
                        System.out.println("Time Travel Mode: " + gameEngine.getTimeTravelManager().isTimeTravelMode());
                        System.out.println("Executing: " + gameEngine.getTimeTravelManager().isExecuting());
                    } else {
                        System.out.println("❌ Time Travel Mode is OFF - press T first!");
                    }
                    break;
                case KeyEvent.VK_P:
                    // P key to pause/resume time travel
                    if (gameEngine.getTimeTravelManager().isTimeTravelMode()) {
                        gameEngine.toggleTimeTravelPause();
                        System.out.println("Time Travel: " + 
                            (gameEngine.getTimeTravelManager().isPaused() ? "PAUSED" : "PLAYING"));
                    }
                    break;
                case KeyEvent.VK_LEFT:
                    // Left arrow to rewind time
                    if (gameEngine.getTimeTravelManager().isTimeTravelMode()) {
                        long currentTime = gameEngine.getTimeTravelManager().getCurrentTime();
                        long newTime = Math.max(0, currentTime - 1000); // Rewind 1 second
                        gameEngine.seekToTime(newTime);
                        System.out.println("⏪ Rewound to: " + newTime + "ms");
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    // Right arrow to fast forward time
                    if (gameEngine.getTimeTravelManager().isTimeTravelMode()) {
                        long currentTime = gameEngine.getTimeTravelManager().getCurrentTime();
                        long maxTime = gameEngine.getTimeTravelManager().getMaxTime();
                        long newTime = Math.min(maxTime, currentTime + 1000); // Forward 1 second
                        gameEngine.seekToTime(newTime);
                        System.out.println("⏩ Fast forwarded to: " + newTime + "ms");
                    }
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_W) {
                gameEngine.toggleWiringMode(false);
            }
        }
    }
}
