package dev.aminhashemi.blueprinthell.shared.network;

import dev.aminhashemi.blueprinthell.model.SaveData;

/**
 * Interface for synchronizing game state between client and server.
 * Handles state updates, snapshots, and delta synchronization.
 */
public interface GameStateSync {
    
    /**
     * Synchronizes complete game state.
     * @param gameState The complete game state
     */
    void syncGameState(SaveData gameState);
    
    /**
     * Synchronizes delta changes to game state.
     * @param deltaState The changed state data
     */
    void syncDeltaState(SaveData deltaState);
    
    /**
     * Handles time travel snapshot synchronization.
     * @param snapshot The snapshot data
     * @param snapshotIndex The index of the snapshot
     */
    void syncSnapshot(SaveData snapshot, int snapshotIndex);
    
    /**
     * Handles real-time packet movement updates.
     * @param packetData The packet movement data
     */
    void syncPacketMovement(String packetData);
    
    /**
     * Handles system updates (placement, movement, etc.).
     * @param systemData The system data
     */
    void syncSystemUpdate(String systemData);
    
    /**
     * Handles wire updates (creation, deletion, modification).
     * @param wireData The wire data
     */
    void syncWireUpdate(String wireData);
    
    /**
     * Handles impact effects and visual updates.
     * @param impactData The impact effect data
     */
    void syncImpactEffect(String impactData);
}
