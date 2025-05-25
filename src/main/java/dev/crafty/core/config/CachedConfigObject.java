package dev.crafty.core.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.crafty.core.CraftyCore;
import dev.crafty.core.config.serializer.ConfigSerializer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

/**
 * An abstract class that provides a cached, file-backed configuration object.
 *
 * <p>
 * This class uses a Caffeine cache to store objects in memory and synchronizes them
 * with a YAML configuration file on disk. Subclasses or instances (via the Builder)
 * must provide the config file, serializer, and config section.
 * </p>
 *
 * @param <K> the type of the key used to identify objects
 * @param <V> the type of the value to be cached and serialized
 *
 * @since 1.0.5
 */
public abstract class CachedConfigObject<K, V> {
    private final Cache<K, V> cache = Caffeine.newBuilder().build();

    /**
     * Returns the configuration file backing this cache.
     *
     * @return the config file
     */
    protected abstract File getConfigFile();

    /**
     * Returns the serializer used to (de)serialize objects to/from the config.
     *
     * @return an optional serializer
     */
    protected abstract Optional<ConfigSerializer<V>> getSerializer();

    /**
     * Returns the section name in the config file where objects are stored.
     *
     * @return the config section name
     */
    protected abstract String getConfigSection();

    /**
     * Converts a String key from the config to the correct key type.
     *
     * @param key the string key from config
     * @return the key of type K
     */
    protected abstract K keyFromString(String key);

    /**
     * Converts a key of type K to a String for config storage.
     *
     * @param key the key of type K
     */
    protected abstract String keyToString(K key);

    /**
     * Gets a value from the cache, loading from config if not present.
     *
     * @param key the key to look up
     * @return an optional containing the value if present
     */
    public Optional<V> get(K key) {
        return Optional.ofNullable(cache.get(key, k -> {
            Optional<V> value = getFromConfig(k);
            return value.orElse(null);
        }));
    }

