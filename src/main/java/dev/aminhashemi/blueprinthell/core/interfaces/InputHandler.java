package dev.aminhashemi.blueprinthell.core.interfaces;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.Point;

/**
 * Interface for input handling following Single Responsibility Principle.
 * Handles all user input including keyboard and mouse events.
 */
public interface InputHandler {
    
    /**
     * Handles key press events.
     * @param e KeyEvent containing key information
     */
    void handleKeyPress(KeyEvent e);
    
    /**
     * Handles key release events.
     * @param e KeyEvent containing key information
     */
    void handleKeyRelease(KeyEvent e);
    
    /**
     * Handles mouse press events.
     * @param e MouseEvent containing mouse information
     */
    void handleMousePress(MouseEvent e);
    
    /**
     * Handles mouse release events.
     * @param e MouseEvent containing mouse information
     */
    void handleMouseRelease(MouseEvent e);
    
    /**
     * Handles mouse drag events.
     * @param e MouseEvent containing mouse information
     */
    void handleMouseDrag(MouseEvent e);
    
    /**
     * Handles mouse move events.
     * @param e MouseEvent containing mouse information
     */
    void handleMouseMove(MouseEvent e);
    
    /**
     * Handles left mouse press at a specific point.
     * @param point The point where the mouse was pressed
     */
    void handleLeftMousePress(Point point);
    
    /**
     * Handles right mouse press at a specific point.
     * @param point The point where the mouse was pressed
     */
    void handleRightMousePress(Point point);
    
    /**
     * Handles left mouse release at a specific point.
     * @param point The point where the mouse was released
     */
    void handleLeftMouseRelease(Point point);
    
    /**
     * Handles mouse drag to a specific point.
     * @param point The point where the mouse was dragged to
     */
    void handleMouseDrag(Point point);
    
    /**
     * Handles mouse move to a specific point.
     * @param point The point where the mouse moved to
     */
    void handleMouseMove(Point point);
}