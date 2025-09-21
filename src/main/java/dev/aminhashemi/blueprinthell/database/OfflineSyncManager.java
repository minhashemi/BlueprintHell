package dev.aminhashemi.blueprinthell.database;

import dev.aminhashemi.blueprinthell.model.entities.OfflineSyncEntity;
import dev.aminhashemi.blueprinthell.utils.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manager for offline data synchronization
 */
public class OfflineSyncManager {
    private final SessionFactory sessionFactory;
    private final Logger logger;
    private final ScheduledExecutorService scheduler;
    // These managers are available for future use in sync operations
    // private final UserProfileDatabaseManager userProfileManager;
    // private final LeaderboardDatabaseManager leaderboardManager;
    private boolean isRunning = false;
    
    public OfflineSyncManager() {
        this.sessionFactory = DatabaseManager.getInstance().getSessionFactory();
        this.logger = Logger.getInstance();
        this.scheduler = Executors.newScheduledThreadPool(2);
        // this.userProfileManager = new UserProfileDatabaseManager();
        // this.leaderboardManager = new LeaderboardDatabaseManager();
    }
    
    /**
     * Start the offline sync manager
     */
    public void start() {
        if (isRunning) {
            return;
        }
        
        isRunning = true;
        
        // Schedule sync attempts every 30 seconds
        scheduler.scheduleAtFixedRate(this::syncPendingData, 30, 30, TimeUnit.SECONDS);
        
        // Schedule cleanup every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupOldSyncData, 5, 5, TimeUnit.MINUTES);
        
        logger.info("Offline sync manager started");
    }
    
    /**
     * Stop the offline sync manager
     */
    public void stop() {
        if (!isRunning) {
            return;
        }
        
        isRunning = false;
        scheduler.shutdown();
        
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        logger.info("Offline sync manager stopped");
    }
    
