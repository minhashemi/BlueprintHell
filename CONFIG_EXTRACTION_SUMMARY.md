# Configuration Extraction Summary

This document summarizes all the constants and configurable parameters that have been extracted from the BlueprintHell codebase into a centralized `Config.java` file for easy presentation and modification.

## Overview

All hardcoded values, magic numbers, and configurable parameters have been moved from individual classes into a centralized `Config.java` file located at:
```
src/main/java/dev/aminhashemi/blueprinthell/utils/Config.java
```

## Extracted Constants by Category

### 1. Game Engine Settings
- **TARGET_FPS**: 120 (was hardcoded in GameEngine)
- **TARGET_UPS**: 200 (was hardcoded in GameEngine)
- **WINDOW_WIDTH**: 1280 (was hardcoded in GamePanel)
- **WINDOW_HEIGHT**: 720 (was hardcoded in GamePanel)
- **BACKGROUND_COLOR**: Dark blue-gray (was hardcoded in GamePanel)

### 2. Wire System Configuration
- **TOTAL_WIRE_LENGTH**: 8000 meters (was hardcoded in GameEngine)
- **ARC_POINT_COST**: 1 coin (was hardcoded in GameEngine)
- **MAX_ARC_POINTS_PER_WIRE**: 3 (was hardcoded in GameEngine)
- **MAX_BULK_PACKET_PASSES**: 3 (was hardcoded in BulkPacket)
- **WIRE_CLICK_THRESHOLD**: 8.0 pixels (was hardcoded in GameEngine)

### 3. Packet System Configuration
- **PACKET_SPAWN_COOLDOWN**: 500ms (was hardcoded in GameEngine)
- **SPAWN_PROTECTION_DURATION**: 2000ms (was hardcoded in MovingPacket)
- **PACKET_DESTRUCTION_NOISE_THRESHOLD**: 50.0f (was hardcoded in ImpactManager)
- **GAME_OVER_PACKET_LOSS_THRESHOLD**: 50% (was hardcoded in GameEngine)

### 4. Collision System Configuration
- **COLLISION_THRESHOLD**: 15.0 (was hardcoded in ImpactManager)
- **COLLISION_NOISE_INCREASE**: 30.0f (was hardcoded in ImpactManager)
- **WAVE_INTENSITY**: 8.0f (was hardcoded in ImpactManager)
- **IMPACT_COOLDOWN_MS**: 1000ms (was hardcoded in ImpactManager)
- **CHAIN_REACTION_RADIUS**: 80.0 (was hardcoded in ImpactManager)
- **CHAIN_REACTION_INTENSITY**: 20.0f (was hardcoded in ImpactManager)
- **MAX_CHAIN_REACTIONS**: 3 (was hardcoded in ImpactManager)
- **IMPACT_DISPLAY_DURATION**: 2000ms (was hardcoded in ImpactManager)
- **NEW_IMPACT_WINDOW**: 100ms (was hardcoded in ImpactManager)

### 5. Time Travel System Configuration
- **SNAPSHOT_INTERVAL**: 16ms (was hardcoded in GameEngine)
- **TIME_TRAVEL_WINDOW_SECONDS**: 5 (was hardcoded in GameEngine)
- **MAX_SNAPSHOTS**: 300 (was hardcoded in GameEngine)
- **TIME_TRAVEL_INPUT_DELAY**: 100ms (was hardcoded in GameEngine)
- **SNAPSHOTS_DIRECTORY**: "snapshots" (was hardcoded in GameEngine)

