package dev.crafty.core.config;

import dev.crafty.core.config.annotation.ConfigurationFile;
import dev.crafty.core.config.annotation.ConfigValue;
import dev.crafty.core.config.serializer.ConfigSerializer;
import dev.crafty.core.config.serializer.SerializerRegistry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

/**
 * Manager for handling configuration files and annotations.
 * This class is responsible for loading configuration files,
 * processing configuration annotations, and populating fields.
 *
 * @since 1.0.0
 */
public class ConfigurationManager {

    private final Plugin plugin;
    private final Map<Class<?>, YamlConfiguration> configCache = new HashMap<>();
    private final Map<Class<?>, File> configFiles = new HashMap<>();
    private final Map<Class<?>, Plugin> configPlugins = new HashMap<>();

    /**
     * Create a new configuration manager.
     *
     * @param plugin The plugin instance
     */
    public ConfigurationManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Load a configuration file for a class.
     *
     * @param clazz The class to load the configuration for
     * @return The loaded configuration
     * @throws IllegalArgumentException If the class is not annotated with @ConfigurationFile
     */
    public YamlConfiguration loadConfig(Class<?> clazz) {
        return loadConfig(clazz, plugin);
    }

    /**
     * Load a configuration file for a class using a specific plugin's data folder.
     *
     * @param clazz The class to load the configuration for
     * @param plugin The plugin to load the configuration from
     * @return The loaded configuration
     * @throws IllegalArgumentException If the class is not annotated with @ConfigurationFile
     */
    public YamlConfiguration loadConfig(Class<?> clazz, Plugin plugin) {
        ConfigurationFile annotation = clazz.getAnnotation(ConfigurationFile.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @ConfigurationFile");
        }

        if (configCache.containsKey(clazz)) {
            return configCache.get(clazz);
        }

        configPlugins.put(clazz, plugin);

        String filePath = annotation.file();
        File configFile = new File(plugin.getDataFolder(), filePath);
        configFiles.put(clazz, configFile);

        if (!configFile.exists() && annotation.createIfNotExists()) {
            configFile.getParentFile().mkdirs();
            plugin.saveResource(filePath, false);
        }

        var config = YamlConfiguration.loadConfiguration(configFile);
        configCache.put(clazz, config);

        return config;
    }

    /**
     * Reload a configuration file for a class from disk.
     *
     * @param clazz The class to reload the configuration for
     * @return The reloaded configuration
     * @throws IllegalArgumentException If the class is not annotated with @ConfigurationFile
     */
    public YamlConfiguration reloadConfig(Class<?> clazz) {
        ConfigurationFile annotation = clazz.getAnnotation(ConfigurationFile.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @ConfigurationFile");
        }

        Plugin pluginForClass = configPlugins.getOrDefault(clazz, plugin);

        File configFile = configFiles.get(clazz);
        if (configFile == null) {
            return loadConfig(clazz, pluginForClass);
        }

        var config = YamlConfiguration.loadConfiguration(configFile);
        configCache.put(clazz, config);

        return config;
    }

    /**
     * Populate an object with values from its configuration file using a specific plugin's data folder.
     *
     * @param object The object to populate
     * @param plugin The plugin to load the configuration from
     * @throws IllegalArgumentException If the object's class is not annotated with @ConfigurationFile
     */
    public void populateObject(Object object, Plugin plugin) {
        Class<?> clazz = object.getClass();
        ConfigurationFile annotation = clazz.getAnnotation(ConfigurationFile.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @ConfigurationFile");
        }

        YamlConfiguration configFile = loadConfig(clazz, plugin);

        populateFieldsWithAnnotation(object, plugin, clazz, new SectionWrapper(configFile));

        saveConfigFile(configFile, clazz);
    }