    /**
     * Queue data for offline sync
     */
    public void queueForSync(String userMacAddress, OfflineSyncEntity.DataType dataType, 
                           String dataId, String dataJson) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                OfflineSyncEntity syncEntity = new OfflineSyncEntity(userMacAddress, dataType, dataId, dataJson);
                session.persist(syncEntity);
                tx.commit();
                logger.info("Queued data for sync: " + dataType + " for user " + userMacAddress);
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to queue data for sync", e);
                throw e;
            }
        }
    }
    
    /**
     * Sync pending data
     */
    private void syncPendingData() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Get pending sync items
                Query<OfflineSyncEntity> query = session.createQuery(
                    "FROM OfflineSyncEntity WHERE syncStatus = :status ORDER BY createdTime ASC", 
                    OfflineSyncEntity.class
                );
                query.setParameter("status", OfflineSyncEntity.SyncStatus.PENDING);
                query.setMaxResults(10); // Process 10 items at a time
                
                List<OfflineSyncEntity> pendingItems = query.list();
                
                for (OfflineSyncEntity item : pendingItems) {
                    try {
                        syncItem(item);
                        item.markAsSuccess();
                        session.merge(item);
                    } catch (Exception e) {
                        item.markAsFailed(e.getMessage());
                        session.merge(item);
                        logger.error("Failed to sync item: " + item.getSyncId(), e);
                    }
                }
                
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to sync pending data", e);
            }
        }
    }
    
    /**
     * Sync a single item
     */
    private void syncItem(OfflineSyncEntity item) {
        switch (item.getDataType()) {
            case USER_PROFILE:
                syncUserProfile(item);
                break;
            case GAME_RECORD:
                syncGameRecord(item);
                break;
            case LEADERBOARD_RECORD:
                syncLeaderboardRecord(item);
                break;
            case PLAYER_STATS:
                syncPlayerStats(item);
                break;
            case SAVE_DATA:
                syncSaveData(item);
                break;
            case SHOP_ITEM:
                syncShopItem(item);
                break;
            default:
                throw new IllegalArgumentException("Unknown data type: " + item.getDataType());
        }
    }
    
    /**
     * Sync user profile data
     */
    private void syncUserProfile(OfflineSyncEntity item) {
        // This would typically involve sending data to server
        // For now, we'll just mark it as synced
        logger.info("Syncing user profile for: " + item.getUserMacAddress());
    }
    
    /**
     * Sync game record data
     */
    private void syncGameRecord(OfflineSyncEntity item) {
        // This would typically involve sending data to server
        // For now, we'll just mark it as synced
        logger.info("Syncing game record for: " + item.getUserMacAddress());
    }
    
    /**
     * Sync leaderboard record data
     */
    private void syncLeaderboardRecord(OfflineSyncEntity item) {
        // This would typically involve sending data to server
        // For now, we'll just mark it as synced
        logger.info("Syncing leaderboard record for: " + item.getUserMacAddress());
    }
    
    /**
     * Sync player stats data
     */
    private void syncPlayerStats(OfflineSyncEntity item) {
        // This would typically involve sending data to server
        // For now, we'll just mark it as synced
        logger.info("Syncing player stats for: " + item.getUserMacAddress());
    }
    
    /**
     * Sync save data
     */
    private void syncSaveData(OfflineSyncEntity item) {
        // This would typically involve sending data to server
        // For now, we'll just mark it as synced
        logger.info("Syncing save data for: " + item.getUserMacAddress());
    }
    
    /**
     * Sync shop item data
     */
    private void syncShopItem(OfflineSyncEntity item) {
        // This would typically involve sending data to server
        // For now, we'll just mark it as synced
        logger.info("Syncing shop item for: " + item.getUserMacAddress());
    }
    
    /**
     * Get pending sync items for a user
     */
    public List<OfflineSyncEntity> getPendingSyncItems(String userMacAddress) {
        try (Session session = sessionFactory.openSession()) {
            Query<OfflineSyncEntity> query = session.createQuery(
                "FROM OfflineSyncEntity WHERE userMacAddress = :macAddress AND syncStatus = :status ORDER BY createdTime ASC", 
                OfflineSyncEntity.class
            );
            query.setParameter("macAddress", userMacAddress);
            query.setParameter("status", OfflineSyncEntity.SyncStatus.PENDING);
            return query.list();
        } catch (Exception e) {
            logger.error("Failed to get pending sync items", e);
            throw e;
        }
    }
    
    /**
     * Get failed sync items for a user
     */
    public List<OfflineSyncEntity> getFailedSyncItems(String userMacAddress) {
        try (Session session = sessionFactory.openSession()) {
            Query<OfflineSyncEntity> query = session.createQuery(
                "FROM OfflineSyncEntity WHERE userMacAddress = :macAddress AND syncStatus = :status ORDER BY createdTime ASC", 
                OfflineSyncEntity.class
            );
            query.setParameter("macAddress", userMacAddress);
            query.setParameter("status", OfflineSyncEntity.SyncStatus.FAILED);
            return query.list();
        } catch (Exception e) {
            logger.error("Failed to get failed sync items", e);
            throw e;
        }
    }
    
    /**
     * Retry failed sync items
     */
    public void retryFailedSyncItems(String userMacAddress) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Query<OfflineSyncEntity> query = session.createQuery(
                    "FROM OfflineSyncEntity WHERE userMacAddress = :macAddress AND syncStatus = :status", 
                    OfflineSyncEntity.class
                );
                query.setParameter("macAddress", userMacAddress);
                query.setParameter("status", OfflineSyncEntity.SyncStatus.FAILED);
                
                List<OfflineSyncEntity> failedItems = query.list();
                for (OfflineSyncEntity item : failedItems) {
                    item.markAsRetry();
                    session.merge(item);
                }
                
                tx.commit();
                logger.info("Retried " + failedItems.size() + " failed sync items for user: " + userMacAddress);
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to retry failed sync items", e);
                throw e;
            }
        }
    }
    
    /**
     * Cleanup old sync data
     */
    private void cleanupOldSyncData() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Delete synced items older than 7 days
                LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
                Query<OfflineSyncEntity> query = session.createQuery(
                    "FROM OfflineSyncEntity WHERE syncStatus = :status AND serverSyncTime < :cutoffDate", 
                    OfflineSyncEntity.class
                );
                query.setParameter("status", OfflineSyncEntity.SyncStatus.SYNCED);
                query.setParameter("cutoffDate", cutoffDate);
                
                List<OfflineSyncEntity> oldItems = query.list();
                for (OfflineSyncEntity item : oldItems) {
                    session.remove(item);
                }
                
                tx.commit();
                if (!oldItems.isEmpty()) {
                    logger.info("Cleaned up " + oldItems.size() + " old sync items");
                }
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to cleanup old sync data", e);
            }
        }
    }
    
    /**
     * Check if there are pending sync items
     */
    public boolean hasPendingSyncItems(String userMacAddress) {
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createQuery(
                "SELECT COUNT(*) FROM OfflineSyncEntity WHERE userMacAddress = :macAddress AND syncStatus = :status", 
                Long.class
            );
            query.setParameter("macAddress", userMacAddress);
            query.setParameter("status", OfflineSyncEntity.SyncStatus.PENDING);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            logger.error("Failed to check pending sync items", e);
            return false;
        }
    }
}
