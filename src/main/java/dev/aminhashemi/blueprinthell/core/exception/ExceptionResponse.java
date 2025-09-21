package dev.aminhashemi.blueprinthell.core.exception;

/**
 * Response object for exception handling results.
 * Contains the error message and additional metadata.
 */
public class ExceptionResponse {
    private final String message;
    private final String errorCode;
    private final boolean shouldLog;
    private final boolean shouldRetry;
    
    public ExceptionResponse(String message, String errorCode, boolean shouldLog, boolean shouldRetry) {
        this.message = message;
        this.errorCode = errorCode;
        this.shouldLog = shouldLog;
        this.shouldRetry = shouldRetry;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public boolean shouldLog() {
        return shouldLog;
    }
    
    public boolean shouldRetry() {
        return shouldRetry;
    }
    
    @Override
    public String toString() {
        return String.format("ExceptionResponse{message='%s', errorCode='%s', shouldLog=%s, shouldRetry=%s}", 
                           message, errorCode, shouldLog, shouldRetry);
    }
}
