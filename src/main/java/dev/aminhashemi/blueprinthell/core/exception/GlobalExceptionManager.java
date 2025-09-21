package dev.aminhashemi.blueprinthell.core.exception;

import dev.aminhashemi.blueprinthell.utils.Logger;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Global Exception Manager using reflection to discover and invoke exception handlers.
 * 
 * This class scans for classes annotated with @GlobalExceptionHandler and methods
 * annotated with @ExceptionHandler, then provides a centralized way to handle
 * exceptions throughout the application.
 */
public class GlobalExceptionManager {
    
    private static final Logger logger = Logger.getInstance();
    private static final Map<Class<?>, Method> exceptionHandlers = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> handlerInstances = new ConcurrentHashMap<>();
    private static boolean initialized = false;
    
    // Package names to scan for exception handlers
    private static final String[] PACKAGES_TO_SCAN = {
        "dev.aminhashemi.blueprinthell.core.exception",
        "dev.aminhashemi.blueprinthell.database",
        "dev.aminhashemi.blueprinthell.network"
    };
    
    /**
     * Initializes the exception manager by scanning for handlers.
     * This method is thread-safe and can be called multiple times.
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            logger.info("Initializing Global Exception Manager...");
            discoverExceptionHandlers();
            initialized = true;
            logger.info("Global Exception Manager initialized successfully. Found " + 
                       exceptionHandlers.size() + " exception handlers.");
        } catch (Exception e) {
            logger.error("Failed to initialize Global Exception Manager", e);
            throw new RuntimeException("Failed to initialize exception handling", e);
        }
    }
    
    /**
     * Handles an exception using the appropriate handler method.
     * 
     * @param exception The exception to handle
     * @return ExceptionResponse containing the handling result
     */
    public static ExceptionResponse handleException(Throwable exception) {
        if (!initialized) {
            initialize();
        }
        
        if (exception == null) {
            return new ExceptionResponse("Null exception provided", "NULL_EXCEPTION", false, false);
        }
        
        try {
            Method handler = findBestHandler(exception.getClass());
            if (handler != null) {
                Object handlerInstance = getHandlerInstance(handler.getDeclaringClass());
                ExceptionResponse response = (ExceptionResponse) handler.invoke(handlerInstance, exception);
                
                if (response.shouldLog()) {
                    logger.info("Exception handled by " + handler.getDeclaringClass().getSimpleName() + 
                              "." + handler.getName() + ": " + response.getMessage());
                }
                
                return response;
            } else {
                logger.warning("No handler found for exception: " + exception.getClass().getSimpleName());
                return new ExceptionResponse(
                    "No handler found for: " + exception.getClass().getSimpleName(),
                    "NO_HANDLER_FOUND",
                    true,
                    false
                );
            }
        } catch (Exception e) {
            logger.error("Error in exception handler: " + e.getMessage(), e);
            return new ExceptionResponse(
                "Error in exception handler: " + e.getMessage(),
                "HANDLER_ERROR",
                true,
                false
            );
        }
    }
    
    /**
     * Handles an exception and returns just the message string.
     * Convenience method for simple error handling.
     * 
     * @param exception The exception to handle
     * @return Error message string
     */
    public static String handleExceptionMessage(Throwable exception) {
        ExceptionResponse response = handleException(exception);
        return response.getMessage();
    }
    
    /**
     * Checks if a handler exists for the given exception type.
     * 
     * @param exceptionType The exception type to check
     * @return True if a handler exists
     */
    public static boolean hasHandler(Class<? extends Throwable> exceptionType) {
        if (!initialized) {
            initialize();
        }
        return findBestHandler(exceptionType) != null;
    }
    
