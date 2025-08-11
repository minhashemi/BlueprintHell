package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.core.GameEngine;

import javax.swing.*;
import java.awt.*;

/**
 * The panel where the main game is rendered and played.
 * This class now acts as a canvas, delegating all drawing logic to the GameEngine.
 */
public class GamePanel extends JPanel {

    private GameEngine gameEngine;

    public GamePanel() {
        initPanel();
        // The GameEngine will be set from the GameFrame
    }

    // A new method to link the engine to the panel
    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;
    }

    private void initPanel() {
        setName(GameFrame.GAME_PANEL);
        setPreferredSize(new Dimension(1280, 720));
        setBackground(Color.BLACK);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // If the engine exists, tell it to render everything.
        if (gameEngine != null) {
            gameEngine.render((Graphics2D) g);
        } else {
            // Placeholder if the engine hasn't been set yet
            g.setColor(Color.RED);
            g.drawString("Game Engine not loaded!", 20, 20);
        }
    }
}
