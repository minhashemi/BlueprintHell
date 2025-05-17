package me.minhashemi.view;

import javax.swing.*;
import java.awt.*;

public class GameControlsPanel extends JPanel {
    public interface GameControlListener {
        void onShop();
        void onTimeForward();
        void onTimeBackward();
        void onQuitToMenu();
    }

    public GameControlsPanel(GameControlListener listener) {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setOpaque(false);

        JButton shopBtn = new JButton("🛒 Shop");
        JButton forwardBtn = new JButton("⏩ Time +");
        JButton backBtn = new JButton("⏪ Time -");
        JButton quitBtn = new JButton("🚪 Quit");

        // Prevent SPACE from triggering these buttons
        shopBtn.setFocusable(false);
        forwardBtn.setFocusable(false);
        backBtn.setFocusable(false);
        quitBtn.setFocusable(false);

        shopBtn.addActionListener(e -> listener.onShop());
        forwardBtn.addActionListener(e -> listener.onTimeForward());
        backBtn.addActionListener(e -> listener.onTimeBackward());
        quitBtn.addActionListener(e -> listener.onQuitToMenu());

        add(shopBtn);
        add(forwardBtn);
        add(backBtn);
        add(quitBtn);
    }
}