    /**
     * Reload an object's configuration from disk and repopulate its fields.
     * This is useful for updating an object's fields when the configuration file has changed.
     *
     * @param object The object to reload
     * @throws IllegalArgumentException If the object's class is not annotated with @ConfigurationFile
     */
    public void reloadObject(Object object) {
        reloadObject(object, plugin);
    }

    /**
     * Reload an object's configuration from disk and repopulate its fields using a specific plugin's data folder.
     * This is useful for updating an object's fields when the configuration file has changed.
     *
     * @param object The object to reload
     * @param plugin The plugin to reload the configuration from
     * @throws IllegalArgumentException If the object's class is not annotated with @ConfigurationFile
     */
    public void reloadObject(Object object, Plugin plugin) {
        Class<?> clazz = object.getClass();
        ConfigurationFile annotation = clazz.getAnnotation(ConfigurationFile.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not annotated with @ConfigurationFile");
        }

        if (!configPlugins.containsKey(clazz)) {
            configPlugins.put(clazz, plugin);
        }

        YamlConfiguration configFile = reloadConfig(clazz);
        var config = new SectionWrapper(configFile);

        populateFieldsWithAnnotation(object, plugin, clazz, config);

        saveConfigFile(configFile, clazz);

        plugin.getLogger().info("Reloaded configuration for " + clazz.getSimpleName());
    }

    private void populateFieldsWithAnnotation(Object object, Plugin plugin, Class<?> clazz, SectionWrapper config) {
        for (Field field : clazz.getDeclaredFields()) {
            ConfigValue valueAnnotation = field.getAnnotation(ConfigValue.class);
            if (valueAnnotation == null) {
                continue;
            }

            try {
                populateField(object, field, valueAnnotation, config);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error populating field " + field.getName() + " in class " + clazz.getName(), e);
            }
        }
    }

