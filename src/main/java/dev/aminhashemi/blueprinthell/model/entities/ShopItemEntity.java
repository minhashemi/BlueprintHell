package dev.aminhashemi.blueprinthell.model.entities;

import jakarta.persistence.*;

/**
 * JPA Entity for ShopItem data
 */
@Entity
@Table(name = "shop_items", indexes = {
    @Index(name = "idx_item_name", columnList = "name"),
    @Index(name = "idx_effect_type", columnList = "effectType")
})
public class ShopItemEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;
    
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;
    
    @Column(name = "price", nullable = false)
    private int price;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "effect_type", length = 50, nullable = false)
    private String effectType;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "duration_seconds")
    private Integer durationSeconds;
    
    @Column(name = "cooldown_seconds")
    private Integer cooldownSeconds;
    
    // Constructors
    public ShopItemEntity() {}
    
    public ShopItemEntity(String name, int price, String description, String effectType) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.effectType = effectType;
    }
    
    // Getters and setters
    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getEffectType() { return effectType; }
    public void setEffectType(String effectType) { this.effectType = effectType; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public Integer getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; }
    
    public Integer getCooldownSeconds() { return cooldownSeconds; }
    public void setCooldownSeconds(Integer cooldownSeconds) { this.cooldownSeconds = cooldownSeconds; }
}
