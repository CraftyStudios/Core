package dev.crafty.core.storage;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Base interface for all storage providers.
 * Provides methods for CRUD operations on data objects.
 *
 * @since 1.0.0
 * @param <T> The type of object to store
 * @param <K> The type of key used to identify objects
 */
public interface StorageProvider<T, K> {

    /**
     * Saves an object to the storage.
     *
     * @param key The key to identify the object
     * @param value The object to save
     * @return A CompletableFuture that completes when the operation is done
     */
    CompletableFuture<Void> save(K key, T value);

    /**
     * Retrieves an object from the storage.
     *
     * @param key The key of the object to retrieve
     * @return A CompletableFuture that completes with the retrieved object, or empty if not found
     */
    CompletableFuture<Optional<T>> get(K key);

    /**
     * Retrieves all objects from the storage.
     *
     * @return A CompletableFuture that completes with a collection of all objects
     */
    CompletableFuture<Collection<T>> getAll();

    /**
     * Deletes an object from the storage.
     *
     * @param key The key of the object to delete
     * @return A CompletableFuture that completes when the operation is done
     */
    CompletableFuture<Void> delete(K key);

    /**
     * Checks if an object exists in the storage.
     *
     * @param key The key to check
     * @return A CompletableFuture that completes with true if the object exists, false otherwise
     */
    CompletableFuture<Boolean> exists(K key);

    /**
     * Initializes the storage provider.
     * This method should be called before using the provider.
     *
     * @return A CompletableFuture that completes when initialization is done
     */
    CompletableFuture<Void> initialize();

    /**
     * Closes the storage provider and releases any resources.
     * This method should be called when the provider is no longer needed.
     *
     * @return A CompletableFuture that completes when the provider is closed
     */
    CompletableFuture<Void> close();
}
