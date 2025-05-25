package dev.crafty.core.config;

import dev.crafty.core.CraftyCore;
import dev.crafty.core.config.annotation.ConfigurationFile;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.config.serializer.SerializerFactory;
import dev.crafty.core.config.serializer.SerializerRegistry;
import dev.crafty.core.config.serializer.builtin.ItemStackSerializer;
import dev.crafty.core.geometry.Point2d;
import dev.crafty.core.geometry.polygons.Polygon3d;
import dev.crafty.core.geometry.serializers.Point2dSerializer;
import dev.crafty.core.geometry.serializers.Polygon3dSerializer;
import dev.crafty.core.plugin.CraftyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Utility class for working with the configuration system.
 * This class provides helper methods for common configuration operations.
 *
 * @since 1.0.0
 */
public class ConfigurationUtils {

    private static ConfigurationManager configManager;
    private static Plugin plugin;
    private static final Map<Plugin, ConfigurationManager> configManagers = new HashMap<>();

    private static final Map<Plugin, List<Object>> configRegistry = new HashMap<>();

    /**
     * Initialize the configuration system.
     * This method should be called when the plugin is enabled.
     *
     * @param plugin The plugin instance
     */
    public static void initialize(Plugin plugin) {
        ConfigurationUtils.plugin = plugin;
        configManager = new ConfigurationManager(plugin);
        configManagers.put(plugin, configManager);

        SerializerFactory.registerBuiltInSerializers();

        SerializerRegistry.register(new ItemStackSerializer());
        SerializerRegistry.register(new Point2dSerializer());
        SerializerRegistry.register(new Polygon3dSerializer());

        plugin.getLogger().info("Configuration system initialized");
    }

    /**
     * Get or create a configuration manager for a plugin.
     *
     * @param plugin The plugin instance
     * @return The configuration manager for the plugin
     */
    private static ConfigurationManager getConfigManager(Plugin plugin) {
        if (plugin == ConfigurationUtils.plugin) {
            return configManager;
        }

        return configManagers.computeIfAbsent(plugin, ConfigurationManager::new);
    }

    /**
     * Load configuration values into an object.
     * The object's class must be annotated with @ConfigurationFile,
     * and its fields must be annotated with @ConfigValue.
     *
     * @param object The object to load configuration values into
     * @throws IllegalArgumentException If the object's class is not annotated with @ConfigurationFile
     */
    public static void load(Object object) {
        if (configManager == null) {
            throw new IllegalStateException("Configuration system not initialized");
        }

        try {
            configManager.populateObject(object, plugin);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error loading configuration for " + object.getClass().getName(), e);
        }
    }

    /**
     * Register and load configuration values into an object using a specific plugin's data folder.
     * The object's class must be annotated with @ConfigurationFile,
     * and its fields must be annotated with @ConfigValue.
     *
     * @param object The object to load configuration values into
     * @param plugin The plugin to load the configuration from
     * @throws IllegalArgumentException If the object's class is not annotated with @ConfigurationFile
     */
    public static void registerAndLoad(Object object, Plugin plugin) {
        if (configManager == null) {
            throw new IllegalStateException("Configuration system not initialized");
        }

        try {
            registerConfigObject(object, plugin);

            ConfigurationManager manager = getConfigManager(plugin);
            manager.populateObject(object, plugin);
        } catch (Exception e) {
            ConfigurationUtils.plugin.getLogger().log(Level.SEVERE, "Error loading configuration for " + object.getClass().getName(), e);
        }
    }

    /**
     * Register a configuration object with the registry.
     * This allows the object to be automatically reloaded when the plugin's configuration is reloaded.
     *
     * @param object The configuration object to register
     * @param plugin The plugin that owns the configuration object
     */
    private static void registerConfigObject(Object object, Plugin plugin) {
        if (object.getClass().getAnnotation(ConfigurationFile.class) == null) {
            return;
        }

        configRegistry.computeIfAbsent(plugin, k -> new ArrayList<>()).add(object);

        CraftyCore craftyCore = (CraftyCore) Bukkit.getPluginManager().getPlugin("CraftyCore");
        if (craftyCore != null) {
            craftyCore.registerConfig(plugin, object);
        }
    }

    /**
     * Reload configuration values for an object from disk.
     * This is useful when the configuration file has been modified externally
     * and you want to update the object's fields with the new values.
     * The object's class must be annotated with @ConfigurationFile,
     * and its fields must be annotated with @ConfigValue.
     *
     * @param object The object to reload configuration values for
     * @throws IllegalArgumentException If the object's class is not annotated with @ConfigurationFile
     */
    public static void reload(Object object) {
        reload(object, plugin);
    }

    /**
     * Reload configuration values for an object from disk using a specific plugin's data folder.
     * This is useful when the configuration file has been modified externally
     * and you want to update the object's fields with the new values.
     * The object's class must be annotated with @ConfigurationFile,
     * and its fields must be annotated with @ConfigValue.
     *
     * @param object The object to reload configuration values for
     * @param plugin The plugin to reload the configuration from
     * @throws IllegalArgumentException If the object's class is not annotated with @ConfigurationFile
     */
    public static void reload(Object object, Plugin plugin) {
        if (configManager == null) {
            throw new IllegalStateException("Configuration system not initialized");
        }

        try {
            // Register the object if it's not already registered
            registerConfigObject(object, plugin);

            // Reload the configuration
            ConfigurationManager manager = getConfigManager(plugin);
            manager.reloadObject(object, plugin);
        } catch (Exception e) {
            ConfigurationUtils.plugin.getLogger().log(Level.SEVERE, "Error reloading configuration for " + object.getClass().getName(), e);
        }
    }

