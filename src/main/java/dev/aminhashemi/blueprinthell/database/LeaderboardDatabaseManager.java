package dev.aminhashemi.blueprinthell.database;

import dev.aminhashemi.blueprinthell.model.entities.LeaderboardRecordEntity;
import dev.aminhashemi.blueprinthell.model.entities.PlayerStatsEntity;
import dev.aminhashemi.blueprinthell.model.LeaderboardData;
import dev.aminhashemi.blueprinthell.utils.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

/**
 * Database manager for leaderboard operations
 */
public class LeaderboardDatabaseManager {
    private final SessionFactory sessionFactory;
    private final Logger logger;
    
    public LeaderboardDatabaseManager() {
        this.sessionFactory = DatabaseManager.getInstance().getSessionFactory();
        this.logger = Logger.getInstance();
    }
    
    /**
     * Add a leaderboard record
     */
    public void addLeaderboardRecord(LeaderboardRecordEntity record) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(record);
                tx.commit();
                logger.info("Added leaderboard record for: " + record.getPlayerName());
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to add leaderboard record", e);
                throw e;
            }
        }
    }
    
    /**
     * Add player stats
     */
    public void addPlayerStats(PlayerStatsEntity stats) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(stats);
                tx.commit();
                logger.info("Added player stats for: " + stats.getPlayerName());
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to add player stats", e);
                throw e;
            }
        }
    }
    
    /**
     * Get leaderboard records for a specific level
     */
    public List<LeaderboardRecordEntity> getLevelRecords(String levelName, int limit) {
        try (Session session = sessionFactory.openSession()) {
            Query<LeaderboardRecordEntity> query = session.createQuery(
                "FROM LeaderboardRecordEntity WHERE levelName = :levelName ORDER BY completionTime ASC", 
                LeaderboardRecordEntity.class
            );
            query.setParameter("levelName", levelName);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            logger.error("Failed to get level records", e);
            throw e;
        }
    }
    
    /**
     * Get global leaderboard records
     */
    public List<LeaderboardRecordEntity> getGlobalRecords(int limit) {
        try (Session session = sessionFactory.openSession()) {
            Query<LeaderboardRecordEntity> query = session.createQuery(
                "FROM LeaderboardRecordEntity ORDER BY completionTime ASC", 
                LeaderboardRecordEntity.class
            );
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            logger.error("Failed to get global records", e);
            throw e;
        }
    }
    
    /**
     * Get player's best record for a level
     */
    public LeaderboardRecordEntity getPlayerBestRecord(String playerName, String levelName) {
        try (Session session = sessionFactory.openSession()) {
            Query<LeaderboardRecordEntity> query = session.createQuery(
                "FROM LeaderboardRecordEntity WHERE playerName = :playerName AND levelName = :levelName ORDER BY completionTime ASC", 
                LeaderboardRecordEntity.class
            );
            query.setParameter("playerName", playerName);
            query.setParameter("levelName", levelName);
            query.setMaxResults(1);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Failed to get player best record", e);
            throw e;
        }
    }
    
    /**
     * Get player's best record for a level by MAC address
     */
    public LeaderboardRecordEntity getPlayerBestRecordByMac(String userMacAddress, String levelName) {
        try (Session session = sessionFactory.openSession()) {
            Query<LeaderboardRecordEntity> query = session.createQuery(
                "FROM LeaderboardRecordEntity WHERE userMacAddress = :userMacAddress AND levelName = :levelName ORDER BY completionTime ASC", 
                LeaderboardRecordEntity.class
            );
            query.setParameter("userMacAddress", userMacAddress);
            query.setParameter("levelName", levelName);
            query.setMaxResults(1);
            return query.uniqueResult();
        } catch (Exception e) {
            logger.error("Failed to get player best record by MAC", e);
            throw e;
        }
    }
    
    /**
     * Get all level names that have records
     */
    public List<String> getLevelNames() {
        try (Session session = sessionFactory.openSession()) {
            Query<String> query = session.createQuery(
                "SELECT DISTINCT levelName FROM LeaderboardRecordEntity ORDER BY levelName", 
                String.class
            );
            return query.list();
        } catch (Exception e) {
            logger.error("Failed to get level names", e);
            throw e;
        }
    }
    
    /**
     * Get leaderboard data for a specific level
     */
    public LeaderboardData getLevelLeaderboardData(String levelName) {
        LeaderboardData leaderboardData = new LeaderboardData();
        
        // Get level records
        List<LeaderboardRecordEntity> records = getLevelRecords(levelName, 10);
        for (LeaderboardRecordEntity record : records) {
            LeaderboardData.PlayerRecord playerRecord = new LeaderboardData.PlayerRecord(
                record.getPlayerName(),
                record.getCompletionTime(),
                record.getXpEarned(),
                record.getLevelNumber(),
                record.getPacketLossPercentage(),
                record.getCoinsEarned()
            );
            leaderboardData.addRecord(levelName, playerRecord);
        }
        
        return leaderboardData;
    }
    
    /**
     * Get global leaderboard data
     */
    public LeaderboardData getGlobalLeaderboardData() {
        LeaderboardData leaderboardData = new LeaderboardData();
        
        // Get global records
        List<LeaderboardRecordEntity> records = getGlobalRecords(50);
        for (LeaderboardRecordEntity record : records) {
            LeaderboardData.PlayerRecord playerRecord = new LeaderboardData.PlayerRecord(
                record.getPlayerName(),
                record.getCompletionTime(),
                record.getXpEarned(),
                record.getLevelNumber(),
                record.getPacketLossPercentage(),
                record.getCoinsEarned()
            );
            leaderboardData.getGlobalRecords().add(playerRecord);
        }
        
        return leaderboardData;
    }
    
    /**
     * Get player statistics
     */
    public LeaderboardData.PlayerStats getPlayerStats(String playerName) {
        try (Session session = sessionFactory.openSession()) {
            Query<PlayerStatsEntity> query = session.createQuery(
                "FROM PlayerStatsEntity WHERE playerName = :playerName", 
                PlayerStatsEntity.class
            );
            query.setParameter("playerName", playerName);
            PlayerStatsEntity stats = query.uniqueResult();
            
            if (stats == null) {
                return new LeaderboardData.PlayerStats();
            }
            
            LeaderboardData.PlayerStats playerStats = new LeaderboardData.PlayerStats();
            playerStats.playerName = stats.getPlayerName();
            playerStats.totalXP = stats.getTotalXP();
            playerStats.totalCoins = stats.getTotalCoins();
            playerStats.levelsCompleted = stats.getLevelsCompleted();
            playerStats.bestTime = stats.getBestTime() != null ? stats.getBestTime() : Long.MAX_VALUE;
            playerStats.bestXP = stats.getBestXP();
            playerStats.levelBestTimes = stats.getLevelBestTimes();
            playerStats.levelBestXP = stats.getLevelBestXP();
            
            return playerStats;
        } catch (Exception e) {
            logger.error("Failed to get player stats", e);
            throw e;
        }
    }
    
    /**
     * Update player statistics
     */
    public void updatePlayerStats(String playerName, int levelNumber, long completionTime, 
                                int xpEarned, int coinsEarned) {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Query<PlayerStatsEntity> query = session.createQuery(
                    "FROM PlayerStatsEntity WHERE playerName = :playerName", 
                    PlayerStatsEntity.class
                );
                query.setParameter("playerName", playerName);
                PlayerStatsEntity stats = query.uniqueResult();
                
                if (stats == null) {
                    stats = new PlayerStatsEntity(playerName, null);
                    session.persist(stats);
                }
                
                stats.updateStats(playerName, levelNumber, completionTime, xpEarned, coinsEarned);
                session.merge(stats);
                tx.commit();
                logger.info("Updated player stats for: " + playerName);
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to update player stats", e);
                throw e;
            }
        }
    }
    
    /**
     * Clear old records (keep only top records)
     */
    public void cleanupOldRecords() {
        try (Session session = sessionFactory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                // Get all level names
                List<String> levelNames = getLevelNames();
                
                for (String levelName : levelNames) {
                    // Keep only top 10 records per level
                    Query<LeaderboardRecordEntity> query = session.createQuery(
                        "FROM LeaderboardRecordEntity WHERE levelName = :levelName ORDER BY completionTime ASC", 
                        LeaderboardRecordEntity.class
                    );
                    query.setParameter("levelName", levelName);
                    List<LeaderboardRecordEntity> records = query.list();
                    
                    if (records.size() > 10) {
                        List<LeaderboardRecordEntity> toDelete = records.subList(10, records.size());
                        for (LeaderboardRecordEntity record : toDelete) {
                            session.remove(record);
                        }
                    }
                }
                
                // Keep only top 50 global records
                Query<LeaderboardRecordEntity> globalQuery = session.createQuery(
                    "FROM LeaderboardRecordEntity ORDER BY completionTime ASC", 
                    LeaderboardRecordEntity.class
                );
                List<LeaderboardRecordEntity> globalRecords = globalQuery.list();
                
                if (globalRecords.size() > 50) {
                    List<LeaderboardRecordEntity> toDelete = globalRecords.subList(50, globalRecords.size());
                    for (LeaderboardRecordEntity record : toDelete) {
                        session.remove(record);
                    }
                }
                
                tx.commit();
                logger.info("Cleaned up old leaderboard records");
            } catch (Exception e) {
                tx.rollback();
                logger.error("Failed to cleanup old records", e);
                throw e;
            }
        }
    }
}
