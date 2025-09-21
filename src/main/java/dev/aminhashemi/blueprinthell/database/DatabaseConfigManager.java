package dev.aminhashemi.blueprinthell.database;

import dev.aminhashemi.blueprinthell.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Database configuration manager
 */
public class DatabaseConfigManager {
    private static final String CONFIG_FILE = "database.properties";
    private static DatabaseConfigManager instance;
    private Properties properties;
    private Logger logger;
    
    private DatabaseConfigManager() {
        this.logger = Logger.getInstance();
        loadProperties();
    }
    
    public static synchronized DatabaseConfigManager getInstance() {
        if (instance == null) {
            instance = new DatabaseConfigManager();
        }
        return instance;
    }
    
    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                logger.info("Database properties file not found, using default values");
                setDefaultProperties();
                return;
            }
            properties.load(input);
            logger.info("Database properties loaded successfully");
        } catch (IOException e) {
            logger.error("Failed to load database properties", e);
            setDefaultProperties();
        }
    }
    
    private void setDefaultProperties() {
        properties.setProperty("database.type", "h2");
        properties.setProperty("h2.url", "jdbc:h2:./database/blueprinthell;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");
        properties.setProperty("h2.username", "sa");
        properties.setProperty("h2.password", "");
        properties.setProperty("h2.driver", "org.h2.Driver");
        properties.setProperty("h2.dialect", "org.hibernate.dialect.H2Dialect");
        properties.setProperty("connection.pool.minimumIdle", "5");
        properties.setProperty("connection.pool.maximumPoolSize", "20");
        properties.setProperty("connection.pool.idleTimeout", "30000");
        properties.setProperty("connection.pool.maxLifetime", "2000000");
        properties.setProperty("connection.pool.connectionTimeout", "30000");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "true");
        properties.setProperty("hibernate.hbm2ddl.auto", "update");
        properties.setProperty("hibernate.cache.use_second_level_cache", "true");
        properties.setProperty("hibernate.cache.use_query_cache", "true");
        properties.setProperty("hibernate.jdbc.batch_size", "20");
        properties.setProperty("hibernate.order_inserts", "true");
        properties.setProperty("hibernate.order_updates", "true");
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("offline.sync.interval.seconds", "30");
        properties.setProperty("offline.sync.cleanup.interval.minutes", "5");
        properties.setProperty("offline.sync.max.retry.attempts", "3");
        properties.setProperty("offline.sync.retry.delay.seconds", "60");
    }
    
    public String getDatabaseType() {
        return properties.getProperty("database.type", "h2");
    }
    
    public String getDatabaseUrl() {
        String dbType = getDatabaseType();
        return properties.getProperty(dbType + ".url");
    }
    
    public String getDatabaseUsername() {
        String dbType = getDatabaseType();
        return properties.getProperty(dbType + ".username");
    }
    
    public String getDatabasePassword() {
        String dbType = getDatabaseType();
        return properties.getProperty(dbType + ".password");
    }
    
    public String getDatabaseDriver() {
        String dbType = getDatabaseType();
        return properties.getProperty(dbType + ".driver");
    }
    
    public String getDatabaseDialect() {
        String dbType = getDatabaseType();
        return properties.getProperty(dbType + ".dialect");
    }
    
    public int getConnectionPoolMinimumIdle() {
        return Integer.parseInt(properties.getProperty("connection.pool.minimumIdle", "5"));
    }
    
    public int getConnectionPoolMaximumPoolSize() {
        return Integer.parseInt(properties.getProperty("connection.pool.maximumPoolSize", "20"));
    }
    
    public int getConnectionPoolIdleTimeout() {
        return Integer.parseInt(properties.getProperty("connection.pool.idleTimeout", "30000"));
    }
    
    public int getConnectionPoolMaxLifetime() {
        return Integer.parseInt(properties.getProperty("connection.pool.maxLifetime", "2000000"));
    }
    
    public int getConnectionPoolConnectionTimeout() {
        return Integer.parseInt(properties.getProperty("connection.pool.connectionTimeout", "30000"));
    }
    
    public boolean isHibernateShowSql() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.show_sql", "false"));
    }
    
    public boolean isHibernateFormatSql() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.format_sql", "true"));
    }
    
    public String getHibernateHbm2ddlAuto() {
        return properties.getProperty("hibernate.hbm2ddl.auto", "update");
    }
    
    public boolean isHibernateCacheUseSecondLevelCache() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.cache.use_second_level_cache", "true"));
    }
    
    public boolean isHibernateCacheUseQueryCache() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.cache.use_query_cache", "true"));
    }
    
    public int getHibernateJdbcBatchSize() {
        return Integer.parseInt(properties.getProperty("hibernate.jdbc.batch_size", "20"));
    }
    
    public boolean isHibernateOrderInserts() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.order_inserts", "true"));
    }
    
    public boolean isHibernateOrderUpdates() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.order_updates", "true"));
    }
    
    public boolean isHibernateJdbcBatchVersionedData() {
        return Boolean.parseBoolean(properties.getProperty("hibernate.jdbc.batch_versioned_data", "true"));
    }
    
    public int getOfflineSyncIntervalSeconds() {
        return Integer.parseInt(properties.getProperty("offline.sync.interval.seconds", "30"));
    }
    
    public int getOfflineSyncCleanupIntervalMinutes() {
        return Integer.parseInt(properties.getProperty("offline.sync.cleanup.interval.minutes", "5"));
    }
    
    public int getOfflineSyncMaxRetryAttempts() {
        return Integer.parseInt(properties.getProperty("offline.sync.max.retry.attempts", "3"));
    }
    
    public int getOfflineSyncRetryDelaySeconds() {
        return Integer.parseInt(properties.getProperty("offline.sync.retry.delay.seconds", "60"));
    }
    
    public Properties getProperties() {
        return new Properties(properties);
    }
}
