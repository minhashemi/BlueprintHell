package me.minhashemi.view;

import javax.swing.*;
import java.awt.*;

public class GameControlsPanel extends JPanel {
    public interface GameControlListener {
        void onTimeForward();
        void onTimeBackward();
        void onQuitToMenu();
    }

    public GameControlsPanel(GameControlListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setOpaque(false);

        JButton forwardBtn = new JButton("⏩ Time +");
        JButton backBtn = new JButton("⏪ Time -");
        JButton quitBtn = new JButton("🚪 Quit");

        // Prevent SPACE from triggering these buttons
        forwardBtn.setFocusable(false);
        backBtn.setFocusable(false);
        quitBtn.setFocusable(false);

        forwardBtn.addActionListener(e -> listener.onTimeForward());
        backBtn.addActionListener(e -> listener.onTimeBackward());
        quitBtn.addActionListener(e -> listener.onQuitToMenu());

        add(forwardBtn);
        add(backBtn);
        add(quitBtn);
    }
}