    /**
     * Loads all values from the config section into the cache, replacing any existing cache entries.
     */
    public void loadAll() {
        cache.invalidateAll();
        Optional<File> fileOpt = ensureFileExists();
        if (fileOpt.isEmpty()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(fileOpt.get());
        ConfigurationSection section = config.getConfigurationSection(getConfigSection());

        if (section == null) {
            return;
        }

        for (String key : section.getKeys(false)) {
            K typedKey = keyFromString(key);
            Optional<V> value = getFromConfig(typedKey);
            value.ifPresent(v -> cache.put(typedKey, v));
        }
    }

    /**
     * Sets a value in the cache and saves it to the config file.
     *
     * @param key the key to set
     * @param value the value to associate with the key
     */
    public void set(K key, V value) {
        cache.put(key, value);
        saveToConfig(key, value);
    }

    /**
     * Saves a value to the config file.
     *
     * @param key the key to save
     * @param value the value to save
     */
    private void saveToConfig(K key, V value) {
        Optional<File> fileOpt = ensureFileExists();
        if (fileOpt.isEmpty()) {
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(fileOpt.get());
        ConfigurationSection section = config.getConfigurationSection(getConfigSection());

        if (section == null) {
            config.createSection(getConfigSection());
            section = config.getConfigurationSection(getConfigSection());
        }

        if (getSerializer().isPresent()) {
            getSerializer().get().serialize(value, new SectionWrapper(section), getConfigSection() + "." + key);
        }

        try {
            config.save(fileOpt.get());
        } catch (IOException e) {
            CraftyCore.INSTANCE.logger.error("Failed to save config file", e);
        }
    }

    /**
     * Ensures the config file exists, creating it if necessary.
     *
     * @return an optional containing the file if it exists or was created successfully
     */
    private Optional<File> ensureFileExists() {
        if (!getConfigFile().exists()) {
            try {
                getConfigFile().createNewFile();
            } catch (IOException e) {
                CraftyCore.INSTANCE.logger.error("Failed to create config file", e);
                return Optional.empty();
            }
        }
        return Optional.of(getConfigFile());
    }

    /**
     * Loads a value from the config file.
     *
     * @param id the key to load
     * @return an optional containing the value if present
     */
    private Optional<V> getFromConfig(K id) {
        Optional<File> fileOpt = ensureFileExists();
        if (fileOpt.isEmpty()) {
            return Optional.empty();
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(
                fileOpt.get()
        );

        ConfigurationSection regionsSection = config.getConfigurationSection(getConfigSection());

        if (regionsSection == null) {
            config.createSection(getConfigSection());
            try {
                config.save(fileOpt.get());
            } catch (IOException e) {
                CraftyCore.INSTANCE.logger.error("Failed to save config file", e);
                return Optional.empty();
            }
            return Optional.empty();
        }

        SectionWrapper section = new SectionWrapper(regionsSection);

        if (!regionsSection.contains(keyToString(id))) {
            return Optional.empty();
        }

        if (getSerializer().isPresent()) {
            return getSerializer().get().deserialize(section, getConfigSection() + "." + keyToString(id));
        }

        return Optional.empty();
    }

    /**
     * Builder for creating instances of {@link CachedConfigObject} with custom configuration.
     *
     * @param <K> the type of the key
     * @param <V> the type of the value
     */
    public static class Builder<K, V> {
        private File configFile;
        private Optional<ConfigSerializer<V>> serializer = Optional.empty();
        private String configSection = "";
        private Function<String, K> keyFromStringFunction;
        private Function<K, String> keyToStringFunction;

        /**
         * Sets the config file to use.
         *
         * @param configFile the config file
         * @return this builder
         */
        public Builder<K, V> configFile(File configFile) {
            this.configFile = configFile;
            return this;
        }

        /**
         * Sets the serializer to use.
         *
         * @param serializer the serializer
         * @return this builder
         */
        public Builder<K, V> serializer(ConfigSerializer<V> serializer) {
            this.serializer = Optional.ofNullable(serializer);
            return this;
        }

        /**
         * Sets the config section to use.
         *
         * @param configSection the config section name
         * @return this builder
         */
        public Builder<K, V> configSection(String configSection) {
            this.configSection = configSection;
            return this;
        }

        /**
         * Sets the function to convert string keys to keys of type K.
         *
         * @param keyFromStringFunction the function to convert string to K
         * @return this builder
         */
        public Builder<K, V> keyFromStringFunction(Function<String, K> keyFromStringFunction) {
            this.keyFromStringFunction = keyFromStringFunction;
            return this;
        }

        /**
         * Sets the function to convert keys of type K to string keys.
         *
         * @param keyToStringFunction the function to convert K to string
         * @return this builder
         */
        public Builder<K, V> keyToStringFunction(Function<K, String> keyToStringFunction) {
            this.keyToStringFunction = keyToStringFunction;
            return this;
        }

        /**
         * Builds a new {@link CachedConfigObject} instance with the configured parameters.
         *
         * @return a new CachedConfigObject instance
         */
        public CachedConfigObject<K, V> build() {
            final File file = this.configFile;
            final Optional<ConfigSerializer<V>> ser = this.serializer;
            final String section = this.configSection;
            final Function<String, K> keyFromString = this.keyFromStringFunction;
            final Function<K, String> keyToString = this.keyToStringFunction;

            return new CachedConfigObject<>() {
                @Override
                protected File getConfigFile() {
                    return file;
                }

                @Override
                protected Optional<ConfigSerializer<V>> getSerializer() {
                    return ser;
                }

                @Override
                protected String getConfigSection() {
                    return section;
                }

                @Override
                protected K keyFromString(String key) {
                    return keyFromString.apply(key);
                }

                @Override
                protected String keyToString(K key) {
                    return keyToString.apply(key);
                }
            };
        }
    }

}