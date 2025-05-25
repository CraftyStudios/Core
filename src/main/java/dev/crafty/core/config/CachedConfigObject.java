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
            Optional<V> value = getFromConfig((K) key);
            value.ifPresent(v -> cache.put((K) key, v));
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

        if (!config.contains(String.valueOf(id))) {
            return Optional.empty();
        }

        if (getSerializer().isPresent()) {
            return getSerializer().get().deserialize(section, getConfigSection() + "." + id);
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
         * Builds a new {@link CachedConfigObject} instance with the configured parameters.
         *
         * @return a new CachedConfigObject instance
         */
        public CachedConfigObject<K, V> build() {
            final File file = this.configFile;
            final Optional<ConfigSerializer<V>> ser = this.serializer;
            final String section = this.configSection;

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
            };
        }
    }

}