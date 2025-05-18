package me.minhashemi.model.shop;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ShopPanel extends JPanel {
    private JLabel descriptionLabel;
    private JLabel priceLabel;
    private JButton buyButton;
    private JButton cancelButton;
    private ShopItem selectedItem;

    public interface ShopListener {
        void onBuy(ShopItem item);
        void onCancel();
    }

    public ShopPanel(ShopListener listener) {
        List<ShopItem> items = ShopLoader.loadItems();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(new Color(30, 30, 30, 230));
        setBorder(BorderFactory.createLineBorder(Color.WHITE));

        // Panel for item buttons
        JPanel itemsPanel = new JPanel();
        itemsPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        itemsPanel.setOpaque(false);

        for (ShopItem item : items) {
            JButton itemButton = new JButton(item.getName());
            itemButton.addActionListener(e -> {
                selectedItem = item;
                updateItemDisplay(item);
            });
            itemsPanel.add(itemButton);
        }
        itemsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        descriptionLabel = new JLabel();
        descriptionLabel.setForeground(Color.LIGHT_GRAY);
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        priceLabel = new JLabel();
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        buyButton = new JButton("Buy");
        buyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyButton.addActionListener(e -> {
            if (selectedItem != null) {
                listener.onBuy(selectedItem);
            }
        });

        cancelButton = new JButton("Close");
        cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelButton.addActionListener(e -> listener.onCancel());

        add(Box.createVerticalGlue());
        add(itemsPanel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(descriptionLabel);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(priceLabel);
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(buyButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(cancelButton);
        add(Box.createVerticalGlue());

        if (!items.isEmpty()) {
            selectedItem = items.get(0);
            updateItemDisplay(selectedItem);
        } else {
            descriptionLabel.setText("No items available");
            priceLabel.setText("");
            buyButton.setEnabled(false);
        }
    }

    private void updateItemDisplay(ShopItem item) {
        descriptionLabel.setText(item.getDescription());
        priceLabel.setText("Price: " + item.getPrice() + " coins");
    }
}
