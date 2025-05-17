package me.minhashemi.model.shop;

import javax.swing.*;
import java.awt.*;

public class ShopPanel extends JPanel {
    private JButton buyButton;
    private JButton cancelButton;
    private JLabel itemLabel;
    private JLabel priceLabel;
    private static int price = 100;

    public static int getPrice(){
        return price;
    }

    public interface ShopListener {
        void onBuy();
        void onCancel();
    }

    public ShopPanel(ShopListener listener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(30, 30, 30, 230)); // Semi-transparent dark background
        setBorder(BorderFactory.createLineBorder(Color.WHITE));

        itemLabel = new JLabel("Item: Super Packet");
        itemLabel.setForeground(Color.WHITE);
        itemLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        priceLabel = new JLabel("Price: 100 coins");
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        buyButton = new JButton("Buy");
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.addActionListener(e -> listener.onBuy());

        cancelButton = new JButton("Close");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> listener.onCancel());

        add(Box.createVerticalGlue());
        add(itemLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(priceLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(buyButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(cancelButton);
        add(Box.createVerticalGlue());
    }
}
