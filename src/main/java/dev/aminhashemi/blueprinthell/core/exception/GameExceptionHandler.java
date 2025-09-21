package dev.aminhashemi.blueprinthell.core.exception;

import dev.aminhashemi.blueprinthell.utils.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.TimeoutException;

/**
 * Global exception handler for BlueprintHell game.
 * Handles database and network connection errors centrally.
 * 
 * This class uses reflection-based annotation processing to automatically
 * handle exceptions thrown throughout the application.
 */
@GlobalExceptionHandler
public class GameExceptionHandler {
    
    private static final Logger logger = Logger.getInstance();
    
    // ==================== DATABASE EXCEPTION HANDLERS ====================
    
    /**
     * Handles SQL connection errors
     */
    @ExceptionHandler({SQLException.class})
    public ExceptionResponse handleSQLException(SQLException e) {
        String errorCode = "DB_CONNECTION_ERROR";
        String message = "Database connection failed. Please check your database configuration.";
        
        logger.error("Database SQL Error: " + e.getMessage(), e);
        
        return new ExceptionResponse(message, errorCode, true, true);
    }
    
    /**
     * Handles database timeout errors
     */
    @ExceptionHandler({SQLTimeoutException.class})
    public ExceptionResponse handleSQLTimeout(SQLTimeoutException e) {
        String errorCode = "DB_TIMEOUT_ERROR";
        String message = "Database operation timed out. Please try again.";
        
        logger.warning("Database timeout: " + e.getMessage());
        
        return new ExceptionResponse(message, errorCode, true, true);
    }
    
    /**
     * Handles database constraint violations
     */
    @ExceptionHandler({java.sql.SQLIntegrityConstraintViolationException.class})
    public ExceptionResponse handleConstraintViolation(java.sql.SQLIntegrityConstraintViolationException e) {
        String errorCode = "DB_CONSTRAINT_ERROR";
        String message = "Database constraint violation. The operation cannot be completed.";
        
        logger.warning("Database constraint violation: " + e.getMessage());
        
        return new ExceptionResponse(message, errorCode, true, false);
    }
    
    // ==================== NETWORK EXCEPTION HANDLERS ====================
    
    /**
     * Handles connection refused errors
     */
    @ExceptionHandler({ConnectException.class})
    public ExceptionResponse handleConnectionRefused(ConnectException e) {
        String errorCode = "NET_CONNECTION_REFUSED";
        String message = "Cannot connect to server. Server may be down or unreachable.";
        
        logger.warning("Connection refused: " + e.getMessage());
        
        return new ExceptionResponse(message, errorCode, true, true);
    }
    
    /**
     * Handles socket timeout errors
     */
    @ExceptionHandler({SocketTimeoutException.class})
    public ExceptionResponse handleSocketTimeout(SocketTimeoutException e) {
        String errorCode = "NET_TIMEOUT_ERROR";
        String message = "Network operation timed out. Please check your connection.";
        
        logger.warning("Socket timeout: " + e.getMessage());
        
        return new ExceptionResponse(message, errorCode, true, true);
    }
    
    /**
     * Handles general socket errors
     */
    @ExceptionHandler({SocketException.class})
    public ExceptionResponse handleSocketException(SocketException e) {
        String errorCode = "NET_SOCKET_ERROR";
        String message = "Network socket error occurred. Please try reconnecting.";
        
        logger.error("Socket error: " + e.getMessage(), e);
        
        return new ExceptionResponse(message, errorCode, true, true);
    }
    
    /**
     * Handles general IO errors during network operations
     */
    @ExceptionHandler({IOException.class})
    public ExceptionResponse handleIOException(IOException e) {
        String errorCode = "NET_IO_ERROR";
        String message = "Network I/O error occurred. Please check your connection.";
        
        logger.error("Network I/O error: " + e.getMessage(), e);
        
        return new ExceptionResponse(message, errorCode, true, true);
    }
    
    /**
     * Handles general timeout errors
     */
    @ExceptionHandler({TimeoutException.class})
    public ExceptionResponse handleTimeout(TimeoutException e) {
        String errorCode = "GENERAL_TIMEOUT_ERROR";
        String message = "Operation timed out. Please try again.";
        
        logger.warning("General timeout: " + e.getMessage());
        
        return new ExceptionResponse(message, errorCode, true, true);
    }
    
    // ==================== GENERAL EXCEPTION HANDLERS ====================
    
    /**
     * Handles illegal argument exceptions
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ExceptionResponse handleIllegalArgument(IllegalArgumentException e) {
        String errorCode = "INVALID_ARGUMENT";
        String message = "Invalid argument provided: " + e.getMessage();
        
        logger.warning("Invalid argument: " + e.getMessage());
        
        return new ExceptionResponse(message, errorCode, true, false);
    }
    
    /**
     * Handles null pointer exceptions
     */
    @ExceptionHandler({NullPointerException.class})
    public ExceptionResponse handleNullPointer(NullPointerException e) {
        String errorCode = "NULL_POINTER_ERROR";
        String message = "Null pointer exception occurred. This may indicate a bug.";
        
        logger.error("Null pointer exception: " + e.getMessage(), e);
        
        return new ExceptionResponse(message, errorCode, true, false);
    }
    
    /**
     * Handles runtime exceptions
     */
    @ExceptionHandler({RuntimeException.class})
    public ExceptionResponse handleRuntime(RuntimeException e) {
        String errorCode = "RUNTIME_ERROR";
        String message = "An unexpected runtime error occurred: " + e.getMessage();
        
        logger.error("Runtime error: " + e.getMessage(), e);
        
        return new ExceptionResponse(message, errorCode, true, false);
    }
    
    /**
     * Handles all other exceptions (catch-all)
     */
    @ExceptionHandler({Exception.class})
    public ExceptionResponse handleGeneric(Exception e) {
        String errorCode = "GENERAL_ERROR";
        String message = "An unexpected error occurred: " + e.getMessage();
        
        logger.error("General error: " + e.getMessage(), e);
        
        return new ExceptionResponse(message, errorCode, true, false);
    }
}
