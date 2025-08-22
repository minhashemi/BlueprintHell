package dev.aminhashemi.blueprinthell.view;

import dev.aminhashemi.blueprinthell.controller.InputHandler;
import dev.aminhashemi.blueprinthell.core.GameEngine;

import javax.swing.*;
import java.awt.*;

public class GamePanel extends JPanel {

    private GameEngine gameEngine;

    public GamePanel() {
        initPanel();
    }

    public void setGameEngine(GameEngine gameEngine) {
        this.gameEngine = gameEngine;

        // Create and add the mouse listener
        InputHandler mouseHandler = new InputHandler(gameEngine);
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);

        // Create and add the keyboard listener
        InputHandler.KeyInput keyHandler = new InputHandler.KeyInput(gameEngine);
        this.addKeyListener(keyHandler);
    }

    private void initPanel() {
        setName(GameFrame.GAME_PANEL);
        setPreferredSize(new Dimension(1280, 720));
        setBackground(new Color(20, 25, 40)); // Dark blue-gray instead of pure black for better contrast
        setFocusable(true); // Crucial for receiving key events
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameEngine != null) {
            gameEngine.render((Graphics2D) g);
        } else {
            g.setColor(Color.RED);
            g.drawString("Game Engine not loaded!", 20, 20);
        }
    }
}