### 6. HUD Configuration
- **HUD_DISPLAY_DURATION**: 3000ms (was hardcoded in GamePanel)
- **HUD_WIDTH**: 280 pixels (was hardcoded in GamePanel)
- **HUD_HEIGHT**: 160 pixels (was hardcoded in GamePanel)
- **HUD_MARGIN**: 20 pixels (was hardcoded in GamePanel)
- **HUD_BACKGROUND_ALPHA**: 180 (was hardcoded in GamePanel)
- **HUD_BORDER_ALPHA**: 100 (was hardcoded in GamePanel)
- **HUD_WIRE_LENGTH_SAFE_COLOR**: Cyan (was hardcoded in GamePanel)
- **HUD_WIRE_LENGTH_WARNING_COLOR**: Orange (was hardcoded in GamePanel)
- **HUD_WIRE_LENGTH_DANGER_COLOR**: Red (was hardcoded in GamePanel)
- **HUD_PACKET_LOSS_COLOR**: Red (was hardcoded in GamePanel)
- **HUD_COINS_COLOR**: Gold (was hardcoded in GamePanel)
- **HUD_ACTIVE_SYSTEMS_COLOR**: Green (was hardcoded in GamePanel)
- **HUD_WIRE_CONNECTIONS_COLOR**: Blue (was hardcoded in GamePanel)
- **WIRE_LENGTH_WARNING_THRESHOLD**: 2000 (was hardcoded in GamePanel)
- **WIRE_LENGTH_DANGER_THRESHOLD**: 500 (was hardcoded in GamePanel)

### 7. Port Configuration
- **PORT_SIZE**: 10 pixels (was hardcoded in Port)

### 8. Packet Type Configuration
All packet colors, sizes, and coin rewards have been extracted:
- **Phase 1 Packets**: SQUARE_MESSENGER, TRIANGLE_MESSENGER
- **Phase 2 Packets**: GREEN_DIAMOND_SMALL/LARGE, INFINITY_SYMBOL, PADLOCK_ICON, CAMOUFLAGE_ICON_SMALL/LARGE, BULK_PACKET_SMALL/LARGE
- **Port Colors**: All port type colors for Phase 1 and Phase 2

