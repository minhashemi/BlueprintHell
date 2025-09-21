package dev.aminhashemi.blueprinthell.core.exception;

import dev.aminhashemi.blueprinthell.utils.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;

/**
 * Example class demonstrating how to use Global Exception Handling
 * in your BlueprintHell game application.
 */
public class ExceptionHandlingExample {
    
    private static final Logger logger = Logger.getInstance();
    
    /**
     * Example of database operation with Global Exception Handling
     */
    public static void databaseOperationExample() {
        try {
            // Simulate database operation that might fail
            performDatabaseOperation();
        } catch (Exception e) {
            // Instead of traditional try-catch, use Global Exception Handling
            ExceptionResponse response = GlobalExceptionManager.handleException(e);
            
            // The response contains all the information you need
            logger.error("Database operation failed: " + response.getMessage());
            
            // You can check if the operation should be retried
            if (response.shouldRetry()) {
                logger.info("Retrying database operation...");
                // Implement retry logic here
            }
        }
    }
    
    /**
     * Example of network operation with Global Exception Handling
     */
    public static void networkOperationExample() {
        try {
            // Simulate network operation that might fail
            performNetworkOperation();
        } catch (Exception e) {
            // Global Exception Handling automatically determines the appropriate response
            String errorMessage = GlobalExceptionManager.handleExceptionMessage(e);
            logger.error("Network operation failed: " + errorMessage);
            
            // The error message is already user-friendly and localized
            showUserError(errorMessage);
        }
    }
    
    /**
     * Example of simplified exception handling
     */
    public static void simplifiedExceptionHandling() {
        try {
            // Any operation that might throw an exception
            performRiskyOperation();
        } catch (Exception e) {
            // One line to handle any exception with proper logging and user messaging
            String userMessage = GlobalExceptionManager.handleExceptionMessage(e);
            logger.error("Operation failed: " + userMessage);
            showUserError(userMessage);
        }
    }
    
    /**
     * Example of checking if a handler exists before attempting operation
     */
    public static void conditionalExceptionHandling() {
        // Check if we have a handler for specific exception types
        if (GlobalExceptionManager.hasHandler(SQLException.class)) {
            logger.info("Database exception handling is available");
        }
        
        if (GlobalExceptionManager.hasHandler(ConnectException.class)) {
            logger.info("Network exception handling is available");
        }
        
        try {
            performOperation();
        } catch (Exception e) {
            // Only handle if we have a proper handler
            if (GlobalExceptionManager.hasHandler(e.getClass())) {
                ExceptionResponse response = GlobalExceptionManager.handleException(e);
                logger.error("Handled exception: " + response.getMessage());
            } else {
                logger.error("Unhandled exception: " + e.getMessage());
            }
        }
    }
    
    // Mock methods for demonstration
    private static void performDatabaseOperation() throws SQLException {
        throw new SQLException("Connection failed");
    }
    
    private static void performNetworkOperation() throws ConnectException {
        throw new ConnectException("Server unreachable");
    }
    
    private static void performRiskyOperation() throws IOException {
        throw new IOException("File not found");
    }
    
    private static void performOperation() throws Exception {
        // Some operation that might fail
    }
    
    private static void showUserError(String message) {
        // In a real application, this would show a user-friendly error dialog
        System.out.println("User Error: " + message);
    }
    
    /**
     * Main method to demonstrate usage
     */
    public static void main(String[] args) {
        // Initialize Global Exception Handling
        GlobalExceptionManager.initialize();
        
        System.out.println("📚 Global Exception Handling Usage Examples");
        System.out.println("=" + "=".repeat(50));
        
        System.out.println("\n1. Database Operation Example:");
        databaseOperationExample();
        
        System.out.println("\n2. Network Operation Example:");
        networkOperationExample();
        
        System.out.println("\n3. Simplified Exception Handling:");
        simplifiedExceptionHandling();
        
        System.out.println("\n4. Conditional Exception Handling:");
        conditionalExceptionHandling();
        
        System.out.println("\n✅ All examples completed!");
    }
}
