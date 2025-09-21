package dev.aminhashemi.blueprinthell.database;

import dev.aminhashemi.blueprinthell.model.entities.UserProfileEntity;
import dev.aminhashemi.blueprinthell.model.entities.GameRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.PlayerStatsEntity;
import dev.aminhashemi.blueprinthell.model.UserProfile;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.core.exception.GlobalExceptionManager;
import dev.aminhashemi.blueprinthell.core.exception.ExceptionResponse;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Database manager for user profile operations
 */
public class UserProfileDatabaseManager {
    private final SessionFactory sessionFactory;
    private final Logger logger;
    
    public UserProfileDatabaseManager() {
        this.sessionFactory = DatabaseManager.getInstance().getSessionFactory();
        this.logger = Logger.getInstance();
    }
    
    /**
     * Get or create user profile by MAC address
     */
    public UserProfileEntity getUserProfile(String macAddress) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                UserProfileEntity profile = session.get(UserProfileEntity.class, macAddress);
                
                if (profile == null) {
                    profile = new UserProfileEntity(macAddress, "Player_" + macAddress.substring(0, 8));
                    session.persist(profile);
                    logger.info("Created new user profile for MAC: " + macAddress);
                }
                
                tx.commit();
                return profile;
            } catch (Exception e) {
                tx.rollback();
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                logger.error("Failed to get user profile: " + response.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Update user profile
     */
    public void updateUserProfile(UserProfileEntity profile) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                profile.updateSyncTime();
                session.merge(profile);
                tx.commit();
                logger.info("Updated user profile: " + profile.getUsername());
            } catch (Exception e) {
                tx.rollback();
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                logger.error("Failed to update user profile: " + response.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Add a game record to user profile
     */
    public void addGameRecord(String macAddress, GameRecordEntity record) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                UserProfileEntity profile = session.get(UserProfileEntity.class, macAddress);
                if (profile != null) {
                    record.setUserMacAddress(macAddress);
                    session.persist(record);
                    profile.addGameRecord(record);
                    session.merge(profile);
                }
                tx.commit();
                logger.info("Added game record for user: " + macAddress);
            } catch (Exception e) {
                tx.rollback();
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                logger.error("Failed to add game record: " + response.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Get user's game history
     */
    public List<GameRecordEntity> getUserGameHistory(String macAddress, int limit) {
        try (Session session = sessionFactory.openSession()) {
            Query<GameRecordEntity> query = session.createQuery(
                "FROM GameRecordEntity WHERE userMacAddress = :macAddress ORDER BY timestamp DESC", 
                GameRecordEntity.class
            );
            query.setParameter("macAddress", macAddress);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            logger.error("Failed to get user game history: " + response.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get or create player stats
     */
    public PlayerStatsEntity getPlayerStats(String macAddress, String username) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Query<PlayerStatsEntity> query = session.createQuery(
                    "FROM PlayerStatsEntity WHERE userMacAddress = :macAddress", 
                    PlayerStatsEntity.class
                );
                query.setParameter("macAddress", macAddress);
                PlayerStatsEntity stats = query.uniqueResult();
                
                if (stats == null) {
                    stats = new PlayerStatsEntity(username, macAddress);
                    session.persist(stats);
                    logger.info("Created new player stats for: " + username);
                }
                
                tx.commit();
                return stats;
            } catch (Exception e) {
                tx.rollback();
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                logger.error("Failed to get player stats: " + response.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Update player stats
     */
    public void updatePlayerStats(PlayerStatsEntity stats) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                stats.setLastUpdated(LocalDateTime.now());
                session.merge(stats);
                tx.commit();
                logger.info("Updated player stats for: " + stats.getPlayerName());
            } catch (Exception e) {
                tx.rollback();
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                logger.error("Failed to update player stats: " + response.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Get all user profiles
     */
    public List<UserProfileEntity> getAllUserProfiles() {
        try (Session session = sessionFactory.openSession()) {
            Query<UserProfileEntity> query = session.createQuery("FROM UserProfileEntity", UserProfileEntity.class);
            return query.list();
        } catch (Exception e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            logger.error("Failed to get all user profiles: " + response.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Get online users
     */
    public List<UserProfileEntity> getOnlineUsers() {
        try (Session session = sessionFactory.openSession()) {
            Query<UserProfileEntity> query = session.createQuery(
                "FROM UserProfileEntity WHERE isOnline = true", 
                UserProfileEntity.class
            );
            return query.list();
        } catch (Exception e) {
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            logger.error("Failed to get online users: " + response.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Set user online status
     */
    public void setUserOnlineStatus(String macAddress, boolean isOnline) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                UserProfileEntity profile = session.get(UserProfileEntity.class, macAddress);
                if (profile != null) {
                    profile.setOnline(isOnline);
                    if (isOnline) {
                        profile.updateLastLogin();
                    }
                    session.merge(profile);
                }
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                logger.error("Failed to set user online status: " + response.getMessage(), e);
                throw e;
            }
        }
    }
    
    /**
     * Convert UserProfileEntity to UserProfile (for JSON communication)
     */
    public UserProfile toUserProfile(UserProfileEntity entity) {
        UserProfile profile = new UserProfile(entity.getMacAddress(), entity.getUsername());
        profile.setTotalXP(entity.getTotalXP());
        profile.setTotalCoins(entity.getTotalCoins());
        profile.setUnlockedFeatures(entity.getUnlockedFeatures());
        profile.setActiveAbilities(entity.getActiveAbilities());
        profile.setLastLoginTime(entity.getLastLoginTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        profile.setAccountCreatedTime(entity.getAccountCreatedTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
        
        // Convert game history
        for (GameRecordEntity record : entity.getGameHistory()) {
            UserProfile.GameRecord gameRecord = new UserProfile.GameRecord(
                record.getLevelName(),
                record.getCompletionTime(),
                record.getXpEarned(),
                record.getCoinsEarned(),
                record.getPacketLossPercentage()
            );
            gameRecord.setTimestamp(record.getTimestamp().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            profile.addGameRecord(gameRecord);
        }
        
        return profile;
    }
    
    /**
     * Convert UserProfile to UserProfileEntity
     */
    public UserProfileEntity fromUserProfile(UserProfile profile) {
        UserProfileEntity entity = new UserProfileEntity(profile.getMacAddress(), profile.getUsername());
        entity.setTotalXP(profile.getTotalXP());
        entity.setTotalCoins(profile.getTotalCoins());
        entity.setUnlockedFeatures(profile.getUnlockedFeatures());
        entity.setActiveAbilities(profile.getActiveAbilities());
        entity.setLastLoginTime(LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(profile.getLastLoginTime()),
            java.time.ZoneId.systemDefault()
        ));
        entity.setAccountCreatedTime(LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(profile.getAccountCreatedTime()),
            java.time.ZoneId.systemDefault()
        ));
        
        return entity;
    }
}
