package dev.aminhashemi.blueprinthell.core;

import dev.aminhashemi.blueprinthell.view.GamePanel;

/**
 * The core of the game, containing the main game loop.
 * It runs on a separate thread to handle game logic updates and rendering
 * independently of the Swing Event Dispatch Thread (EDT).
 */
public class GameEngine implements Runnable {

    private final GamePanel gamePanel;
    private Thread gameThread;
    private volatile boolean running = false;

    // Game loop timing
    private final int FPS_SET = 120; // Target frames per second
    private final int UPS_SET = 200; // Target updates per second

    public GameEngine(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
        // In the future, we will initialize game state, entities, etc. here
    }

    public void startGameLoop() {
        gameThread = new Thread(this);
        running = true;
        gameThread.start();
    }

    @Override
    public void run() {
        double timePerFrame = 1_000_000_000.0 / FPS_SET;
        double timePerUpdate = 1_000_000_000.0 / UPS_SET;

        long previousTime = System.nanoTime();
        int frames = 0;
        int updates = 0;
        long lastCheck = System.currentTimeMillis();

        double deltaU = 0;
        double deltaF = 0;

        while (running) {
            long currentTime = System.nanoTime();
            deltaU += (currentTime - previousTime) / timePerUpdate;
            deltaF += (currentTime - previousTime) / timePerFrame;
            previousTime = currentTime;

            if (deltaU >= 1) {
                update();
                updates++;
                deltaU--;
            }

            if (deltaF >= 1) {
                gamePanel.repaint();
                frames++;
                deltaF--;
            }

            if (System.currentTimeMillis() - lastCheck >= 1000) {
                lastCheck = System.currentTimeMillis();
                System.out.println("FPS: " + frames + " | UPS: " + updates);
                frames = 0;
                updates = 0;
            }
        }
    }

    private void update() {
        // This is where all game logic will be updated in the future.
        // e.g., packet movement, collision checks, etc.
    }

    public void render(java.awt.Graphics2D g) {
        // This is where all entities will be drawn.
        // The GamePanel will call this method.
        g.setColor(java.awt.Color.GREEN);
        g.drawString("Game Engine is Running!", 20, 20);
    }
}
