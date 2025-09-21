package dev.aminhashemi.blueprinthell.core.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that handle specific exceptions.
 * Similar to @ExceptionHandler in Spring Boot.
 * 
 * Methods annotated with this will be called when the specified
 * exception types are thrown anywhere in the application.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandler {
    /**
     * The exception types this method can handle.
     * If empty, the method parameter type will be used.
     */
    Class<? extends Throwable>[] value() default {};
}
