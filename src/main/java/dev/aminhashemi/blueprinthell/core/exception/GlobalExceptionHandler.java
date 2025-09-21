package dev.aminhashemi.blueprinthell.core.exception;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a global exception handler.
 * Similar to @ControllerAdvice in Spring Boot.
 * 
 * Classes annotated with this will be scanned for @ExceptionHandler methods
 * to handle exceptions globally across the application.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GlobalExceptionHandler {
}
