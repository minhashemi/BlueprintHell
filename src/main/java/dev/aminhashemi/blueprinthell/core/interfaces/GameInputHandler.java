package dev.aminhashemi.blueprinthell.core.interfaces;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

/**
 * Interface for handling game input.
 * Follows Interface Segregation Principle by focusing only on input handling.
 */
public interface GameInputHandler {
    
    /**
     * Handles key press events
     * @param e Key event
     */
    void handleKeyPress(KeyEvent e);
    
    /**
     * Handles key release events
     * @param e Key event
     */
    void handleKeyRelease(KeyEvent e);
    
    /**
     * Handles mouse press events
     * @param e Mouse event
     */
    void handleMousePress(MouseEvent e);
    
    /**
     * Handles mouse release events
     * @param e Mouse event
     */
    void handleMouseRelease(MouseEvent e);
    
    /**
     * Handles mouse drag events
     * @param e Mouse event
     */
    void handleMouseDrag(MouseEvent e);
    
    /**
     * Handles mouse move events
     * @param e Mouse event
     */
    void handleMouseMove(MouseEvent e);
    
    /**
     * Handles mouse click events
     * @param e Mouse event
     */
    void handleMouseClick(MouseEvent e);
}
