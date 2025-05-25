package dev.crafty.core.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a field as a configuration value.
 * This annotation specifies which configuration key should be used
 * to populate the annotated field.
 * 
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigValue {
    
    /**
     * The configuration key path.
     * 
     * @return The configuration key path
     */
    String key();
    
    /**
     * Whether the configuration value is required.
     * If true and the value is not found in the configuration,
     * an exception will be thrown.
     * 
     * @return True if the value is required, false otherwise
     */
    boolean required() default true;
    
    /**
     * The default value to use if the configuration key is not found.
     * This is only used if required is set to false.
     * 
     * @return The default value as a string
     */
    String defaultValue() default "";
}