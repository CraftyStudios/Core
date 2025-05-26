package dev.crafty.core;

import dev.crafty.core.bridge.BridgeAutoRegistrar;
import dev.crafty.core.bukkit.CraftyLogger;
import dev.crafty.core.config.ConfigurationUtils;
import dev.crafty.core.config.ConfigWatcher;
import dev.crafty.core.config.SectionWrapper;
import dev.crafty.core.storage.ProviderManager;
import dev.crafty.core.storage.StorageProviderFactory;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.*;

/**
 * Main class for the CraftyCore plugin.
 * This plugin provides a comprehensive configuration library for Spigot plugins.
 *
 * @since 1.0.0
 */
public final class CraftyCore extends JavaPlugin {
    public static CraftyCore INSTANCE;

    private final Map<String, List<Object>> registeredConfigs = new HashMap<>();
    private ConfigWatcher configWatcher;

    @Getter
    private boolean configWatcherEnabled = true;

    private boolean configInitialized = false;

    public CraftyLogger logger;

    @Override
    public void onEnable() {
        logger = new CraftyLogger(this);
        INSTANCE = this;

        // Save default config
        saveDefaultConfig();

        setDefaultStorageProvider(new SectionWrapper(getConfig().getConfigurationSection("storage")));

        if (!configInitialized) {
            ConfigurationUtils.initialize(this, () -> configInitialized = true);
        }

        // Set up the config watcher if enabled
        if (configWatcherEnabled) {
            setupConfigWatcher();
        }

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> new BridgeAutoRegistrar(this).registerAll());

        logger.info("CraftyCore has been enabled!");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();

        // issue where reloadConfig is potentially called before onEnable()...
        if (!configInitialized) {
            ConfigurationUtils.initialize(this, () -> configInitialized = true);
        }

