package dev.crafty.core.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as a configuration holder.
 * This annotation specifies which configuration file should be used
 * for the annotated class.
 * 
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigurationFile {
    
    /**
     * The path to the configuration file, relative to the plugin's data folder.
     * 
     * @return The configuration file path
     */
    String file();
    
    /**
     * Whether to automatically create the configuration file if it doesn't exist.
     * 
     * @return True if the file should be created automatically, false otherwise
     */
    boolean createIfNotExists() default true;
}