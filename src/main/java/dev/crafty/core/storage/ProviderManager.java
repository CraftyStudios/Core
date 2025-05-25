package dev.crafty.core.storage;

import dev.crafty.core.storage.providers.PlayerDataProviderImpl;
import lombok.Getter;
import lombok.Setter;
import dev.crafty.core.storage.StorageProviderFactory.StorageType;
import dev.crafty.core.storage.StorageProviderFactory.StorageConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages storage provider instances, handling creation, caching, and configuration.
 * This class centralizes the provider management logic that was previously spread across
 * multiple classes.
 * @since 1.0.0
 */
public class ProviderManager {
    private static final ProviderManager INSTANCE = new ProviderManager();

    private final Map<String, StorageProvider<?, String>> providers = new HashMap<>();

    // Global storage configuration
    @Getter
    @Setter
    private StorageType defaultStorageType = StorageType.YAML;
    private StorageConfig defaultYamlConfig = StorageConfig.forYaml("data");
    private StorageConfig defaultPostgresConfig = null; // Will be initialized when needed
    private StorageConfig defaultMySqlConfig = null; // Will be initialized when needed
    private StorageConfig defaultMogoDbConfig = null; // Will be initialized when needed

    /**
     * Private constructor to enforce singleton pattern.
     */
    private ProviderManager() {
        // Private constructor
    }

    /**
     * Gets the singleton instance of the ProviderManager.
     *
     * @return The ProviderManager instance
     */
    public static ProviderManager getInstance() {
        return INSTANCE;
    }

    /**
     * Gets a YAML storage provider for the specified type.
     * If the provider doesn't exist, it will be created.
     *
     * @param valueType The class of the value type
     * @param directory The directory to store files in
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public <T> StorageProvider<T, String> getYamlProvider(Class<T> valueType, String directory) {
        String key = "yaml:" + valueType.getName() + ":" + directory;
        return getOrCreateProvider(key, () -> 
            StorageProviderFactory.createProvider(
                StorageType.YAML,
                valueType,
                StorageConfig.forYaml(directory)
            )
        );
    }

    /**
     * Gets a PostgreSQL storage provider for the specified type.
     * If the provider doesn't exist, it will be created.
     *
     * @param valueType The class of the value type
     * @param tableName The name of the table to store objects in
     * @param connectionUrl The JDBC connection URL
     * @param username The database username
     * @param password The database password
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public <T> StorageProvider<T, String> getPostgresProvider(
            Class<T> valueType, String tableName, String connectionUrl, String username, String password) {
        String key = "postgres:" + valueType.getName() + ":" + tableName;
        return getOrCreateProvider(key, () -> 
            StorageProviderFactory.createProvider(
                StorageType.POSTGRES,
                valueType,
                StorageConfig.forPostgres(tableName, connectionUrl, username, password)
            )
        );
    }

    /**
     * Gets a MongoDb storage provider for the specified type.
     * If the provider doesn't exist, it will be created.
     *
     * @param valueType The class of the value type
     * @param tableName The name of the table to store objects in
     * @param connectionUrl The JDBC connection URL
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public <T> StorageProvider<T, String> getMongoDbProvider(
            Class<T> valueType, String tableName, String connectionUrl) {
        String key = "mongodb:" + valueType.getName() + ":" + tableName;
        return getOrCreateProvider(key, () ->
            StorageProviderFactory.createProvider(
                StorageType.MONGODB,
                valueType,
                StorageConfig.forMongoDb(tableName, connectionUrl)
            )
        );
    }

    /**
     * Gets a MySQL storage provider for the specified type.
     * If the provider doesn't exist, it will be created.
     *
     * @param valueType The class of the value type
     * @param tableName The name of the table to store objects in
     * @param connectionUrl The JDBC connection URL
     * @param username The database username
     * @param password The database password
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public <T> StorageProvider<T, String> getMySqlProvider(
            Class<T> valueType, String tableName, String connectionUrl, String username, String password) {
        String key = "mysql:" + valueType.getName() + ":" + tableName;
        return getOrCreateProvider(key, () -> 
            StorageProviderFactory.createProvider(
                StorageType.MYSQL,
                valueType,
                StorageConfig.forMySql(tableName, connectionUrl, username, password)
            )
        );
    }

    /**
     * Sets the default YAML configuration.
     *
     * @param directory The directory to store files in
     */
    public void setDefaultYamlConfig(String directory) {
        this.defaultYamlConfig = StorageConfig.forYaml(directory);
    }

    /**
     * Sets the default PostgreSQL configuration.
     *
     * @param tableName The base table name to use
     * @param connectionUrl The JDBC connection URL
     * @param username The database username
     * @param password The database password
     */
    public void setDefaultPostgresConfig(String tableName, String connectionUrl, String username, String password) {
        this.defaultPostgresConfig = StorageConfig.forPostgres(tableName, connectionUrl, username, password);
    }

