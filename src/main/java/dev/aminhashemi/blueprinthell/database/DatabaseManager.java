package dev.aminhashemi.blueprinthell.database;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import dev.aminhashemi.blueprinthell.utils.Logger;
import dev.aminhashemi.blueprinthell.core.exception.GlobalExceptionManager;
import dev.aminhashemi.blueprinthell.core.exception.ExceptionResponse;

/**
 * Singleton database manager for Hibernate session factory
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private SessionFactory sessionFactory;
    private boolean isInitialized = false;
    
    private DatabaseManager() {
        // Private constructor for singleton
    }
    
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Initialize the database connection and session factory
     */
    public synchronized void initialize() {
        if (isInitialized) {
            return;
        }
        
        try {
            DatabaseConfigManager configManager = DatabaseConfigManager.getInstance();
            Configuration configuration = new Configuration();
            
            // Set database properties
            configuration.setProperty("hibernate.connection.driver_class", configManager.getDatabaseDriver());
            configuration.setProperty("hibernate.connection.url", configManager.getDatabaseUrl());
            configuration.setProperty("hibernate.connection.username", configManager.getDatabaseUsername());
            configuration.setProperty("hibernate.connection.password", configManager.getDatabasePassword());
            configuration.setProperty("hibernate.dialect", configManager.getDatabaseDialect());
            
            // Set connection pool properties
            configuration.setProperty("hibernate.hikari.minimumIdle", String.valueOf(configManager.getConnectionPoolMinimumIdle()));
            configuration.setProperty("hibernate.hikari.maximumPoolSize", String.valueOf(configManager.getConnectionPoolMaximumPoolSize()));
            configuration.setProperty("hibernate.hikari.idleTimeout", String.valueOf(configManager.getConnectionPoolIdleTimeout()));
            configuration.setProperty("hibernate.hikari.maxLifetime", String.valueOf(configManager.getConnectionPoolMaxLifetime()));
            configuration.setProperty("hibernate.hikari.connectionTimeout", String.valueOf(configManager.getConnectionPoolConnectionTimeout()));
            
            // Set Hibernate properties
            configuration.setProperty("hibernate.show_sql", String.valueOf(configManager.isHibernateShowSql()));
            configuration.setProperty("hibernate.format_sql", String.valueOf(configManager.isHibernateFormatSql()));
            configuration.setProperty("hibernate.hbm2ddl.auto", configManager.getHibernateHbm2ddlAuto());
            // Disable caching for now to avoid additional dependencies
            configuration.setProperty("hibernate.cache.use_second_level_cache", "false");
            configuration.setProperty("hibernate.cache.use_query_cache", "false");
            configuration.setProperty("hibernate.jdbc.batch_size", String.valueOf(configManager.getHibernateJdbcBatchSize()));
            configuration.setProperty("hibernate.order_inserts", String.valueOf(configManager.isHibernateOrderInserts()));
            configuration.setProperty("hibernate.order_updates", String.valueOf(configManager.isHibernateOrderUpdates()));
            configuration.setProperty("hibernate.jdbc.batch_versioned_data", String.valueOf(configManager.isHibernateJdbcBatchVersionedData()));
            
            // Add entity mappings
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.UserProfileEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.GameRecordEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.LeaderboardRecordEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.PlayerStatsEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.LevelDataEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.SystemDataEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.WireDataEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.PortDataEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.ShopItemEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.SaveDataEntity.class);
            configuration.addAnnotatedClass(dev.aminhashemi.blueprinthell.model.entities.OfflineSyncEntity.class);
            
                sessionFactory = configuration.buildSessionFactory();
                isInitialized = true;
                
                // Add shutdown hook to ensure proper cleanup
                addShutdownHook();
                
                Logger.getInstance().info("Database initialized successfully with " + configManager.getDatabaseType() + " database");
        } catch (Exception e) {
            // Use Global Exception Handling
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            Logger.getInstance().error("Database initialization failed: " + response.getMessage(), e);
            throw new RuntimeException("Database initialization failed: " + response.getMessage(), e);
        }
    }
    
    /**
     * Get the Hibernate session factory
     */
    public SessionFactory getSessionFactory() {
        if (!isInitialized) {
            initialize();
        }
        return sessionFactory;
    }
    
    /**
     * Check if database is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Close the database connection
     */
    public synchronized void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            isInitialized = false;
            Logger.getInstance().info("Database connection closed");
        }
    }
    
    /**
     * Test database connection
     */
    public boolean testConnection() {
        try {
            if (!isInitialized) {
                initialize();
            }
            
            try (var session = sessionFactory.openSession()) {
                return session.isConnected();
            }
        } catch (Exception e) {
            // Use Global Exception Handling
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            Logger.getInstance().error("Database connection test failed: " + response.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Add shutdown hook to ensure database is properly closed
     */
    private void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.getInstance().info("Application shutting down, closing database connections...");
            shutdown();
        }));
    }
}