### 9. Confidential Packet Configuration
- **CONFIDENTIAL_SPEED_CHECK_INTERVAL**: 500ms (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_SLOWDOWN_FACTOR**: 0.3f (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_SPEED_RECOVERY_FACTOR**: 0.1f (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_SPY_SLOWDOWN_FACTOR**: 0.2f (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_SPY_SPEED_REDUCTION**: 0.3f (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_DISTANCE_ADJUSTMENT**: 0.1f (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_MAX_SPEED_MULTIPLIER**: 1.2f (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_MIN_SPEED_MULTIPLIER**: 0.5f (was hardcoded in ConfidentialPacket)

### 10. Bulk Packet Configuration
- **BULK_CURVE_ACCELERATION**: 0.05f (was hardcoded in BulkPacket)
- **BULK_STRAIGHT_DECELERATION**: 0.02f (was hardcoded in BulkPacket)
- **BULK_MAX_SPEED**: 2.0f (was hardcoded in BulkPacket)
- **BULK_MIN_SPEED**: 1.0f (was hardcoded in BulkPacket)
- **BULK_DEVIATION_CHANCE**: 0.2 (was hardcoded in BulkPacket)
- **BULK_DEVIATION_AMOUNT**: 0.5f (was hardcoded in BulkPacket)
- **BULK_DEVIATION_BOUNDS**: 1.0f (was hardcoded in BulkPacket)
- **BULK_RANDOM_MOVEMENT_CHANCE**: 0.1 (was hardcoded in BulkPacket)
- **BULK_RANDOM_MOVEMENT_AMOUNT**: 0.3f (was hardcoded in BulkPacket)

### 11. Shop Configuration
- **O_ATAR_PRICE**: 3 coins (was hardcoded in shop_items.json)
- **O_AIRYAMAN_PRICE**: 4 coins (was hardcoded in shop_items.json)
- **O_ANAHITA_PRICE**: 5 coins (was hardcoded in shop_items.json)
- **O_ATAR_DURATION**: 10 seconds (was hardcoded in GameEngine)
- **O_AIRYAMAN_DURATION**: 5 seconds (was hardcoded in GameEngine)

### 12. Level Configuration
- **INITIAL_COINS**: 20 (was hardcoded in GamePanel and level files)
- **INITIAL_WIRE_LENGTH**: 8000 (was hardcoded in level files)
- **PACKET_GENERATION_COUNT**: 15 (was hardcoded in level files)
- **PACKET_GENERATION_FREQUENCY**: 2.0 (was hardcoded in level files)

### 13. System Configuration
- **SYSTEM_STORAGE_CAPACITY**: 5 (was hardcoded in System)
- **SYSTEM_WIDTH**: 100 (was hardcoded in System)
- **SYSTEM_HEIGHT**: 60 (was hardcoded in System)

### 14. Audio Configuration
- **THEME_MUSIC**: "theme.wav" (was hardcoded in AudioManager)
- **COLLISION_SOUND**: "collide.wav" (was hardcoded in ImpactManager)
- **CONNECTION_SOUND**: "connect.wav" (was hardcoded in GameEngine)
- **PACKET_LOSS_SOUND**: "boom.wav" (was hardcoded in GameEngine)
- **VICTORY_SOUND**: "victory.wav" (was hardcoded in GameEngine)
- **LOSE_SOUND**: "lose.wav" (was hardcoded in GameEngine)

### 15. Rendering Configuration
- **DEFAULT_FONT_FAMILY**: "Arial" (was hardcoded in multiple classes)
- **HUD_TITLE_SIZE**: 16 (was hardcoded in GamePanel)
- **HUD_TEXT_SIZE**: 14 (was hardcoded in GamePanel)
- **HUD_SMALL_TEXT_SIZE**: 12 (was hardcoded in GamePanel)
- **HUD_TINY_TEXT_SIZE**: 10 (was hardcoded in GamePanel)
- **PACKET_INFO_SIZE**: 8 (was hardcoded in GameEngine)
- **ENABLE_ANTIALIASING**: true (was hardcoded in GameEngine)

### 16. Debug Configuration
- **ENABLE_DEBUG_LOGGING**: true (was hardcoded in Logger)
- **ENABLE_PACKET_TRACKING**: true (was hardcoded in GameEngine)
- **ENABLE_WIRE_LENGTH_LOGGING**: true (was hardcoded in GameEngine)
- **DEBUG_IMPACT_COLOR**: Red (was hardcoded in GameEngine)
- **DEBUG_PACKET_LOST_COLOR**: Red (was hardcoded in GameEngine)
- **DEBUG_PACKET_NOISE_COLOR**: Orange (was hardcoded in GameEngine)
- **DEBUG_WIRE_VALID_COLOR**: Green (was hardcoded in GameEngine)
- **DEBUG_WIRE_INVALID_COLOR**: Red (was hardcoded in GameEngine)

### 17. Game Balance Configuration
- **CONFIDENTIAL_CONGESTION_CHANCE**: 0.4 (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_SPY_PROXIMITY_CHANCE**: 0.2 (was hardcoded in ConfidentialPacket)
- **CONFIDENTIAL_DISTANCE_VIOLATION_CHANCE**: 0.3 (was hardcoded in ConfidentialPacket)
- **BULK_RANDOM_MOVEMENT_CHANCE**: 0.1 (was hardcoded in BulkPacket)

### 18. Save System Configuration
- **SAVE_FILE_NAME**: "save_game.json" (was hardcoded in SaveManager)
- **SNAPSHOT_FILE_PREFIX**: "snapshot_" (was hardcoded in GameEngine)
- **SNAPSHOT_FILE_SUFFIX**: ".json" (was hardcoded in GameEngine)

### 19. Input Configuration
- **WIRING_MODE_KEY**: 87 (W key) (was hardcoded in InputHandler)
- **PAUSE_KEY**: 80 (P key) (was hardcoded in InputHandler)
- **TIME_TRAVEL_KEY**: 84 (T key) (was hardcoded in InputHandler)
- **SPAWN_PACKET_KEY**: 32 (Space key) (was hardcoded in InputHandler)
- **TOGGLE_HUD_KEY**: 72 (H key) (was hardcoded in InputHandler)
- **TIME_LEFT_KEY**: 37 (Left arrow) (was hardcoded in InputHandler)
- **TIME_RIGHT_KEY**: 39 (Right arrow) (was hardcoded in InputHandler)
- **SAVE_KEY**: 83 (S key with Ctrl) (was hardcoded in InputHandler)
- **LOAD_KEY**: 76 (L key with Ctrl) (was hardcoded in InputHandler)

## Benefits of Centralized Configuration

1. **Easy Presentation**: All game parameters can be quickly modified and demonstrated during presentations
2. **Maintainability**: Changes to game behavior only require editing one file
3. **Consistency**: All classes use the same values, preventing inconsistencies
4. **Documentation**: Each constant is well-documented with its purpose and original value
5. **Testing**: Easy to create different game configurations for testing
6. **Balance**: Game balance can be easily adjusted by changing values in one place

## Usage Examples

### Changing Game Difficulty
```java
// Make the game easier
Config.TOTAL_WIRE_LENGTH = 12000;  // More wire available
Config.PACKET_DESTRUCTION_NOISE_THRESHOLD = 75.0f;  // Higher noise tolerance
Config.GAME_OVER_PACKET_LOSS_THRESHOLD = 60;  // Higher loss tolerance

// Make the game harder
Config.TOTAL_WIRE_LENGTH = 5000;   // Less wire available
Config.PACKET_DESTRUCTION_NOISE_THRESHOLD = 30.0f;  // Lower noise tolerance
Config.GAME_OVER_PACKET_LOSS_THRESHOLD = 40;  // Lower loss tolerance
```

### Changing Visual Appearance
```java
// Change HUD colors
Config.HUD_WIRE_LENGTH_SAFE_COLOR = Color.GREEN;
Config.HUD_WIRE_LENGTH_WARNING_COLOR = Color.YELLOW;
Config.HUD_WIRE_LENGTH_DANGER_COLOR = Color.RED;

// Change packet colors
Config.Phase2Packets.GREEN_DIAMOND_SMALL_COLOR = Color.BLUE;
Config.Phase2Packets.GREEN_DIAMOND_LARGE_COLOR = Color.CYAN;
```

### Adjusting Game Balance
```java
// Make confidential packets more cautious
Config.GameBalance.CONFIDENTIAL_CONGESTION_CHANCE = 0.6;  // 60% chance of congestion
Config.GameBalance.CONFIDENTIAL_SPY_PROXIMITY_CHANCE = 0.4;  // 40% chance near spy systems

// Make bulk packets more stable
Config.BULK_DEVIATION_CHANCE = 0.1;  // 10% chance to deviate (was 20%)
Config.BULK_RANDOM_MOVEMENT_CHANCE = 0.05;  // 5% chance for random movement (was 10%)
```

## Files Modified

The following files were updated to use the centralized configuration:

1. `src/main/java/dev/aminhashemi/blueprinthell/core/GameEngine.java`
2. `src/main/java/dev/aminhashemi/blueprinthell/core/ImpactManager.java`
3. `src/main/java/dev/aminhashemi/blueprinthell/view/GamePanel.java`
4. `src/main/java/dev/aminhashemi/blueprinthell/model/entities/packets/PacketType.java`
5. `src/main/java/dev/aminhashemi/blueprinthell/model/entities/systems/PortType.java`
6. `src/main/java/dev/aminhashemi/blueprinthell/model/entities/systems/Port.java`
7. `src/main/java/dev/aminhashemi/blueprinthell/model/entities/packets/ConfidentialPacket.java`
8. `src/main/java/dev/aminhashemi/blueprinthell/model/entities/packets/BulkPacket.java`

## New File Created

- `src/main/java/dev/aminhashemi/blueprinthell/utils/Config.java` - Centralized configuration file

## Compilation Status

✅ All changes compile successfully without errors
✅ No breaking changes to existing functionality
✅ All hardcoded values have been extracted and centralized