    /**
     * Sets the default MongoDB configuration.
     *
     * @param tableName The base table name to use
     * @param connectionUrl The JDBC connection URL
     */
    public void setDefaultMongoDbConfig(String tableName, String connectionUrl) {
        this.defaultMogoDbConfig = StorageConfig.forMongoDb(tableName, connectionUrl);
    }

    /**
     * Sets the default MySQL configuration.
     *
     * @param tableName The base table name to use
     * @param connectionUrl The JDBC connection URL
     * @param username The database username
     * @param password The database password
     */
    public void setDefaultMySqlConfig(String tableName, String connectionUrl, String username, String password) {
        this.defaultMySqlConfig = StorageConfig.forMySql(tableName, connectionUrl, username, password);
    }

    /**
     * Gets a storage provider using the default configuration.
     * The provider type is determined by the default storage type.
     *
     * @param valueType The class of the value type
     * @param <T> The type of object to store
     * @return The storage provider
     * @throws IllegalStateException If the default configuration for the selected storage type is not set
     */
    public <T> StorageProvider<T, String> getProvider(Class<T> valueType) {
        return getProvider(valueType, valueType.getSimpleName().toLowerCase());
    }

    /**
     * Gets a storage provider using the default configuration.
     * The provider type is determined by the default storage type.
     *
     * @param valueType The class of the value type
     * @param identifier A unique identifier for this provider (e.g., table name or directory suffix)
     * @param <T> The type of object to store
     * @return The storage provider
     * @throws IllegalStateException If the default configuration for the selected storage type is not set
     */
    public <T> StorageProvider<T, String> getProvider(Class<T> valueType, String identifier) {
        switch (defaultStorageType) {
            case YAML:
                if (defaultYamlConfig == null) {
                    throw new IllegalStateException("Default YAML configuration not set");
                }
                String directory = defaultYamlConfig.getDirectory() + "/" + identifier;
                return getYamlProvider(valueType, directory);
            case POSTGRES:
                if (defaultPostgresConfig == null) {
                    throw new IllegalStateException("Default PostgreSQL configuration not set");
                }
                String tableName = identifier;
                return getPostgresProvider(
                        valueType,
                        tableName,
                        defaultPostgresConfig.getConnectionUrl(),
                        defaultPostgresConfig.getUsername(),
                        defaultPostgresConfig.getPassword()
                );
            case MYSQL:
                if (defaultMySqlConfig == null) {
                    throw new IllegalStateException("Default MySQL configuration not set");
                }
                String mySqlTableName = identifier;
                return getMySqlProvider(
                        valueType,
                        mySqlTableName,
                        defaultMySqlConfig.getConnectionUrl(),
                        defaultMySqlConfig.getUsername(),
                        defaultMySqlConfig.getPassword()
                );
            case MONGODB:
                if (defaultMogoDbConfig == null) {
                    throw new IllegalStateException("Default MongoDB configuration not set");
                }
                String mongoDbTableName = identifier;
                return getMongoDbProvider(
                        valueType,
                        mongoDbTableName,
                        defaultMogoDbConfig.getConnectionUrl()
                );
            default:
                throw new IllegalStateException("Unsupported default storage type: " + defaultStorageType);
        }
    }

    /**
     * Helper method to get a provider from the cache or create it if it doesn't exist.
     *
     * @param key The cache key
     * @param providerSupplier A supplier that creates the provider if needed
     * @param <T> The type of object to store
     * @return The storage provider
     */
    @SuppressWarnings("unchecked")
    private <T> StorageProvider<T, String> getOrCreateProvider(String key, ProviderSupplier<T> providerSupplier) {
        if (!providers.containsKey(key)) {
            StorageProvider<T, String> provider = providerSupplier.get();
            providers.put(key, provider);
        }
        return (StorageProvider<T, String>) providers.get(key);
    }

    /**
     * Gets a player data provider for the specified player ID and value type.
     *
     * @param playerId The UUID of the player
     * @param valueType The class of the value type
     * @param <T> The type of object to store
     * @return The player data provider
     */
    public <T> PlayerDataProvider<T> forPlayer(UUID playerId, Class<T> valueType) {
        StorageProvider<T, String> provider = getProvider(valueType, playerId.toString());
        return new PlayerDataProviderImpl<>(provider, playerId);
    }

    /**
     * Closes all storage providers.
     * This should be called when the plugin is disabled.
     */
    public void closeAllProviders() {
        for (StorageProvider<?, String> provider : providers.values()) {
            provider.close();
        }
        providers.clear();
    }

    /**
     * Functional interface for creating providers.
     *
     * @param <T> The type of object to store
     */
    @FunctionalInterface
    private interface ProviderSupplier<T> {
        StorageProvider<T, String> get();
    }
}