    /**
     * Reload all registered configuration objects for a specific plugin.
     * This method will find all configuration objects registered for the plugin
     * and reload their values from the configuration files.
     *
     * @param plugin The plugin to reload configurations for
     * @return The number of configuration objects reloaded
     */
    public static int reloadAllConfigs(Plugin plugin) {
        if (configManager == null) {
            throw new IllegalStateException("Configuration system not initialized");
        }

        int count = 0;
        List<Object> configs = configRegistry.get(plugin);
        if (configs != null) {
            for (Object config : configs) {
                try {
                    ConfigurationManager manager = getConfigManager(plugin);
                    manager.reloadObject(config, plugin);
                    count++;
                } catch (Exception e) {
                    ConfigurationUtils.plugin.getLogger().log(Level.SEVERE, "Error reloading configuration for " + config.getClass().getName(), e);
                }
            }
        }

        return count;
    }

    /**
     * Reload all registered configuration objects for a plugin by name.
     * This method will find the plugin by name and reload all its registered configurations.
     *
     * @param pluginName The name of the plugin to reload configurations for
     * @return The number of configuration objects reloaded, or -1 if the plugin was not found
     */
    public static int reloadAllConfigs(String pluginName) {
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);
        if (targetPlugin == null) {
            plugin.getLogger().warning("Could not find plugin: " + pluginName);
            return -1;
        }

        return reloadAllConfigs(targetPlugin);
    }

    /**
     * Register a custom serializer.
     *
     * @param serializer The serializer to register
     * @param <T> The type of object the serializer handles
     */
    public static <T> void registerSerializer(ConfigSerializer<T> serializer) {
        SerializerRegistry.register(serializer);
    }

    /**
     * Check if the configuration system is initialized.
     *
     * @return True if the configuration system is initialized, false otherwise
     */
    public static boolean isInitialized() {
        return configManager != null;
    }

    /**
     * Get the configuration manager.
     *
     * @return The configuration manager
     * @throws IllegalStateException If the configuration system is not initialized
     */
    public static ConfigurationManager getConfigManager() {
        if (configManager == null) {
            throw new IllegalStateException("Configuration system not initialized");
        }
        return configManager;
    }

    /**
     * Get all registered configuration objects for a plugin.
     *
     * @param plugin The plugin to get configurations for
     * @return A list of registered configuration objects for the plugin
     */
    public static List<Object> getRegisteredConfigs(Plugin plugin) {
        if (configManager == null) {
            throw new IllegalStateException("Configuration system not initialized");
        }

        return configRegistry.getOrDefault(plugin, new ArrayList<>());
    }

    /**
     * Scan a plugin for classes with the @ConfigurationFile annotation and automatically
     * register and load them. This method uses reflection to find and instantiate
     * configuration classes.
     *
     * @param plugin The plugin to scan for configuration classes
     * @return The number of configuration classes found and registered
     */
    public static int scanAndLoadConfigs(Plugin plugin) {
        if (configManager == null) {
            throw new IllegalStateException("Configuration system not initialized");
        }

        int count = 0;
        try {
            // Get the plugin's JAR file
            File pluginFile = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().toURI());
            if (!pluginFile.exists() || !pluginFile.getName().endsWith(".jar")) {
                plugin.getLogger().warning("Could not find JAR file for plugin: " + plugin.getName());
                return 0;
            }

            // Open the JAR file
            try (JarFile jarFile = new JarFile(pluginFile)) {
                // Get all class entries in the JAR
                Enumeration<JarEntry> entries = jarFile.entries();

                // Create a class loader for loading classes from the JAR
                URLClassLoader classLoader = new URLClassLoader(
                    new URL[] { pluginFile.toURI().toURL() },
                    ConfigurationUtils.class.getClassLoader()
                );

                // Iterate through all entries
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();

                    // Check if the entry is a class file
                    if (entryName.endsWith(".class")) {
                        // Convert the entry name to a class name
                        String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');

                        try {
                            // Load the class
                            Class<?> clazz = classLoader.loadClass(className);

                            // Check if the class has the ConfigurationFile annotation
                            if (clazz.isAnnotationPresent(ConfigurationFile.class)) {
                                // Check if the class is not abstract and has a public no-arg constructor
                                if (!Modifier.isAbstract(clazz.getModifiers())) {
                                    try {
                                        // Try to get a constructor that takes a Plugin parameter
                                        Constructor<?> constructor = null;
                                        try {
                                            constructor = clazz.getConstructor(Plugin.class);
                                            // Create an instance using the Plugin constructor
                                            Object instance = constructor.newInstance(plugin);
                                            // The instance should already be registered by the constructor
                                            count++;
                                            plugin.getLogger().info("Loaded configuration class: " + className);
                                        } catch (NoSuchMethodException e) {
                                            // Try to get a no-arg constructor
                                            constructor = clazz.getConstructor();
                                            // Create an instance using the no-arg constructor
                                            Object instance = constructor.newInstance();
                                            // Register and load the instance
                                            registerAndLoad(instance, plugin);
                                            count++;
                                            plugin.getLogger().info("Loaded configuration class: " + className);
                                        }
                                    } catch (Exception e) {
                                        plugin.getLogger().log(Level.WARNING, "Could not instantiate configuration class: " + className, e);
                                    }
                                }
                            }
                        } catch (ClassNotFoundException | NoClassDefFoundError e) {
                            // Ignore classes that can't be loaded
                        }
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error scanning for configuration classes", e);
        }

        return count;
    }
}