    /**
     * Discovers all exception handlers using reflection.
     */
    private static void discoverExceptionHandlers() {
        for (String packageName : PACKAGES_TO_SCAN) {
            try {
                Class<?>[] classes = getClassesInPackage(packageName);
                for (Class<?> clazz : classes) {
                    if (clazz.isAnnotationPresent(GlobalExceptionHandler.class)) {
                        registerExceptionHandlers(clazz);
                    }
                }
            } catch (Exception e) {
                logger.warning("Could not scan package " + packageName + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Registers exception handlers from a class.
     */
    private static void registerExceptionHandlers(Class<?> handlerClass) {
        Method[] methods = handlerClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                ExceptionHandler annotation = method.getAnnotation(ExceptionHandler.class);
                Class<?>[] exceptionTypes = annotation.value();
                
                if (exceptionTypes.length == 0) {
                    // Use parameter type if no specific types specified
                    Class<?>[] paramTypes = method.getParameterTypes();
                    if (paramTypes.length == 1 && Throwable.class.isAssignableFrom(paramTypes[0])) {
                        exceptionTypes = new Class<?>[]{paramTypes[0]};
                    }
                }
                
                for (Class<?> exceptionType : exceptionTypes) {
                    if (Throwable.class.isAssignableFrom(exceptionType)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends Throwable> throwableType = (Class<? extends Throwable>) exceptionType;
                        exceptionHandlers.put(throwableType, method);
                        logger.debug("Registered handler: " + method.getName() + " for " + exceptionType.getSimpleName());
                    }
                }
            }
        }
    }
    
    /**
     * Finds the best handler for an exception type, considering inheritance hierarchy.
     */
    private static Method findBestHandler(Class<? extends Throwable> exceptionType) {
        // Direct match
        Method handler = exceptionHandlers.get(exceptionType);
        if (handler != null) {
            return handler;
        }
        
        // Look for handlers in the inheritance hierarchy
        Class<?> currentType = exceptionType;
        while (currentType != null && currentType != Object.class) {
            handler = exceptionHandlers.get(currentType);
            if (handler != null) {
                return handler;
            }
            currentType = currentType.getSuperclass();
        }
        
        // Look for interface matches
        for (Class<?> interfaceType : getAllInterfaces(exceptionType)) {
            handler = exceptionHandlers.get(interfaceType);
            if (handler != null) {
                return handler;
            }
        }
        
        return null;
    }
    
    /**
     * Gets all interfaces implemented by a class and its superclasses.
     */
    private static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
        Set<Class<?>> interfaces = new HashSet<>();
        Class<?> current = clazz;
        
        while (current != null && current != Object.class) {
            interfaces.addAll(Arrays.asList(current.getInterfaces()));
            current = current.getSuperclass();
        }
        
        return interfaces;
    }
    
    /**
     * Gets or creates an instance of the handler class.
     */
    private static Object getHandlerInstance(Class<?> handlerClass) {
        return handlerInstances.computeIfAbsent(handlerClass, clazz -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                logger.error("Failed to create instance of " + clazz.getSimpleName(), e);
                throw new RuntimeException("Failed to create handler instance", e);
            }
        });
    }
    
    /**
     * Gets classes in a package using reflection.
     * This is a simplified implementation - in a real application,
     * you might want to use a more sophisticated classpath scanner.
     */
    private static Class<?>[] getClassesInPackage(String packageName) {
        try {
            // For this implementation, we'll manually specify the known classes
            // In a production system, you'd use a library like Reflections or ClassGraph
            if (packageName.equals("dev.aminhashemi.blueprinthell.core.exception")) {
                return new Class<?>[]{
                    GameExceptionHandler.class
                };
            }
            return new Class<?>[0];
        } catch (Exception e) {
            logger.warning("Could not load classes from package " + packageName + ": " + e.getMessage());
            return new Class<?>[0];
        }
    }
    
    /**
     * Clears all registered handlers. Useful for testing.
     */
    public static synchronized void clearHandlers() {
        exceptionHandlers.clear();
        handlerInstances.clear();
        initialized = false;
    }
    
    /**
     * Gets the number of registered handlers.
     */
    public static int getHandlerCount() {
        return exceptionHandlers.size();
    }
}
