package dev.aminhashemi.blueprinthell;

import dev.aminhashemi.blueprinthell.view.GameFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // It's crucial to create and show Swing components on the Event Dispatch Thread (EDT)
        // to ensure thread safety.
        SwingUtilities.invokeLater(GameFrame::new);
    }
}
