package dev.crafty.core.storage;

import dev.crafty.core.storage.providers.MongoDbStorageProvider;
import dev.crafty.core.storage.providers.MySqlStorageProvider;
import dev.crafty.core.storage.providers.PostgresStorageProvider;
import dev.crafty.core.storage.providers.YamlStorageProvider;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Factory for creating storage providers.
 * @since 1.0.0
 */
public class StorageProviderFactory {

    /**
     * Creates a YAML storage provider.
     *
     * @param valueType The class of the value type
     * @param directory The directory to store files in
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public static <T> StorageProvider<T, String> createYamlProvider(Class<T> valueType, String directory) {
        return createYamlProvider(valueType, Paths.get(directory));
    }

    /**
     * Creates a YAML storage provider.
     *
     * @param valueType The class of the value type
     * @param directory The directory to store files in
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public static <T> StorageProvider<T, String> createYamlProvider(Class<T> valueType, Path directory) {
        YamlStorageProvider<T> provider = new YamlStorageProvider<>(valueType, directory);
        provider.initialize();
        return createCachedProvider(provider);
    }

    /**
     * Creates a cached storage provider that wraps the given provider.
     *
     * @param provider The provider to wrap
     * @param <T> The type of object to store
     * @param <K> The type of key used to identify objects
     * @return The cached storage provider
     */
    public static <T, K> StorageProvider<T, K> createCachedProvider(StorageProvider<T, K> provider) {
        return new CachedStorageProvider<>(provider);
    }

    /**
     * Creates a PostgreSQL storage provider.
     *
     * @param valueType The class of the value type
     * @param tableName The name of the table to store objects in
     * @param connectionUrl The JDBC connection URL
     * @param username The database username
     * @param password The database password
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public static <T> StorageProvider<T, String> createPostgresProvider(
            Class<T> valueType, String tableName, String connectionUrl, String username, String password) {
        PostgresStorageProvider<T> provider = new PostgresStorageProvider<>(
                valueType, tableName, connectionUrl, username, password);
        provider.initialize();
        return createCachedProvider(provider);
    }

    /**
     * Creates a MySQL storage provider.
     *
     * @param valueType The class of the value type
     * @param tableName The name of the table to store objects in
     * @param connectionUrl The JDBC connection URL
     * @param username The database username
     * @param password The database password
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public static <T> StorageProvider<T, String> createMySqlProvider(
            Class<T> valueType, String tableName, String connectionUrl, String username, String password) {
        MySqlStorageProvider<T> provider = new MySqlStorageProvider<>(
                valueType, tableName, connectionUrl, username, password);
        provider.initialize();
        return createCachedProvider(provider);
    }

    /**
     * Creates a MongoDb storage provider.
     *
     * @param valueType The class of the value type
     * @param tableName The name of the table to store objects in
     * @param connectionUrl The JDBC connection URL
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public static <T> StorageProvider<T, String> createMongoDbProvider(
            Class<T> valueType, String tableName, String connectionUrl) {
        MongoDbStorageProvider<T> provider = new MongoDbStorageProvider<>(
                valueType, tableName, connectionUrl
        );
        provider.initialize();
        return createCachedProvider(provider);
    }

    /**
     * Creates a storage provider based on the specified type.
     *
     * @param type The type of storage provider to create
     * @param valueType The class of the value type
     * @param config The configuration for the storage provider
     * @param <T> The type of object to store
     * @return The storage provider
     */
    public static <T> StorageProvider<T, String> createProvider(
            StorageType type, Class<T> valueType, StorageConfig config) {
        switch (type) {
            case YAML:
                return createYamlProvider(valueType, config.getDirectory());
            case POSTGRES:
                return createPostgresProvider(
                        valueType,
                        config.getTableName(),
                        config.getConnectionUrl(),
                        config.getUsername(),
                        config.getPassword());
            case MYSQL:
                return createMySqlProvider(
                        valueType,
                        config.getTableName(),
                        config.getConnectionUrl(),
                        config.getUsername(),
                        config.getPassword());
            case MONGODB:
                return createMongoDbProvider(
                        valueType,
                        config.getTableName(),
                        config.getConnectionUrl()
                );
            default:
                throw new IllegalArgumentException("Unsupported storage type: " + type);
        }
    }

    /**
     * Enum representing the available storage types.
     */
    public enum StorageType {
        YAML,
        POSTGRES,
        MYSQL,
        MONGODB
    }

    /**
     * Configuration for storage providers.
     */
    @Getter
    public static class StorageConfig {
        private String directory;
        private String tableName;
        private String connectionUrl;
        private String username;
        private String password;

        /**
         * Creates a new StorageConfig for YAML storage.
         *
         * @param directory The directory to store files in
         * @return The storage config
         */
        public static StorageConfig forYaml(String directory) {
            StorageConfig config = new StorageConfig();
            config.directory = directory;
            return config;
        }

        public static StorageConfig forMongoDb(String connectionUrl, String databaseName) {
            StorageConfig config = new StorageConfig();
            config.connectionUrl = connectionUrl;
            config.tableName = databaseName;
            return config;
        }

        /**
         * Creates a new StorageConfig for PostgreSQL storage.
         *
         * @param tableName The name of the table to store objects in
         * @param connectionUrl The JDBC connection URL
         * @param username The database username
         * @param password The database password
         * @return The storage config
         */
        public static StorageConfig forPostgres(
                String tableName, String connectionUrl, String username, String password) {
            return forMySql(tableName, connectionUrl, username, password);
        }

        /**
         * Creates a new StorageConfig for MySQL storage.
         *
         * @param tableName The name of the table to store objects in
         * @param connectionUrl The JDBC connection URL
         * @param username The database username
         * @param password The database password
         * @return The storage config
         */
        public static StorageConfig forMySql(
                String tableName, String connectionUrl, String username, String password) {
            StorageConfig config = new StorageConfig();
            config.tableName = tableName;
            config.connectionUrl = connectionUrl;
            config.username = username;
            config.password = password;
            return config;
        }

    }
}
