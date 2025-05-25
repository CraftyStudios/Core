package dev.crafty.core.storage.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.crafty.core.storage.AbstractStorageProvider;
import dev.crafty.core.storage.serialization.StorageSerializer;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * A storage provider that stores objects in a PostgreSQL database.
 * Objects are serialized to JSON before storage.
 *
 * @param <T> The type of object to store
 * @since 1.0.0
 */
public class PostgresStorageProvider<T> extends AbstractStorageProvider<T, String> {
    
    private final String tableName;
    private final String connectionUrl;
    private final String username;
    private final String password;
    private HikariDataSource dataSource;
    
    /**
     * Creates a new PostgresStorageProvider.
     *
     * @param valueType The class of the value type
     * @param tableName The name of the table to store objects in
     * @param connectionUrl The JDBC connection URL
     * @param username The database username
     * @param password The database password
     */
    public PostgresStorageProvider(Class<T> valueType, String tableName, String connectionUrl, String username, String password) {
        super(valueType);
        this.tableName = tableName;
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> initialize() {
        return CompletableFuture.runAsync(() -> {
            // Set up connection pool
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(connectionUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            
            dataSource = new HikariDataSource(config);
            
            // Create table if it doesn't exist
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {
                
                String createTableSql = String.format(
                        "CREATE TABLE IF NOT EXISTS %s (" +
                        "key VARCHAR(255) PRIMARY KEY, " +
                        "data JSONB NOT NULL, " +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
                        ")", tableName);
                
                stmt.execute(createTableSql);
                
                // Create index on key
                String createIndexSql = String.format(
                        "CREATE INDEX IF NOT EXISTS %s_key_idx ON %s (key)",
                        tableName, tableName);
                
                stmt.execute(createIndexSql);
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to initialize database", e);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> save(String key, T value) {
        return CompletableFuture.runAsync(() -> {
            try {
                String json = StorageSerializer.toJson(value);
                
                String sql = String.format(
                        "INSERT INTO %s (key, data, updated_at) " +
                        "VALUES (?, ?::jsonb, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (key) " +
                        "DO UPDATE SET data = ?::jsonb, updated_at = CURRENT_TIMESTAMP",
                        tableName);
                
                try (Connection conn = dataSource.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    
                    stmt.setString(1, key);
                    stmt.setString(2, json);
                    stmt.setString(3, json);
                    
                    stmt.executeUpdate();
                }
            } catch (JsonProcessingException | SQLException e) {
                throw new RuntimeException("Failed to save object with key: " + key, e);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Optional<T>> get(String key) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = String.format("SELECT data FROM %s WHERE key = ?", tableName);
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, key);
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String json = rs.getString("data");
                        T value = StorageSerializer.fromJson(json, valueType);
                        return Optional.ofNullable(value);
                    } else {
                        return Optional.empty();
                    }
                }
            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to get object with key: " + key, e);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Collection<T>> getAll() {
        return CompletableFuture.supplyAsync(() -> {
            String sql = String.format("SELECT data FROM %s", tableName);
            Collection<T> result = new ArrayList<>();
            
            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    String json = rs.getString("data");
                    T value = StorageSerializer.fromJson(json, valueType);
                    result.add(value);
                }
                
                return result;
            } catch (SQLException | IOException e) {
                throw new RuntimeException("Failed to get all objects", e);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> delete(String key) {
        return CompletableFuture.runAsync(() -> {
            String sql = String.format("DELETE FROM %s WHERE key = ?", tableName);
            
            try (Connection conn = dataSource.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, key);
                stmt.executeUpdate();
                
            } catch (SQLException e) {
                throw new RuntimeException("Failed to delete object with key: " + key, e);
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public CompletableFuture<Void> close() {
        return CompletableFuture.runAsync(() -> {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.close();
            }
        });
    }
}