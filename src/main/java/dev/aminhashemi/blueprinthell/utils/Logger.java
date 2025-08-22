package dev.aminhashemi.blueprinthell.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Centralized logging system for the game */
public class Logger {
    private static Logger instance;
    private static final String LOG_FILE = "game.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    
    private PrintWriter logWriter;
    private boolean initialized = false;
    
    private Logger() {
        initializeLogger();
    }
    
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    private void initializeLogger() {
        try {
            File logFile = new File(LOG_FILE);
            logWriter = new PrintWriter(new FileWriter(logFile, true), true);
            initialized = true;
            // Avoid circular dependency during initialization
        } catch (IOException e) {
            // Fallback to console if file logging fails
            System.err.println("Failed to initialize file logger: " + e.getMessage());
            initialized = false;
        }
    }
    
    public enum Level {
        DEBUG, INFO, WARNING, ERROR
    }
    
    public void log(Level level, String message) {
        String timestamp = DATE_FORMAT.format(new Date());
        String logEntry = String.format("[%s] [%s] %s", timestamp, level, message);
        
        if (initialized && logWriter != null) {
            logWriter.println(logEntry);
        }
        
        // Log to file only
    }
    
    public void debug(String message) {
        log(Level.DEBUG, message);
    }
    
    public void info(String message) {
        log(Level.INFO, message);
    }
    
    public void warning(String message) {
        log(Level.WARNING, message);
    }
    
    public void error(String message) {
        log(Level.ERROR, message);
    }
    
    public void error(String message, Throwable throwable) {
        error(message + ": " + throwable.getMessage());
        if (initialized && logWriter != null) {
            throwable.printStackTrace(logWriter);
        }
    }
    
    public void close() {
        if (logWriter != null) {
            logWriter.close();
        }
    }
}