        onConfigReloaded();
    }

    /**
     * Scan a plugin for classes with the @ConfigurationFile annotation and automatically
     * register and load them. This method uses reflection to find and instantiate
     * configuration classes.
     *
     * @param plugin The plugin to scan for configuration classes
     * @return The number of configuration classes found and registered
     */
    public int scanAndLoadConfigs(Plugin plugin) {
        return ConfigurationUtils.scanAndLoadConfigs(plugin);
    }

    /**
     * Scan a plugin for configuration classes and reload all of them.
     * This method first scans for classes with the @ConfigurationFile annotation,
     * then reloads all registered configurations for the plugin.
     *
     * @param plugin The plugin to scan and reload configurations for
     * @return The total number of configurations reloaded
     */
    public int scanAndReloadConfigs(Plugin plugin) {
        // First scan for configuration classes
        int scannedCount = scanAndLoadConfigs(plugin);

        // Then reload all registered configurations
        int reloadedCount = ConfigurationUtils.reloadAllConfigs(plugin);

        logger.info("Scanned and found " + scannedCount + " configuration classes for " + plugin.getName());
        logger.info("Reloaded " + reloadedCount + " configurations for " + plugin.getName());

        return reloadedCount;
    }

    @Override
    public void onDisable() {
        // Clean up resources
        if (configWatcher != null) {
            configWatcher.stop();
        }

        logger.info("CraftyCore has been disabled!");
    }

    /**
     * Enable or disable the automatic configuration watcher.
     * The configuration watcher monitors changes to the config.yml file
     * and automatically reloads the configuration when changes are detected.
     * 
     * @param enabled True to enable the watcher, false to disable
     */
    public void setConfigWatcherEnabled(boolean enabled) {
        this.configWatcherEnabled = enabled;

        if (configWatcher != null) {
            configWatcher.setEnabled(enabled);
        } else if (enabled) {
            setupConfigWatcher();
        }
    }

    /**
     * Set up a file watcher to monitor changes to the config.yml file
     * and automatically reload the configuration when changes are detected.
     */
    private void setupConfigWatcher() {
        // Create a config watcher for the plugin's config.yml file
        configWatcher = ConfigWatcher.forPluginConfig(this, () -> {
            logger.info("Detected changes to config.yml, reloading configuration...");
            reloadConfig();
            onConfigReloaded();
        });
        
        // Start the watcher
        configWatcher.start();
    }

    /**
     * Called when the configuration is reloaded.
     * This method reloads all registered configurations and can be overridden
     * by subclasses to handle additional configuration changes.
     */
    public void onConfigReloaded() {
        // Reload all registered configurations
        scanAndReloadConfigs(this);
        logger.info("CraftyCore configuration reloaded");
    }

    /**
     * Register a configuration object with CraftyCore.
     *
     * @param plugin The plugin that owns the configuration object
     * @param configObject The configuration object to register
     */
    public void registerConfig(Plugin plugin, Object configObject) {
        String pluginName = plugin.getName().toLowerCase();
        if (!registeredConfigs.containsKey(pluginName)) {
            registeredConfigs.put(pluginName, new ArrayList<>());
        }
        registeredConfigs.get(pluginName).add(configObject);
        logger.info("Registered configuration for " + plugin.getName());
    }

    /**
     * Unregister all configuration objects for a plugin.
     *
     * @param plugin The plugin to unregister configurations for
     */
    public void unregisterConfigs(Plugin plugin) {
        String pluginName = plugin.getName().toLowerCase();
        registeredConfigs.remove(pluginName);
    }

    /**
     * Reload all registered configuration objects for a plugin.
     * This method will also scan for new configuration classes.
     *
     * @param pluginName The name of the plugin to reload configurations for
     * @return True if configurations were reloaded, false if no configurations were found
     */
    public boolean reloadConfigs(String pluginName) {
        // Find the plugin instance
        Plugin targetPlugin = Bukkit.getPluginManager().getPlugin(pluginName);

        if (targetPlugin == null) {
            logger.warn("Could not find plugin: " + pluginName);
            return false;
        }

        // Scan and reload all configurations for the plugin
        int count = scanAndReloadConfigs(targetPlugin);

        return count > 0;
    }

    /**
     * Reload all registered configuration objects for all plugins.
     * This method will also scan for new configuration classes in all plugins.
     */
    public void reloadAllConfigs() {
        int totalCount = 0;

        // Reload configurations for all registered plugins
        for (String pluginName : registeredConfigs.keySet()) {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
            if (plugin != null) {
                totalCount += scanAndReloadConfigs(plugin);
            }
        }

        logger.info("Reloaded " + totalCount + " configurations for all plugins");
    }

    /**
     * Get the data folder path for the plugin.
     * This is used by other classes to access the plugin's data folder.
     *
     * @return The data folder path
     */
    public Path getDataPath() {
        return getDataFolder().toPath();
    }

    /**
     * Save a resource from the plugin's jar file to the plugin's data folder.
     * This is a convenience method that wraps the JavaPlugin.saveResource method.
     *
     * @param resourcePath The path to the resource in the jar file
     * @param replace Whether to replace the file if it already exists
     */
    public void saveResource(String resourcePath, boolean replace) {
        super.saveResource(resourcePath, replace);
    }

    private void setDefaultStorageProvider(SectionWrapper storageSection) {
        Optional<String> type = storageSection.getString("type");
        StorageProviderFactory.StorageType storageType = StorageProviderFactory.StorageType.valueOf(type.orElse("YAML"));

        switch (storageType) {
            case YAML -> {
                String dataFolder = storageSection.getString("yaml.data-folder").orElse("data");
                ProviderManager.getInstance().setDefaultStorageType(StorageProviderFactory.StorageType.YAML);
                ProviderManager.getInstance().setDefaultYamlConfig(dataFolder);
            }
            case MYSQL -> {
                ProviderManager.getInstance().setDefaultStorageType(StorageProviderFactory.StorageType.MYSQL);
                String host = storageSection.getString("mysql.host").orElse("localhost");
                String port = storageSection.getString("mysql.port").orElse("3306");
                String database = storageSection.getString("mysql.database").orElse("database");
                String username = storageSection.getString("mysql.username").orElse("username");
                String password = storageSection.getString("mysql.password").orElse("password");
                String connectionUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
                ProviderManager.getInstance().setDefaultMySqlConfig("main", connectionUrl, username, password);
            }
            case POSTGRES -> {
                ProviderManager.getInstance().setDefaultStorageType(StorageProviderFactory.StorageType.POSTGRES);
                String host = storageSection.getString("postgres.host").orElse("localhost");
                String port = storageSection.getString("postgres.port").orElse("5432");
                String database = storageSection.getString("postgres.database").orElse("database");
                String username = storageSection.getString("postgres.username").orElse("username");
                String password = storageSection.getString("postgres.password").orElse("password");
                String connectionUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
                ProviderManager.getInstance().setDefaultPostgresConfig("main", connectionUrl, username, password);
            }
            case MONGODB -> {
                ProviderManager.getInstance().setDefaultStorageType(StorageProviderFactory.StorageType.MONGODB);
                String connectionString = storageSection.getString("mongodb.connection-string").orElse("localhost");
                ProviderManager.getInstance().setDefaultMongoDbConfig("main", connectionString);
            }
        }
    }
}