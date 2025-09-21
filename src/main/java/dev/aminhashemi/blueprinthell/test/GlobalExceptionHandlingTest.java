package dev.aminhashemi.blueprinthell.test;

import dev.aminhashemi.blueprinthell.core.exception.GlobalExceptionManager;
import dev.aminhashemi.blueprinthell.core.exception.ExceptionResponse;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * Test class to demonstrate Global Exception Handling functionality.
 * This class shows how the Global Exception Manager handles different types
 * of database and network exceptions using reflection-based annotation processing.
 */
public class GlobalExceptionHandlingTest {
    
    private static final Logger logger = Logger.getInstance();
    
    public static void main(String[] args) {
        System.out.println("🧪 Testing Global Exception Handling System");
        System.out.println("=" + "=".repeat(50));
        
        // Initialize the Global Exception Manager
        GlobalExceptionManager.initialize();
        
        // Test database exceptions
        testDatabaseExceptions();
        
        // Test network exceptions
        testNetworkExceptions();
        
        // Test general exceptions
        testGeneralExceptions();
        
        System.out.println("\n✅ Global Exception Handling Test Completed!");
        System.out.println("📊 Total handlers registered: " + GlobalExceptionManager.getHandlerCount());
    }
    
    /**
     * Test database-related exceptions
     */
    private static void testDatabaseExceptions() {
        System.out.println("\n🗄️  Testing Database Exceptions:");
        System.out.println("-".repeat(30));
        
        // Test SQLException
        testException("SQLException", new SQLException("Connection to database failed"));
        
        // Test SQLTimeoutException
        testException("SQLTimeoutException", new SQLTimeoutException("Query timed out"));
        
        // Test SQLIntegrityConstraintViolationException
        testException("SQLIntegrityConstraintViolationException", 
                     new java.sql.SQLIntegrityConstraintViolationException("Duplicate key violation"));
    }
    
    /**
     * Test network-related exceptions
     */
    private static void testNetworkExceptions() {
        System.out.println("\n🌐 Testing Network Exceptions:");
        System.out.println("-".repeat(30));
        
        // Test ConnectException
        testException("ConnectException", new ConnectException("Connection refused"));
        
        // Test SocketTimeoutException
        testException("SocketTimeoutException", new SocketTimeoutException("Read timed out"));
        
        // Test SocketException
        testException("SocketException", new java.net.SocketException("Network is unreachable"));
        
        // Test IOException
        testException("IOException", new IOException("I/O error occurred"));
    }
    
    /**
     * Test general exceptions
     */
    private static void testGeneralExceptions() {
        System.out.println("\n⚠️  Testing General Exceptions:");
        System.out.println("-".repeat(30));
        
        // Test IllegalArgumentException
        testException("IllegalArgumentException", new IllegalArgumentException("Invalid argument provided"));
        
        // Test NullPointerException
        testException("NullPointerException", new NullPointerException("Object is null"));
        
        // Test RuntimeException
        testException("RuntimeException", new RuntimeException("Unexpected runtime error"));
        
        // Test generic Exception
        testException("Generic Exception", new Exception("Generic error occurred"));
    }
    
    /**
     * Test a specific exception type
     */
    private static void testException(String exceptionType, Throwable exception) {
        try {
            System.out.print("Testing " + exceptionType + "... ");
            
            // Handle the exception using Global Exception Manager
            ExceptionResponse response = GlobalExceptionManager.handleException(exception);
            
            // Display results
            System.out.println("✅ Handled");
            System.out.println("   Message: " + response.getMessage());
            System.out.println("   Error Code: " + response.getErrorCode());
            System.out.println("   Should Log: " + response.shouldLog());
            System.out.println("   Should Retry: " + response.shouldRetry());
            
        } catch (Exception e) {
            System.out.println("❌ Failed to handle " + exceptionType + ": " + e.getMessage());
        }
    }
    
    /**
     * Test exception handling with retry logic
     */
    public static void testRetryLogic() {
        System.out.println("\n🔄 Testing Retry Logic:");
        System.out.println("-".repeat(30));
        
        // Simulate a retryable exception
        ConnectException retryableException = new ConnectException("Temporary connection failure");
        ExceptionResponse response = GlobalExceptionManager.handleException(retryableException);
        
        if (response.shouldRetry()) {
            System.out.println("✅ Exception is retryable: " + response.getMessage());
            System.out.println("   Retry logic should be implemented here");
        } else {
            System.out.println("❌ Exception is not retryable: " + response.getMessage());
        }
    }
    
    /**
     * Test handler discovery
     */
    public static void testHandlerDiscovery() {
        System.out.println("\n🔍 Testing Handler Discovery:");
        System.out.println("-".repeat(30));
        
        // Test if handlers exist for specific exception types
        System.out.println("SQLException handler exists: " + GlobalExceptionManager.hasHandler(SQLException.class));
        System.out.println("ConnectException handler exists: " + GlobalExceptionManager.hasHandler(ConnectException.class));
        System.out.println("IllegalArgumentException handler exists: " + GlobalExceptionManager.hasHandler(IllegalArgumentException.class));
        
        // Test unknown exception type
        System.out.println("UnknownException handler exists: " + GlobalExceptionManager.hasHandler(UnknownException.class));
    }
    
    /**
     * Custom exception for testing
     */
    private static class UnknownException extends Exception {
        public UnknownException(String message) {
            super(message);
        }
    }
}
