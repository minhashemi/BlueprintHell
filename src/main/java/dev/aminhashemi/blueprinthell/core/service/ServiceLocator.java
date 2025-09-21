package dev.aminhashemi.blueprinthell.core.service;

import dev.aminhashemi.blueprinthell.core.factory.IPacketFactory;
import dev.aminhashemi.blueprinthell.core.factory.ISystemFactory;
import dev.aminhashemi.blueprinthell.core.factory.PacketFactory;
import dev.aminhashemi.blueprinthell.core.factory.SystemFactory;
import dev.aminhashemi.blueprinthell.core.interfaces.IGameStateManager;
import dev.aminhashemi.blueprinthell.core.impl.GameStateManager;
import dev.aminhashemi.blueprinthell.utils.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Service Locator for dependency injection and service management.
 * 
 * This class follows the Service Locator Pattern and provides a centralized
 * way to register and retrieve services. It implements the Singleton Pattern
 * to ensure only one instance exists throughout the application.
 * 
 * It follows the Dependency Inversion Principle by allowing high-level modules
 * to depend on abstractions rather than concrete implementations.
 */
public class ServiceLocator {
    
    private static final Logger logger = Logger.getInstance();
    private static volatile ServiceLocator instance;
    private final Map<Class<?>, Object> services;
    private final Map<Class<?>, Class<?>> serviceImplementations;
    
    /**
     * Private constructor to prevent instantiation.
     */
    private ServiceLocator() {
        this.services = new ConcurrentHashMap<>();
        this.serviceImplementations = new ConcurrentHashMap<>();
        initializeDefaultServices();
    }
    
    /**
     * Gets the singleton instance of ServiceLocator.
     * 
     * @return The ServiceLocator instance
     */
    public static ServiceLocator getInstance() {
        if (instance == null) {
            synchronized (ServiceLocator.class) {
                if (instance == null) {
                    instance = new ServiceLocator();
                }
            }
        }
        return instance;
    }
    
    /**
     * Initializes default services.
     */
    private void initializeDefaultServices() {
        // Register default implementations
        registerService(IPacketFactory.class, PacketFactory.class);
        registerService(ISystemFactory.class, SystemFactory.class);
        registerService(IGameStateManager.class, GameStateManager.class);
        
        logger.info("ServiceLocator initialized with default services");
    }
    
    /**
     * Registers a service implementation.
     * 
     * @param serviceInterface The service interface
     * @param implementationClass The implementation class
     */
    public <T> void registerService(Class<T> serviceInterface, Class<? extends T> implementationClass) {
        if (serviceInterface == null || implementationClass == null) {
            logger.warning("Cannot register service: interface or implementation class is null");
            return;
        }
        
        serviceImplementations.put(serviceInterface, implementationClass);
        logger.debug("Registered service: " + serviceInterface.getSimpleName() + " -> " + implementationClass.getSimpleName());
    }
    
    /**
     * Registers a service instance.
     * 
     * @param serviceInterface The service interface
     * @param serviceInstance The service instance
     */
    public <T> void registerService(Class<T> serviceInterface, T serviceInstance) {
        if (serviceInterface == null || serviceInstance == null) {
            logger.warning("Cannot register service: interface or instance is null");
            return;
        }
        
        services.put(serviceInterface, serviceInstance);
        logger.debug("Registered service instance: " + serviceInterface.getSimpleName());
    }
    
    /**
     * Gets a service instance.
     * 
     * @param serviceInterface The service interface
     * @return The service instance
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceInterface) {
        if (serviceInterface == null) {
            logger.warning("Cannot get service: interface is null");
            return null;
        }
        
        // Check if we have a registered instance
        Object service = services.get(serviceInterface);
        if (service != null) {
            return (T) service;
        }
        
        // Check if we have a registered implementation class
        Class<?> implementationClass = serviceImplementations.get(serviceInterface);
        if (implementationClass != null) {
            try {
                service = implementationClass.getDeclaredConstructor().newInstance();
                services.put(serviceInterface, service);
                logger.debug("Created service instance: " + serviceInterface.getSimpleName());
                return (T) service;
            } catch (Exception e) {
                logger.error("Failed to create service instance: " + serviceInterface.getSimpleName(), e);
                return null;
            }
        }
        
        logger.warning("Service not found: " + serviceInterface.getSimpleName());
        return null;
    }
    
    /**
     * Checks if a service is registered.
     * 
     * @param serviceInterface The service interface
     * @return True if the service is registered
     */
    public boolean hasService(Class<?> serviceInterface) {
        return services.containsKey(serviceInterface) || serviceImplementations.containsKey(serviceInterface);
    }
    
    /**
     * Unregisters a service.
     * 
     * @param serviceInterface The service interface
     */
    public void unregisterService(Class<?> serviceInterface) {
        if (serviceInterface == null) {
            return;
        }
        
        services.remove(serviceInterface);
        serviceImplementations.remove(serviceInterface);
        logger.debug("Unregistered service: " + serviceInterface.getSimpleName());
    }
    
    /**
     * Clears all registered services.
     */
    public void clearServices() {
        services.clear();
        serviceImplementations.clear();
        logger.info("Cleared all services");
    }
    
    /**
     * Gets the number of registered services.
     * 
     * @return The number of registered services
     */
    public int getServiceCount() {
        return services.size() + serviceImplementations.size();
    }
    
    /**
     * Gets a service or creates it if it doesn't exist.
     * 
     * @param serviceInterface The service interface
     * @param defaultImplementation The default implementation class
     * @return The service instance
     */
    public <T> T getOrCreateService(Class<T> serviceInterface, Class<? extends T> defaultImplementation) {
        T service = getService(serviceInterface);
        if (service == null) {
            registerService(serviceInterface, defaultImplementation);
            service = getService(serviceInterface);
        }
        return service;
    }
    
    /**
     * Resets the ServiceLocator to its initial state.
     */
    public void reset() {
        clearServices();
        initializeDefaultServices();
        logger.info("ServiceLocator reset");
    }
}