    /**
     * Populate a field with a value from the configuration.
     *
     * @param object The object to populate
     * @param field The field to populate
     * @param annotation The ConfigValue annotation
     * @param config The configuration to get the value from
     * @throws IllegalAccessException If the field cannot be accessed
     */
    @SuppressWarnings("unchecked")
    private void populateField(Object object, Field field, ConfigValue annotation, SectionWrapper config) throws IllegalAccessException {
        String key = annotation.key();
        boolean required = annotation.required();
        String defaultValue = annotation.defaultValue();

        field.setAccessible(true);

        if (!config.contains(key)) {
            if (required) {
                throw new IllegalArgumentException("Required configuration key " + key + " not found");
            } else if (!defaultValue.isEmpty()) {
                // Use the default value
                setFieldValue(object, field, defaultValue, config);
            }
            return;
        }

        Class<?> fieldType = field.getType();

        @SuppressWarnings("unchecked")
        Optional<ConfigSerializer<?>> serializer = (Optional<ConfigSerializer<?>>) SerializerRegistry.getSerializer((Class) fieldType);
        if (serializer.isPresent()) {
            Object value = ((ConfigSerializer<Object>) serializer.get()).deserialize(config, key).orElse(null);
            field.set(object, value);
        } else if (List.class.isAssignableFrom(fieldType)) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType paramType) {
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> itemType) {
                    @SuppressWarnings("unchecked")
                    Optional<ConfigSerializer<?>> itemSerializer = (Optional<ConfigSerializer<?>>) SerializerRegistry.getSerializer((Class) itemType);
                    if (itemSerializer.isPresent()) {
                        List<?> list = config.getList(key).orElse(new ArrayList<>());
                        field.set(object, list);
                    } else {
                        Object value = config.getList(key).orElse(new ArrayList<>());
                        field.set(object, value);
                    }
                }
            }
        } else {
            Object value = config.get(key).orElse(null);
            field.set(object, value);
        }
    }

    /**
     * Set a field value from a string.
     *
     * @param object The object to set the field on
     * @param field The field to set
     * @param value The string value to parse
     * @param config The configuration to use for complex types
     * @throws IllegalAccessException If the field cannot be accessed
     */
    @SuppressWarnings("unchecked")
    private void setFieldValue(Object object, Field field, String value, SectionWrapper config) throws IllegalAccessException {
        Class<?> fieldType = field.getType();

        field.setAccessible(true);

        if (fieldType == String.class) {
            field.set(object, value);
        } else if (fieldType == int.class || fieldType == Integer.class) {
            field.set(object, Integer.parseInt(value));
        } else if (fieldType == boolean.class || fieldType == Boolean.class) {
            field.set(object, Boolean.parseBoolean(value));
        } else if (fieldType == double.class || fieldType == Double.class) {
            field.set(object, Double.parseDouble(value));
        } else if (fieldType == float.class || fieldType == Float.class) {
            field.set(object, Float.parseFloat(value));
        } else if (fieldType == long.class || fieldType == Long.class) {
            field.set(object, Long.parseLong(value));
        } else {
            Optional<ConfigSerializer<?>> serializer = (Optional<ConfigSerializer<?>>) SerializerRegistry.getSerializer((Class) fieldType);
            if (serializer.isPresent()) {
                SectionWrapper tempSection = config.createSection("__temp");
                tempSection.set("value", value);

                Object deserializedValue = ((ConfigSerializer<Object>) serializer.get()).deserialize(tempSection, "value");
                field.set(object, deserializedValue);

                config.set("__temp", null);
            } else if (List.class.isAssignableFrom(fieldType)) {
                Type genericType = field.getGenericType();
                if (genericType instanceof ParameterizedType paramType) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> itemType) {

                        String[] items = value.split(",");
                        List<Object> list = new ArrayList<>();

                        for (String item : items) {
                            String trimmedItem = item.trim();

                            if (itemType == String.class) {
                                list.add(trimmedItem);
                            } else if (itemType == Integer.class) {
                                list.add(Integer.parseInt(trimmedItem));
                            } else if (itemType == Boolean.class) {
                                list.add(Boolean.parseBoolean(trimmedItem));
                            } else if (itemType == Double.class) {
                                list.add(Double.parseDouble(trimmedItem));
                            } else if (itemType == Float.class) {
                                list.add(Float.parseFloat(trimmedItem));
                            } else if (itemType == Long.class) {
                                list.add(Long.parseLong(trimmedItem));
                            } else {
                                Optional<ConfigSerializer<?>> itemSerializer = (Optional<ConfigSerializer<?>>) SerializerRegistry.getSerializer((Class) itemType);
                                if (itemSerializer.isPresent()) {
                                    SectionWrapper tempSection = config.createSection("__temp");
                                    tempSection.set("value", trimmedItem);

                                    Object deserializedItem = ((ConfigSerializer<Object>) itemSerializer.get()).deserialize(tempSection, "value");
                                    list.add(deserializedItem);

                                    config.set("__temp", null);
                                } else {
                                    throw new IllegalArgumentException("Cannot convert string to list item of type " + itemType.getName());
                                }
                            }
                        }

                        field.set(object, list);
                    } else {
                        throw new IllegalArgumentException("Cannot determine generic type for list field " + field.getName());
                    }
                } else {
                    throw new IllegalArgumentException("List field " + field.getName() + " does not have generic type parameters");
                }
            } else {
                try {
                    SectionWrapper tempSection = config.createSection("__temp");
                    tempSection.set("value", value);

                    Optional<Object> configValue = tempSection.get("value");
                    if (configValue.isPresent() && fieldType.isInstance(configValue.get())) {
                        field.set(object, configValue);
                    } else {
                        throw new IllegalArgumentException("Cannot set field of type " + fieldType.getName() + " from string value");
                    }

                    config.set("__temp", null);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Cannot set field of type " + fieldType.getName() + " from string value", e);
                }
            }
        }
    }

    private void saveConfigFile(YamlConfiguration configFile, Class<?> clazz) {
        try {
            configFile.save(configFiles.get(clazz));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
